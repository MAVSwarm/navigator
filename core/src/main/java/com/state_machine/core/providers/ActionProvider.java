package com.state_machine.core.providers;

import com.state_machine.core.actions.*;
import com.state_machine.core.droneState.DroneStateTracker;
import com.state_machine.core.actions.util.Waypoint;
import com.state_machine.core.droneState.NeighborStateTracker;
import org.apache.commons.logging.Log;
import org.ros.message.Duration;

public class ActionProvider {

    private RosServiceProvider serviceProvider;
    private DroneStateTracker stateTracker;
    private RosPublisherProvider rosPublisherProvider;
    private ArmAction armAction;
    private DisarmAction disarmAction;
    private PX4LandAction px4LandAction;
    private DecentralizedAction decentralizedAction;
    private RosServerProvider rosServerProvider;
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
            RosParamProvider rosParamProvider){
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


    public PX4FlyToAction getPX4FlyToAction(Waypoint objective,int seq){
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
}
