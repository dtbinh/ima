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

package it.polimi.ima.controller;


import it.polimi.ima.model.TileBasedMap;
import it.polimi.ima.view.View;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

public class GUIUpdaterAgent extends Agent{


    // References to the model and the view
    private TileBasedMap map;
    private View view;


    /**
     * Framework method to set up the agent
     */
    public void setup() {
        map = (TileBasedMap) getArguments()[0];
        view = (View) getArguments()[1];
        addBehaviour(new AgentBehavior(this, 1));
    }


    /**
     * this behavior is used to deal with update job for painting
     */
    private class AgentBehavior extends TickerBehaviour {

        public AgentBehavior(jade.core.Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            view.update(map.getTerrainDTO(), map.getAgentDTO());
        }

    }
}
