package com.navigator.core.providers;

import com.navigator.core.states.EmergencyLandingState;
import com.navigator.core.states.IdleState;
import com.navigator.core.states.ScriptedState;
import com.navigator.core.actions.Action;
import com.navigator.core.droneState.DroneStateTracker;
import org.apache.commons.logging.Log;

import java.util.Queue;

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

public class StateProvider {

    private IdleState idleState;
    private EmergencyLandingState emergencyLandingState;
    private RosServiceProvider serviceProvider;
    private RosPublisherProvider rosPublisherProvider;
    private ActionProvider actionProvider;
    private Log log;
    private DroneStateTracker stateTracker;

    public StateProvider(ActionProvider actionProvider, RosServiceProvider serviceProvider, RosPublisherProvider rosPublisherProvider, Log log, DroneStateTracker stateTracker){
        this.serviceProvider = serviceProvider;
        this.actionProvider = actionProvider;
        this.rosPublisherProvider = rosPublisherProvider;
        this.log = log;
        this.stateTracker = stateTracker;


        this.emergencyLandingState = new EmergencyLandingState(actionProvider, serviceProvider.getSetFCUModeService(), log, stateTracker);

    }

    public IdleState getIdleState(long duration) { return new IdleState(actionProvider, serviceProvider.getSetFCUModeService(), log, duration); }

    public EmergencyLandingState getEmergencyLandingState(){ return emergencyLandingState;}

    public ScriptedState getScriptedState(Queue<Action> actions) {
        return new ScriptedState(actions, actionProvider, serviceProvider.getSetFCUModeService(), log);
    }

}
