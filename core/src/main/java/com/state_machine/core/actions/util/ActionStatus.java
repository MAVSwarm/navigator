package com.state_machine.core.actions.util;

public enum ActionStatus {
    Running,                //The action is running now
    Success,                //The action is success
    ConnectionFailure,      //There is a connection failure
    Failure                //There is another failure
}
