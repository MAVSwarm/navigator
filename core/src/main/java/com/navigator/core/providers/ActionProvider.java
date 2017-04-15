package com.navigator.core.providers;

import com.navigator.core.actions.*;
import com.navigator.core.actions.util.Waypoint;
import com.navigator.core.droneState.DroneStateTracker;
import com.navigator.core.droneState.NeighborStateTracker;
import org.apache.commons.logging.Log;
import org.ros.message.Duration;

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

public class ActionProvider {

    private RosServiceProvider serviceProvider;
    private DroneStateTracker stateTracker;
    private RosPublisherProvider rosPublisherProvider;
    private ArmAction armAction;
    private DisarmAction disarmAction;
    private PX4LandAction px4LandAction;
    private DecentralizedAction decentralizedAction;
    private ExternalAction externalAction;
    private HoldAction holdAction;
    private RosServerProvider rosServerProvider;
    private RosSubscriberProvider rosSubscriberProvider;
    private Duration timeOut;
    private Log logger;

    public ActionProvider(
            Log logger,
            RosServiceProvider serviceProvider,
            DroneStateTracker stateTracker,
            NeighborStateTracker neighborStateTracker,
            RosPublisherProvider rosPublisherProvider,
            Duration timeOut,
            RosServerProvider rosServerProvider,
            RosParamProvider rosParamProvider,
            RosSubscriberProvider rosSubscriberProvider){
        this.rosSubscriberProvider = rosSubscriberProvider;
        this.serviceProvider = serviceProvider;
        this.stateTracker = stateTracker;
        this.rosPublisherProvider = rosPublisherProvider;
        this.rosServerProvider = rosServerProvider;
        this.timeOut = timeOut;
        this.logger = logger;
        armAction = new ArmAction(logger, serviceProvider.getArmingService(), stateTracker);
        disarmAction = new DisarmAction(serviceProvider.getArmingService(), stateTracker);
        px4LandAction = new PX4LandAction(stateTracker,timeOut,serviceProvider.getLandService());
        decentralizedAction = new DecentralizedAction(logger,stateTracker,neighborStateTracker,timeOut, rosParamProvider,rosPublisherProvider,serviceProvider);
    }

    public ArmAction getArmAction(){ return armAction; }
    public DisarmAction getDisarmAction(){ return disarmAction; }


    public PX4FlyToAction getPX4FlyToAction(Waypoint objective, int seq){
        return new PX4FlyToAction(logger,objective,stateTracker,rosPublisherProvider,timeOut,serviceProvider,seq);
    }

    public SetFCUModeAction getSetFCUModeAction(String newMode){
        return new SetFCUModeAction(serviceProvider.getSetFCUModeService(),newMode);
    }

    public PX4LandAction getPX4LandAction(){ return px4LandAction;}

    public PX4TakeoffAction getPX4TakeoffAction(double target_heightm){
        return new PX4TakeoffAction(logger, stateTracker,target_heightm,timeOut,serviceProvider.getTakeoffService());
    }

    public DecentralizedAction getDecentralizedAction(){return decentralizedAction;}

    public ExternalAction getExternalAction(int durationMs){
        return new ExternalAction(rosPublisherProvider, rosSubscriberProvider, logger, stateTracker, serviceProvider, new Duration(durationMs/1000, (durationMs%1000)*1000000));
    }

    public HoldAction getHoldAction(int durationMs){

        return new HoldAction(logger, stateTracker, new Duration(durationMs/1000, (durationMs%1000)*1000000),serviceProvider,rosPublisherProvider);
    }
}
