package com.navigator.core.states;

import com.navigator.core.states.util.ErrorType;
import com.navigator.core.states.util.Failure;
import com.navigator.core.states.util.StateHandle;
import com.navigator.core.actions.Action;
import com.navigator.core.actions.util.ActionStatus;
import com.navigator.core.providers.ActionProvider;
import mavros_msgs.SetModeRequest;
import mavros_msgs.SetModeResponse;
import org.apache.commons.logging.Log;
import org.ros.message.Time;
import org.ros.node.service.ServiceClient;

import java.util.ArrayDeque;
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

public abstract class State implements StateHandle {

    protected Queue<Action> actionQueue;
    protected ServiceClient<SetModeRequest, SetModeResponse> setModeService;
    protected Action currentAction, nextAction;
    protected Time currentTime;
    protected Failure lastFailure;
    protected Log log;

    public State(ActionProvider actionProvider,
                 ServiceClient<SetModeRequest, SetModeResponse> setModeService,
                 Log log){
        actionQueue = new ArrayDeque<Action>();
        this.setModeService = setModeService;
        this.log = log;
    }

    final public void update(Time time) {
        currentTime = time;
        if(currentAction != null) {
            ActionStatus status = currentAction.loopAction(time);
            handleActionResult(status, time);
        }
    }

    public void enterAction(){
        lastFailure = null;
        currentAction = null;
        if (!actionQueue.isEmpty())
            setAction(actionQueue.remove());
    }

    public void exitAction(){
        if (currentAction != null) {
            currentAction.exitAction();
        }
    }

    //The state is finished, everything is clean and ready to exit.
    public abstract boolean isSafeToExit();
    //The state is idling and doing nothing.
    public boolean isIdling(){ return (currentAction == null);};
    //The services that are used in the state are all connected
    public boolean isConnected(){
        return setModeService.isConnected();
    }
    //get last failure occured.
    public Failure getLastFailure(){ return lastFailure; }
    //get the current excuting action
    public Action getCurrentAction(){ return currentAction; }
    //handle the result of an action.
    private boolean handleActionResult(ActionStatus status, Time time) {
        switch (status) {
            case Success:
                if (!actionQueue.isEmpty()) {
                    setAction(actionQueue.poll());
                }else{
                    currentAction = null;
                }
                return true;
            case ConnectionFailure:
                lastFailure = new Failure(ErrorType.ConnectionFailure, currentTime);
            case Failure:
                lastFailure = new Failure(ErrorType.ActionFailure, currentTime);
            case Running:
            default:
                return false;
        }
    }
    //set next action
    protected void setAction(Action newAction) {
        if (currentAction != null) {
            currentAction.exitAction();
        }
        currentAction = newAction;
        newAction.enterAction(currentTime);
    }
}
