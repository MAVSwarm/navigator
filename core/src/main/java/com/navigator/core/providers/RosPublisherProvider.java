package com.navigator.core.providers;

import geometry_msgs.TwistStamped;
import mavros_msgs.OverrideRCIn;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import geometry_msgs.PoseStamped;

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

public class RosPublisherProvider {
    private Publisher<OverrideRCIn> overrideRCInPublisher;
    private Publisher<PoseStamped> setpointPositionLocalPublisher;
    private Publisher<TwistStamped> setpointVelocityPublisher;

    public RosPublisherProvider(ConnectedNode node){
        overrideRCInPublisher = node.newPublisher("mavros/rc/override",OverrideRCIn._TYPE);
        setpointPositionLocalPublisher = node.newPublisher("mavros/setpoint_position/local", PoseStamped._TYPE);
        setpointVelocityPublisher = node.newPublisher("mavros/setpoint_velocity/cmd_vel",TwistStamped._TYPE);
    }

    public Publisher<OverrideRCIn> getOverrideRCInPubliser(){
        return overrideRCInPublisher;
    }

    public Publisher<PoseStamped> getSetpointPositionLocalPublisher(){ return setpointPositionLocalPublisher; }

    public Publisher<TwistStamped> getSetpointVelocityPublisher(){ return setpointVelocityPublisher;}
}
