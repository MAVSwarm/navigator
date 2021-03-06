package com.navigator.core.actions;

import com.navigator.core.providers.RosPublisherProvider;
import com.navigator.core.providers.RosServiceProvider;
import com.navigator.core.actions.util.ActionStatus;
import com.navigator.core.droneState.DroneLanded;
import com.navigator.core.droneState.DroneStateTracker;
import com.navigator.core.providers.RosSubscriberProvider;
import geometry_msgs.PoseStamped;
import geometry_msgs.TwistStamped;
import mavros_msgs.SetModeRequest;
import mavros_msgs.SetModeResponse;
import org.apache.commons.logging.Log;
import org.ros.exception.RemoteException;
import org.ros.message.Duration;
import org.ros.message.MessageListener;
import org.ros.message.Time;
import org.ros.node.service.ServiceClient;
import org.ros.node.service.ServiceResponseListener;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

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

public class ExternalAction extends Action {

    private Log log;
    private ServiceClient<SetModeRequest, SetModeResponse> setFCUModeService;
    private Publisher<PoseStamped> setpointPositionLocalPublisher;
    private Publisher<TwistStamped> setpointVelocityPublisher;
    private DroneStateTracker droneStateTracker;
    private PoseStamped setpointPositionLocal;
    private Subscriber<TwistStamped> externalVelocitySetpointSubscriber;
    private TwistStamped lastVelocitySetpoint;
    private Time lastVelocitySetpointTime = new Time(0,0);
    private Duration timeout;

    public ExternalAction(
            RosPublisherProvider rosPublisherProvider,
            RosSubscriberProvider rosSubscriberProvider,
            Log log,
            DroneStateTracker droneStateTracker,
            RosServiceProvider rosServiceProvider,
            Duration timeout
    ){
        this.timeout = timeout;
        this.externalVelocitySetpointSubscriber = rosSubscriberProvider.getExternalVelocitySetpointSubscriber();
        this.setpointPositionLocalPublisher = rosPublisherProvider.getSetpointPositionLocalPublisher();
        this.setpointVelocityPublisher = rosPublisherProvider.getSetpointVelocityPublisher();
        this.setFCUModeService = rosServiceProvider.getSetFCUModeService();
        this.log = log;
        this.droneStateTracker = droneStateTracker;
        this.lastVelocitySetpoint = setpointVelocityPublisher.newMessage();
    }



    @Override
    public ActionStatus enterAction(Time time){
        if(droneStateTracker.getDroneLanded() == DroneLanded.InAir){

            if(!setFCUModeService.isConnected()) return ActionStatus.ConnectionFailure; //return connection failure if the setFCUModeService is not connected

            //construct a setpoint message containing the current MAV position
            setpointPositionLocal = setpointPositionLocalPublisher.newMessage();
            setpointPositionLocal.getHeader().setStamp(time);
            setpointPositionLocal.getPose().getPosition().setX(droneStateTracker.getLocalPosition()[0]);
            setpointPositionLocal.getPose().getPosition().setY(droneStateTracker.getLocalPosition()[1]);
            setpointPositionLocal.getPose().getPosition().setZ(droneStateTracker.getLocalPosition()[2]);

            //publish this message. Because it requires a velocity or position setpoint before switching to OFFBOARD mode.
            setpointPositionLocalPublisher.publish(setpointPositionLocal);

            //listen to the external velocity setpoint
            MessageListener<TwistStamped> externalVelocitySetpointListener = new MessageListener<TwistStamped>() {
                @Override
                public void onNewMessage(TwistStamped twistStamped) {
                    lastVelocitySetpoint.setHeader(twistStamped.getHeader());
                    lastVelocitySetpoint.setTwist(twistStamped.getTwist());
                }
            };
            externalVelocitySetpointSubscriber.addMessageListener(externalVelocitySetpointListener);

            //switch to OFFBOARD mode
            if (!droneStateTracker.getFCUMode().equals("OFFBOARD")){


                SetModeRequest request = setFCUModeService.newMessage();
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
                setFCUModeService.call(request, listener);
            }

            timeStamp = time;

            //initialize the velocity setpoint
            lastVelocitySetpoint.getHeader().setStamp(timeStamp);
            lastVelocitySetpoint.getTwist().getLinear().setX(0);
            lastVelocitySetpoint.getTwist().getLinear().setY(0);
            lastVelocitySetpoint.getTwist().getLinear().setZ(0);
            lastVelocitySetpoint.getTwist().getAngular().setX(0);
            lastVelocitySetpoint.getTwist().getAngular().setY(0);
            lastVelocitySetpoint.getTwist().getAngular().setZ(0);

            return serviceResult;

        }else {
            return ActionStatus.Failure;
        }
    }

    @Override
    public ActionStatus loopAction(Time time) {
        try {
            lastVelocitySetpointTime = lastVelocitySetpoint.getHeader().getStamp();
            if (time.subtract(lastVelocitySetpointTime).compareTo(new Duration(1, 0)) < 0) {
                if(time.subtract(timeStamp).compareTo(timeout) > 0){
                    return ActionStatus.Success;
                }else {
                    setpointVelocityPublisher.publish(lastVelocitySetpoint);
                    return ActionStatus.Running;
                }
            } else {
                log.warn("external velocity setpoint time out.");
                return ActionStatus.Failure;
            }

        }catch (Exception e){
            log.warn("error in loopAction of ExternalAction");
            return ActionStatus.Failure;
        }

    }

    public String toString() {
        return "ExternalAction";
    }
}
