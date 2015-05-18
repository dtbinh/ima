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

package it.polimi.ima.view;

import it.polimi.ima.utils.AgentOrientation;
import it.polimi.ima.utils.Constants;
import it.polimi.ima.utils.TerrainType;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * The following class is in charge of rendering the state of the model upon request
 * To inform this class about the state of the model, a DTO is passed to both the constructor {@link #MapPanel(TerrainType[][], AgentOrientation[][])}
 * of the class and to the {@link #update(TerrainType[][], AgentOrientation[][])} method
 */
public class MapPanel extends JPanel {
    /* Last update from the model */
    private TerrainType[][] terrainDTO;
    private AgentOrientation[][] agentDTO;
    /* The list of tile images to render the map */
    private Map<TerrainType,Image> terrainTiles = new EnumMap<>(TerrainType.class);
    private Map<AgentOrientation,Image> agentTiles = new EnumMap<>(AgentOrientation.class);
    /* The offscreen buffer used for rendering in the wonder world of Java 2D */
    private Image buffer;


    /**
     * Create a new view for visualizing the game
     *
     * @param terrainDTO a DTO representing the current state of the model
     */
    public MapPanel(TerrainType[][] terrainDTO, AgentOrientation[][] agentDTO) {
        this.terrainDTO = terrainDTO;
        this.agentDTO = agentDTO;
        // Assign to each tile its image
        try {
            terrainTiles.put(TerrainType.EMPTY, ImageIO.read(getResource("res/empty_cell.png")));
            terrainTiles.put(TerrainType.TO_FILL, ImageIO.read(getResource("res/cell_to_fill.png")));
            terrainTiles.put(TerrainType.FILLED, ImageIO.read(getResource("res/filled_cell.png")));
            terrainTiles.put(TerrainType.LANDMARK, ImageIO.read(getResource("res/landmark_cell.png")));
            agentTiles.put(AgentOrientation.AGENT_NORTH, ImageIO.read(getResource("res/agent_north.png")));
            agentTiles.put(AgentOrientation.AGENT_SOUTH, ImageIO.read(getResource("res/agent_south.png")));
            agentTiles.put(AgentOrientation.AGENT_WEST, ImageIO.read(getResource("res/agent_west.png")));
            agentTiles.put(AgentOrientation.AGENT_EAST, ImageIO.read(getResource("res/agent_east.png")));
        } catch (IOException e) {
            System.err.println("Failed to load resources: "+e.getMessage());
            System.exit(0);
        }
        // Define view dimension and visibility
        setSize(700, 700);
        setVisible(true);
    }

    /**
     * Load a resource based on a file reference
     *
     * @param ref The reference to the file to load
     * @return The stream loaded from either the classpath or file system
     * @throws IOException Indicates a failure to read the resource
     */
    private InputStream getResource(String ref) throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(ref);
        if (in != null) {
            return in;
        }

        return new FileInputStream(ref);
    }

    /**
     * @see java.awt.Container#paint(java.awt.Graphics)
     */
    public void paint(Graphics graphics) {
        // create an offscreen buffer to render the map
        if (buffer == null) {
            buffer = new BufferedImage(700, 700, BufferedImage.TYPE_INT_ARGB);
        }
        Graphics g = buffer.getGraphics();

        g.clearRect(0,0,700,700);
        g.translate(50, 15);

        // Cycle through the tiles in the map drawing the appropriate image for the terrain and units where appropriate

        for (int x=0;x<Constants.HEIGHT;x++) {
            for (int y=0;y<Constants.WIDTH;y++) {
                if(agentDTO[x][y] != AgentOrientation.NO_AGENT){
                    g.drawImage(agentTiles.get(agentDTO[x][y]),x*20,y*20,null);
                }
                else {
                    g.drawImage(terrainTiles.get(terrainDTO[x][y]), x * 20, y * 20, null);
                }
            }
        }

        // Finally draw the buffer to the real graphics context in one atomic action
        graphics.drawImage(buffer, 0, 0, null);
    }

    /**
     * Update the view according to the new state of the model
     *
     * @param terrainDTO a DTO representing the current state of the model
     */
    public void update(TerrainType[][] terrainDTO, AgentOrientation[][] agentDTO){
        this.terrainDTO = terrainDTO;
        this.agentDTO = agentDTO;
        repaint();
    }
}