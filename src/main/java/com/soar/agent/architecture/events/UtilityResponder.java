package com.soar.agent.architecture.events;

import java.util.ArrayList;
import java.util.List;

import org.jsoar.kernel.SoarProperties;
import org.jsoar.kernel.events.AfterDecisionCycleEvent;
import org.jsoar.kernel.events.AfterInitSoarEvent;
import org.jsoar.kernel.events.InputEvent;
import org.jsoar.kernel.io.beans.SoarBeanExceptionHandler;
import org.jsoar.kernel.io.beans.SoarBeanOutputContext;
import org.jsoar.kernel.io.beans.SoarBeanOutputHandler;
import org.jsoar.kernel.io.beans.SoarBeanOutputManager;
import org.jsoar.kernel.io.commands.OutputCommandHandler;
import org.jsoar.kernel.io.commands.OutputCommandManager;
import org.jsoar.kernel.symbols.Identifier;
import org.jsoar.util.events.SoarEvent;
import org.jsoar.util.events.SoarEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.soar.agent.architecture.beans.Move;
import com.soar.agent.architecture.beans.Radar;
import com.soar.agent.architecture.enums.CellTypeEnum;
import com.soar.agent.architecture.enums.DirectionEnum;
import com.soar.agent.architecture.enums.MemoryEnum;
import com.soar.agent.architecture.enums.UtilitiesEnum;
import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.robot.RobotAgent;
import com.soar.agent.architecture.world.WorldPanel;

@Service
public class UtilityResponder extends UtilityListener {

    private List<Double> cpuTimes = new ArrayList<Double>();
    private List<Double> kernelTimes = new ArrayList<Double>();
    private List<Integer> decisionCycles = new ArrayList<Integer>();
    private List<Long> totalMemory = new ArrayList<Long>();

    @Autowired
    private Move move;

    @Autowired
    public UtilityResponder(RobotAgent robotAgent, Robot robot, MemoryResponder memoryResponder,
            AreaResponder areaResponder, WorldPanel worldPanel) {
        super(robotAgent, robot, memoryResponder, areaResponder, worldPanel);

        // memoryResponder = new MemoryResponder(robot, robotAgent);
        // areaResponder = new AreaResponder(robot, robotAgent);
        // move = new Move();
    }

    @Override
    public void addAllListeners() {
        initRadarCommandListenerObject();
        initCommandListener("move");
        initMoveCommandListenerObject();
        startAfterInitSoarEventListener();
        startAfterDecisionCycleEventListener();

        robotAgent.getEvents().addListener(MemoryResponder.class, event -> {
            memoryResponder.updateRobotMemory();
        });

        // robotAgent.getEvents().addListener(AreaResponder.class, event -> {
        //     areaResponder.updateAreaMemory();
        // });

        robotAgent.getEvents().addListener(AreaResponder.class, event -> {
            areaResponder.updateAreaMemory();

            if (move != null && move.getDirection() != null
                    && !move.getDirection().equals("")) {
                areaResponder.setFormerLocaleInfo(robotAgent.getQMemory(),
                        CellTypeEnum.NORMAL.getName());
                areaResponder.setLocaleInfo(robotAgent.getQMemory(), move.getDirection(),
                        CellTypeEnum.NORMAL.getName());
            }
        });
    }

    @Override
    public void startAfterDecisionCycleEventListener() {
        robotAgent.getThreadedAgent().getEvents().addListener(AfterDecisionCycleEvent.class, new SoarEventListener() {
            @Override
            public void onEvent(SoarEvent event) {
                robotAgent.getEvents().fireEvent(memoryResponder);
                robotAgent.getEvents().fireEvent(areaResponder);
                worldPanel.repaint();


                //calculate the required performances after every decision and UI update and them to their respective list
                double cpuTime = robotAgent.getThreadedAgent().getAgent().getTotalCpuTimer().getTotalSeconds();
                double kernelTime = robotAgent.getThreadedAgent().getAgent().getTotalKernelTimer().getTotalSeconds();
                int dc = robotAgent.getThreadedAgent().getAgent().getProperties().get(SoarProperties.D_CYCLE_COUNT).intValue();
                long mem = Runtime.getRuntime().totalMemory();
                
                cpuTimes.add(cpuTime);
                kernelTimes.add(kernelTime);
                decisionCycles.add(dc);
                totalMemory.add(mem);
            }
        });
    }

    @Override
    public void startAfterInitSoarEventListener() {
        robotAgent.getThreadedAgent().getEvents().addListener(AfterInitSoarEvent.class, new SoarEventListener() {

            @Override
            public void onEvent(SoarEvent event) {
                // calculate the shortest landmarks(s) path in the start
                robot.getWorld().updateShortestPath();

                robotAgent.getEvents().fireEvent(memoryResponder);

                // update the starting locale of the agent

                double currentYaw = robot.getYaw(); // get the starting yaw

                // convert back the yaw to degree and get the starting direction by the
                // converted yaw which is the angle degree
                DirectionEnum startingDirection = DirectionEnum.findByAngleDegree((int) Math.toDegrees(currentYaw));

                
                move.setDirection(startingDirection.getName());
                robotAgent.getEvents().fireEvent(areaResponder);
            }
        });
    }

    @Override
    public void startInputEventListener() {
        robotAgent.getThreadedAgent().getEvents().addListener(InputEvent.class, new SoarEventListener() {

            @Override
            public void onEvent(SoarEvent event) {
                // InputEvent ie = (InputEvent) event;
            }
        });
    }

    public void initRadarCommandListenerObject() {
        final SoarBeanOutputManager manager = new SoarBeanOutputManager(robotAgent.getThreadedAgent().getEvents());
        final SoarBeanOutputHandler<Radar> handler = new SoarBeanOutputHandler<Radar>() {

            @Override
            public void setExceptionHandler(SoarBeanExceptionHandler handler) {
                super.setExceptionHandler(handler);
            }

            @Override
            public void handleOutputCommand(SoarBeanOutputContext context, Radar bean) {

                try {
                    synchronized (robotAgent.getThreadedAgent().getAgent()) {
                        // set the radar status only if there is battery otherwise it be set to off
                        if (robot.getRadarBattery() > 0) {
                            robot.setToggleRadar(bean.isToggleRadar());
                        } else if (robot.getRadarBattery() <= 0) {
                            robot.setToggleRadar(false);
                        }

                        context.setStatus("complete");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
        manager.registerHandler("radar", handler, Radar.class);
    }

    private void removeMemoryPath(String path) {
        synchronized (robotAgent.getQMemory()) {
            robotAgent.getQMemory().remove(path);
        }
    }

    private void initCommandListener(String commandNameToListen) {
        OutputCommandManager outputManager = new OutputCommandManager(robotAgent.getThreadedAgent().getEvents());
        OutputCommandHandler handler = new OutputCommandHandler() {
            @Override
            public void onCommandRemoved(String commandName, Identifier commandId) {

            }

            @Override
            public void onCommandAdded(String commandName, Identifier commandId) {
                // area.view
                removeMemoryPath("area.view");

                // radar.live
                removeMemoryPath(
                        MemoryEnum.RADAR_BASE.getName() +
                                UtilitiesEnum.DOTSEPERATOR.getName() +
                                MemoryEnum.RADAR_LIVE.getName());

                // landmarks.landmark-a.path
                robot.getWorld().getLandmarkMap().forEach((landmark, v) -> {
                    removeMemoryPath(MemoryEnum.LANDMARK_MAIN.getName() +
                            UtilitiesEnum.DOTSEPERATOR.getName() +
                            MemoryEnum.LANDMARK_SUB.getName() +
                            UtilitiesEnum.DASHSEPERATOR.getName() +
                            landmark.getName() +
                            UtilitiesEnum.DOTSEPERATOR.getName() +
                            MemoryEnum.LANDMARK_PATH.getName());
                });

            }
        };
        outputManager.registerHandler(commandNameToListen, handler);
    }

    public void initMoveCommandListenerObject() {
        final SoarBeanOutputManager manager = new SoarBeanOutputManager(robotAgent.getThreadedAgent().getEvents());
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
                    synchronized (robotAgent.getThreadedAgent().getAgent()) {
                        robotAgent.getThreadedAgent().getAgent().wait(100);

                        bean.setAttribute(context.getCommand().getAttribute());
                        bean.setTimeTag(context.getCommand().getTimetag());
                        bean.setChildren(context.getCommand().getChildren());
                        bean.setIdentifier(context.getCommand().getIdentifier());
                        bean.setPreference(context.getCommand().getPreferences());
                        bean.setDirection(bean.getDirection());
                        // robotAgent.setMove(bean);
                        // move = bean;
                        move.setDirection(bean.getDirection());

                        context.setStatus("complete");

                        // notify the listers that are outside of the agent listening
                        for (MoveListenerEvent listener : robotAgent.getMoveListeners()) {
                            listener.moveCompleted(bean, robot, robotAgent);

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

    @Override
    public void startOutputEventListener() {

    }

    @Override
    public void startWorkingMemoryChangedEventListener() {

    }

}
