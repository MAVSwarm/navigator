package com.navigator.core.droneState;

import com.navigator.core.providers.RosSubscriberProvider;
import geometry_msgs.PoseStamped;
import geometry_msgs.TwistStamped;
import mavros_msgs.ExtendedState;
import mavros_msgs.State;
import org.ros.message.Duration;
import org.ros.message.Time;
import org.ros.node.ConnectedNode;
import sensor_msgs.BatteryState;
import org.ros.message.MessageListener;
import sensor_msgs.NavSatFix;

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

public class DroneStateTracker {

    private ConnectedNode node;
    private boolean armed;      //the drone arming serviceResult
    private float battery;              //battery remaining
    private DroneLanded droneLanded;    //the drone landing serviceResult
    private double[] localVelocity = new double[3];
    private double[] localPosition = new double[3];
    private double[] localOrigin = new double[3];// temporally use this variable to store the local origin coordinate in world frame.
    private double[] visionPosition = new double[3];
    private double lattitude;
    private double longitude;
    private double altitude;
    private String FCUMode;

    private Time lastBatteryTimestamp = new Time(0,0); // last update time of the battery serviceResult
    private Time lastExtendedTimestamp = new Time(0,0); // last update time of the extended serviceResult

    public DroneStateTracker(
            RosSubscriberProvider rosSubscriberProvider, ConnectedNode node){

        this.node = node;

        MessageListener<NavSatFix> globalPositionListener = new MessageListener<NavSatFix>() {
            @Override
            public void onNewMessage(NavSatFix navSatFix) {
                lattitude = navSatFix.getLatitude();
                longitude = navSatFix.getLongitude();
                altitude = navSatFix.getAltitude();
            }
        };
        rosSubscriberProvider.getGlobalPositionGlobalSubscriber().addMessageListener(globalPositionListener);

        MessageListener<TwistStamped> localVelocityListener = new MessageListener<TwistStamped>() {
            @Override
            public void onNewMessage(TwistStamped twistStamped) {
                localVelocity[0] = twistStamped.getTwist().getLinear().getX();
                localVelocity[1] = twistStamped.getTwist().getLinear().getY();
                localVelocity[2] = twistStamped.getTwist().getLinear().getZ();
            }
        };
        rosSubscriberProvider.getLocalPositionVelocitySubscriber().addMessageListener(localVelocityListener);

        MessageListener<PoseStamped> localPoseListener = new MessageListener<PoseStamped>() {
            @Override
            public void onNewMessage(PoseStamped poseStamped) {
                localPosition[0] = poseStamped.getPose().getPosition().getX();
                localPosition[1] = poseStamped.getPose().getPosition().getY();
                localPosition[2] = poseStamped.getPose().getPosition().getZ();
            }
        };
        rosSubscriberProvider.getLocalPositionPoseSubscriber().addMessageListener(localPoseListener);

        MessageListener<PoseStamped> visionPoseListener = new MessageListener<PoseStamped>() {
            @Override
            public void onNewMessage(PoseStamped poseStamped) {
                visionPosition[0] = poseStamped.getPose().getPosition().getX();
                visionPosition[1] = poseStamped.getPose().getPosition().getY();
                visionPosition[2] = poseStamped.getPose().getPosition().getZ();
            }
        };
        rosSubscriberProvider.getVisionPositionPoseSubscriber().addMessageListener(visionPoseListener);

        MessageListener<State> stateListener = new MessageListener<State>() {
            @Override
            public void onNewMessage(State state) {
                armed = state.getArmed();
                FCUMode = state.getMode();
            }
        };
        rosSubscriberProvider.getStateSubscriber().addMessageListener(stateListener);

        MessageListener<BatteryState> batteryListener = new MessageListener<BatteryState>() {
            @Override
            public void onNewMessage(BatteryState state) {
                battery = state.getPercentage();
                lastBatteryTimestamp = state.getHeader().getStamp();
            }
        };
        rosSubscriberProvider.getBatteryStateSubscriber().addMessageListener(batteryListener);

        MessageListener<ExtendedState> extendedStateListener = new MessageListener<ExtendedState>() {
            @Override
            public void onNewMessage(ExtendedState extendedState) {
                switch(extendedState.getLandedState()){
                    case 0: droneLanded = DroneLanded.Undefined;
                        break;
                    case 1: droneLanded = DroneLanded.OnGround;
                        break;
                    case 2: droneLanded = DroneLanded.InAir;
                        break;
                }
                lastExtendedTimestamp = extendedState.getHeader().getStamp();
            }
        };
        rosSubscriberProvider.getExtendedStateSubscriber().addMessageListener(extendedStateListener);

        armed = false;
        battery = -1;
        droneLanded = DroneLanded.Undefined;
    }

    //getters
    public boolean getArmed(){
        return armed;
    }
    public float getRemaining() {return battery;}
    public DroneLanded getDroneLanded() {return droneLanded;}
    public double[] getLocalPosition() { return localPosition;}
    public double getLattitude() { return lattitude;}
    public double getLongitude() { return longitude;}
    public double getAltitude() { return  altitude;}
    public String getFCUMode() { return FCUMode;}
    public double[] getLocalOrigin(){return localOrigin;}
    public double[] getVisionPosition(){return visionPosition; }
    public double[] getLocalVelocity(){return localVelocity;}

    //setters
    public void setLocalOrigin(double[] originValue){
        localOrigin[0] = originValue[0];
        localOrigin[1] = originValue[1];
        localOrigin[2] = originValue[2];
    }

    public boolean ready(){
        return node.getCurrentTime().subtract(lastBatteryTimestamp).compareTo(new Duration(1, 0)) < 0
                && node.getCurrentTime().subtract(lastExtendedTimestamp).compareTo(new Duration(1, 0)) < 0;
    }
}
