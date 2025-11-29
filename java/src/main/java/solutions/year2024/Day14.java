package solutions.year2024;

import solutions.Solution;
import solutions.Utils;
import solutions.common.Point;
import solutions.common.TerminalScreen;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day14 implements Solution {
    private static final int SIMULATE_SECONDS = 100;
    private static final int SIMULATE_SECONDS_PART_2 = 1000000000;

    @Override
    public void firstPart(String inputFile) {

        List<Robot> robots = readRobots(Utils.readInputAsStream(inputFile));
        System.out.println(Utils.toStringLineByLine(robots));

        int bathroomWidth;
        int bathroomHeight;
        if (inputFile.contains("example")) {
            // Example values
            bathroomWidth = 11;
            bathroomHeight = 7;
        } else {
            // Real values
            bathroomWidth = 101;
            bathroomHeight = 103;
        }

        System.out.println("Starting simulation.");
        for (int i = 0; i < SIMULATE_SECONDS; i++) {
            robots.forEach(r -> r.simulateMovement(bathroomWidth, bathroomHeight));
            List<List<Integer>> bathroomMap = initializeEmptyBathroom(bathroomWidth,
                                                                      bathroomHeight);
            traceRobots(bathroomMap, robots);
            System.out.println(IntStream.range(0, bathroomWidth)
                                       .mapToObj(x -> "=")
                                       .collect(Collectors.joining("")));
            System.out.println(printBathroom(bathroomMap));
        }

        Map<Robot.Quadrant, List<Robot>> robotsClassifiedIntoQuadrants = robots.stream()
                .filter(r -> r.determineQuadrant(bathroomWidth, bathroomHeight)
                             != Robot.Quadrant.MIDDLE)
                .collect(Collectors.groupingBy(r -> r.determineQuadrant(bathroomWidth,
                                                                        bathroomHeight)));


        long safetyFactor = robotsClassifiedIntoQuadrants.values()
                .stream()
                .map(l -> l.size())
                .map(Long::valueOf)
                .reduce((x, y) -> x * y)
                .get();

        System.out.printf("Bathroom safety factor: %d\n", safetyFactor);

    }

    private static final String CHRISTMAS_TREE = """
            1111111111111111111111111111111
            1                             1
            1                             1
            1                             1
            1                             1
            1              1              1
            1             111             1
            1            11111            1
            1           1111111           1
            1          111111111          1
            1            11111            1
            1           1111111           1
            1          111111111          1
            1         11111111111         1
            1        1111111111111        1
            1          111111111          1
            1         11111111111         1
            1        1111111111111        1
            1       111111111111111       1
            1      11111111111111111      1
            1        1111111111111        1
            1       111111111111111       1
            1      11111111111111111      1
            1     1111111111111111111     1
            1    111111111111111111111    1
            1             111             1
            1             111             1
            1             111             1
            1                             1
            1                             1
            1                             1
            1                             1
            1111111111111111111111111111111
            """;

    /**
     * The solution for the second part is a bit messy because
     * we don't know what the Christmas tree looks like in advance.
     * So the steps I took to find it were:
     * - set up Lanterna library to display the bathroom with robots
     * so that it doesn't cause cli scrolling and stays in one place
     * - iterate in chunks of 200 as I noticed that some weird patterns
     * are appearing about every 200 iterations
     * - looked at the robots manually while evolving 200 iterations every
     * 50ms
     * - then I found the tree (see above)
     * - the last step was to create a mask with this tree and then iterate
     * - over all iterations between 1,111400 that contain this picture
     */
    @Override
    public void secondPart(String inputFile) {
        List<Robot> robots = readRobots(Utils.readInputAsStream(inputFile));
        System.out.println(Utils.toStringLineByLine(robots));
        TerminalScreen screen = new TerminalScreen(4);
        int bathroomWidth = 101;
        int bathroomHeight = 103;

        System.out.println("Starting simulation.");
        boolean treeShapeFound = true;

        if (!treeShapeFound) {
            // If we don't know what the tree looks like yet, we need to iterate
            // every N steps and look at the Lanterna rendering of the bathroom
            // floor
            int chunkSize = 200;
            // If we run for this many iterations we are guaranteed not to get a tree
            // (found by manually inspecting the output)
            int noTreeBefore = 111400;
            int screenRefreshMillis = 250;
            int firstOccurrenceFoundAt = 111400;
            for (int i = 0; i < SIMULATE_SECONDS_PART_2; i++) {
                robots.forEach(r -> r.simulateMovement(bathroomWidth, bathroomHeight));
                System.out.println("Iteration number: %s".formatted(i));
                // First we were skipping module and after that the threshold was found
                if (i % chunkSize != 0 || i < noTreeBefore) {
                    continue;
                }
                List<List<Integer>> bathroomMap = initializeEmptyBathroom(bathroomWidth,
                                                                          bathroomHeight);
                traceRobots(bathroomMap, robots);

                printBathroomIntoScreen(screen, bathroomMap, i);
                try {
                    Thread.sleep(screenRefreshMillis);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // Final number found
                if (i == firstOccurrenceFoundAt) {
                    System.out.println(printBathroom(bathroomMap));
                    return;
                }
            }
        } else {
            // Here we already know what the tree looks like and when was the first time it appeared
            // Hence we can create a mask and iterate over all seconds between [1,
            // firstOccurrenceFoundAt]
            // and try to match the mask.
            List<List<Integer>> treeMask = createTreeMask(CHRISTMAS_TREE);

            int firstOccurrenceFoundAt = 111400;
            for (int i = 0; i <= firstOccurrenceFoundAt; i++) {
                robots.forEach(r -> r.simulateMovement(bathroomWidth, bathroomHeight));
                System.out.println("Iteration number: %s".formatted(i+1));
                List<List<Integer>> bathroomMap = initializeEmptyBathroom(bathroomWidth,
                                                                          bathroomHeight);
                traceRobots(bathroomMap, robots);

                for (int y = 0; y < bathroomHeight - treeMask.size(); y++) {
                    for (int x = 0; x < bathroomWidth - treeMask.get(0).size(); x++) {
                        if (maskMatchFound(treeMask, bathroomMap, x, y)) {
                            System.out.println(printBathroom(bathroomMap));
                            return;
                        }
                    }
                }
            }

        }
    }

    private boolean maskMatchFound(
            List<List<Integer>> treeMask,
            List<List<Integer>> bathroomMap,
            int x,
            int y
    ) {
        for (int i = 0; i < treeMask.size(); i++) {
            for (int j = 0; j < treeMask.get(0).size(); j++) {
                if (!Objects.equals(bathroomMap.get(y + i).get(x + j), treeMask.get(i).get(j)))  {
                    return false;
                }
            }
        }
        return true;
    }

    private List<List<Integer>> createTreeMask(String christmasTree) {

        final var mask = christmasTree.lines()
                .map(String::trim)
                .map(String::chars)
                .map(str -> str
                        .mapToObj(c -> Integer.parseInt(String.valueOf((char) c).replace(" ", "0")))
                        .toList())
                .toList();

        // Check if the mask was created successfully
        System.out.println(Utils.toStringLineByLine(mask));
        return mask;
    }

    private String printBathroom(List<List<Integer>> bathroomMap) {
        return bathroomMap.stream()
                .map(row -> row.stream()
                        .map(x -> x == 0 ? " " : String.valueOf(x))
                        .collect(Collectors.joining("")))
                .collect(Collectors.joining("\n"));
    }

    private void printBathroomIntoScreen(
            TerminalScreen screen,
            List<List<Integer>> bathroomMap,
            int iteration
    ) {
        List<String> lines = bathroomMap.stream()
                .map(row -> row.stream()
                        .map(x -> x == 0 ? " " : String.valueOf(x))
                        .collect(Collectors.joining("")))
                .toList();

        try {
            screen.clearScreen();
            screen.resetCursorPosition();
            screen.printLine("Iteration: %d".formatted(iteration));
            screen.printLine("-".repeat(bathroomMap.get(0).size()));
            for (int i = 0; i < lines.size(); i++) {
                screen.printLine("%3d %s".formatted(i, lines.get(i)));
            }
            screen.flushChanges();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void traceRobots(List<List<Integer>> bathroom, List<Robot> robots) {
        for (final var r : robots) {
            bathroom.get(r.position.y()).set(r.position.x(), r.position.indexGrid(bathroom) + 1);
        }
    }

    private List<List<Integer>> initializeEmptyBathroom(int bathroomWidth, int bathroomHeight) {
        List<List<Integer>> bathroom = new ArrayList<>();
        for (int i = 0; i < bathroomHeight; i++) {
            List<Integer> bathroomRow = new ArrayList<>();
            for (int j = 0; j < bathroomWidth; j++) {
                bathroomRow.add(0);
            }
            bathroom.add(bathroomRow);
        }
        return bathroom;
    }


    private List<Robot> readRobots(Stream<String> input) {
        return input.map(Robot::parseFromString).toList();
    }

    static class Robot {
        private Point position;
        private Point velocity;

        Robot(Point position, Point velocity) {
            this.position = position;
            this.velocity = velocity;
        }

        static Robot parseFromString(String spec) {
            String[] positionAndVelocity = spec.split(" ");
            assert (positionAndVelocity.length >= 2) :
                    "Both position and velocity need to be specified.";
            String positionStr = positionAndVelocity[0];
            String velocityStr = positionAndVelocity[1];
            Point position = parsePoint(positionStr.split("=")[1]);
            Point velocity = parsePoint(velocityStr.split("=")[1]);

            return new Robot(position, velocity);
        }

        static Point parsePoint(String input) {
            // pre: input is of the form: x,y
            String[] xAndY = input.split(",");
            int x = Integer.parseInt(xAndY[0]);
            int y = Integer.parseInt(xAndY[1]);
            return new Point(x, y);
        }

        void simulateMovement(int bathroomWidth, int bathroomHeight) {
            // Turns out java modulus is in fact a remainder for division
            // and for negative inputs it stays negative.
            // Turns out one learns every day
            // Hence we need to do this repeated modulus to ensure positive outcomes.
            int newXWrappingAround = (bathroomWidth + (position.x() + velocity.x()) % bathroomWidth)
                                     % bathroomWidth;
            int newYWrappingAround = (bathroomHeight
                                      + (position.y() + velocity.y()) % bathroomHeight)
                                     % bathroomHeight;
            position = new Point(newXWrappingAround, newYWrappingAround);
        }

        Quadrant determineQuadrant(int bathroomWidth, int bathroomHeight) {
            int horizontalMidpoint = bathroomWidth / 2;
            int verticalMidpoint = bathroomHeight / 2;

            int x = position.x();
            int y = position.y();

            if (x < horizontalMidpoint && y < verticalMidpoint) {
                return Quadrant.TOP_LEFT;
            } else if (x > horizontalMidpoint && y < verticalMidpoint) {
                return Quadrant.TOP_RIGHT;
            } else if (x < horizontalMidpoint && y > verticalMidpoint) {
                return Quadrant.BOTTOM_LEFT;
            } else if (x > horizontalMidpoint && y > verticalMidpoint) {
                return Quadrant.BOTTOM_RIGHT;
            } else {
                return Quadrant.MIDDLE;
            }

        }

        @Override
        public String toString() {
            return "Robot[" + "position=" + position + ", " + "velocity=" + velocity + ']';
        }

        private enum Quadrant {
            TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, MIDDLE,
        }
    }
}
