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

import it.polimi.ima.utils.AgentType;
import it.polimi.ima.utils.Constants;
import it.polimi.ima.utils.Movement;
import it.polimi.ima.model.TileBasedMap;
import it.polimi.ima.view.View;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import java.awt.*;
import java.util.Random;

/**
 * This class represents a jade agent.  It provides {@link #setup()} and {@link #takeDown()} methods to handle
 * correctly agent lifecycle.
 */
public class MyAgent extends Agent{

    // References to the model and the view
    private TileBasedMap map;
    private View view;

    // Position of the agent
    private Point position;

    private Movement lastMovement;

    /**
     * This methods choose randomly a movement, updates the model and trigger the repaint of the view
     */
    public void move(Point destination) {
        // Update of the model
        if(lastMovement != Movement.STOP) {
            map.setUnit(position.x,position.y,AgentType.NO_AGENT);
            if(lastMovement == Movement.UP) {
                map.setUnit(destination.x, destination.y, AgentType.AGENT_NORTH);
            } else if(lastMovement == Movement.DOWN) {
                map.setUnit(destination.x, destination.y, AgentType.AGENT_SOUTH);
            } else if (lastMovement == Movement.LEFT) {
                map.setUnit(destination.x, destination.y, AgentType.AGENT_WEST);
            } else if (lastMovement == Movement.RIGHT) {
                map.setUnit(destination.x, destination.y, AgentType.AGENT_EAST);
            }
        }
        System.out.println("posizione: " + position.x + " " + position.y);
        // Update of the view
        view.update(map.getTerrainDTO(), map.getAgentDTO());
        position = destination;
    }

    private Point makeOneStep(){
        Point destination;
        do {
            destination = (Point) position.clone();
            Movement movement = Movement.getRandomMovement();
            switch (movement) {
                case UP:
                    destination.y -= 1;
                    break;
                case DOWN:
                    destination.y += 1;
                    break;
                case LEFT:
                    destination.x -= 1;
                    break;
                case RIGHT:
                    destination.x += 1;
                default:
                    break;
            }
            lastMovement = movement;
        } while (isOutOfBoundary(destination) || computeDistances(destination)<2);
        return destination;
    }

    private int computeDistances(Point reference){
        int distance = Integer.MAX_VALUE;
        for(int i=0; i<Constants.HEIGHT; i++){
            for (int j=0; j<Constants.WIDTH; j++){
                if(map.getUnit(j,i) != AgentType.NO_AGENT && (reference.x != j || reference.y != i) &&
                        !(i==position.y && j==position.x)){
                    if (Math.abs(j-reference.x) + Math.abs(i-reference.y) < distance){
                        distance = Math.abs(j-reference.x) + Math.abs(i-reference.y);
                    }
                }
            }
        }
        return distance;
    }

    public Boolean isOutOfBoundary(Point p){
        return p.x >= map.getWidthInTiles() || p.x < 0 || p.y >= map.getWidthInTiles() || p.y < 0;
    }

    public void setup(){
        map = (TileBasedMap)getArguments()[0];
        view = (View)getArguments()[1];
        Random random = new Random();
        position = new Point();
        do {
            position.x = random.nextInt(map.getWidthInTiles());
            position.y = random.nextInt(map.getWidthInTiles());
        }while(map.blocked(position.x,position.y));
        map.setUnit(position.x, position.y, AgentType.AGENT_NORTH);
        view.update(map.getTerrainDTO(), map.getAgentDTO());
        System.out.println("posizione: " + position.x + " " + position.y);
        addBehaviour(new AgentBehavior(this, 125));
    }

    /**
     * this behavior is used to deal with update job for painting
     */
    private class AgentBehavior extends TickerBehaviour
    {
        public AgentBehavior(jade.core.Agent a, long period) {
            super(a, period);
        }
        @Override
        protected void onTick() {
            move(makeOneStep());
        }
    }
}
