package com.soar.agent.architecture.loader;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jsoar.util.ByRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.soar.agent.architecture.beans.Landmark;
import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.world.World;

@Component
public class MapLoader {
    @Autowired
    private World world;

    @Autowired
    private Robot robot;

    private final List<Color> LANDMARK_BASE_COLOR_LIST = new ArrayList<Color>(
            Arrays.asList(Color.ORANGE, Color.BLUE, Color.GREEN, Color.CYAN));
    private final List<String> LANDMARK_BASE_COLOR_NAME_LIST = new ArrayList<String>(
            Arrays.asList("orange", "blue", "green", "cyan"));
    private int colorIndex = 0;

    private final double CELL_SIZE = 2.0;

    /*
     * In this map matrix the values are as follows:
     * Empty spaces are represented as 0
     * Obstacles are represented as 1
     * Landmarks are represented as 2
     * Robot/Agent is represented as 3
     */
    private int[][] mapMatrix;
    private int[][] completeMapMatrix;

    public World load(URL url) throws IOException {
        final InputStream in = url.openStream();
        try {
            return load(in);
        } finally {
            in.close();
        }
    }

    public World load(InputStream in) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        final ByRef<Integer> maxLine = ByRef.create(0);
        final String[] lines = readLines(reader, maxLine);

        int maxX = Integer.MIN_VALUE;
        for (int i = 0; i < lines.length; ++i) {
            lines[i] = padLine(lines[i], maxLine.value);

            if (lines[i].length() > maxX) {
                maxX = lines[i].length();
            }
        }
        mapMatrix = new int[lines.length][maxX];

        int completeMatrixY = (int) ((lines.length - 1) * CELL_SIZE + CELL_SIZE / 2.0);
        int completeMatrixX = (int) ((lines[0].length() - 1) * CELL_SIZE + CELL_SIZE / 2.0);

        completeMapMatrix = new int[completeMatrixY][completeMatrixX];

        // final World world = new World();
        world.extents.setFrame(0.0, 0.0, maxLine.value * CELL_SIZE, lines.length * CELL_SIZE);

        readObstacles(world, lines);
        readLandmarks(world, lines);
        readRobots(world, lines);

        world.setMapMatrix(mapMatrix);
        world.setCompleteMapMatrix(completeMapMatrix);

        return world;
    }

    private void readObstacles(World world, String[] lines) {

        for (int y = 0; y < lines.length; ++y) {
            final String line = lines[y];
            int x = line.indexOf('#');
            int startX = x;
            while (x != -1) {

                x = readRectangle(world, line, x, y);

                // add obstacles to map matrix
                Arrays.fill(mapMatrix[y], startX, x, 1);

                x = line.indexOf('#', x);
            }
        }
    }

    private void readLandmarks(World world, String[] lines) {
        for (int y = 0; y < lines.length; ++y) {
            final String line = lines[y];
            for (int x = 0; x < line.length(); ++x) {
                final char c = line.charAt(x);
                double cx = x * CELL_SIZE + CELL_SIZE / 2.0;
                double cy = y * CELL_SIZE + CELL_SIZE / 2.0;

                if (Character.isLetter(c) && Character.isLowerCase(c)) {
                    // for(Field field : obj.getClass().getFields()) {
                    // if(fooList.contains(field.get()) {
                    // System.out.println(field.getName());
                    // }
                    // }

                    if (colorIndex >= LANDMARK_BASE_COLOR_NAME_LIST.size()) {
                        colorIndex = 0;
                    }

                    Landmark landmark = new Landmark(Character.toString(c), new Point2D.Double(cx, cy),
                            LANDMARK_BASE_COLOR_LIST.get(colorIndex), LANDMARK_BASE_COLOR_NAME_LIST.get(colorIndex));

                    colorIndex++;

                    world.addLandmark(landmark);
                    world.addLandmarkMap(landmark, false);

                    mapMatrix[y][x] = 2;

                    // double xMatrix = cx;
                    // double yMatrix = cy;
                    // if (x == 0) {
                    // cx = cx - CELL_SIZE / 2.0;
                    // }

                    // if (y == 0) {
                    // cy = cy - CELL_SIZE / 2.0;
                    // }

                    completeMapMatrix[(int) cy - 1][(int) cx - 1] = 2;
                }
            }
        }
    }

    private void readRobots(World world, String[] lines) {
        for (int y = 0; y < lines.length; ++y) {
            final String line = lines[y];
            for (int x = 0; x < line.length(); ++x) {
                final char c = line.charAt(x);
                double cx = x * CELL_SIZE + CELL_SIZE / 2.0;
                double cy = y * CELL_SIZE + CELL_SIZE / 2.0;
                if (Character.isLetter(c) && Character.isUpperCase(c)) {
                    // final Robot r = new Robot(world, Character.toString(c));
                    // r.setYaw(Math.toRadians(180));

                    // robot.setName(Character.toString(c));

                    // for instances if the agent is in the edge modify so the agent doesnt look its
                    // outside the main frame
                    if (x == line.length() - 1) {
                        // 0.5 similar to speed
                        cx = cx - 0.5;
                    }

                    robot.setSpeed(0.5);
                    robot.setTurnRate(Math.toRadians(25));
                    robot.setYaw(Math.toRadians(0));
                    robot.move(cx, cy);
                    
                    world.addRobot(robot);
                    
                    mapMatrix[y][x] = 3;

                    // complete matrix
                    cy = cy - 1;
                    cx = cx - 1;

                    completeMapMatrix[(int) cy][(int) cx] = 3;
                    completeMapMatrix[(int) cy][(int) ((int) (cx
                            + robot.getSpeed() * 2) >= completeMapMatrix[(int) cy].length
                                    ? (cx - robot.getSpeed() * 2)
                                    : (cx + robot.getSpeed() * 2))] = 3;

                    // double xMatrix = cx - CELL_SIZE / 2.0;
                    // double yMatrix = cy - CELL_SIZE / 2.0;

                    // // // complete matrix
                    // completeMapMatrix[(int) yMatrix][(int) xMatrix] = 3;
                    // completeMapMatrix[(int) ((int) yMatrix)][(int) ((int) xMatrix +
                    // robot.getSpeed() * 2)] = 3;
                }
            }
        }
    }

    private int readRectangle(World world, String line, int start, int y) {
        assert line.charAt(start) == '#';

        int i = start + 1; // line.indexOf('#', start);
        i = i == -1 ? line.length() : i;
        while (i < line.length()) {
            if (line.charAt(i) != '#') {
                break;
            }
            i++;
        }

        // start and end of obstacle rectangle
        double startX = start;
        double endX = i;

        // smooth the start of the rectangle
        if (startX > 0) {
            startX = startX + 0.2;
        }

        // smooth the end of the rectangle
        if (endX != line.length()) {
            endX = endX - 0.2;
        }

        double w = (endX - startX) * CELL_SIZE;
        double obstacleY = y * CELL_SIZE;

        //check to fit the obstacle to top and edge in correspond to height smooth of 0.
        //map matrix is used only to check if y is the max y
        if(y == mapMatrix.length - 1){
            obstacleY = obstacleY + 0.3;
        }
        Rectangle2D obstacle = new Rectangle2D.Double(startX * CELL_SIZE, obstacleY, w, CELL_SIZE - 0.3); // smooth
                                                                                                              // the
                                                                                                              // height
                                                                                                              // of
                                                                                                              // rectangle
                
        world.addObstacle(obstacle);

        // complete matrix
        int tempStart = (int) (start == 0 ? 0 : (start * CELL_SIZE));

        // && tempStart > start
        if (start == 1) {
            tempStart = start;
        }

        int tempEnd = (int) (i * CELL_SIZE + CELL_SIZE / 2.0);
        // if temp end is bigger it means the next center node is not available(is at
        // the end) so deduct the center
        // otherwise it throws error
        if (tempEnd > completeMapMatrix[0].length - 1) {
            tempEnd = (int) (tempEnd - CELL_SIZE / 2.0);
        }

        double topCY = y * CELL_SIZE + CELL_SIZE / 2.0;
        double midlleCY = (y * CELL_SIZE + CELL_SIZE / 2.0) - 1;
        double bottomCY = (y * CELL_SIZE + CELL_SIZE / 2.0) - 2;

        if (topCY >= completeMapMatrix.length) {
            topCY--;
            midlleCY--;
            bottomCY--;
        } else if (bottomCY < 0) {
            topCY++;
            midlleCY++;
            bottomCY++;
        }

        Arrays.fill(completeMapMatrix[(int) topCY], tempStart, (int) tempEnd - 1, 1);
        Arrays.fill(completeMapMatrix[(int) bottomCY], tempStart, (int) tempEnd - 1, 1);
        Arrays.fill(completeMapMatrix[(int) midlleCY], tempStart, (int) tempEnd - 1, 1);

        return i;
    }

    private String[] readLines(BufferedReader reader, ByRef<Integer> maxLine) throws IOException {
        final List<String> lines = new ArrayList<String>();
        int max = 0;
        String line = reader.readLine();
        while (line != null && !"---".equals(line.trim())) {
            max = Math.max(max, line.length());
            lines.add(line);
            line = reader.readLine();

        }

        Collections.reverse(lines);

        maxLine.value = max;
        return lines.toArray(new String[lines.size()]);
    }

    private String padLine(String line, int newLength) {
        if (line.length() >= newLength) {
            return line;
        }
        final char newLine[] = Arrays.copyOf(line.toCharArray(), newLength);
        Arrays.fill(newLine, line.length(), newLine.length, ' ');
        return new String(newLine);
    }
}
