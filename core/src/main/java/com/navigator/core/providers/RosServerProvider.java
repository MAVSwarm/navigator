package com.navigator.core.providers;

import org.ros.node.ConnectedNode;
import org.ros.node.service.ServiceResponseBuilder;
import navigator_msgs.*;

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

public class RosServerProvider {

    private String currentAction;
    private boolean actionFinished;

    public RosServerProvider (ConnectedNode node){
        currentAction = "";
        actionFinished = false;

        node.newServiceServer("~ActionStatus", ActionStatus._TYPE,
                new ServiceResponseBuilder<ActionStatusRequest, ActionStatusResponse>() {
                    @Override
                    public void build(ActionStatusRequest request, ActionStatusResponse response){
                        currentAction = request.getCurrentAction();
                        actionFinished = request.getActionFinished();

                        response.setSuccess(true);
                    }
                });
    }

    public String getCurrentAction(){return currentAction;}

    public boolean getActionFinished(){return actionFinished;}

    public void resetActionStatus(){
        currentAction = "";
        actionFinished = false;
    }


}
