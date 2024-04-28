package com.soar.agent.architecture.robot;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.soar.agent.architecture.beans.Move;
import com.soar.agent.architecture.beans.Radar;
import com.soar.agent.architecture.enums.MemoryEnum;
import com.soar.agent.architecture.enums.UtilitiesEnum;
import com.soar.agent.architecture.events.MoveListenerEvent;
import com.soar.agent.architecture.events.SemanticMemoryResponder;
import com.soar.agent.architecture.events.UtilityResponder;
import com.soar.agent.architecture.rhs.IdentifierSize;

import org.jsoar.kernel.DebuggerProvider;
import org.jsoar.kernel.RunType;
import org.jsoar.kernel.SoarException;
import org.jsoar.kernel.DebuggerProvider.CloseAction;
import org.jsoar.kernel.io.CycleCountInput;
import org.jsoar.kernel.io.quick.DefaultQMemory;
import org.jsoar.kernel.io.quick.QMemory;
import org.jsoar.kernel.io.quick.SoarQMemoryAdapter;
import org.jsoar.runtime.ThreadedAgent;
import org.jsoar.util.commands.SoarCommands;
import org.jsoar.util.events.SoarEventManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RobotAgent {
    @Autowired
    private Robot robot;

    private final ThreadedAgent threadedAgent;
    private final QMemory qMemory = DefaultQMemory.create();
    private File source = null;
    private Set<MoveListenerEvent> moveListeners = new HashSet<MoveListenerEvent>();

    public final SoarEventManager events = new SoarEventManager();

    public RobotAgent() {
        this.threadedAgent = ThreadedAgent.create();
        // move = new Move(); // this need to be initialised otherwise it will throw an
        // error if we are
        // setting values from other classes
        SoarQMemoryAdapter.attach(threadedAgent.getAgent(), getQMemory());
        new CycleCountInput(threadedAgent.getInputOutput());

        threadedAgent.getRhsFunctions().registerHandler(new IdentifierSize());
    }

    public void addListener(MoveListenerEvent toAdd) {
        moveListeners.add(toAdd);
    }

    public void setRobot(Robot newRobot) {
        try {
            robot = newRobot;

            threadedAgent.setName(robot.getName());

            threadedAgent.initialize(); // Do an init-soar

            // source = new
            // File(getClass().getResource("/rules/move-north-2.soar").toURI());
            // source = new
            // File(getClass().getResource("/rules/move-to-food.soar").toURI());
            // source = new
            // File(getClass().getResource("/rules/move-to-food-prefer-forward.soar").toURI());
            // source = new
            // File(getClass().getResource("/rules/move-forward-prefer-current-direction.soar").toURI());
            // source = new
            // File(getClass().getResource("/rules/move-to-landmark-1.0.soar").toURI());
            // source = new
            // File(getClass().getResource("/rules/move-to-landmark-1.1.soar").toURI());
            // source = new
            // File(getClass().getResource("/rules/move-to-landmark-2.0.soar").toURI());
            // source = new
            // File(getClass().getResource("/rules/move-to-landmark-2.1.soar").toURI());
            // source = new
            // File(getClass().getResource("/rules/move-to-landmark-2.2.soar").toURI());
            // source = new
            // File(getClass().getResource("/rules/move-to-landmark-2.3.soar").toURI());
            // source = new
            // File(getClass().getResource("/rules/move-to-landmark-2.4.soar").toURI());
            // source = new
            // File(getClass().getResource("/rules/move-to-landmark-3.0.soar").toURI());
            // source = new
            // File(getClass().getResource("/rules/move-to-landmark-3.0_ShortPath.soar").toURI());
            // source = new
            // File(getClass().getResource("/rules/move-to-landmark-3.0-rl.soar").toURI());
            // source = new
            // File(getClass().getResource("/rules/move-to-landmark-3.0-epmem.soar").toURI());
            // source = new
            // File(getClass().getResource("/rules/move-to-landmark-3.0-epmem-radar.soar").toURI());
            // source = new
            // File(getClass().getResource("/rules/move-to-landmark-3.0-smem.soar").toURI());

            // source = new
            // File(getClass().getResource("/rules/move-to-landmark-3.0-smem-epmem.soar").toURI());
            // source = new
            // File(getClass().getResource("/rules/main/main-default.soar").toURI());

            // source = new File(getClass().getResource("/rules/move-random.soar").toURI());
            // source = new
            // File(getClass().getResource("/rules/advanced-move.soar").toURI());

            // source = new
            // File(getClass().getResource("/rules/explore-map-radar_1.0.soar").toURI());
            // source = new
            // File(getClass().getResource("/rules/explore-map-radar_2.0.soar").toURI());
            // source = new
            // File(getClass().getResource("/rules/explore-map-radar_3.0.soar").toURI());
            // source = new
            // File(getClass().getResource("/rules/explore-map-radar_4.0_epmem.soar").toURI());
            
            
            // source = new File(getClass().getResource("/rules/move-to-landmark-3.0_ShortPath.soar").toURI()); 
            // source = new File(getClass().getResource("/rules/explore-map-radar_4.0_smem-epmem.soar").toURI());
            source = new File(getClass().getResource("/rules/explore-map-radar_4.0_smem-epmem_v2.soar").toURI());

            if (source != null) {
                final Callable<Void> call = () -> {
                    SoarCommands.source(threadedAgent.getInterpreter(), source);
                    return null;
                };

                threadedAgent.execute(call, null);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void reInitialize() {
        // threadedAgent.getInterpreter().eval("init-soar");
        threadedAgent.initialize();
    }

    public void openDebugger() {
        try {
            Map<String, Object> debuggerProps = threadedAgent.getDebuggerProvider().getProperties();
            debuggerProps.put(DebuggerProvider.CLOSE_ACTION, CloseAction.DETACH);
            threadedAgent.getDebuggerProvider().setProperties(debuggerProps);

            threadedAgent.getAgent().openDebugger();
        } catch (SoarException e) {
            e.printStackTrace();
        }
    }

    public void closeDebugger() {
        try {
            // if there is a instace = there is property then close it otherwise it throws
            // nullpointer exception
            if (threadedAgent.getDebuggerProvider().getProperties().size() > 0) {
                threadedAgent.closeDebugger();

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SoarException e) {
            e.printStackTrace();
        }
    }

    public void dispose() {
        threadedAgent.detach();
    }

    public Robot getRobot() {
        return robot;
    }

    public ThreadedAgent getThreadedAgent() {
        return threadedAgent;
    }

    public SoarEventManager getEvents() {
        return events;
    }

    public QMemory getQMemory() {
        return qMemory;
    }

    public void start() {
        threadedAgent.runForever();
    }

    public void step() {
        threadedAgent.runFor(1, RunType.DECISIONS);
    }

    public void stop() {
        threadedAgent.stop();
    }

    public Set<MoveListenerEvent> getMoveListeners() {
        return moveListeners;
    }

}
