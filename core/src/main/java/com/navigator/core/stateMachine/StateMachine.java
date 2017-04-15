package com.navigator.core.stateMachine;


import com.navigator.core.states.util.StateHandle;
import com.navigator.core.droneState.DroneStateTracker;
import com.navigator.core.states.util.ErrorType;
import com.navigator.core.states.State;
import com.navigator.core.stateMachine.utils.StateException;
import org.apache.commons.logging.Log;
import org.ros.message.Time;

/*
 * Copyright (C) 2016-2017 Gao Changyu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

public class StateMachine {

    private State currentState;
    private State emergencyLanding;
    private Log logger;
    private DroneStateTracker tracker;
    private boolean statemachineRunningFlag;

    public StateMachine(State initialState, State emergencyLanding, Log log, DroneStateTracker tracker){
        this.emergencyLanding = emergencyLanding;
        this.logger = log;
        this.tracker = tracker;
        currentState = initialState;
        statemachineRunningFlag = false;
        initialState.enterAction();
    }

    //Update the statemachine
    public void update(Time time){
        ErrorType error = failsafeCheck();
        switch (error) {
            case NoError:
                if(statemachineRunningFlag) {
                    currentState.update(time);
                }
                break;
            case ConnectionFailure:
                if (currentState != emergencyLanding) {
                    logger.warn("Switching to emergency landing control due to losing the wireless connection");
                    forceState(emergencyLanding);
                }
                break;
            case DangerousPosition:
                if (currentState != emergencyLanding) {
                    logger.warn("Switching to emergencyLanding due to perceived danger");
                    forceState(emergencyLanding);
                }
                break;
            case BatteryLow:
                if (currentState != emergencyLanding) {
                    logger.warn("Starting emergency landing due to critical battery level");
                    forceState(emergencyLanding);
                }
                break;
            case ControllerSignalLoss:
                if (currentState != emergencyLanding) {
                    logger.warn("Starting emergency landing due to losing the signal of the manual controller");
                    forceState(emergencyLanding);
                }
                break;
            case MotorFailure:
                if (currentState != emergencyLanding) {
                    logger.warn("Starting emergency landing due to motor malfunction");
                    forceState(emergencyLanding);
                }
                break;
            case ActionFailure:
                if (currentState != emergencyLanding) {
                    logger.warn("Switching to emergency landing due to " + currentState.getCurrentAction().toString() + " failing");
                    forceState(emergencyLanding);
                }
                break;
        }
    }

    private ErrorType failsafeCheck(){
        if (currentState.getLastFailure() != null && currentState.getLastFailure().getError() != ErrorType.NoError){
            return currentState.getLastFailure().getError();
        }
        else if (tracker.getRemaining() < 0.05) return ErrorType.BatteryLow;
        else if (!currentState.isConnected()) return ErrorType.ConnectionFailure;

        return ErrorType.NoError;
    }

    public void setState(State newState) throws StateException {
        if(currentState.isSafeToExit()) {
            forceState(newState);
        }
        else {
            throw new StateException("The current state is unsafe to switch from!");
        }
    }

    public void forceState(State newState) {
        currentState.exitAction();
        currentState = newState;
        newState.enterAction();
    }

    public boolean getStatemachineRunningFlag(){return statemachineRunningFlag;}

    public void setStatemachineRunningFlag(boolean statemachineRunningFlag){this.statemachineRunningFlag = statemachineRunningFlag;}

    public StateHandle getCurrentState(){
        return currentState;
    }
}
