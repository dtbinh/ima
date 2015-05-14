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

import it.polimi.ima.utils.Constants;
import it.polimi.ima.utils.Heading;
import it.polimi.ima.utils.RandomDirection;
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
    private Heading head;

    // Position of the agent
    private Point position;

    /**
     * This methods choose randomly a movement, updates the model and trigger the repaint of the view
     */
    public void move(Point destination) {
        // Update of the model
        map.setUnit(position.x,position.y,0);
        if(head == Heading.NORTH) {
            map.setUnit(destination.x, destination.y, Constants.AGENT_NORTH);
        }else if (head == Heading.EAST) {
            map.setUnit(destination.x, destination.y, Constants.AGENT_EAST);
        }else if (head == Heading.WEST) {
            map.setUnit(destination.x, destination.y, Constants.AGENT_WEST);
        }else {
            map.setUnit(destination.x, destination.y, Constants.AGENT_SOUTH);
        }
        System.out.println("posizione: " + position.x + " " + position.y);
        // Update of the view
        view.update(map.getDTO());
        position = destination;
    }

    private Point makeOneStep(){
        Point destination;
        do {
            destination = (Point) position.clone();
            RandomDirection d = RandomDirection.getRandomDirection();
            switch (d) {
                case UP:
                    destination.y -= 1;
                    head = Heading.NORTH;
                    break;
                case DOWN:
                    destination.y += 1;
                    head = Heading.SOUTH;
                    break;
                case LEFT:
                    destination.x -= 1;
                    head = Heading.WEST;
                    break;
                case RIGHT:
                    destination.x += 1;
                    head = Heading.EAST;
                default:
                    break;
            }
        } while (isOutOfBoundary(destination) || computeDistances(destination)<2);
        return destination;
    }

    private int computeDistances(Point reference){
        int distance = Integer.MAX_VALUE;
        for(int i=0; i<Constants.HEIGHT; i++){
            for (int j=0; j<Constants.WIDTH; j++){
                if(map.getUnit(j,i) != 0 && (reference.x != j || reference.y != i) &&
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
        map.setUnit(position.x, position.y, Constants.AGENT_NORTH);
        head = Heading.NORTH;
        view.update(map.getDTO());
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
