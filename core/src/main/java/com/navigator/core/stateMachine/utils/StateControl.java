package com.navigator.core.stateMachine.utils;

import com.navigator.core.providers.FileProvider;
import com.navigator.core.providers.RosParamProvider;
import com.navigator.core.providers.StateProvider;
import com.navigator.core.stateMachine.StateMachine;
import com.navigator.core.states.State;
import org.apache.commons.logging.Log;
import org.ros.node.ConnectedNode;
import navigator_msgs.*;
import org.ros.node.service.ServiceResponseBuilder;

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

public class StateControl {
    private Queue<State> stateQueue;
    private FileProvider fileProvider;
    private RosParamProvider rosParamProvider;
    private StateMachine stateMachine;
    private StateProvider stateProvider;
    private Log log;

    public StateControl(ConnectedNode node,
                        StateProvider stateProvider,
                        StateMachine stateMachine,
                        RosParamProvider rosParamProvider,
                        FileProvider fileProvider){
        this.rosParamProvider = rosParamProvider;
        this.stateProvider = stateProvider;
        this.stateMachine = stateMachine;
        this.fileProvider = fileProvider;
        this.log = node.getLog();
        node.newServiceServer("~ControlInterface", StateMachineControl._TYPE,
                new ServiceResponseBuilder<StateMachineControlRequest, StateMachineControlResponse>() {
                    @Override
                    public void build(StateMachineControlRequest request, StateMachineControlResponse response){
                        response.setSuccess(handleRequest(request.getOperation()));
                    }
                });
    }

    //wrapper of handleRequest for other modules
    public boolean wrapHandleRequest(String request){
        return handleRequest(request);
    }

    private boolean handleRequest(String request){
        if(request.equals("start")){
            stateQueue = fileProvider.readScript(rosParamProvider.getFlightScriptPath());
            stateMachine.setStatemachineRunningFlag(true);
        }else if(request.equals("stop")){

            if(stateMachine.getCurrentState().isSafeToExit()){
                stateMachine.setStatemachineRunningFlag(false);
                while(!stateQueue.isEmpty()) {
                    stateQueue.remove();
                }
                stateMachine.setState(stateProvider.getIdleState(1000));
            }
        }else if(request.equals("restart")){
            if(stateMachine.getCurrentState().isSafeToExit()){
                stateMachine.setStatemachineRunningFlag(false);
                while(!stateQueue.isEmpty()) {
                    stateQueue.remove();
                }
                stateMachine.setState(stateProvider.getIdleState(1000));
            }
            stateQueue = fileProvider.readScript(rosParamProvider.getFlightScriptPath());
            stateMachine.setStatemachineRunningFlag(true);
        }else if(request.equals("pause")){
            stateMachine.setStatemachineRunningFlag(false);
        }else if(request.equals("resume")){
            stateMachine.setStatemachineRunningFlag(true);
        }else{
            log.warn("unsupported request.supported requests are 'start', 'stop', 'restart', 'pause', 'resume'.");
            return false;
        }

        return true;
    }

    public    Queue<State> getStateQueue(){return stateQueue;}


}
