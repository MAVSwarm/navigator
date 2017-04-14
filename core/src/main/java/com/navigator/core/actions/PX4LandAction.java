package com.navigator.core.actions;

import com.navigator.core.actions.util.ActionStatus;
import com.navigator.core.droneState.DroneLanded;
import com.navigator.core.droneState.DroneStateTracker;
import mavros_msgs.CommandTOLRequest;
import mavros_msgs.CommandTOLResponse;
import org.ros.exception.RemoteException;
import org.ros.message.Duration;
import org.ros.message.Time;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;

/**
 * Created by firefly on 10/23/16.
 */
public class PX4LandAction extends Action{

    private DroneStateTracker stateTracker;
    private Duration timeOut;
    private ServiceClient<CommandTOLRequest, CommandTOLResponse> landService;


    public PX4LandAction(
            DroneStateTracker stateTracker,
            Duration timeOut,
            ServiceClient<CommandTOLRequest,CommandTOLResponse> landService
    ){
        this.stateTracker = stateTracker;
        this.timeOut = timeOut;
        this.landService = landService;
    }

    @Override
    public ActionStatus enterAction(Time time){
        double currentLongitude = stateTracker.getLongitude();
        double currentLattitude = stateTracker.getLattitude();

        if(stateTracker.getDroneLanded() == DroneLanded.OnGround){
            return ActionStatus.Success;
        }else if(stateTracker.getDroneLanded() == DroneLanded.InAir){
            if(!landService.isConnected())
                return ActionStatus.ConnectionFailure;

            CommandTOLRequest message = landService.newMessage();
            message.setLatitude((float)(currentLattitude));
            message.setLongitude((float)(currentLongitude));
            ServiceResponseListener<CommandTOLResponse> listener = new ServiceResponseListener<CommandTOLResponse>() {
                @Override
                public void onSuccess(CommandTOLResponse commandTOLResponse) {
                    serviceResult = ActionStatus.Success;
                }

                @Override
                public void onFailure(RemoteException e) {
                    serviceResult = ActionStatus.Failure;
                }
            };
            landService.call(message,listener);

            timeStamp = time;

            return serviceResult;
        }else{
            return ActionStatus.Failure;
        }
    }

    @Override
    public ActionStatus loopAction(Time time) {
        if(stateTracker.getDroneLanded() == DroneLanded.OnGround) {
            return ActionStatus.Success;
        }else if(time.subtract(timeStamp).compareTo(timeOut) >= 0){
            return ActionStatus.Failure;
        }else{
            return ActionStatus.Running;
        }
    }

    public String toString() {
        return "PX4LandAction";
    }
}
