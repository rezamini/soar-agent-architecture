package com.soar.agent.architecture.graph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
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
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;

public class NodeGraphUI extends JPanel {

    private ThreadedAgent agent;
    private NodeGraph nodeGraph;
    private JToolBar nodesToolbar;
    private Map<String, JCheckBox> checboxMap = new HashMap<String, JCheckBox>();
    private List<Wme> inputList = new ArrayList<Wme>();

    public NodeGraphUI(ThreadedAgent agent) {
        super(new BorderLayout());

        this.agent = agent;
        initMemoryInputListener();
        initGraphUI();
        initGraphMenu();
        initGraphToolbar();
    }

    private void initGraphUI() {
        SwingTools.initializeLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            try {
                // add this class as the content pane instead
                f.setContentPane(this);
                f.setSize(1000, 800);
                f.setVisible(true);
                f.setTitle("Soar Working Memory Visualisation");
                f.setLocationRelativeTo(null);

                // call and add the viewer to UI from this class.
                nodeGraph = new NodeGraph(agent);
                add((DefaultView) nodeGraph.viewer.addDefaultView(false, new SwingGraphRenderer()),
                        BorderLayout.CENTER);

            } catch (IOException e) {
                System.err.println("****** ERROR in graph init *****");
                e.printStackTrace();
            }
        });
    }

    private void initGraphMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");

        // JMenuItem menuItem = new JMenuItem("Enable Node Menu");
        JCheckBox nodeEnableCheckBox = new JCheckBox(" Enable Nodes Menu ");
        nodeEnableCheckBox.addActionListener((event) -> {
            if (nodeEnableCheckBox.isSelected()) {
                nodesToolbar.setVisible(true);
            } else {
                nodesToolbar.setVisible(false);
            }
        });

        menu.add(nodeEnableCheckBox);
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

        //set a title for the toolbar nodes
        JLabel toolbarTitle = new JLabel("Working Memory Nodes: ");
        toolbarTitle.setFont(new Font(toolbarTitle.getFont().getName(), toolbarTitle.getFont().getStyle(), 16));
        // make the title bold
        toolbarTitle.setFont(toolbarTitle.getFont().deriveFont(toolbarTitle.getFont().getStyle() | Font.BOLD));
        nodesToolbar.add(toolbarTitle);
        nodesToolbar.addSeparator();
        
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

                if (inputList != null) {
                    for (int i = 0; i < inputList.size(); i++) {
                        Wme current = inputList.get(i);

                        if (current.getChildren() != null) {
                            nodeGraph.addTopNodesAndChildren(current, current.getChildren());
                        }
                    }

                    nodeGraph.setGraphNodeAndEdgeNames();
                    setNodeMenuItems(inputList.iterator());
                    // explore(graph.getNode("landmarks"));
                }
            }

        });
    }

    private void setNodeMenuItems(Iterator<Wme> inputs) {

        for (Iterator<Wme> iter = inputs; inputs.hasNext();) {

            Wme currentNode = iter.next();

            JCheckBox nodeCheckBox = new JCheckBox(currentNode.getAttribute().toString());
            nodeCheckBox.setFont(new Font(nodeCheckBox.getFont().getName(), nodeCheckBox.getFont().getStyle(), 16));
            nodeCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);

            // nodeCheckBox.addActionListener((event) -> {
            // // if (nodeCheckBox.isSelected()) {
            // // nodesToolbar.setVisible(true);
            // // } else {
            // // nodesToolbar.setVisible(false);
            // // }
            // });

            // nodeCheckBox.setAlignmentX(FlowLayout.LEFT);

            if (!checboxMap.containsKey(currentNode.getAttribute().toString())) {
                checboxMap.put(currentNode.getAttribute().toString(), nodeCheckBox);

                nodesToolbar.add(nodeCheckBox);
                // nodesToolbar.add(new JSeparator());
                nodesToolbar.addSeparator();

                // refresh the toolbar
                nodesToolbar.repaint();
                nodesToolbar.revalidate();

            }

        }

        // nodesToolbar.add(list);
        // nodesToolbar.add(new AbstractAction("Graph") {

        // @Override
        // public void actionPerformed(ActionEvent e) {
        // // TODO Auto-generated method stub

        // }
        // });
    }
}
