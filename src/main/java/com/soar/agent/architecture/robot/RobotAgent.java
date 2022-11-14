package com.soar.agent.architecture.robot;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.soar.agent.architecture.beans.Move;
import com.soar.agent.architecture.events.MoveListenerEvent;
import com.soar.agent.architecture.events.UtilityResponder;
import org.jsoar.kernel.DebuggerProvider;
import org.jsoar.kernel.RunType;
import org.jsoar.kernel.SoarException;
import org.jsoar.kernel.DebuggerProvider.CloseAction;
import org.jsoar.kernel.io.CycleCountInput;
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
import org.jsoar.runtime.ThreadedAgent;
import org.jsoar.util.commands.SoarCommands;
import org.jsoar.util.events.SoarEventManager;

public class RobotAgent {
    private Robot robot;
    private final ThreadedAgent threadedAgent;
    private final QMemory qMemory = DefaultQMemory.create();
    private File source = null;
    private Set<MoveListenerEvent> moveListeners = new HashSet<MoveListenerEvent>();
    private UtilityResponder utilityResponder;
    public final SoarEventManager events = new SoarEventManager();

    private Move move;

    public RobotAgent() {
        this.threadedAgent = ThreadedAgent.create();

        SoarQMemoryAdapter.attach(threadedAgent.getAgent(), getQMemory());
        new CycleCountInput(threadedAgent.getInputOutput());
    }

    public void addListener(MoveListenerEvent toAdd) {
        moveListeners.add(toAdd);
    }

    public void setRobot(Robot newRobot) {
        try {
            robot = newRobot;

            threadedAgent.setName(robot.getName());

            initMoveCommandListenerObject(); // initialize the output command listener for later use
            initCommandListener("move");

            // areaResponder = new AreaResponder(robot, this);
            utilityResponder = new UtilityResponder(this, robot, events);
            utilityResponder.addAllListeners();

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
            // File(getClass().getResource("/rules/move-to-landmark-3.0-rl.soar").toURI());
            source = new File(getClass().getResource("/rules/main/main-default.soar").toURI());

            // source = new File(getClass().getResource("/rules/move-random.soar").toURI());
            // source = new
            // File(getClass().getResource("/rules/advanced-move.soar").toURI());

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

    private void addEventListeners() {


        // events.addListener(AreaResponder.class, event -> {
        //     if (move != null && move.getDirection() != null && !move.getDirection().equals("")) {
        //         areaResponder.setFormerLocaleInfo(qMemory, CellTypeEnum.NONE.getName());
        //         areaResponder.setLocaleInfo(qMemory, move.getDirection(), CellTypeEnum.NORMAL.getName());
        //     }
        // });
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

                /*
                 * -> Other ways of delaying the agent for UI updates:
                 * 
                 * int delay = 100; // number of milliseconds to sleep
                 * long start = System.currentTimeMillis();
                 * while(start >= System.currentTimeMillis() - delay); // do nothing
                 * do {
                 * } while (System.currentTimeMillis() < timestamp + timeInMilliSeconds);
                 */

                try {
                    // utilise the agent thread to synchronized and make 100 milisecond pause before
                    // every move. much more realistic ui
                    synchronized (threadedAgent.getAgent()) {
                        threadedAgent.getAgent().wait(100);

                        bean.setAttribute(context.getCommand().getAttribute());
                        bean.setTimeTag(context.getCommand().getTimetag());
                        bean.setChildren(context.getCommand().getChildren());
                        bean.setIdentifier(context.getCommand().getIdentifier());
                        bean.setPreference(context.getCommand().getPreferences());
                        move = bean;

                        context.setStatus("complete");

                        // notify the listers that are outside of the agent listening
                        for (MoveListenerEvent listener : moveListeners) {
                            listener.moveCompleted(bean, robot, RobotAgent.this);

                            // Old way of calling area responder
                            // areaResponder.setFormerLocaleInfo(qMemory, CellTypeEnum.NONE.getName());
                            // areaResponder.setLocaleInfo(qMemory, bean.getDirection(),
                            // CellTypeEnum.NORMAL.getName());
                            // areaResponder.updateOppositeCell(qMemory, bean.getDirection());
                            // updateRobotMemory();
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };
        manager.registerHandler("move", handler, Move.class);
    }

    // it will listen to specific commands upon remove/add. for example move command
    private void initCommandListener(String commandNameToListen) {
        OutputCommandManager outputManager = new OutputCommandManager(threadedAgent.getEvents());
        OutputCommandHandler handler = new OutputCommandHandler() {
            @Override
            public void onCommandRemoved(String commandName, Identifier commandId) {

            }

            @Override
            public void onCommandAdded(String commandName, Identifier commandId) {
                removeMemoryPath("area.view");
                // removeMemoryPath("landmarks");

            }
        };
        outputManager.registerHandler(commandNameToListen, handler);
    }

    private void removeMemoryPath(String path) {
        synchronized (qMemory) {
            qMemory.remove(path);
        }
    }

    public Robot getRobot() {
        return robot;
    }

    public ThreadedAgent getThreadedAgent() {
        return threadedAgent;
    }

    public Move getMove() {
        return move;
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

}
