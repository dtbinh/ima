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
import it.polimi.ima.utils.AgentAction;
import it.polimi.ima.view.MapPanel;
import it.polimi.ima.view.View;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

/**
 * The following class is in charge of receiving view inputs and triggering view updates
 */
public class MapUpdaterAgent extends Agent{

    // References to the model and the view
    private TileBasedMap map;
    private View view;
    // Current behaviour parameters
    private AgentBehavior agentBehavior;
    private int period;

    /**
     * Framework method to set up the agent
     */
    public void setup() {
        map = (TileBasedMap) getArguments()[0];
        view = (View) getArguments()[1];
        // Add the behaviour to the agent
        period = view.getControls().getValue();
        agentBehavior = new AgentBehavior(this, period);
        addBehaviour(agentBehavior);
    }

    /*
     * This behavior is used to deal with update job for painting
     */
    private class AgentBehavior extends TickerBehaviour {
        public AgentBehavior(jade.core.Agent a, long period) {
            super(a, period);
        }
        @Override
        protected void onTick() {
            int newPeriod = view.getControls().getValue() + 1;
            if(newPeriod != period){
                period = newPeriod;
                removeBehaviour(agentBehavior);
                agentBehavior = new AgentBehavior(MapUpdaterAgent.this, period);
                MapUpdaterAgent.this.addBehaviour(agentBehavior);
            }
            view.getMapPanel().update(map.getTerrainDTO(), map.getAgentDTO());
        }
    }
}
