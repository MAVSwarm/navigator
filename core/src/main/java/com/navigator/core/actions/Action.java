package com.navigator.core.actions;

import com.navigator.core.actions.util.ActionStatus;
import org.ros.message.Duration;
import org.ros.message.Time;

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
