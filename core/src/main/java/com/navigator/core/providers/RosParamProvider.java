package com.navigator.core.providers;

import mavros_msgs.ParamPullRequest;
import mavros_msgs.ParamPullResponse;
import org.apache.commons.logging.Log;
import org.ros.exception.RemoteException;
import org.ros.node.ConnectedNode;
import org.ros.node.parameter.*;
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

public class RosParamProvider {
    private ParameterTree params;
    private ServiceClient<ParamPullRequest, ParamPullResponse> paramPullService;
    private Log log;

    public RosParamProvider(ConnectedNode node,
                            RosServiceProvider rosServiceProvider,
                            Log log){
        this.paramPullService = rosServiceProvider.getParamPullService();
            ParamPullRequest request = paramPullService.newMessage();
            request.setForcePull(false);
            paramPullService.call(request, new ServiceResponseListener<ParamPullResponse>() {
                @Override
                public void onSuccess(ParamPullResponse paramPullResponse) {
                }

                @Override
                public void onFailure(RemoteException e) {
                }
            });
        params=node.getParameterTree();
    }

    public String getFlightScriptPath(){ return params.getString("~FlightScriptPath");}

    public String getPropertiesPath(){ return params.getString("~PropertiesPath");}

    public double getD0(){return params.getDouble("~DecentralizedAction/D0",5.0);}

    public double getC(){return params.getDouble("~DecentralizedAction/C", 0.2);}

    public double getLamda(){return params.getDouble("~DecentralizedAction/Lamda", 1e-3);}

    public int getHp(){return params.getInteger("~DecentralizedAction/Hp",3);}

    public int getHu(){return params.getInteger("~DecentralizedAction/Hu", 2);}

    public double getTs(){return params.getDouble("~DecentralizedAction/Ts", 0.10);}

    public double getVmax(){return params.getDouble("~DecentralizedAction/Vmax", 3.0);}

}
