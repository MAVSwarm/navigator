package com.navigator.core.actions;

import com.navigator.core.actions.util.ActionStatus;
import com.navigator.core.actions.util.Waypoint;
import com.navigator.core.providers.RosPublisherProvider;
import com.navigator.core.providers.RosServiceProvider;
import com.navigator.core.droneState.DroneLanded;
import com.navigator.core.droneState.DroneStateTracker;
import geometry_msgs.Pose;
import geometry_msgs.PoseStamped;
import mavros_msgs.SetModeRequest;
import mavros_msgs.SetModeResponse;
import org.ros.exception.RemoteException;
import org.ros.message.Duration;
import org.ros.message.Time;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;
import org.ros.node.topic.Publisher;
import org.apache.commons.logging.Log;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

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

public class PX4FlyToAction extends Action {

    private PoseStamped objective;
    private DroneStateTracker stateTracker;
    private Publisher<PoseStamped> setpointPositionLocalPub;
    private Duration timeOut;
    private ServiceClient<SetModeRequest, SetModeResponse> setModeService;
    private int seq;
    private Log logger;

    public PX4FlyToAction(Log logger,
                          Waypoint objective,
                          DroneStateTracker stateTracker,
                          RosPublisherProvider rosPublisherProvider,
                          Duration timeOut,
                          RosServiceProvider serviceProvider,
                          int seq){
        this.logger =logger;
        this.seq = seq;
        setModeService = serviceProvider.getSetFCUModeService();
        this.setpointPositionLocalPub = rosPublisherProvider.getSetpointPositionLocalPublisher();

        this.objective = this.setpointPositionLocalPub.newMessage();

        //temporarily store the objective in reference with world frame
        this.objective.getPose().getPosition().setX(objective.getX());
        this.objective.getPose().getPosition().setY(objective.getY());
        this.objective.getPose().getPosition().setZ(objective.getZ());

        this.objective.getHeader().setSeq(seq);

        this.stateTracker = stateTracker;

        this.timeOut = timeOut;
    }

    private double calculateDistance(Pose objective, double[] localPosition){
        return sqrt(pow((objective.getPosition().getX() - localPosition[0]) ,2) + pow((objective.getPosition().getY() - localPosition[1]),2) + pow((objective.getPosition().getZ() - localPosition[2]),2));
    }

    @Override
    public ActionStatus enterAction(Time time){

        if(stateTracker.getDroneLanded() == DroneLanded.InAir){

            objective.getPose().getPosition().setX(objective.getPose().getPosition().getX());
            objective.getPose().getPosition().setY(objective.getPose().getPosition().getY());
            objective.getPose().getPosition().setZ(objective.getPose().getPosition().getZ());

            objective.getHeader().setStamp(time);
            setpointPositionLocalPub.publish(objective);
            seq++;

            if (!stateTracker.getFCUMode().equals("OFFBOARD")){
                if(!setModeService.isConnected()) return ActionStatus.ConnectionFailure;

                SetModeRequest request = setModeService.newMessage();
                request.setCustomMode("OFFBOARD");

                ServiceResponseListener<SetModeResponse> listener = new ServiceResponseListener<SetModeResponse>() {
                    @Override
                    public void onSuccess(SetModeResponse setModeResponse) {
                        serviceResult = ActionStatus.Success;
                    }

                    @Override
                    public void onFailure(RemoteException e) {
                        serviceResult = ActionStatus.Failure;
                    }
                };
                setModeService.call(request, listener);
            }

            timeStamp = time;

            return serviceResult;
        }else{
            return ActionStatus.Failure;
        }
    }

    @Override
    public ActionStatus loopAction(Time time) {
        if(calculateDistance(objective.getPose(),stateTracker.getLocalPosition()) < 0.5
                && stateTracker.getDroneLanded() == DroneLanded.InAir){
            return ActionStatus.Success;
        }else if(time.subtract(timeStamp).compareTo(timeOut) >= 0){
            return ActionStatus.Failure;
        }else{
            objective.getHeader().setStamp(time);
            objective.getHeader().setSeq(seq);
            setpointPositionLocalPub.publish(objective);
            seq++;
            return ActionStatus.Running;
        }
    }
}
