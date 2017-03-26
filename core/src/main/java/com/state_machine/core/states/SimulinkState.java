package com.state_machine.core.states;

import com.state_machine.core.providers.ActionProvider;
import mavros_msgs.SetModeRequest;
import mavros_msgs.SetModeResponse;
import org.apache.commons.logging.Log;
import org.ros.node.service.ServiceClient;

/**
 * Created by edward on 17-3-26.
 */
public class SimulinkState extends State {

    public SimulinkState(ActionProvider actionProvider,
                         ServiceClient<SetModeRequest, SetModeResponse> setModeService,
                         Log log)
    {
        super(actionProvider, setModeService, log);

    }

    @Override
    public boolean isSafeToExit() {
        return false;
    }
}
