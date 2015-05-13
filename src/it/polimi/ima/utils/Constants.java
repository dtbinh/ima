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

package it.polimi.ima.utils;

/**
 * Constants useful for the whole application
 */
public interface Constants {
    /** The map width in tiles */
    int WIDTH = 30;
    /** The map height in tiles */
    int HEIGHT = 30;
    /** Indicate terrain to be left empty at a given location */
    int EMPTY_CELL = 0;
    /** Indicate a cell to fill at a given location */
    int CELL_TO_FILL = 1;
    /** Indicate a filled cell at a given location */
    int FILLED_CELL = 2;
    /** Indicate an agent of the game */
    int AGENT = 3;
}
