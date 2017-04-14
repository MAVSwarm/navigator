package com.navigator.core.providers;

import com.navigator.core.states.EmergencyLandingState;
import com.navigator.core.states.IdleState;
import com.navigator.core.states.ScriptedState;
import com.navigator.core.actions.Action;
import com.navigator.core.droneState.DroneStateTracker;
import org.apache.commons.logging.Log;

import java.util.Queue;

public class StateProvider {

    private IdleState idleState;
    private EmergencyLandingState emergencyLandingState;
    private RosServiceProvider serviceProvider;
    private RosPublisherProvider rosPublisherProvider;
    private ActionProvider actionProvider;
    private Log log;
    private DroneStateTracker stateTracker;

    public StateProvider(ActionProvider actionProvider, RosServiceProvider serviceProvider, RosPublisherProvider rosPublisherProvider, Log log, DroneStateTracker stateTracker){
        this.serviceProvider = serviceProvider;
        this.actionProvider = actionProvider;
        this.rosPublisherProvider = rosPublisherProvider;
        this.log = log;
        this.stateTracker = stateTracker;


        this.emergencyLandingState = new EmergencyLandingState(actionProvider, serviceProvider.getSetFCUModeService(), log, stateTracker);

    }

    public IdleState getIdleState(long duration) { return new IdleState(actionProvider, serviceProvider.getSetFCUModeService(), log, duration); }

    public EmergencyLandingState getEmergencyLandingState(){ return emergencyLandingState;}

    public ScriptedState getScriptedState(Queue<Action> actions) {
        return new ScriptedState(actions, actionProvider, serviceProvider.getSetFCUModeService(), log);
    }

}
