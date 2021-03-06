package com.navigator.core.providers;

import geometry_msgs.PoseStamped;
import geometry_msgs.TwistStamped;
import sensor_msgs.BatteryState;
import mavros_msgs.ExtendedState;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;
import mavros_msgs.State;
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


public class RosSubscriberProvider {

    private Subscriber<State> stateSubscriber;
    private Subscriber<BatteryState> batteryStateSubscriber;
    private Subscriber<ExtendedState> extendedStateSubscriber;

    private Subscriber<NavSatFix> globalPositionGlobalSubscriber;
    private Subscriber<TwistStamped> localPositionVelocitySubscriber;
    private Subscriber<PoseStamped> localPositionPoseSubscriber;
    private Subscriber<PoseStamped> visionPositionPoseSubscriber;

    private Subscriber<TwistStamped> externalVelocitySetpointSubscriber;


    public RosSubscriberProvider(ConnectedNode node){
        stateSubscriber = node.newSubscriber("mavros/state", State._TYPE);
        batteryStateSubscriber = node.newSubscriber("mavros/battery", BatteryState._TYPE);
        extendedStateSubscriber = node.newSubscriber("mavros/extended_state", ExtendedState._TYPE);
        globalPositionGlobalSubscriber = node.newSubscriber("mavros/global_position/global", NavSatFix._TYPE);
        localPositionVelocitySubscriber = node.newSubscriber("mavros/local_position/velocity", TwistStamped._TYPE);
        localPositionPoseSubscriber = node.newSubscriber("mavros/local_position/pose", PoseStamped._TYPE);
        visionPositionPoseSubscriber = node.newSubscriber("mavros/vision_pose/pose", PoseStamped._TYPE);
        externalVelocitySetpointSubscriber = node.newSubscriber("~velocity_setpoint", TwistStamped._TYPE);
    }

    public Subscriber<State> getStateSubscriber() { return stateSubscriber; }
    public Subscriber<BatteryState> getBatteryStateSubscriber() { return batteryStateSubscriber;  }
    public Subscriber<ExtendedState> getExtendedStateSubscriber() { return extendedStateSubscriber; }
    public Subscriber<NavSatFix> getGlobalPositionGlobalSubscriber() { return globalPositionGlobalSubscriber;}
    public Subscriber<TwistStamped> getLocalPositionVelocitySubscriber(){return localPositionVelocitySubscriber;}
    public Subscriber<PoseStamped> getLocalPositionPoseSubscriber() { return localPositionPoseSubscriber;}
    public Subscriber<PoseStamped> getVisionPositionPoseSubscriber() { return visionPositionPoseSubscriber;}
    public Subscriber<TwistStamped> getExternalVelocitySetpointSubscriber() { return externalVelocitySetpointSubscriber;}
}
