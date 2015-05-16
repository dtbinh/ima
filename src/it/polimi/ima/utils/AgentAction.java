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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Enum representing the possible directions in which an agent can move
 */
public enum AgentAction {
    UP,DOWN,LEFT,RIGHT,STOP;

    private static final List<AgentAction> VALUES =
            Collections.unmodifiableList(Arrays.asList(values()));
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random();

    /**
     * Returns a random direction chosen among the members of the enum
     * @return a random direction chosen among the members of the enum
     */
    public static AgentAction getRandomAction()  {
        return VALUES.get(RANDOM.nextInt(SIZE));
    }
}
