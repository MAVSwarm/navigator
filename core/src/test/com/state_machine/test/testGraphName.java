package com.state_machine.test;

import org.ros.master.client.MasterStateClient;
import org.ros.namespace.GraphName;
import org.ros.node.AbstractNodeMain;
import org.ros.node.ConnectedNode;

/**
 * Created by Gao Changyu on 12/11/16.
 */
public class testGraphName extends AbstractNodeMain{

    private ConnectedNode node;
    private MasterStateClient masterStateClient;

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("test");
    }

    @Override
    public void onStart(final ConnectedNode node){
        this.node = node;
        masterStateClient = new MasterStateClient(node,node.getMasterUri());

        for(int index = 1;;){
            try{
                if(!masterStateClient.lookupNode("/drone" + index + "/state_machine").isOpaque()){
                    System.out.println("drone"+index);
                }
            }catch (Exception e){
                System.out.println("end of stuff");
                break;
            }
            index++;
        }
        System.out.println("end of for");


    }
}