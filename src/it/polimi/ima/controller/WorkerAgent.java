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

    /**
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

    private boolean atLandmark(){
        return map.getTerrain(position.x, position.y) == TerrainType.LANDMARK;
    }

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
     * Metodo chiamato quando troov il landmark senza alcun blocco a fianco.
     * Mi sposto verso il buco da riempire. (Per regola, a fianco di un
     * landmark vi può essere una sola cella di tipo TO_FILL)
     */
    private AgentAction goToFirstBlock() {
        Point destination = (Point)position.clone();

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

    /* Segnala sulla mappa la presenza del blocco,
     *  imposta lo stato dell'agente a WANDERNING e
     *  sposta l'agente a ridosso di uno dei bordi della mappa,
     *  come se  fosse tornato sulla scena con un nuovo blocco
     */
    private void attachBlockHere(){
        // riempie la cella con un blocco
        map.fillCell(position.x, position.y);

        //reimposta lo stato del robot a WANDERING
        currentState = AgentFSMState.WANDERING;
        seenRowStart = false;

        //resetta la posizione del robot
        map.setUnit(position.x, position.y, AgentOrientation.NO_AGENT);
        position = pickRandomPosition();
    }

    /*
     * An end-of-row site is defined as an empty site
     * at which either a robot is about to turn a corner
     * to the left, or the occupancy matrix specifies
     * that the site directly ahead is to be left empty.
     */
    private boolean atEndOfRow(){

        if(movementMemory == AgentAction.UP){
            // se il blocco "avanti a sinistra" non è
            // occupato, allora la casella in cui mi trovo
            // è una end-of-row
            if (map.getTerrain(position.x - 1, position.y-1) != TerrainType.FILLED)
                return true;
            // se mi trovo in un quadrato di terreno da riempire
            // e il quadrato antistante va lasciato vuoto, allora
            // sono ad un' end-of-row
            return map.getTerrain(position.x, position.y) == TerrainType.TO_FILL
                    &&
                    map.getTerrain(position.x, position.y - 1) == TerrainType.EMPTY;
        }

        if(movementMemory == AgentAction.DOWN){
            // se il blocco "avanti a sinistra" non è
            // occupato, allora la casella in cui mi trovo
            // è una end-of-row
            if (map.getTerrain(position.x + 1, position.y+1) != TerrainType.FILLED)
                return true;
            // se mi trovo in un quadrato di terreno da riempire
            // e il quadrato antistante va lasciato vuoto, allora
            // sono ad un' end-of-row
            return map.getTerrain(position.x, position.y) == TerrainType.TO_FILL
                    &&
                    map.getTerrain(position.x, position.y + 1) == TerrainType.EMPTY;
        }

        if(movementMemory == AgentAction.RIGHT){
            // se il blocco "avanti a sinistra" non è
            // occupato, allora la casella in cui mi trovo
            // è una end-of-row
            if (map.getTerrain(position.x + 1, position.y-1) != TerrainType.FILLED)
                return true;
            // se mi trovo in un quadrato di terreno da riempire
            // e il quadrato antistante va lasciato vuoto, allora
            // sono ad un' end-of-row
            return map.getTerrain(position.x, position.y) == TerrainType.TO_FILL
                    &&
                    map.getTerrain(position.x + 1, position.y) == TerrainType.EMPTY;
        }

        //se arrivo qui, lastAction==LEFT

        // se il blocco "avanti a sinistra" non è
        // occupato, allora la casella in cui mi trovo
        // è una end-of-row
        if (map.getTerrain(position.x - 1, position.y+1) != TerrainType.FILLED)
            return true;
        // se mi trovo in un quadrato di terreno da riempire
        // e il quadrato antistante va lasciato vuoto, allora
        // sono ad un' end-of-row
        return map.getTerrain(position.x, position.y) == TerrainType.TO_FILL
                &&
                map.getTerrain(position.x - 1, position.y) == TerrainType.EMPTY;

    }

    /*
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

    private boolean isOverFilledTerrain() {
        return map.getTerrain(position.x, position.y) == TerrainType.FILLED;
    }

    @Override
    public void doDelete() {
        map.setUnit(position.x, position.y, AgentOrientation.NO_AGENT);
        System.out.println("Agent " + getAID().getLocalName() + ": Goodbye, cruel world. =(");
        super.doDelete();
    }
}

/*

    /*
     * Implementation of the construction algorithm defined in the paper
     *
    private void constructionAlgorithm(){

        // In case of emergency
        if(isOverFilledTerrain()){
            teleport();
        }
        // Algorithm of the paper
        if(siteShouldHaveABlock() && (atInsideCorner() || (seenRowStart && atEndOfRow()))){
            attachBlockHere();
        }
        else{
            if(atEndOfRow()){
                seenRowStart = true;
            }
            followPerimeterCounterclockwise();
        }
    }

    /*
     * This method teleport a robot to a random free position in the map
     *
    private void teleport(){
        System.out.println("Accessing the stargate! =)");

        // Set internal state back to WANDERING
        currentState = AgentFSMState.WANDERING;
        seenRowStart = false;

        // Reset robot position
        map.setUnit(position.x, position.y, AgentOrientation.NO_AGENT);
        position = pickRandomPosition();
    }

    @Override
    public void doDelete() {
        map.setUnit(position.x, position.y, AgentOrientation.NO_AGENT);
        System.out.println("Goodbye, cruel world. =(");
        super.doDelete();
    }

    /*
     * Metodo chiamato quando troov il landmark senza alcun blocco a fianco.
     * Mi sposto verso il buco da riempire. (Per regola, a fianco di un
     * landmark vi può essere una sola cella di tipo TO_FILL)
     *
    private Point goToFirstBlock() {
        Point destination = (Point)position.clone();

        if(map.getTerrain(position.x+1, position.y) == TerrainType.TO_FILL) {
            destination.x += 1;
            lastAction = AgentAction.RIGHT;
            lastMovement = lastAction;
            return destination;
        }
        if(map.getTerrain(position.x-1, position.y) == TerrainType.TO_FILL) {
            destination.x -= 1;
            lastAction = AgentAction.LEFT;
            lastMovement = lastAction;
            return destination;
        }
        if(map.getTerrain(position.x, position.y+1) == TerrainType.TO_FILL) {
            destination.y += 1;
            lastAction = AgentAction.DOWN;
            lastMovement = lastAction;
            return destination;
        }

        destination.y -= 1;
        lastAction = AgentAction.UP;
        lastMovement = lastAction;
        return destination;
    }

    private boolean approachingPerimeter(){
        if(position.x+1 < Constants.WIDTH
                &&    map.getTerrain(position.x+1, position.y) == TerrainType.FILLED) {
            lastAction = AgentAction.DOWN;
            lastMovement = lastAction;
            return true;
        }
        if(position.x-1 >= 0
                &&    map.getTerrain(position.x-1, position.y) == TerrainType.FILLED) {
            lastAction = AgentAction.UP;
            lastMovement = lastAction;
            return true;
        }
        if(position.y+1 < Constants.HEIGHT
                &&    map.getTerrain(position.x, position.y+1) == TerrainType.FILLED) {
            lastAction = AgentAction.LEFT;
            lastMovement = lastAction;
            return true;
        }
        if(position.y-1 >= 0
                &&    map.getTerrain(position.x, position.y-1) == TerrainType.FILLED) {
            lastAction = AgentAction.RIGHT;
            lastMovement = lastAction;
            return true;
        }
        return false;
    }

    /*
     * This method computes the minimum distance between a cell and the surrounding agents
     *
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
     * This method check if a given point is outside the boundaries of the map
     *
    private Boolean isOutOfBoundary(Point p) {
        return p.x >= map.getWidthInTiles() || p.x < 0 || p.y >= map.getHeightInTiles() || p.y < 0;
    }

    private boolean isOverFilledTerrain() {
        return map.getTerrain(position.x, position.y) == TerrainType.FILLED;
    }


    private boolean atLandmark(){
        return map.getTerrain(position.x, position.y) == TerrainType.LANDMARK;
    }

    private boolean siteShouldHaveABlock(){
        return map.getTerrain(position.x, position.y) == TerrainType.TO_FILL;
    }

    /*
     * An inside corner is defined as an empty site with
     * blocks at two adjacent sites
     *
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
     * An end-of-row site is defined as an empty site
     * at which either a robot is about to turn a corner
     * to the left, or the occupancy matrix specifies
     * that the site directly ahead is to be left empty.
     *
    private boolean atEndOfRow(){

        if(lastAction == AgentAction.UP){
            // se il blocco "avanti a sinistra" non è
            // occupato, allora la casella in cui mi trovo
            // è una end-of-row
            if (map.getTerrain(position.x - 1, position.y-1) != TerrainType.FILLED)
                return true;
            // se mi trovo in un quadrato di terreno da riempire
            // e il quadrato antistante va lasciato vuoto, allora
            // sono ad un' end-of-row
            return map.getTerrain(position.x, position.y) == TerrainType.TO_FILL
                    &&
                    map.getTerrain(position.x, position.y - 1) == TerrainType.EMPTY;
        }

        if(lastAction == AgentAction.DOWN){
            // se il blocco "avanti a sinistra" non è
            // occupato, allora la casella in cui mi trovo
            // è una end-of-row
            if (map.getTerrain(position.x + 1, position.y+1) != TerrainType.FILLED)
                return true;
            // se mi trovo in un quadrato di terreno da riempire
            // e il quadrato antistante va lasciato vuoto, allora
            // sono ad un' end-of-row
            return map.getTerrain(position.x, position.y) == TerrainType.TO_FILL
                    &&
                    map.getTerrain(position.x, position.y + 1) == TerrainType.EMPTY;
        }

        if(lastAction == AgentAction.RIGHT){
            // se il blocco "avanti a sinistra" non è
            // occupato, allora la casella in cui mi trovo
            // è una end-of-row
            if (map.getTerrain(position.x + 1, position.y-1) != TerrainType.FILLED)
                return true;
            // se mi trovo in un quadrato di terreno da riempire
            // e il quadrato antistante va lasciato vuoto, allora
            // sono ad un' end-of-row
            return map.getTerrain(position.x, position.y) == TerrainType.TO_FILL
                    &&
                    map.getTerrain(position.x + 1, position.y) == TerrainType.EMPTY;
        }

        //se arrivo qui, lastAction==LEFT

        // se il blocco "avanti a sinistra" non è
        // occupato, allora la casella in cui mi trovo
        // è una end-of-row
        if (map.getTerrain(position.x - 1, position.y+1) != TerrainType.FILLED)
            return true;
        // se mi trovo in un quadrato di terreno da riempire
        // e il quadrato antistante va lasciato vuoto, allora
        // sono ad un' end-of-row
        return map.getTerrain(position.x, position.y) == TerrainType.TO_FILL
                &&
                map.getTerrain(position.x - 1, position.y) == TerrainType.EMPTY;

    }

    /* Segnala sulla mappa la presenza del blocco,
     *  imposta lo stato dell'agente a WANDERNING e
     *  sposta l'agente a ridosso di uno dei bordi della mappa,
     *  come se  fosse tornato sulla scena con un nuovo blocco
     *
    private void attachBlockHere(){
        // riempie la cella con un blocco
        map.fillCell(position.x, position.y);

        //reimposta lo stato del robot a WANDERING
        currentState = AgentFSMState.WANDERING;
        seenRowStart = false;

        //resetta la posizione del robot
        map.setUnit(position.x, position.y, AgentOrientation.NO_AGENT);
        Random random = new Random();
        do {
            position.x = random.nextInt(map.getWidthInTiles());
            position.y = random.nextInt(map.getWidthInTiles());
        } while (map.blocked(position.x, position.y));
        map.setUnit(position.x, position.y, AgentOrientation.AGENT_NORTH);
    }


    private void followPerimeterCounterclockwise(){

        Point destination = (Point) position.clone();

        if(lastMovement == AgentAction.UP){
            if(map.getTerrain(position.x-1, position.y) != TerrainType.FILLED){
                destination.x -= 1;
                lastAction = AgentAction.LEFT;
            }else{
                if(map.getTerrain(position.x, position.y-1) != TerrainType.FILLED){
                    destination.y -= 1;
                    lastAction = AgentAction.UP;
                }
                else{
                    destination.x += 1;
                    lastAction = AgentAction.RIGHT;
                }
            }
        }

        else if(lastMovement == AgentAction.DOWN){
            if(map.getTerrain(position.x+1, position.y) != TerrainType.FILLED){
                destination.x += 1;
                lastAction = AgentAction.RIGHT;
            }else{
                if(map.getTerrain(position.x, position.y+1) != TerrainType.FILLED){
                    destination.y += 1;
                    lastAction = AgentAction.DOWN;
                }
                else{
                    destination.x -= 1;
                    lastAction = AgentAction.LEFT;
                }
            }
        }

        else if(lastMovement == AgentAction.RIGHT){
            if(map.getTerrain(position.x, position.y-1) != TerrainType.FILLED){
                destination.y -= 1;
                lastAction = AgentAction.UP;
            }else{
                if(map.getTerrain(position.x+1, position.y) != TerrainType.FILLED){
                    destination.x += 1;
                    lastAction = AgentAction.RIGHT;
                }
                else{
                    destination.y += 1;
                    lastAction = AgentAction.DOWN;
                }
            }
        }

        else if(lastMovement == AgentAction.LEFT){
            if(map.getTerrain(position.x, position.y+1) != TerrainType.FILLED){
                destination.y += 1;
                lastAction = AgentAction.DOWN;
            }else{
                if(map.getTerrain(position.x-1, position.y) != TerrainType.FILLED){
                    destination.x -= 1;
                    lastAction = AgentAction.LEFT;
                }
                else{
                    destination.y -= 1;
                    lastAction = AgentAction.UP;
                }
            }
        }

        if(computeDistances(destination) >= 2) {
            updateModel(destination);
        }
        else {
            lastAction = AgentAction.STOP;
        }
    }

    /*
     * Assign a random position on the map checking that the chosen position is not already occupied
     *
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

 */
