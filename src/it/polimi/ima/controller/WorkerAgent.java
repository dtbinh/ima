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
import jade.core.behaviours.TickerBehaviour;

import java.awt.*;
import java.util.Random;

/**
 * This class represents a jade agent.  It provides {@link #setup()} and {@link #takeDown()} methods to handle
 * correctly agent lifecycle.
 */
public class WorkerAgent extends jade.core.Agent {

    // References to the model
    private TileBasedMap map;
    // Current state of the agent
    private AgentFSMState currentState;
    // Position of the agent
    private Point position;
    // Last movement of the agent
    private AgentAction movementMemory;
    // Number of cycles without moving
    private int stopCounter;
    // Check if the agent has seen a row-start
    private boolean seenRowStart;


    /**
     * Framework method to set up the agent
     */
    public void setup() {
        // Initializations
        map = (TileBasedMap) getArguments()[0];
        currentState = AgentFSMState.WANDERING;
        seenRowStart = false;
        stopCounter = 0;
        position = pickRandomPosition();

        // Add the behaviour to the agent
        addBehaviour(new AgentBehavior(this, 125));
    }

    /**
     * This class defines the behaviour of the agent
     */
    private class AgentBehavior extends TickerBehaviour {
        public AgentBehavior(jade.core.Agent a, long period) {
            super(a, period);
        }
        @Override
        protected void onTick() {
            agentFSM();
        }
    }

    /*
     * Defines the behaviour of the agent according to its current internal state
     */
    private void agentFSM(){
        switch (currentState){
            case WANDERING:
                if(approachingPerimeter()){
                    if(atLandmark()){
                        currentState = AgentFSMState.AT_LANDMARK;
                        constructionAlgorithm();
                    }else{
                        currentState = AgentFSMState.PERIMETER_FOLLOWING;
                        move(getActionAlongPerimeter());
                    }
                }else {
                    if(atLandmark()){
                        currentState = AgentFSMState.PLACING_FIRST_BLOCK;
                        move(goToFirstBlock());
                    }else {
                        move(AgentAction.getRandomAction());
                    }
                }
                break;
            case PLACING_FIRST_BLOCK:
                if(!atLandmark()) {
                    attachBlockHere();
                }
                else {
                    move(goToFirstBlock());
                }
                break;
            case PERIMETER_FOLLOWING:
                if(atLandmark()){
                    currentState = AgentFSMState.AT_LANDMARK;
                    constructionAlgorithm();
                }else{
                    move(getActionAlongPerimeter());
                }
                break;
            case AT_LANDMARK:
                if(!atLandmark()){
                    currentState = AgentFSMState.ALGORITHM1;
                }
                constructionAlgorithm();
                break;
            case ALGORITHM1:
                if(atLandmark()){
                    doDelete();
                }
                else {
                    constructionAlgorithm();
                }
                break;
            default:
                break;
        }
    }

    /*
     * This methods updates the model according to the chosen movement and trigger the repaint of the view
     */
    private void move(AgentAction action) {
        if(action != AgentAction.STOP){
            // Compute destination
            Point destination = (Point) position.clone();
            if(action == AgentAction.UP){
                destination.y--;
            }
            else if (action == AgentAction.DOWN){
                destination.y++;
            }
            else if (action == AgentAction.RIGHT){
                destination.x++;
            }
            else if(action == AgentAction.LEFT){
                destination.x--;
            }
            if(!isOutOfBoundary(destination) && computeDistances(destination) >= 2){
                // Update the model
                map.setUnit(position.x, position.y, AgentOrientation.NO_AGENT);
                if (action == AgentAction.UP) {
                    map.setUnit(destination.x, destination.y, AgentOrientation.AGENT_NORTH);
                } else if (action == AgentAction.DOWN) {
                    map.setUnit(destination.x, destination.y, AgentOrientation.AGENT_SOUTH);
                } else if (action == AgentAction.LEFT) {
                    map.setUnit(destination.x, destination.y, AgentOrientation.AGENT_WEST);
                } else if (action == AgentAction.RIGHT) {
                    map.setUnit(destination.x, destination.y, AgentOrientation.AGENT_EAST);
                }
                // Reset the number of cycles without moving
                stopCounter = 0;
                // Update movement memory
                movementMemory = action;
                // Update the agent position
                position = destination;
            }
            else {
                stopCounter++;
                if(stopCounter > Constants.MAX_ITERATIONS_WITHOUT_MOVING){
                    doDelete();
                }
            }
        }
        else {
            stopCounter++;
            if(stopCounter > Constants.MAX_ITERATIONS_WITHOUT_MOVING){
                doDelete();
            }
        }
    }

    /*
     * This method check if a given point is outside the boundaries of the map
     */
    private Boolean isOutOfBoundary(Point p) {
        return p.x >= map.getWidthInTiles() || p.x < 0 || p.y >= map.getHeightInTiles() || p.y < 0;
    }

    /*
     * This method computes the minimum distance between a cell and the surrounding agents
     */
    private int computeDistances(Point destination) {
        int distance = Integer.MAX_VALUE;
        if(map.getUnit(destination.x, destination.y) != AgentOrientation.NO_AGENT ){
            return 0;
        }
        for (int i = 0; i < Constants.HEIGHT; i++) {
            for (int j = 0; j < Constants.WIDTH; j++) {
                if (map.getUnit(j, i) != AgentOrientation.NO_AGENT && (destination.x != j || destination.y != i) &&
                        !(i == position.y && j == position.x)) {
                    if (Math.abs(j - destination.x) + Math.abs(i - destination.y) < distance) {
                        distance = Math.abs(j - destination.x) + Math.abs(i - destination.y);
                    }
                }
            }
        }
        return distance;
    }

    /*
     * Returns true il the agent is approaching the perimeter of the construction
     * Modifies the orientation of the agent in order to align it in parallel to the perimeter of the construction
     */
    private boolean approachingPerimeter(){
        if(position.x+1 < Constants.WIDTH
                &&    map.getTerrain(position.x+1, position.y) == TerrainType.FILLED) {
            movementMemory = AgentAction.DOWN;
            return true;
        }
        if(position.x-1 >= 0
                &&    map.getTerrain(position.x-1, position.y) == TerrainType.FILLED) {
            movementMemory = AgentAction.UP;
            return true;
        }
        if(position.y+1 < Constants.HEIGHT
                &&    map.getTerrain(position.x, position.y+1) == TerrainType.FILLED) {
            movementMemory = AgentAction.LEFT;
            return true;
        }
        if(position.y-1 >= 0
                &&    map.getTerrain(position.x, position.y-1) == TerrainType.FILLED) {
            movementMemory = AgentAction.RIGHT;
            return true;
        }
        return false;
    }

    /*
     * Returns true if the agent is over the landmark
     */
    private boolean atLandmark(){
        return map.getTerrain(position.x, position.y) == TerrainType.LANDMARK;
    }

    /*
     * Returns true if the agent is over a site that should have a block
     */
    private boolean siteShouldHaveABlock(){
        return map.getTerrain(position.x, position.y) == TerrainType.TO_FILL;
    }

    /*
     * Implementation of the construction algorithm defined in the paper
     */
    private void constructionAlgorithm(){
        // In case of emergency
        if(isOverFilledTerrain()){
            System.out.println("Fuck, I was buried alive! Activate teleportation! =)");
            // Set internal state back to WANDERING
            currentState = AgentFSMState.WANDERING;
            seenRowStart = false;
            // Reset robot position
            map.setUnit(position.x, position.y, AgentOrientation.NO_AGENT);
            position = pickRandomPosition();
        }
        // Algorithm of the paper
        if(siteShouldHaveABlock() && (atInsideCorner() || (seenRowStart && atEndOfRow()))){
            attachBlockHere();
        }
        else{
            if(atEndOfRow()){
                seenRowStart = true;
            }
            move(getActionAlongPerimeter());
        }
    }

    /*
     * Assign a random position on the map checking that the chosen position is not already occupied
     */
    private Point pickRandomPosition(){
        Random random = new Random();
        Point position = new Point();
        do {
            position.x = random.nextInt(map.getWidthInTiles());
            position.y = random.nextInt(map.getWidthInTiles());
        } while (map.blocked(position.x, position.y));
        map.setUnit(position.x, position.y, AgentOrientation.AGENT_NORTH);
        return position;
    }

    /*
     * This method returns the action that should be performed in order to follow the perimeter
     */
    private AgentAction getActionAlongPerimeter(){
        if(movementMemory == AgentAction.UP){
            if(map.getTerrain(position.x-1, position.y) != TerrainType.FILLED) {
                return AgentAction.LEFT;
            }
            if(map.getTerrain(position.x, position.y-1) != TerrainType.FILLED){
                return AgentAction.UP;
            }
            return AgentAction.RIGHT;
        }
        if(movementMemory == AgentAction.DOWN){
            if(map.getTerrain(position.x+1, position.y) != TerrainType.FILLED){
                return AgentAction.RIGHT;
            }
            if(map.getTerrain(position.x, position.y+1) != TerrainType.FILLED){
                return AgentAction.DOWN;
            }
            return AgentAction.LEFT;
        }
        if(movementMemory == AgentAction.RIGHT){
            if(map.getTerrain(position.x, position.y-1) != TerrainType.FILLED){
                return AgentAction.UP;
            }
            if(map.getTerrain(position.x+1, position.y) != TerrainType.FILLED){
                return AgentAction.RIGHT;
            }
            return AgentAction.DOWN;
        }
        else {
            if(map.getTerrain(position.x, position.y+1) != TerrainType.FILLED){
               return AgentAction.DOWN;
            }
            if(map.getTerrain(position.x-1, position.y) != TerrainType.FILLED){
                return AgentAction.LEFT;
            }
            return AgentAction.UP;
        }
    }

    /*
     * This method is called when the agent is over the landmark and there has nothing to side
     * Returns the action that has to be performed by the agent to move over the first cell of the construction
     *
     * Remember that, by design, there should be only one TO_FILL cell next to the landmark
     */
    private AgentAction goToFirstBlock() {
        if(map.getTerrain(position.x+1, position.y) == TerrainType.TO_FILL) {
            return AgentAction.RIGHT;
        }
        if(map.getTerrain(position.x-1, position.y) == TerrainType.TO_FILL) {
            return AgentAction.LEFT;
        }
        if(map.getTerrain(position.x, position.y+1) == TerrainType.TO_FILL) {
            return AgentAction.DOWN;
        }
        return AgentAction.UP;
    }

    /*
     * Updates the model and the agent inserting a block at the current position
     */
    private void attachBlockHere(){
        // Place the block
        map.fillCell(position.x, position.y);

        // Reset the agent
        currentState = AgentFSMState.WANDERING;
        seenRowStart = false;
        map.setUnit(position.x, position.y, AgentOrientation.NO_AGENT);
        position = pickRandomPosition();
    }

    /*
     * Returns true if the agent is in a end-of-row site
     *
     * An end-of-row site is defined as an empty site at which either a robot is about to turn a corner
     * to the left, or the occupancy matrix specifies that the site directly ahead is to be left empty.
     */
    private boolean atEndOfRow() {

        if (movementMemory == AgentAction.UP) {
            return map.getTerrain(position.x - 1, position.y - 1) != TerrainType.FILLED ||
                    map.getTerrain(position.x, position.y) == TerrainType.TO_FILL &&
                            map.getTerrain(position.x, position.y - 1) == TerrainType.EMPTY;
        }

        if (movementMemory == AgentAction.DOWN) {
            return map.getTerrain(position.x + 1, position.y + 1) != TerrainType.FILLED ||
                    map.getTerrain(position.x, position.y) == TerrainType.TO_FILL &&
                            map.getTerrain(position.x, position.y + 1) == TerrainType.EMPTY;
        }

        if (movementMemory == AgentAction.RIGHT) {
            return map.getTerrain(position.x + 1, position.y - 1) != TerrainType.FILLED ||
                    map.getTerrain(position.x, position.y) == TerrainType.TO_FILL &&
                            map.getTerrain(position.x + 1, position.y) == TerrainType.EMPTY;
        }

        return map.getTerrain(position.x - 1, position.y + 1) != TerrainType.FILLED ||
                map.getTerrain(position.x, position.y) == TerrainType.TO_FILL &&
                        map.getTerrain(position.x - 1, position.y) == TerrainType.EMPTY;
    }

    /*
     * Returns true if the agent is in a end-of-row site
     *
     * An inside corner is defined as an empty site with blocks at two adjacent sites
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

    /*
     * Returns true if the agent is over a filled cell
     */
    private boolean isOverFilledTerrain() {
        return map.getTerrain(position.x, position.y) == TerrainType.FILLED;
    }

    /**
     * Override of the parent class deletion method
     */
    @Override
    public void doDelete() {
        map.setUnit(position.x, position.y, AgentOrientation.NO_AGENT);
        System.out.println("Agent " + getAID().getLocalName() + ": Goodbye, cruel world. =(");
        super.doDelete();
    }
}