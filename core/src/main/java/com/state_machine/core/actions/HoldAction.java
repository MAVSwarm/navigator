package com.state_machine.core.actions;

import com.state_machine.core.actions.util.ActionStatus;
import com.state_machine.core.actions.util.Waypoint;
import com.state_machine.core.droneState.DroneLanded;
import com.state_machine.core.droneState.DroneStateTracker;
import com.state_machine.core.providers.RosPublisherProvider;
import com.state_machine.core.providers.RosServiceProvider;
import geometry_msgs.Pose;
import geometry_msgs.PoseStamped;
import mavros_msgs.SetModeRequest;
import mavros_msgs.SetModeResponse;
import org.apache.commons.logging.Log;
import org.ros.exception.RemoteException;
import org.ros.message.Duration;
import org.ros.message.Time;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;
import org.ros.node.topic.Publisher;

/**
 * Created by edward on 17-3-27.
 */
public class HoldAction extends Action {

    private Log logger;
    private DroneStateTracker stateTracker;
    private Duration timeOut;
    private ServiceClient<SetModeRequest, SetModeResponse> setModeService;
    private Publisher<PoseStamped> setpointPositionLocalPub;
    private PoseStamped objective;

    public HoldAction(
            Log logger,
            DroneStateTracker stateTracker,
            Duration timeOut,
            RosServiceProvider serviceProvider,
            RosPublisherProvider rosPublisherProvider
    ){
        this.logger = logger;
        this.stateTracker = stateTracker;
        this.timeOut = timeOut;
        this.setModeService = serviceProvider.getSetFCUModeService();
        this.setpointPositionLocalPub = rosPublisherProvider.getSetpointPositionLocalPublisher();

        this.objective = this.setpointPositionLocalPub.newMessage();
    }


    @Override
    public ActionStatus enterAction(Time time){
        status = ActionStatus.Inactive;

        if(stateTracker.getDroneLanded() == DroneLanded.InAir){

            if(!setModeService.isConnected()) return ActionStatus.ConnectionFailure;

            objective.getPose().getPosition().setX(stateTracker.getLocalPosition()[0]);
            objective.getPose().getPosition().setY(stateTracker.getLocalPosition()[1]);
            objective.getPose().getPosition().setZ(stateTracker.getLocalPosition()[2]);
            objective.getHeader().setStamp(time);

            setpointPositionLocalPub.publish(objective);

            SetModeRequest request = setModeService.newMessage();
            request.setCustomMode("OFFBOARD");

            ServiceResponseListener<SetModeResponse> listener = new ServiceResponseListener<SetModeResponse>() {
                @Override
                public void onSuccess(SetModeResponse setModeResponse) {
                    status = ActionStatus.Success;
                }

                @Override
                public void onFailure(RemoteException e) {
                    status = ActionStatus.Failure;
                }
            };
            setModeService.call(request, listener);
            timeStamp = time;

            return status;
        }else{
            return ActionStatus.Failure;
        }




        }

    @Override
    public ActionStatus loopAction(Time time) {
        if(time.subtract(timeStamp).compareTo(timeOut) > 0){
            status = ActionStatus.Success;
            return status;
        }else{

            objective.getHeader().setStamp(time);

            setpointPositionLocalPub.publish(objective);

            status = ActionStatus.Waiting;
            return status;
        }
    }

    public String toString() {
        return "HoldAction";
    }
}
