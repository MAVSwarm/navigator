package com.navigator.core.states;

import com.navigator.core.providers.ActionProvider;
import com.navigator.core.droneState.DroneLanded;
import com.navigator.core.droneState.DroneStateTracker;
import mavros_msgs.SetModeRequest;
import mavros_msgs.SetModeResponse;
import org.apache.commons.logging.Log;
import org.ros.node.service.ServiceClient;

/**
 * Created by parallels on 8/20/16.
 */
public class EmergencyLandingState extends State {

    private DroneStateTracker stateTracker;

    public EmergencyLandingState(
            ActionProvider actionProvider,
            ServiceClient<SetModeRequest, SetModeResponse> setModeService,
            Log log,
            DroneStateTracker stateTracker
    ){
        super(actionProvider, setModeService, log);
        this.stateTracker = stateTracker;
        actionQueue.add(actionProvider.getSetFCUModeAction("OFFBOARD"));
        actionQueue.add(actionProvider.getPX4LandAction());
        actionQueue.add(actionProvider.getDisarmAction());
    }

    public boolean isSafeToExit(){
        if(stateTracker.getDroneLanded()== DroneLanded.OnGround && !stateTracker.getArmed())
            return true;
        else
            return false;
    }

    public String toString() {
        return "EmergencyLandingState";
    }

}
