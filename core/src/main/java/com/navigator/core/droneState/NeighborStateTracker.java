package com.navigator.core.droneState;

import geometry_msgs.PoseStamped;
import geometry_msgs.TwistStamped;
import org.apache.commons.logging.Log;
import org.ros.master.client.MasterStateClient;
import org.ros.message.MessageListener;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Subscriber;


import java.util.Vector;

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

public class NeighborStateTracker {

    private ConnectedNode node;
    private String thisDroneName;
    private MasterStateClient masterStateClient;
    private int neighborNum;
    private Vector<Subscriber<PoseStamped> > neighborVisionPoseSubscribers;
    private Vector<Subscriber<TwistStamped> > neighborLocalVelocitySubscribers;
    private Vector<double[]> neighborVisionPoses;
    private Vector<double[]> neighborLocalVelocities;
    private Log log;
    private DroneStateTracker stateTracker;


    public NeighborStateTracker(
            ConnectedNode node,
            DroneStateTracker stateTracker
    ){
        this.log = node.getLog();
        this.node = node;
        this.stateTracker = stateTracker;
        thisDroneName = node.getName().getParent().getBasename().toString();
        log.info("drone name:" + thisDroneName);
        masterStateClient = new MasterStateClient(node,node.getMasterUri());
        neighborVisionPoseSubscribers = new Vector<>();
        neighborLocalVelocitySubscribers = new Vector<>();
        neighborLocalVelocities = new Vector<>();
        neighborVisionPoses = new Vector<>();
        neighborNum = 0;
    }

    private double calcDistance(double[] pose1, double[] pose2){
        double a,b,c;
        a = pose1[0] - pose2[0];
        b = pose1[1] - pose2[1];
        c = pose2[2] - pose2[2];

        return sqrt(a*a + b*b + c*c);
    }

    public boolean UpdataNeighborList(){

        neighborLocalVelocitySubscribers.clear();
        neighborLocalVelocities.clear();
        neighborVisionPoseSubscribers.clear();
        neighborVisionPoses.clear();

        int neighborIndex = 0;
        for(int droneIndex = 1;;droneIndex++) {
            try {
                if (!thisDroneName.equals("drone" + droneIndex)
                && !masterStateClient.lookupNode("/drone" + droneIndex + "/navigator").isOpaque()) {

                    //log.info("new neighbor found,drone name: drone" + droneIndex);

                    Subscriber<PoseStamped> newPoseSubscriber = node.newSubscriber("/drone" + droneIndex + "/mavros/vision_pose/pose", PoseStamped._TYPE);
                    neighborVisionPoseSubscribers.add(newPoseSubscriber);
                    Subscriber<TwistStamped> newVelocitySubscriber = node.newSubscriber("/drone" + droneIndex + "/mavros/local_position/velocity", TwistStamped._TYPE);
                    neighborLocalVelocitySubscribers.add(newVelocitySubscriber);

                    neighborLocalVelocities.add(new double[3]);
                    neighborVisionPoses.add(new double[3]);
                    final int temp_index = neighborIndex;
                    MessageListener<TwistStamped> localVelocityListener = new MessageListener<TwistStamped>() {
                        @Override
                        public void onNewMessage(TwistStamped twistStamped) {
                            neighborLocalVelocities.elementAt(temp_index)[0] = twistStamped.getTwist().getLinear().getX();
                            neighborLocalVelocities.elementAt(temp_index)[1] = twistStamped.getTwist().getLinear().getY();
                            neighborLocalVelocities.elementAt(temp_index)[2] = twistStamped.getTwist().getLinear().getZ();

                        }
                    };
                    neighborLocalVelocitySubscribers.elementAt(neighborIndex).addMessageListener(localVelocityListener);

                    MessageListener<PoseStamped> visionPoseListener = new MessageListener<PoseStamped>() {
                        @Override
                        public void onNewMessage(PoseStamped poseStamped) {
                            neighborVisionPoses.elementAt(temp_index)[0] = poseStamped.getPose().getPosition().getX();
                            neighborVisionPoses.elementAt(temp_index)[1] = poseStamped.getPose().getPosition().getY();
                            neighborVisionPoses.elementAt(temp_index)[2] = poseStamped.getPose().getPosition().getZ();
                        }
                    };
                    neighborVisionPoseSubscribers.elementAt(neighborIndex).addMessageListener(visionPoseListener);
                    neighborIndex++;
                }
            } catch (Exception e) {
                break;
            }
        }
        neighborNum = neighborIndex;
        return true;
    }

    public boolean clearNeighborList(){

        while(!neighborLocalVelocitySubscribers.isEmpty()){
            neighborLocalVelocitySubscribers.remove(0).shutdown();
        }

        while(!neighborVisionPoseSubscribers.isEmpty()){
            neighborVisionPoseSubscribers.remove(0).shutdown();
        }

        neighborLocalVelocities.clear();
        neighborVisionPoses.clear();

        return neighborVisionPoseSubscribers.isEmpty() && neighborLocalVelocitySubscribers.isEmpty();
    }

    public Vector<double[]> getNeighborVisionPoses(){

        Vector<double[]> poses = new Vector<>();
        for(int i =0; i<neighborVisionPoses.size(); i++){
            if(calcDistance(stateTracker.getVisionPosition(),neighborVisionPoses.get(i)) < 20
            ){
                poses.add(neighborVisionPoses.get(i));
            }
        }

        return poses;
    }

    public Vector<double[]> getNeighborLocalVelocities(){

        Vector<double[]> vels = new Vector<>();
        for(int i =0; i<neighborLocalVelocities.size(); i++){
            if(calcDistance(stateTracker.getVisionPosition(),neighborVisionPoses.get(i)) < 20){
                vels.add(neighborLocalVelocities.get(i));
            }
        }

        return vels;
    }
    public int getNeighborNum(){
        int n = 0;
        for(int i =0; i<neighborVisionPoses.size(); i++){
            if(calcDistance(stateTracker.getVisionPosition(),neighborVisionPoses.get(i)) < 20){
                n += 1;
            }
        }
        return n;
    }

}
