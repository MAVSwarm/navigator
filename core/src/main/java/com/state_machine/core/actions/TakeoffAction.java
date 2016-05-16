package com.state_machine.core.actions;

import com.state_machine.core.actions.util.ActionStatus;
import com.state_machine.core.droneState.DroneLanded;
import com.state_machine.core.droneState.DroneStateTracker;
import mavros_msgs.CommandTOLRequest;
import mavros_msgs.CommandTOLResponse;
import org.ros.exception.RemoteException;
import org.ros.message.Time;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;

public class TakeoffAction extends Action {

    private DroneStateTracker stateTracker;
    private ServiceClient<CommandTOLRequest, CommandTOLResponse> takeoffService;

    public TakeoffAction(
            ServiceClient<CommandTOLRequest, CommandTOLResponse> takeoffService,
            DroneStateTracker stateTracker
    ){
        this.takeoffService = takeoffService;
        this.stateTracker = stateTracker;
    }

    public ActionStatus loopAction(Time time){
        if (stateTracker.getDroneLanded() == DroneLanded.InAir) {
            return ActionStatus.Success;
        }
        else if (status == ActionStatus.Inactive) {
            if(!takeoffService.isConnected()) return ActionStatus.ConnectionFailure;

            CommandTOLRequest message = takeoffService.newMessage();
            ServiceResponseListener<CommandTOLResponse> listener = new ServiceResponseListener<CommandTOLResponse>() {
                @Override
                public void onSuccess(CommandTOLResponse commandTolResponse) {
                    status = ActionStatus.Success;
                }

                @Override
                public void onFailure(RemoteException e) { status = ActionStatus.Failure; }
            };
            takeoffService.call(message, listener);

            status = ActionStatus.Waiting;
            return ActionStatus.Waiting;
        }
        else {
            return status;
        }
    }

    public String toString() {
        return "TakeoffAction";
    }
}