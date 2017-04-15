package com.navigator.core.actions;

import com.navigator.core.actions.util.ActionStatus;
import com.navigator.core.droneState.DroneStateTracker;
import mavros_msgs.CommandBoolRequest;
import mavros_msgs.CommandBoolResponse;
import org.ros.exception.RemoteException;
import org.ros.message.Time;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;

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

public class DisarmAction extends Action {

    private DroneStateTracker stateTracker;
    private ServiceClient<CommandBoolRequest, CommandBoolResponse> armingService;
    private int retryCount;

    public DisarmAction(
            ServiceClient<CommandBoolRequest, CommandBoolResponse> armingService,
            DroneStateTracker stateTracker
    ){
        super();
        this.stateTracker = stateTracker;
        this.armingService = armingService;
        retryCount = 0;
    }

    public ActionStatus loopAction(Time time){
        if (!stateTracker.getArmed()) {
            return ActionStatus.Success;
        }
        else{
            if(!armingService.isConnected()) return ActionStatus.ConnectionFailure;

            if(retryCount == 10)
                return ActionStatus.Failure;

            CommandBoolRequest message = armingService.newMessage();
            message.setValue(false);
            ServiceResponseListener<CommandBoolResponse> listener = new ServiceResponseListener<CommandBoolResponse>() {
                @Override
                public void onSuccess(CommandBoolResponse commandBoolResponse) {
                    serviceResult = ActionStatus.Success;
                }

                @Override
                public void onFailure(RemoteException e) {
                    serviceResult = ActionStatus.Failure;
                }
            };
            armingService.call(message, listener);

            retryCount += 1;
            if(serviceResult == ActionStatus.Success){
                return ActionStatus.Success;
            }else{
                return ActionStatus.Running;
            }
        }
    }

    public String toString() {
        return "DisarmAction";
    }
}
