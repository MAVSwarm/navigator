package com.navigator.core.actions;

import com.navigator.core.actions.util.ActionStatus;
import com.navigator.core.droneState.DroneStateTracker;
import mavros_msgs.CommandBoolRequest;
import mavros_msgs.CommandBoolResponse;
import org.ros.exception.RemoteException;
import org.ros.message.Time;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;

public class DisarmAction extends Action {

    private DroneStateTracker stateTracker;
    private ServiceClient<CommandBoolRequest, CommandBoolResponse> armingService;
    private int retryCount;

    public DisarmAction(
            ServiceClient<CommandBoolRequest, CommandBoolResponse> armingService,
            DroneStateTracker stateTracker
    ){
        super();
        this.stateTracker = stateTracker;
        this.armingService = armingService;
        retryCount = 0;
    }

    public ActionStatus loopAction(Time time){
        if (!stateTracker.getArmed()) {
            return ActionStatus.Success;
        }
        else{
            if(!armingService.isConnected()) return ActionStatus.ConnectionFailure;

            if(retryCount == 10)
                return ActionStatus.Failure;

            CommandBoolRequest message = armingService.newMessage();
            message.setValue(false);
            ServiceResponseListener<CommandBoolResponse> listener = new ServiceResponseListener<CommandBoolResponse>() {
                @Override
                public void onSuccess(CommandBoolResponse commandBoolResponse) {
                    serviceResult = ActionStatus.Success;
                }

                @Override
                public void onFailure(RemoteException e) {
                    serviceResult = ActionStatus.Failure;
                }
            };
            armingService.call(message, listener);

            retryCount += 1;
            if(serviceResult == ActionStatus.Success){
                return ActionStatus.Success;
            }else{
                return ActionStatus.Running;
            }
        }
    }

    public String toString() {
        return "DisarmAction";
    }
}
