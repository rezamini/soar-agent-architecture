package com.soar.agent.architecture;

import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

import org.jsoar.debugger.util.SwingTools;

import com.soar.world.World;
import com.soar.world.WorldPanel;

/**
 * Hello world!
 *
 */
public class AppMain extends JPanel {

    private WorldPanel worldPanel;
    private World world;


    public AppMain() throws IOException {
        super(new BorderLayout());
        this.worldPanel = new WorldPanel();
    }
    public static void main(String[] args) {
        initSwing();
    }

    private static void initSwing(){
        SwingTools.initializeLookAndFeel();
        SwingUtilities.invokeLater(() ->
        {
            JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            try {
                AppMain content = new AppMain();
                f.setContentPane(content);
                f.setSize(640, 640);
                f.setVisible(true);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
