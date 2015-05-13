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

    // Id of the agent
    private int tankID = -1;

    // Position of the agent
    private Point position;

    /**
     * This methods choose randomly a movement, updates the model and trigger the repaint of the view
     */
    public void move() {

        RandomDirection d = RandomDirection.getRandomDirection();
        Point destination = (Point) position.clone();
        switch (d) {
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

        if (isOutOfBoundary(destination)) {
            destination = position;
            System.out.println("out of boundary!!!!!");
        }

        // Update of the model
        map.setUnit(position.x,position.y,0);
        map.setUnit(destination.x,destination.y, Constants.AGENT);
        System.out.println("posizione: " + position.x + " " + position.y);
        // Update of the view
        view.update(map.getDTO());
        position = destination;
    }

    public Boolean isOutOfBoundary(Point p){
        return p.x >= map.getWidthInTiles() || p.x < 0 || p.y >= map.getWidthInTiles() || p.y < 0;
    }

    public void setup(){
        map = (TileBasedMap)getArguments()[0];
        view = (View)getArguments()[1];
        Random random = new Random();
        position = new Point();
        position.x = random.nextInt(map.getWidthInTiles());
        position.y = random.nextInt(map.getWidthInTiles());
        map.setUnit(position.x,position.y,0);
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
            move();
        }
    }
}
