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
import it.polimi.ima.utils.Constants;
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
            // Create agents
            for(int i=0; i< Constants.NUM_OF_AGENTS; i++) {
                System.out.println("WorkerAgent" + (i+1) + ": Hello!");
                Object[] args = {map, view, i};
                (cc.createNewAgent("agent" + (i+1), "it.polimi.ima.controller.WorkerAgent", args)).start();
            }
            System.out.println("MapUpdaterAgent: Hello!");
            Object[] args = {map, view};
            (cc.createNewAgent("MapUpdaterAgent", "it.polimi.ima.controller.MapUpdaterAgent", args)).start();
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}

