package com.navigator.core.providers;

import com.navigator.core.io.FlightScriptParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Queue;

import com.navigator.core.states.State;
import org.apache.commons.logging.Log;

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

public class FileProvider {

    private Properties config;
    private FlightScriptParser flightScriptParser;
    private RosParamProvider rosParamProvider;

    public FileProvider(RosParamProvider rosParamProvider, ActionProvider actionProvider, StateProvider stateProvider, Log log){
        flightScriptParser = new FlightScriptParser(actionProvider, stateProvider, log);
        this.rosParamProvider = rosParamProvider;
        initConfig();
    }

    public Properties getConfig(){ return config; }

    public Queue<State> readScript(String filePath){
        return flightScriptParser.parseFile(filePath);
    }

    private void initConfig(){
        Properties properties = new Properties();
        InputStream input = null;
        try{
            //todo: change the path format to relative path
            input = new FileInputStream(rosParamProvider.getPropertiesPath());
            properties.load(input);
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                if(input != null) input.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        config = properties;
    }
}
