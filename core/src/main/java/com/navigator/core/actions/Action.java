package com.navigator.core.actions;

import com.navigator.core.actions.util.ActionStatus;
import org.ros.message.Duration;
import org.ros.message.Time;

public abstract class Action {

    // timeStamp records the start time of an action.
    protected Time timeStamp = new Time(0,0);

    protected ActionStatus serviceResult;

    protected static final Duration enterTimeOut = new Duration(0,50000);

    public abstract ActionStatus loopAction(Time time);

    public Action() {
    }

    public ActionStatus enterAction(Time time) { return ActionStatus.Success; }

    public void exitAction(){}
}
