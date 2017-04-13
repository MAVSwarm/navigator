package com.state_machine.core.actions;

import com.state_machine.core.actions.util.ActionStatus;
import com.state_machine.core.droneState.DroneLanded;
import com.state_machine.core.droneState.DroneStateTracker;
import mavros_msgs.CommandTOLRequest;
import mavros_msgs.CommandTOLResponse;
import org.apache.commons.logging.Log;
import org.ros.exception.RemoteException;
import org.ros.message.Duration;
import org.ros.message.Time;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;

import static java.lang.Math.abs;

/**
 * Created by firefly on 10/21/16.
 */
public class PX4TakeoffAction extends Action{

    private DroneStateTracker stateTracker;
    private double target_heightm;
    private Duration timeOut;
    private ServiceClient<CommandTOLRequest, CommandTOLResponse> takeoffService;
    private Log logger;
    private double targetAltitude;


    public PX4TakeoffAction(
            Log logger,
            DroneStateTracker stateTracker,
            double target_heightm,
            Duration timeOut,
            ServiceClient<CommandTOLRequest,CommandTOLResponse> takeoffService
    ){
        this.stateTracker = stateTracker;
        this.target_heightm = target_heightm;
        this.timeOut = timeOut;
        this.takeoffService = takeoffService;
        this.logger = logger;
    }

    @Override
    public ActionStatus enterAction(Time time){
        double currentLongitude = stateTracker.getLongitude();
        double currentLattitude = stateTracker.getLattitude();
        targetAltitude = stateTracker.getAltitude() + target_heightm;

        if(stateTracker.getDroneLanded() == DroneLanded.InAir){
            return ActionStatus.Success;
        }else if(stateTracker.getDroneLanded() == DroneLanded.OnGround){
            if(!takeoffService.isConnected())
                return ActionStatus.ConnectionFailure;

            CommandTOLRequest message = takeoffService.newMessage();
            message.setAltitude((float)(targetAltitude));
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
            takeoffService.call(message,listener);

            timeStamp = time;

            return serviceResult;
        }else{
            return ActionStatus.Failure;
        }
    }

    @Override
    public ActionStatus loopAction(Time time) {
        if(stateTracker.getDroneLanded() == DroneLanded.InAir
                && abs(stateTracker.getAltitude() - targetAltitude) < 0.5){
            logger.warn("takeoff success,distance" + abs(stateTracker.getAltitude() - targetAltitude));
            return ActionStatus.Success;
        }else if(time.subtract(timeStamp).compareTo(timeOut) >= 0){
            return ActionStatus.Failure;
        }else{
            return ActionStatus.Running;
        }
    }

    public String toString() {
        return "PX4TakeoffAction";
    }
}
