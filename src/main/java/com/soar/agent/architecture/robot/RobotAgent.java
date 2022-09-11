package com.soar.agent.architecture.robot;

import java.io.File;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.soar.agent.architecture.beans.Move;
import com.soar.agent.architecture.enums.DirectionEnum;
import com.soar.agent.architecture.events.MoveListenerEvent;
import com.soar.agent.architecture.events.AreaResponder;

import org.jsoar.kernel.Agent;
import org.jsoar.kernel.DebuggerProvider;
import org.jsoar.kernel.RunType;
import org.jsoar.kernel.SoarException;
import org.jsoar.kernel.DebuggerProvider.CloseAction;
import org.jsoar.kernel.io.CycleCountInput;
import org.jsoar.kernel.io.InputOutput;
import org.jsoar.kernel.io.InputWme;
import org.jsoar.kernel.io.beans.SoarBeanExceptionHandler;
import org.jsoar.kernel.io.beans.SoarBeanOutputContext;
import org.jsoar.kernel.io.beans.SoarBeanOutputHandler;
import org.jsoar.kernel.io.beans.SoarBeanOutputManager;
import org.jsoar.kernel.io.commands.OutputCommandHandler;
import org.jsoar.kernel.io.commands.OutputCommandManager;
import org.jsoar.kernel.io.quick.DefaultQMemory;
import org.jsoar.kernel.io.quick.QMemory;
import org.jsoar.kernel.io.quick.SoarQMemoryAdapter;
import org.jsoar.kernel.symbols.Identifier;
import org.jsoar.kernel.symbols.SymbolFactory;
import org.jsoar.runtime.LegilimensStarter;
import org.jsoar.runtime.ThreadedAgent;
import org.jsoar.util.commands.SoarCommands;
import org.jsoar.util.events.SoarEventManager;

public class RobotAgent {
    private Robot robot;
    private final ThreadedAgent threadedAgent;
    private final QMemory qMemory = DefaultQMemory.create();
    private File source = null;
    private Set<MoveListenerEvent> moveListeners = new HashSet<MoveListenerEvent>();
    private AreaResponder areaResponder;
    public final SoarEventManager events = new SoarEventManager();
    
    public RobotAgent() {
        this.threadedAgent = ThreadedAgent.create();

        SoarQMemoryAdapter.attach(getThreadedAgent().getAgent(), getQMemory());
        new CycleCountInput(getThreadedAgent().getInputOutput());

        debug();
    }

    public void addListener(MoveListenerEvent toAdd) {
        moveListeners.add(toAdd);
    }

    public void setRobot(Robot robot) {
        try {
            this.robot = robot;
            
            getThreadedAgent().setName(robot.getName());

            initMoveCommandListenerObject(); // initialize the output command listener for later use

            getThreadedAgent().initialize(); // Do an init-soar
            source = new File(getClass().getResource("/rules/move-to-food.soar").toURI());
            if (source != null) {
                final Callable<Void> call = () -> {
                    SoarCommands.source(getThreadedAgent().getInterpreter(), source);
                    return null;
                };
                this.getThreadedAgent().execute(call, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initMoveCommandListenerObject() {
        final SoarBeanOutputManager manager = new SoarBeanOutputManager(threadedAgent.getEvents());
        final SoarBeanOutputHandler<Move> handler = new SoarBeanOutputHandler<Move>() {

            @Override
            public void setExceptionHandler(SoarBeanExceptionHandler handler) {
                super.setExceptionHandler(handler);
            }

            @Override
            public void handleOutputCommand(SoarBeanOutputContext context, Move bean) {
                // we can do something with bean.direction etc ...
                // added other related command data that might be used elsewhere

                /* -> Other ways of delaying the agent for UI updates:

                 * int delay = 100; // number of milliseconds to sleep
                 * long start = System.currentTimeMillis();
                 * while(start >= System.currentTimeMillis() - delay); // do nothing
                 * do {
                 * } while (System.currentTimeMillis() < timestamp + timeInMilliSeconds);
                 */

                try {
                    //utilise the agent thread to synchronized and make 100 milisecond pause before every move. much more realistic ui
                    synchronized (threadedAgent.getAgent()) {
                        threadedAgent.getAgent().wait(100);

                        bean.setAttribute(context.getCommand().getAttribute());
                        bean.setTimeTag(context.getCommand().getTimetag());
                        bean.setChildren(context.getCommand().getChildren());
                        bean.setIdentifier(context.getCommand().getIdentifier());
                        bean.setPreference(context.getCommand().getPreferences());

                        context.setStatus("complete");

                        // notify the listers that are outside of the agent listening
                        for (MoveListenerEvent listener : moveListeners) {
                            listener.moveCompleted(bean, robot, RobotAgent.this);
                            updateRobotMemory();
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };
        manager.registerHandler("move", handler, Move.class);
    }

    public void updateRobotMemory() {
        areaResponder = new AreaResponder(robot, this);
        synchronized (qMemory) {
            qMemory.setString("self.name", robot.getName());
            qMemory.setDouble("self.radius", robot.getRadius());

            final double x = robot.getShape().getCenterX();
            final double y = robot.getShape().getCenterY();
            qMemory.setDouble("self.pose.x", x);
            qMemory.setDouble("self.pose.y", y);
            qMemory.setDouble("self.pose.yaw", Math.toDegrees(robot.getYaw()));
            
            
            events.fireEvent(areaResponder);

            // //add surrounding view memory
            // for(DirectionEnum directionEnum: DirectionEnum.values()){
            //     final QMemory sub = qMemory.subMemory("view." + directionEnum.getName() + "");
            //     sub.setString("type", "none");
            //     sub.setInteger("obstacle", 0); // 0=false 1=true
            // }
        }
    }

    private Robot getRobot() {
        return robot;
    }

    private ThreadedAgent getThreadedAgent() {
        return threadedAgent;
    }

    public QMemory getQMemory() {
        return qMemory;
    }

    public void start() {
        this.getThreadedAgent().runForever();
    }

    public void step() {
        this.getThreadedAgent().runFor(1, RunType.DECISIONS);
    }

    public void stop() {
        this.getThreadedAgent().stop();
    }

    public void debug() {
        try {
            this.getThreadedAgent().openDebugger();
        } catch (SoarException e) {
            e.printStackTrace();
        }
    }

    public void dispose() {
        this.getThreadedAgent().detach();
    }

}
