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
import it.polimi.ima.utils.TerrainType;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * The following class is in charge of rendering the view
 */
public class View extends JFrame {

    private MapPanel mapPanel;
    private JSlider controls;

    public View(TerrainType[][] terrainDTO, AgentOrientation[][] agentDTO){
        mapPanel = new MapPanel(terrainDTO,agentDTO);
        controls = new JSlider(JSlider.HORIZONTAL, 0,1000,125);
        controls.setMajorTickSpacing(250);
        controls.setMinorTickSpacing(25);
        controls.setPaintTicks(true);
        controls.setPaintLabels(true);
        setLayout(new BorderLayout(0, 0));
        this.add(mapPanel, BorderLayout.CENTER);
        this.add(controls, BorderLayout.PAGE_END);
        setResizable(false);
        setSize(700, 700);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public MapPanel getMapPanel() {
        return mapPanel;
    }

    public JSlider getControls() {
        return controls;
    }
}
