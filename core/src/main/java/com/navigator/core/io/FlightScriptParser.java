package com.navigator.core.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.navigator.core.actions.util.Waypoint;
import com.navigator.core.actions.Action;
import com.navigator.core.providers.ActionProvider;
import com.navigator.core.providers.StateProvider;
import com.navigator.core.states.State;
import org.apache.commons.logging.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

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

public class FlightScriptParser {

    private StateProvider stateProvider;
    private ActionProvider actionProvider;
    private Log log;
    private Gson gson;
    private int seq;//waypoint sequence

    public FlightScriptParser(ActionProvider actions, StateProvider states, Log log){
        this.stateProvider = states;
        this.actionProvider = actions;
        this.gson = new GsonBuilder().create();
        this.log = log;
        seq = 0;
    }

    public Queue<State> parseFile(String filePath){
        Queue<State> states = new ArrayDeque<>();

        try {
            InputStream in = new FileInputStream(new File(filePath));
            Scanner scanner = null;
            StringBuilder json = new StringBuilder();
            List<StateJsonRepresentation> stateInfo = new ArrayList<>();
            try{
                scanner = new Scanner(in);
                while(scanner.hasNextLine()){
                    json.append(scanner.nextLine() + "\n");
                }
                stateInfo = gson.fromJson(json.toString(), JsonStateList.class).queue;
            } catch (Exception e){
                log.warn("Could not read flight io at " + filePath, e);
            } finally {
                if(scanner != null) scanner.close();
            }
            for(StateJsonRepresentation s : stateInfo){
                State state = parseState(s);
                if(state != null) states.add(state);
            }
            return states;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return states;
        }
    }

    private State parseState(StateJsonRepresentation repr){
        switch(repr.state){
            case "IdleState":
                return stateProvider.getIdleState(repr.duration);
            case "ScriptedState":
                Queue<Action> actions = new ArrayDeque<>();
                for(ActionJsonRepresentation a : repr.scriptedActions){
                    Action action = parseAction(a);
                    if(action != null) actions.add(action);
                }
                return stateProvider.getScriptedState(actions);
            default:
                log.warn("Flight io contained invalid state: " + repr.state);
                return null;
        }
    }

    private Action parseAction(ActionJsonRepresentation repr){
        switch(repr.action){
            case "ArmAction":
                return actionProvider.getArmAction();
            case "DisarmAction":
                return actionProvider.getDisarmAction();
            case "SetFCUModeAction":
                return actionProvider.getSetFCUModeAction(repr.newMode);
            case "PX4TakeoffAction":
                return actionProvider.getPX4TakeoffAction(repr.target_heightm);
            case "PX4LandAction":
                return actionProvider.getPX4LandAction();
            case "DecentralizedAction":
                return actionProvider.getDecentralizedAction();
            case "PX4FlyToAction":
                seq++;
                List<Float> xyz2 = repr.waypoint;
                Waypoint objective2 = new Waypoint(xyz2.get(0), xyz2.get(1), xyz2.get(2));
                return actionProvider.getPX4FlyToAction(objective2,seq);
            case "HoldAction":
                return actionProvider.getHoldAction(repr.durationMs);
            case "ExternalAction":
                return actionProvider.getExternalAction(repr.durationMs);
            default:
                log.warn("Flight io contained invalid action: " + repr.action);
                return null;
        }
    }

    private class JsonStateList {
        List<StateJsonRepresentation> queue;
    }

    private class StateJsonRepresentation {
        String state;
        List<ActionJsonRepresentation> scriptedActions;
        long duration;
        //other types of parameters go here by name
    }

    private class ActionJsonRepresentation {
        String action;
        double target_heightm;
        Float target_heightcm;
        List<Float> waypoint;
        String newMode; // new mode for SetFCUModeAction
        int durationMs;
        //possible parameters go here by name
    }
}
