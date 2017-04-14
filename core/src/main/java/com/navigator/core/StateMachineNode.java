package com.navigator.core;

import com.navigator.core.providers.*;
import com.navigator.core.stateMachine.StateMachine;
import com.navigator.core.stateMachine.utils.StateControl;
import com.navigator.core.states.State;
import com.navigator.core.droneState.DroneStateTracker;
import com.navigator.core.droneState.NeighborStateTracker;
import org.apache.commons.logging.Log;
import org.ros.concurrent.CancellableLoop;
import org.ros.message.Duration;
import org.ros.message.Time;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;

public class StateMachineNode extends AbstractNodeMain {

    private ConnectedNode node;
    private Log log;
    private StateMachine stateMachine;
    private DroneStateTracker droneStateTracker;
    private NeighborStateTracker neighborStateTracker;
    private ActionProvider actionProvider;
    private StateProvider stateProvider;
    private FileProvider fileProvider;
    private Time timeStamp = new Time(0,0);

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("navigator");
    }

    @Override
    public void onStart(final ConnectedNode node) {


        this.node = node;
        this.log = node.getLog();
        try {


            Thread.sleep(10000);

            RosServiceProvider serviceProvider = new RosServiceProvider(node);
            RosSubscriberProvider subscriberProvider = new RosSubscriberProvider(node);
            RosPublisherProvider publisherProvider = new RosPublisherProvider(node);
            RosServerProvider serverProvider = new RosServerProvider(node);
            RosParamProvider rosParamProvider = new RosParamProvider(node,serviceProvider,log);
            Duration timeOut = new Duration(120,0);//todo: remove the magic number here

            droneStateTracker = new DroneStateTracker(
                    subscriberProvider, node);
            neighborStateTracker = new NeighborStateTracker(node, droneStateTracker);
            actionProvider = new ActionProvider(log, serviceProvider, droneStateTracker, neighborStateTracker, publisherProvider,timeOut,serverProvider, rosParamProvider, subscriberProvider);
            stateProvider = new StateProvider(actionProvider, serviceProvider, publisherProvider, log, droneStateTracker);
            fileProvider = new FileProvider(rosParamProvider,actionProvider, stateProvider, log);
            if(!serviceProvider.isConnected()) {
                throw new Exception("service not connected, please run mavros first");
            }

            timeStamp = node.getCurrentTime();
            while (!droneStateTracker.ready()){
                if(node.getCurrentTime().subtract(timeStamp).compareTo(new Duration(20,0)) > 0){
                    throw new Exception("timeout");
                }
                Thread.sleep(1000);
            }
            Thread.sleep(3000);



        State initialState = stateProvider.getIdleState(0);

        stateMachine = new StateMachine(
                initialState,
                stateProvider.getEmergencyLandingState(),
                log,
                droneStateTracker
        );

        final StateControl stateControl = new StateControl(node,stateProvider,stateMachine,rosParamProvider,fileProvider);

        node.executeCancellableLoop(new CancellableLoop() {
            @Override
            protected void loop() throws InterruptedException {
                if(stateControl.getStateQueue() != null
                   && !stateControl.getStateQueue().isEmpty()
                   && stateMachine.getCurrentState().isIdling()
                   && stateMachine.getCurrentState().isSafeToExit()){
                    stateMachine.setState(stateControl.getStateQueue().remove());
                }
                stateMachine.update(node.getCurrentTime());
                Thread.sleep(100);
            }
        });
        } catch(Exception e) {
            log.fatal("Initialization failed", e);
            System.exit(1);
        }
    }
}
