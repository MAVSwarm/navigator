package com.state_machine.core.actions;

import com.state_machine.core.actions.util.ActionStatus;
import org.ros.message.Time;

/**
 * Created by edward on 17-3-26.
 */
public class SimulinkAction extends Action {

    public SimulinkAction(){}

    @Override
    public ActionStatus loopAction(Time time) {
        return null;
    }
}
