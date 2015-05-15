/**
 * Intelligent Multiagent System Course
 * Politecnico di Milano
 * year 2015
 * Prof. Amigoni Francesco
 *
 * Project on:
 * Extended Stigmergy in Collective Construction
 *
 * @author Federico Badini
 * @author Stefano Bodini
 * @version 1.0
 */

package it.polimi.ima;

import it.polimi.ima.model.TileBasedMap;
import it.polimi.ima.view.View;
import jade.core.*;
import jade.core.Runtime;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

/**
 * This class represents the entry point of the application
 */
public class IMAMain {

    /**
     * Entry point to our simple test game
     *
     * @param argv The arguments passed into the game
     */
    public static void main(String[] argv) {
        TileBasedMap map = new TileBasedMap();
        View view = new View(map.getTerrainDTO(), map.getAgentDTO());
        Runtime runtime = jade.core.Runtime.instance();
        runtime.setCloseVM(true);
        ContainerController cc =  runtime.createMainContainer(new ProfileImpl(false));

        try {
            int id = 0;
            // create enemy tank agent and start it
            for(int i=0; i<50; i++) {
                id = i+1;
                System.out.println("Agent: " + id);
                Object[] args = {map};
                (cc.createNewAgent("agent" + id, "it.polimi.ima.controller.AgentBiondo", args)).start();
            }
            id++;
            System.out.println("GUI Agent: " + id);
            Object[] args = {map, view};
            (cc.createNewAgent("GUIAgent" + id, "it.polimi.ima.controller.GUIUpdaterAgent", args)).start();

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}

