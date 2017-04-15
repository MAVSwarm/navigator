package com.navigator.core.states;

import com.navigator.core.providers.ActionProvider;
import mavros_msgs.SetModeRequest;
import mavros_msgs.SetModeResponse;
import org.apache.commons.logging.Log;
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

public class IdleState extends State {

    private long startTime;
    private long duration;
    public IdleState(ActionProvider actionProvider,
                     ServiceClient<SetModeRequest, SetModeResponse> setModeService,
                     Log log, long duration) {
        super(actionProvider, setModeService, log);
        this.duration = duration;
        currentAction = null;
        startTime = System.currentTimeMillis();
    }

    public boolean isSafeToExit(){
        if((System.currentTimeMillis() - startTime) > duration)
            return true;
        else
            return false;
    }

    public String toString() {
        return "IdleState";
    }
}
