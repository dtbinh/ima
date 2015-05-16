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

package it.polimi.ima.model;

import it.polimi.ima.utils.AgentOrientation;
import it.polimi.ima.utils.BuildingMap;
import it.polimi.ima.utils.Constants;
import it.polimi.ima.utils.TerrainType;

/**
 * The data map from our example game. This holds the state and context of each tile
 * on the map.
 */
public class TileBasedMap {

    /** The terrain settings for each tile in the map */
    private TerrainType[][] terrain = new TerrainType[Constants.WIDTH][Constants.HEIGHT];
    /** The unit in each tile of the map */
    private AgentOrientation[][] agent = new AgentOrientation[Constants.WIDTH][Constants.HEIGHT];

    /**
     * Create a new test map with some default configuration
     */
    public TileBasedMap() {

        int map[][] = BuildingMap.buildingMap;

        for(int i=0; i<Constants.HEIGHT; i++){
            for(int j=0; j<Constants.WIDTH; j++){
                if(map[i][j] == 1){
                    terrain[j][i] = TerrainType.TO_FILL;
                } else if (map[i][j] == 2){
                    terrain[j][i] = TerrainType.LANDMARK;
                } else {
                    terrain[j][i] = TerrainType.EMPTY;
                }

            }

        }

        for(int i=0; i<Constants.HEIGHT; i++){
            for(int j=0; j<Constants.WIDTH; j++){
                agent[i][j] = AgentOrientation.NO_AGENT;
            }
        }
    }

    /**
     * Get the terrain at a given location
     *
     * @param x The x coordinate of the terrain tile to retrieve
     * @param y The y coordinate of the terrain tile to retrieve
     * @return The terrain tile at the given location
     */
    public TerrainType getTerrain(int x, int y) {
        return terrain[x][y];
    }

    /**
     * Get the unit at a given location
     *
     * @param x The x coordinate of the tile to check for a unit
     * @param y The y coordinate of the tile to check for a unit
     * @return The ID of the unit at the given location or 0 if there is no unit
     */
    public AgentOrientation getUnit(int x, int y) {
        return agent[x][y];
    }

    /**
     * Set the unit at the given location
     *
     * @param x The x coordinate of the location where the unit should be set
     * @param y The y coordinate of the location where the unit should be set
     * @param agentOrientation The ID of the unit to be placed on the map, or 0 to clear the unit at the
     * given location
     */
    public void setUnit(int x, int y, AgentOrientation agentOrientation) {
        agent[x][y] = agentOrientation;
    }

    /**
     * Set the unit at the given location
     *
     * @param x The x coordinate of the location to fill
     * @param y The y coordinate of the location to fill
     */
    public void fillCell(int x, int y) {
        terrain[x][y] = TerrainType.FILLED;
    }

    /**
     * @see TileBasedMap#blocked(int, int)
     */
    public boolean blocked(int x, int y) {
        return getUnit(x, y) != AgentOrientation.NO_AGENT || terrain[x][y] == TerrainType.FILLED;
    }

    /**
     * @see TileBasedMap#getHeightInTiles()
     */
    public int getHeightInTiles() {
        return Constants.HEIGHT;
    }

    /**
     * @see TileBasedMap#getWidthInTiles()
     */
    public int getWidthInTiles() {
        return Constants.WIDTH;
    }

    /**
     * Compute and return a DTO representing the state of the model of the terrain
     * @return the DTO representing the state of the model
     */
    public TerrainType[][] getTerrainDTO() {
        return terrain;
    }

    /**
     * Compute and return a DTO representing the positions of the agents
     * @return the DTO representing the positions of the agents
     */
    public AgentOrientation[][] getAgentDTO() {
        return agent;
    }

}
