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

import it.polimi.ima.utils.Constants;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

/**
 * The following class is in charge of rendering the state of the model upon request
 * To inform this class about the state of the model, a DTO is passed to both the constructor {@link #View(int[][])}
 * of the class and to the {@link #update(int[][])} method
 */
public class View extends JFrame {
    /* Last update from the model */
    private int[][] mapDTO;
    /* The list of tile images to render the map */
    private Image[] tiles = new Image[7];
    /* The offscreen buffer used for rendering in the wonder world of Java 2D */
    private Image buffer;


    /**
     * Create a new view for visualizing the game
     *
     * @param mapDTO a DTO representing the current state of the model
     */
    public View(int[][] mapDTO) {
        this.mapDTO = mapDTO;
        // Assign to each tile its image
        try {
            tiles[Constants.EMPTY_CELL] = ImageIO.read(getResource("res/empty_cell.png"));
            tiles[Constants.CELL_TO_FILL] = ImageIO.read(getResource("res/cell_to_fill.png"));
            tiles[Constants.FILLED_CELL] = ImageIO.read(getResource("res/filled_cell.png"));
            tiles[Constants.AGENT_NORTH] = ImageIO.read(getResource("res/agent_north.png"));
            tiles[Constants.AGENT_SOUTH] = ImageIO.read(getResource("res/agent_south.png"));
            tiles[Constants.AGENT_WEST] = ImageIO.read(getResource("res/agent_west.png"));
            tiles[Constants.AGENT_EAST] = ImageIO.read(getResource("res/agent_east.png"));
        } catch (IOException e) {
            System.err.println("Failed to load resources: "+e.getMessage());
            System.exit(0);
        }
        // Define view dimension and visibility
        setSize(700,700);
        setResizable(false);
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
        g.translate(50, 50);

        // Cycle through the tiles in the map drawing the appropriate image for the terrain and units where appropriate

        for (int x=0;x<Constants.HEIGHT;x++) {
            for (int y=0;y<Constants.WIDTH;y++) {
                g.drawImage(tiles[mapDTO[x][y]],x*20,y*20,null);
            }
        }

        // Finally draw the buffer to the real graphics context in one atomic action
        graphics.drawImage(buffer, 0, 0, null);
    }

    /**
     * Update the view according to the new state of the model
     *
     * @param mapDTO a DTO representing the current state of the model
     */
    public void update(int[][] mapDTO){
        this.mapDTO = mapDTO;
        repaint();
    }
}