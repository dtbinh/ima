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
 * Enum representing the different internal states an agent can assume
 */
public enum AgentFSMState {
    WANDERING,
    PLACING_FIRST_BLOCK,
    PERIMETER_FOLLOWING,
    AT_LANDMARK,
    ALGORITHM1
}