package com.soar.agent.architecture.robot;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import com.soar.agent.architecture.beans.Landmark;
import com.soar.agent.architecture.beans.Move;
import com.soar.agent.architecture.enums.CellTypeEnum;
import com.soar.agent.architecture.enums.DirectionEnum;
import com.soar.agent.architecture.events.MoveListenerEvent;
import com.soar.agent.architecture.events.AreaResponder;

import org.jsoar.kernel.RunType;
import org.jsoar.kernel.SoarException;
import org.jsoar.kernel.events.InputEvent;
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
import org.jsoar.util.events.SoarEvent;
import org.jsoar.util.events.SoarEventListener;
import org.jsoar.util.events.SoarEventManager;

public class RobotAgent {
    private Robot robot;
    private final ThreadedAgent threadedAgent;
    private final QMemory qMemory = DefaultQMemory.create();
    private File source = null;
    private Set<MoveListenerEvent> moveListeners = new HashSet<MoveListenerEvent>();
    private AreaResponder areaResponder;
    // public final SoarEventManager events = new SoarEventManager();

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
            initInputEventListener();

            threadedAgent.initialize(); // Do an init-soar
            // source = new
            // File(getClass().getResource("/rules/move-north-2.soar").toURI());
            // source = new
            // File(getClass().getResource("/rules/move-to-food.soar").toURI());
            // source = new
            // File(getClass().getResource("/rules/move-to-food-prefer-forward.soar").toURI());
            // source = new
            // File(getClass().getResource("/rules/move-forward-prefer-current-direction.soar").toURI());
            source = new File(getClass().getResource("/rules/move-to-landmark-1.0.soar").toURI());

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

                        context.setStatus("complete");

                        // notify the listers that are outside of the agent listening
                        for (MoveListenerEvent listener : moveListeners) {
                            listener.moveCompleted(bean, robot, RobotAgent.this);

                            areaResponder.setFormerLocaleInfo(qMemory, CellTypeEnum.NONE.getName());
                            areaResponder.setLocaleInfo(qMemory, bean.getDirection(), CellTypeEnum.NORMAL.getName());
                            // areaResponder.updateOppositeCell(qMemory, bean.getDirection());

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

            }
        };
        outputManager.registerHandler(commandNameToListen, handler);
    }

    // a general method for all type of input events listners.
    private void initInputEventListener() {
        threadedAgent.getEvents().addListener(InputEvent.class, new SoarEventListener() {

            @Override
            public void onEvent(SoarEvent event) {
                // update the robot memory for every input event
                updateRobotMemory();
            }
        });
    }

    private void removeMemoryPath(String path) {
        synchronized (qMemory) {
            qMemory.remove(path);
        }
    }

    public void updateRobotMemory() {
        areaResponder = new AreaResponder(robot, this);
        synchronized (qMemory) {
            qMemory.setString("self.name", robot.getName());
            qMemory.setDouble("self.radius", robot.getRadius()); // change to minimum bounding box - mbb

            final double x = robot.getShape().getCenterX();
            final double y = robot.getShape().getCenterY();
            qMemory.setDouble("self.pose.x", x);
            qMemory.setDouble("self.pose.y", y);
            qMemory.setDouble("self.pose.yaw", Math.toDegrees(robot.getYaw()));

            areaResponder.updateAreaMemory();
            // events.fireEvent(areaResponder);

            // //add surrounding view memory
            // for(DirectionEnum directionEnum: DirectionEnum.values()){
            // final QMemory sub = qMemory.subMemory("view." + directionEnum.getName() +
            // "");
            // sub.setString("type", "none");
            // sub.setInteger("obstacle", 0); // 0=false 1=true
            // }

            addMemoryLandmarks(qMemory, robot);
        }
    }

    private void addMemoryLandmarks(QMemory qMemory, Robot robot) {
        synchronized (qMemory) {
            QMemory landmarks = qMemory.subMemory("landmarks");
            for (Landmark landmark : robot.getWorld().getLandmarks()) {

                // create a sub landmark with the landmark name
                
                String subName = "landmark-" + landmark.name + "-" + threadedAgent.getAgent().getRandom().nextInt(99);
                QMemory subLandmark = landmarks.subMemory(subName);

                // get current agent and landmark positions
                double agentXPose = qMemory.getDouble("self.pose.x");
                double agentYPose = qMemory.getDouble("self.pose.y");
                double landmarkX = landmark.getLocation().getX();
                double landmarkY = landmark.getLocation().getY();

                // Calculate where and which direction the landmark is located from agent
                // current position. Dynamic values & movements
                String landmarkDirection = calcLandmarkDirectionSimple(agentXPose, agentYPose, landmarkX, landmarkY);

                // set basic landmark information
                subLandmark.setString("name", landmark.name);
                subLandmark.setDouble("x", landmarkX);
                subLandmark.setDouble("y", landmarkY);
                subLandmark.setDouble("distance", landmark.getLocation().distance(agentXPose, agentYPose)); 
                subLandmark.setString("direction-command", landmarkDirection);  

                /* Note: Might need to use below code in the future */
                // double bearing = Math.toDegrees(Math.atan2(agentYPose - landmarkY, agentXPose - landmarkX) - robot.getYaw());
                // while(bearing <= -180.0) bearing += 180.0;
                // while(bearing >= 180.0) bearing -= 180.0;
                //subLandmark.setDouble("relative-bearing", bearing);
            }
        }
    }

    private String calcLandmarkDirectionSimple(double agentX, double agentY, double landmarkX, double landmarkY) {
        String direction = "";
        direction += agentY < landmarkY ? DirectionEnum.NORTH.getName() : agentY > landmarkY ? DirectionEnum.SOUTH.getName() : "";

        if (direction.equals("")) {
            direction += agentX < landmarkX ? DirectionEnum.EAST.getName() : agentX > landmarkX ? DirectionEnum.WEST.getName() : "";
        }

        return direction.equals("") ? "here" : direction;
    }

    public Robot getRobot() {
        return robot;
    }

    public ThreadedAgent getThreadedAgent() {
        return threadedAgent;
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

    public void openDebugger() {
        try {
            threadedAgent.openDebugger();
        } catch (SoarException e) {
            e.printStackTrace();
        }
    }

    public void closeDebugger() {
        try {
            threadedAgent.closeDebugger();
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
