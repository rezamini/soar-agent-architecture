package com.soar.agent.architecture.graph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.graphstream.ui.swing.SwingGraphRenderer;
import org.graphstream.ui.swing_viewer.DefaultView;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.swing_viewer.ViewPanel;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerPipe;
import org.jsoar.debugger.util.SwingTools;
import org.jsoar.kernel.events.InputEvent;
import org.jsoar.kernel.memory.Wme;
import org.jsoar.runtime.ThreadedAgent;
import org.jsoar.util.events.SoarEvent;
import org.jsoar.util.events.SoarEventListener;

import com.google.common.collect.Iterators;

import org.graphstream.algorithm.generator.DorogovtsevMendesGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import java.awt.*;

public class NodeGraphUI extends JPanel {
    private JFrame mainFrame;
    private ThreadedAgent agent;
    private NodeGraph nodeGraph;
    private JToolBar nodesToolbar;
    private JPanel zoomControlPanel;
    private Map<String, JCheckBox> checboxMap = new HashMap<String, JCheckBox>();
    private List<Wme> inputList = new ArrayList<Wme>();
    private Set<String> uncheckNodeNames = new HashSet<String>();

    public NodeGraphUI(ThreadedAgent agent) {
        super(new BorderLayout());

        this.agent = agent;
        initMemoryInputListener();
        initGraphUI();
        initGraphMenu();
        initGraphToolbar();
        initZoomSlider();
    }

    private void initGraphUI() {
        SwingTools.initializeLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            mainFrame = new JFrame();
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            try {
                // add this class as the content pane instead
                mainFrame.setContentPane(this);
                mainFrame.setSize(1100, 900);
                mainFrame.setVisible(true);
                mainFrame.setTitle("Soar Working Memory Visualisation");
                mainFrame.setLocationRelativeTo(null);

                // call and add the viewer to UI from this class.
                nodeGraph = new NodeGraph(agent);
                add((DefaultView) nodeGraph.view, BorderLayout.CENTER);

            } catch (IOException e) {
                System.err.println("****** ERROR in graph init *****");
                e.printStackTrace();
            }
        });
    }

    private void initGraphMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");

        JCheckBox nodeEnableCheckBox = new JCheckBox(" Enable Nodes Menu ");
        nodeEnableCheckBox.addActionListener((event) -> {
            if (nodeEnableCheckBox.isSelected()) {
                nodesToolbar.setVisible(true);
            } else {
                nodesToolbar.setVisible(false);
            }
        });
        menu.add(nodeEnableCheckBox);

        menu.addSeparator();

        JCheckBox enableZoomCheckBox = new JCheckBox(" Enable Zoom Control ");
        enableZoomCheckBox.setSelected(true);
        enableZoomCheckBox.addActionListener((event) -> {
            if (enableZoomCheckBox.isSelected()) {
                zoomControlPanel.setVisible(true);
            } else {
                zoomControlPanel.setVisible(false);
            }
        });

        menu.add(enableZoomCheckBox);
        menuBar.add(menu);

        add(menuBar, BorderLayout.NORTH);
    }

    private void initGraphToolbar() {
        nodesToolbar = new JToolBar("Draggable Toolbar");

        // page_axis is to-to-bottom layouw and will place the elemnts to the left as
        // well
        nodesToolbar.setLayout(new BoxLayout(nodesToolbar, BoxLayout.Y_AXIS));

        // nodesToolbar.setOrientation(SwingConstants.VERTICAL);

        // initially set the visibility to false unless it is enabled from the menu
        nodesToolbar.setVisible(false);
        nodesToolbar.setFloatable(true);
        nodesToolbar.setMargin(new Insets(10, 10, 10, 10));

        add(nodesToolbar, BorderLayout.WEST);

        // set a title for the toolbar nodes
        JLabel toolbarTitle = new JLabel("Working Memory Nodes: ");
        toolbarTitle.setFont(new Font(toolbarTitle.getFont().getName(), toolbarTitle.getFont().getStyle(), 16));
        // make the title bold
        toolbarTitle.setFont(toolbarTitle.getFont().deriveFont(toolbarTitle.getFont().getStyle() | Font.BOLD));
        nodesToolbar.add(toolbarTitle);
        nodesToolbar.addSeparator();

    }

    private void initZoomSlider() {
        JPanel containerPanel = new JPanel();
        containerPanel.setOpaque(true);
        containerPanel.setBackground(Color.WHITE);

        zoomControlPanel = new JPanel(new BorderLayout());
        zoomControlPanel.setOpaque(true);
        zoomControlPanel.setBackground(Color.WHITE);

        JButton zoomIn = new JButton("Zoom In +");
        zoomIn.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                nodeGraph.zoomIn();
            }
            
        });

        JButton zoomOut = new JButton("Zoom Out -");
        zoomOut.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                nodeGraph.zoomOut();
            }
            
        });

        containerPanel.add(zoomIn);
        containerPanel.add(zoomOut);

        zoomControlPanel.add(containerPanel, BorderLayout.LINE_END);

        add(zoomControlPanel, BorderLayout.PAGE_END);
    }

    private void initMemoryInputListener() {
        agent.getEvents().addListener(InputEvent.class, new SoarEventListener() {

            @Override
            public void onEvent(SoarEvent event) {

                // add wmes to a seperate list.
                // cant assign to a iterator and reuse; apparently the size will be 0 after a
                // loop
                inputList.clear();
                agent.getInputOutput().getInputLink().getWmes().forEachRemaining(inputList::add);

                renderGraphElements();
                setNodeMenuItems(inputList);
            }

        });
    }

    private void renderGraphElements() {
        if (inputList != null) {
            for (int i = 0; i < inputList.size(); i++) {
                Wme current = inputList.get(i);

                // removing nodes
                if (uncheckNodeNames.contains(current.getAttribute().toString())) {
                    // we need to remove & skip because this wme is unchecked from the UI list
                    nodeGraph.removeTopNodesAndChildren(current, current.getChildren());

                    continue;
                }

                // add the rest of the nodes, the checked nodes
                if (current.getChildren() != null) {

                    List<Wme> wmeChildrenList = new ArrayList<Wme>();
                    current.getChildren().forEachRemaining(wmeChildrenList::add);

                    nodeGraph.addTopNodesAndChildren(current, wmeChildrenList);
                }
            }

            nodeGraph.setGraphNodeAndEdgeNames();
            // explore(graph.getNode("landmarks"));
        }
    }

    private void setNodeMenuItems(List<Wme> inputList) {

        for (Wme currentNode : inputList) {
            // Wme currentNode = iter.next();
            String nodeName = "^" + currentNode.getAttribute().toString();

            if (!checboxMap.containsKey(nodeName)) {

                JCheckBox nodeCheckBox = new JCheckBox(nodeName);
                nodeCheckBox.setSelected(true);
                nodeCheckBox.setFont(new Font(nodeCheckBox.getFont().getName(), nodeCheckBox.getFont().getStyle(), 16));
                nodeCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);

                nodeCheckBox.addActionListener((event) -> {
                    if (nodeCheckBox.isSelected()) {
                        if (uncheckNodeNames.contains(currentNode.getAttribute().toString())) {
                            uncheckNodeNames.remove(currentNode.getAttribute().toString());
                        }
                    } else {
                        uncheckNodeNames.add(currentNode.getAttribute().toString());
                    }
                    renderGraphElements();
                });

                // nodeCheckBox.setAlignmentX(FlowLayout.LEFT);

                checboxMap.put(nodeName, nodeCheckBox);
                nodesToolbar.add(nodeCheckBox);
                nodesToolbar.addSeparator();

                // refresh the toolbar
                nodesToolbar.repaint();
                nodesToolbar.revalidate();

            }
        }
    }
}
