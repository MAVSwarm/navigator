package com.navigator.core.providers;

import mavros_msgs.*;
import org.apache.commons.logging.Log;
import org.ros.exception.ServiceNotFoundException;
import org.ros.node.ConnectedNode;
import org.ros.node.service.ServiceClient;

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

public class RosServiceProvider {


    private ServiceClient<CommandBoolRequest, CommandBoolResponse> armingService;
    private ServiceClient<CommandTOLRequest, CommandTOLResponse> takeoffService;
    private ServiceClient<CommandTOLRequest, CommandTOLResponse> landService;
    private ServiceClient<SetModeRequest, SetModeResponse> setFCUModeService;
    private ServiceClient<ParamPullRequest, ParamPullResponse> paramPullService;
    private Log log;

    public RosServiceProvider(ConnectedNode node) throws ServiceNotFoundException {
        this.log = node.getLog();

        armingService = node.newServiceClient("mavros/cmd/arming", CommandBool._TYPE);
        takeoffService = node.newServiceClient("mavros/cmd/takeoff",CommandTOL._TYPE);
        landService = node.newServiceClient("mavros/cmd/land",CommandTOL._TYPE);
        setFCUModeService = node.newServiceClient("mavros/set_mode", SetMode._TYPE);
        paramPullService = node.newServiceClient("mavros/param/pull",ParamPull._TYPE);
    }

    public ServiceClient<CommandBoolRequest, CommandBoolResponse> getArmingService() {
        return armingService;
    }

    public ServiceClient<CommandTOLRequest, CommandTOLResponse> getTakeoffService() {return takeoffService;}

    public ServiceClient<CommandTOLRequest, CommandTOLResponse> getLandService() {return landService;}

    public ServiceClient<SetModeRequest, SetModeResponse> getSetFCUModeService(){
        return setFCUModeService;
    }

    public ServiceClient<ParamPullRequest, ParamPullResponse> getParamPullService() {return paramPullService;}



    public boolean isConnected(){
        return armingService.isConnected()
                && setFCUModeService.isConnected()
                && paramPullService.isConnected()
                && takeoffService.isConnected()
                && landService.isConnected();
    }

}
