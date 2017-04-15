package com.navigator.core.actions;

import com.navigator.core.actions.util.ActionStatus;
import mavros_msgs.SetModeRequest;
import mavros_msgs.SetModeResponse;
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

public class SetFCUModeAction extends Action{

    private ServiceClient<SetModeRequest, SetModeResponse> setModeService;
    private String newMode;
    private final byte baseMode = 0;

    public SetFCUModeAction(ServiceClient<SetModeRequest, SetModeResponse> setModeService,
                            String newMode){
        this.setModeService = setModeService;
        this.newMode = newMode;
    }


    @Override
    public ActionStatus loopAction(Time time) {
            if(!setModeService.isConnected()) return ActionStatus.ConnectionFailure;

            SetModeRequest request = setModeService.newMessage();
            request.setBaseMode(baseMode);
            request.setCustomMode(newMode);

            ServiceResponseListener<SetModeResponse> listener = new ServiceResponseListener<SetModeResponse>() {
                @Override
                public void onSuccess(SetModeResponse setModeResponse) {
                    serviceResult = ActionStatus.Success;
                }

                @Override
                public void onFailure(RemoteException e) {
                    serviceResult = ActionStatus.Failure;
                }
            };
            setModeService.call(request, listener);

            if(serviceResult == ActionStatus.Success){
                return ActionStatus.Success;
            }else{
                return ActionStatus.Running;
            }
    }

    public String toString(){
        return "SetFCUModeAction";
    }
}
