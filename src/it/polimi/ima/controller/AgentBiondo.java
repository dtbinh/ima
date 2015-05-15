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

import it.polimi.ima.utils.*;
import it.polimi.ima.model.TileBasedMap;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import java.awt.*;
import java.util.Random;

/**
 * This class represents a jade agent.  It provides {@link #setup()} and {@link #takeDown()} methods to handle
 * correctly agent lifecycle.
 */
public class AgentBiondo extends Agent{


    // References to the model and the view
    private TileBasedMap map;

    private FSMState currentState;

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

    public void agentFSM(){

        switch (currentState){
            case WANDERING:
                if(approachingPerimeter()){
                    if(atLandmark()){
                        currentState = FSMState.ALGORITHM1;
                        algorithm1();
                    }else{
                        currentState = FSMState.PERIMETER_FOLLOWING;
                        followPerimeterCounterclockwise();
                    }
                }else {
                    move(makeOneStep());
                }
                break;
            case PERIMETER_FOLLOWING:
                if(atLandmark()){
                    currentState = FSMState.ALGORITHM1;
                    algorithm1();
                }else{
                    followPerimeterCounterclockwise();
                }
                break;
            case ALGORITHM1:
                algorithm1();
                break;
            default:
                break;
        }
    }

    /**
     Implementazione algoritmo paper
     */
    private void algorithm1(){
        boolean seenRowStart = false;
        boolean blockPlaced = false;

        while(!blockPlaced){
            if(siteShouldHaveABlock() && (atInsideCorner() || (seenRowStart && atEndOfRow()))){
                attachBlockHere();
                blockPlaced = true;
            }
            else{
                if(atEndOfRow()){
                    seenRowStart = true;
                }
                followPerimeterCounterclockwise();
            }
        }
    }


    private boolean approachingPerimeter(){
        if(map.getTerrain(position.x+1, position.y) == TerrainType.FILLED) {
            lastMovement = Movement.UP;
            return true;
        }
        if(map.getTerrain(position.x-1, position.y) == TerrainType.FILLED) {
            lastMovement = Movement.DOWN;
            return true;
        }
        if(map.getTerrain(position.x, position.y+1) == TerrainType.FILLED) {
            lastMovement = Movement.LEFT;
            return true;
        }
        if(map.getTerrain(position.x, position.y-1) == TerrainType.FILLED) {
            lastMovement = Movement.RIGHT;
            return true;
        }
        return false;
    }

    private boolean atLandmark(){
        if(map.getTerrain(position.x, position.y) == TerrainType.LANDMARK)
            return true;
        return false;
    }

    private boolean siteShouldHaveABlock(){
        if(map.getTerrain(position.x, position.y) == TerrainType.TO_FILL)
            return true;
        return false;
    }

    /**
     * An inside corner is defined as an empty site with
     * blocks at two adjacent sites
     */
    private boolean atInsideCorner() {
        int count = 0;

        if (position.x+1 < map.getWidthInTiles()){
            if (map.getTerrain(position.x + 1, position.y) == TerrainType.FILLED) {
                count++;
            }
        }

        if (position.x-1 >= 0){
            if (map.getTerrain(position.x - 1, position.y) == TerrainType.FILLED) {
                count++;
            }
        }

        if (position.y+1 < map.getHeightInTiles()){
            if (map.getTerrain(position.x, position.y+1) == TerrainType.FILLED) {
                count++;
            }
        }

        if (position.y-1 >= 0){
            if (map.getTerrain(position.x, position.y-1) == TerrainType.FILLED) {
                count++;
            }
        }

        return count >= 2;
    }

    /**
     * An end-of-row site is defined as an empty site
     * at which either a robot is about to turn a corner
     * to the left, or the occupancy matrix specifies
     * that the site directly ahead is to be left empty.
     */
       private boolean atEndOfRow(){

           if(lastMovement == Movement.UP){
               // se il blocco "avanti a sinistra" non è
               // occupato, allora la casella in cui mi trovo
               // è una end-of-row
               if (map.getTerrain(position.x - 1, position.y-1) != TerrainType.FILLED)
                   return true;
               // se mi trovo in un quadrato di terreno da riempire
               // e il quadrato antistante va lasciato vuoto, allora
               // sono ad un' end-of-row
               if (map.getTerrain(position.x, position.y) == TerrainType.TO_FILL
                       &&
                       map.getTerrain(position.x, position.y-1) == TerrainType.EMPTY)
                   return true;
               return false;
           }

           if(lastMovement == Movement.DOWN){
               // se il blocco "avanti a sinistra" non è
               // occupato, allora la casella in cui mi trovo
               // è una end-of-row
               if (map.getTerrain(position.x + 1, position.y+1) != TerrainType.FILLED)
                   return true;
               // se mi trovo in un quadrato di terreno da riempire
               // e il quadrato antistante va lasciato vuoto, allora
               // sono ad un' end-of-row
               if (map.getTerrain(position.x, position.y) == TerrainType.TO_FILL
                       &&
                       map.getTerrain(position.x, position.y+1) == TerrainType.EMPTY)
                   return true;
               return false;
           }

           if(lastMovement == Movement.RIGHT){
               // se il blocco "avanti a sinistra" non è
               // occupato, allora la casella in cui mi trovo
               // è una end-of-row
               if (map.getTerrain(position.x + 1, position.y-1) != TerrainType.FILLED)
                   return true;
               // se mi trovo in un quadrato di terreno da riempire
               // e il quadrato antistante va lasciato vuoto, allora
               // sono ad un' end-of-row
               if (map.getTerrain(position.x, position.y) == TerrainType.TO_FILL
                       &&
                       map.getTerrain(position.x+1, position.y) == TerrainType.EMPTY)
                   return true;
               return false;
           }

           //se arrivo qui, lastMovement==LEFT

           // se il blocco "avanti a sinistra" non è
           // occupato, allora la casella in cui mi trovo
           // è una end-of-row
           if (map.getTerrain(position.x - 1, position.y+1) != TerrainType.FILLED)
               return true;
           // se mi trovo in un quadrato di terreno da riempire
           // e il quadrato antistante va lasciato vuoto, allora
           // sono ad un' end-of-row
           if (map.getTerrain(position.x, position.y) == TerrainType.TO_FILL
                   &&
                   map.getTerrain(position.x-1, position.y) == TerrainType.EMPTY)
               return true;
           return false;

    }

    /** TODO: disegna sulla mappa la presenza del blocco,
     *  imposta lo stato dell'agente a WANDERNING e
     *  sposta l'agente a ridosso di uno dei bordi della mappa,
     *  come se  fosse tornato sulla scena con un nuovo blocco
     */
    private void attachBlockHere(){

    }


    private void followPerimeterCounterclockwise(){

        Point destination = (Point) position.clone();

        if(lastMovement == Movement.UP){
            if(map.getTerrain(position.x-1, position.y) != TerrainType.FILLED){
                destination.x -= 1;
                lastMovement = Movement.LEFT;
            }else{
                if(map.getTerrain(position.x, position.y-1) != TerrainType.FILLED){
                    destination.y -= 1;
                }
                else{
                    destination.x += 1;
                    lastMovement = Movement.RIGHT;
                }
            }
        }

        else if(lastMovement == Movement.DOWN){
            if(map.getTerrain(position.x+1, position.y) != TerrainType.FILLED){
                destination.x += 1;
                lastMovement = Movement.RIGHT;
            }else{
                if(map.getTerrain(position.x, position.y+1) != TerrainType.FILLED){
                    destination.y += 1;
                }
                else{
                    destination.x -= 1;
                    lastMovement = Movement.LEFT;
                }
            }
        }

        else if(lastMovement == Movement.RIGHT){
            if(map.getTerrain(position.x, position.y-1) != TerrainType.FILLED){
                destination.y -= 1;
                lastMovement = Movement.UP;
            }else{
                if(map.getTerrain(position.x, position.y-1) != TerrainType.FILLED){
                    destination.x += 1;
                }
                else{
                    destination.y += 1;
                    lastMovement = Movement.DOWN;
                }
            }
        }

        else if(lastMovement == Movement.LEFT){
            if(map.getTerrain(position.x, position.y+1) != TerrainType.FILLED){
                destination.y += 1;
                lastMovement = Movement.DOWN;
            }else{
                if(map.getTerrain(position.x, position.y-1) != TerrainType.FILLED){
                    destination.x -= 1;
                }
                else{
                    destination.y -= 1;
                    lastMovement = Movement.UP;
                }
            }
        }

        move(destination);
    }


}
