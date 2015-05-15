/**
 * Intelligent Multiagent System Course
 * Politecnico di Milano
 * year 2015
 * Prof. Amigoni Francesco
 * <p>
 * Project on:
 * Extended Stigmergy in Collective Construction
 *
 * @author Federico Badini
 * @author Stefano Bodini
 * @version 1.0
 */

package it.polimi.ima.controller;

import it.polimi.ima.utils.AgentOrientation;
import it.polimi.ima.utils.Constants;
import it.polimi.ima.utils.Movement;
import it.polimi.ima.model.TileBasedMap;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import java.awt.*;
import java.util.Random;

/**
 * This class represents a jade agent.  It provides {@link #setup()} and {@link #takeDown()} methods to handle
 * correctly agent lifecycle.
 */
public class MyAgent extends Agent {

    // References to the model and the view
    private TileBasedMap map;

    // Position of the agent
    private Point position;

    // Last movement of the agent
    private Movement lastMovement;

    /**
     * Framework method to set up the agent
     */
    public void setup() {
        map = (TileBasedMap) getArguments()[0];
        Random random = new Random();
        position = new Point();
        do {
            position.x = random.nextInt(map.getWidthInTiles());
            position.y = random.nextInt(map.getWidthInTiles());
        } while (map.blocked(position.x, position.y));
        map.setUnit(position.x, position.y, AgentOrientation.AGENT_NORTH);
        System.out.println("posizione: " + position.x + " " + position.y);
        addBehaviour(new AgentBehavior(this, 1));
    }

    /*
     * This methods updates the model according to the chosen movement and trigger the repaint of the view
     */
    private void move(Point destination) {
        // Update of the model
        if (lastMovement != Movement.STOP) {
            map.setUnit(position.x, position.y, AgentOrientation.NO_AGENT);
            if (lastMovement == Movement.UP) {
                map.setUnit(destination.x, destination.y, AgentOrientation.AGENT_NORTH);
            } else if (lastMovement == Movement.DOWN) {
                map.setUnit(destination.x, destination.y, AgentOrientation.AGENT_SOUTH);
            } else if (lastMovement == Movement.LEFT) {
                map.setUnit(destination.x, destination.y, AgentOrientation.AGENT_WEST);
            } else if (lastMovement == Movement.RIGHT) {
                map.setUnit(destination.x, destination.y, AgentOrientation.AGENT_EAST);
            }
        }
        position = destination;
    }

    /*
     * This methods choose a movement taking care to both avoid collisions with other agent and avoid going out of
     * the map boundaries
     */
    private Point makeOneStep() {
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
        } while (isOutOfBoundary(destination) || computeDistances(destination) < 2);
        return destination;
    }

    /*
     * This method computes the minimum distance between a cell and the surrounding agents
     */
    private int computeDistances(Point reference) {
        int distance = Integer.MAX_VALUE;
        for (int i = 0; i < Constants.HEIGHT; i++) {
            for (int j = 0; j < Constants.WIDTH; j++) {
                if (map.getUnit(j, i) != AgentOrientation.NO_AGENT && (reference.x != j || reference.y != i) &&
                        !(i == position.y && j == position.x)) {
                    if (Math.abs(j - reference.x) + Math.abs(i - reference.y) < distance) {
                        distance = Math.abs(j - reference.x) + Math.abs(i - reference.y);
                    }
                }
            }
        }
        return distance;
    }

    /*
     * This method check if a given point is outside the boundaries of the map
     */
    private Boolean isOutOfBoundary(Point p) {
        return p.x >= map.getWidthInTiles() || p.x < 0 || p.y >= map.getHeightInTiles() || p.y < 0;
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
            move(makeOneStep());
        }
    }
}
