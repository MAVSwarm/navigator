package com.navigator.core.actions;

import com.navigator.core.actions.util.ActionStatus;
import com.navigator.core.providers.RosPublisherProvider;
import com.navigator.core.providers.RosServiceProvider;
import com.navigator.core.droneState.DroneLanded;
import com.navigator.core.droneState.DroneStateTracker;
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
                    serviceResult = ActionStatus.Success; //  for calling set mode service success
                }

                @Override
                public void onFailure(RemoteException e) {
                    serviceResult = ActionStatus.Failure; //  for calling set mode service failed
                }
            };
            setModeService.call(request, listener);
            timeStamp = time;

            return serviceResult;
        }else{
            return ActionStatus.Failure;
        }




        }

    @Override
    public ActionStatus loopAction(Time time) {
        if (time.subtract(timeStamp).compareTo(timeOut) > 0) {
            return ActionStatus.Success;
        } else {

            objective.getHeader().setStamp(time);

            setpointPositionLocalPub.publish(objective);

            return ActionStatus.Running;
        }
    }

    public String toString() {
        return "HoldAction";
    }
}
