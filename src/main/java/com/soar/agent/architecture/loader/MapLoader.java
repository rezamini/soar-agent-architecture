package com.soar.agent.architecture.loader;

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

import com.soar.agent.architecture.robot.Robot;
import com.soar.agent.architecture.world.World;

public class MapLoader {
    private final double CELL_SIZE = 2.0;

    public static class Result {
        public final World world;

        public Result(World world) {
            this.world = world;
        }
    }

    public Result load(URL url) throws IOException {
        final InputStream in = url.openStream();
        try {
            return load(in);
        } finally {
            in.close();
        }
    }

    public Result load(InputStream in) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        final ByRef<Integer> maxLine = ByRef.create(0);
        final String[] lines = readLines(reader, maxLine);
        for (int i = 0; i < lines.length; ++i) {
            lines[i] = padLine(lines[i], maxLine.value);
        }

        final World world = new World();
        world.extents.setFrame(0.0, 0.0, maxLine.value * CELL_SIZE, lines.length * CELL_SIZE);

        readObstacles(world, lines);
        readRobots(world, lines);

        return new Result(world);
    }

    private void readObstacles(World world, String[] lines) {
        for (int y = 0; y < lines.length; ++y) {
            final String line = lines[y];
            int x = line.indexOf('#');
            while (x != -1) {
                x = readRectangle(world, line, x, y);
                x = line.indexOf('#', x);
            }
        }
    }

    private void readRobots(World world, String[] lines) {
        for (int y = 0; y < lines.length; ++y) {
            final String line = lines[y];
            for (int x = 0; x < line.length(); ++x) {
                final char c = line.charAt(x);
                final double cx = x * CELL_SIZE + CELL_SIZE / 2.0;
                final double cy = y * CELL_SIZE + CELL_SIZE / 2.0;
                if (Character.isLetter(c) && Character.isUpperCase(c)) {
                    final Robot r = new Robot(world, Character.toString(c));
                    r.move(cx, cy);
                    r.setSpeed(0.5);
                    r.setTurnRate(Math.toRadians(25));
                    world.addRobot(r);
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

        double w = (i - start) * CELL_SIZE;
        world.addObstacle(new Rectangle2D.Double(start * CELL_SIZE, y * CELL_SIZE, w, CELL_SIZE));
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
