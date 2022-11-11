package com.soar.agent.architecture.graph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.graphstream.ui.swing_viewer.DefaultView;
import org.jsoar.debugger.util.SwingTools;
import org.jsoar.kernel.events.AfterDecisionCycleEvent;
import org.jsoar.kernel.events.AfterInitSoarEvent;
import org.jsoar.kernel.memory.Wme;
import org.jsoar.runtime.ThreadedAgent;
import org.jsoar.util.events.SoarEvent;
import org.jsoar.util.events.SoarEventListener;

import com.soar.agent.architecture.enums.GraphEnum;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.*;
import java.awt.*;

public class NodeGraphUI extends JPanel {
    private static NodeGraphUI nodeGraphInstance;
    private final JFrame mainFrame;
    private ThreadedAgent agent;
    private NodeGraph nodeGraph;
    private JToolBar nodesToolbar;
    private JPanel zoomControlPanel;
    private Map<String, JCheckBox> checboxMap = new HashMap<String, JCheckBox>();
    private List<Wme> inputList = new ArrayList<Wme>();
    private Set<String> uncheckNodeNames = new HashSet<String>();

    public NodeGraphUI(ThreadedAgent agent) throws IOException {
        super(new BorderLayout());
        this.agent = agent;

        mainFrame = new JFrame();
        nodeGraph = new NodeGraph();

        initialise();
    }

    public static NodeGraphUI getInstance(ThreadedAgent agent) throws IOException {
        if (nodeGraphInstance == null) {
            nodeGraphInstance = new NodeGraphUI(agent);

        } else {
            nodeGraphInstance.agent = agent;
            nodeGraphInstance.nodeGraph = new NodeGraph();
            nodeGraphInstance.initialise();
            
            nodeGraphInstance.mainFrame.setVisible(true);
        }

        return nodeGraphInstance;
    }

    private void initialise() {
        removeAll();
        revalidate();
        repaint();

        initMemoryInputListener();
        initGraphUI();
        initGraphMenu();
        initGraphToolbar();
        initZoomSlider();
    }

    private void initGraphUI() {
        SwingTools.initializeLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            // if(mainFrame == null) mainFrame = new JFrame();
            mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            try {
                // add this class as the content pane instead
                mainFrame.setContentPane(nodeGraphInstance);
                mainFrame.setSize(1100, 900);
                mainFrame.setVisible(true);
                mainFrame.setTitle(GraphEnum.FRAME_TITLE.getName());
                mainFrame.setLocationRelativeTo(null);

                // call(the nodegrapg is final variable now which is initialised in the
                // constrcutre) and add the viewer to UI from this class.

                add((DefaultView) nodeGraph.view, BorderLayout.CENTER);

            } catch (Exception e) {
                System.err.println("****** ERROR in graph init *****");
                e.printStackTrace();
            }
        });
    }

    private void initGraphMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menu = new JMenu(GraphEnum.MAIN_MENUE_TITLE.getName());

        menu.setOpaque(true);
        menu.setFont(new Font(menu.getFont().getName(), menu.getFont().getStyle(), 15));
        menu.setFont(menu.getFont().deriveFont(menu.getFont().getStyle() | Font.BOLD));

        JCheckBoxMenuItem nodeEnableCheckBox = new JCheckBoxMenuItem(GraphEnum.SUB_MENUE_ENABLE_NODE_MENU.getName());

        // Set keystroke for this checbox. this shows the label on the meue item as well
        // as enabling the keyboard action. The action can be trigger at anytime within
        // the window
        // keyboard shortcut is: ALT-T
        nodeEnableCheckBox.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.ALT_MASK));

        nodeEnableCheckBox
                .setFont(new Font(nodeEnableCheckBox.getFont().getName(), nodeEnableCheckBox.getFont().getStyle(), 16));

        nodeEnableCheckBox.setDisplayedMnemonicIndex(2);

        nodeEnableCheckBox.addActionListener((event) -> {
            if (nodeEnableCheckBox.isSelected()) {
                nodesToolbar.setVisible(true);
            } else {
                nodesToolbar.setVisible(false);
            }
        });
        menu.add(nodeEnableCheckBox);
        menu.addSeparator();

        JCheckBoxMenuItem enableZoomCheckBox = new JCheckBoxMenuItem(GraphEnum.SUB_MENUE_ENABLE_ZOOM_CONTROL.getName());
        // Set keystroke for this checbox. this shows the label on the meue item as well
        // as enabling the keyboard action. The action can be trigger at anytime within
        // the window
        // keyboard shortcut is: ALT-Z
        enableZoomCheckBox.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.ALT_MASK));

        enableZoomCheckBox
                .setFont(new Font(enableZoomCheckBox.getFont().getName(), enableZoomCheckBox.getFont().getStyle(), 16));

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
        nodesToolbar = new JToolBar(GraphEnum.TOOLBAR_TITLE.getName());

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
        JLabel toolbarTitle = new JLabel(GraphEnum.NODE_MENU_MAIN_TITLE.getName());
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

        JButton zoomIn = new JButton();
        zoomIn.setIcon(new ImageIcon(NodeGraphUI.class.getResource("/images/graph/zoom-in.png")));

        zoomIn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nodeGraph.zoomIn();
            }

        });

        JButton zoomOut = new JButton();
        zoomOut.setIcon(new ImageIcon(NodeGraphUI.class.getResource("/images/graph/zoom-out.png")));

        zoomOut.addActionListener(new ActionListener() {
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
        agent.getEvents().addListener(AfterDecisionCycleEvent.class, new SoarEventListener() {

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

                } else if (uncheckNodeNames.contains(current.getIdentifier().toString())) {
                    nodeGraph.removeInputParentNode(current);
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

            // if(nodeGraph.graph.getNode("landmarks") != null){
            //     nodeGraph.explore(nodeGraph.graph.getNode("landmarks"));
            // }
            
        }
    }

    private void setNodeMenuItems(List<Wme> inputList) {

        for (Wme currentNode : inputList) {

            String nodeName = GraphEnum.MEMORY_CARET.getName() + currentNode.getAttribute().toString();
            String mainParentNodeI2 = GraphEnum.MEMORY_CARET.getName() + currentNode.getIdentifier().toString();

            if (!checboxMap.containsKey(mainParentNodeI2)) {
                addNewCheckbox(mainParentNodeI2, currentNode, true);
            }

            if (!checboxMap.containsKey(nodeName)) {
                addNewCheckbox(nodeName, currentNode, false);
            }
        }
    }

    // method to add new checkbox items to the node menu items(side bar)
    private void addNewCheckbox(String nodeName, Wme currentNode, boolean isMainNodeI2) {

        JCheckBox nodeCheckBox = new JCheckBox(nodeName);
        nodeCheckBox.setSelected(true);
        nodeCheckBox.setFont(new Font(nodeCheckBox.getFont().getName(), nodeCheckBox.getFont().getStyle(), 16));
        nodeCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        nodeCheckBox.addActionListener((event) -> {
            if (nodeCheckBox.isSelected()) {

                if (!isMainNodeI2 && uncheckNodeNames.contains(currentNode.getAttribute().toString())) {
                    uncheckNodeNames.remove(currentNode.getAttribute().toString());

                    // else if it is the parent node I2
                } else if (isMainNodeI2 && uncheckNodeNames.contains(currentNode.getIdentifier().toString())) {
                    uncheckNodeNames.remove(currentNode.getIdentifier().toString());
                }

            } else {
                // if it is parent node I2
                if (isMainNodeI2) {
                    uncheckNodeNames.add(currentNode.getIdentifier().toString());
                } else {
                    // other nodes
                    uncheckNodeNames.add(currentNode.getAttribute().toString());
                }

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

    public void setFrameVisibility(boolean showFrame) {
        nodeGraphInstance.mainFrame.setVisible(showFrame);
    }
}
