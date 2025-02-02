package solutions.day14;

import solutions.Solution;
import solutions.Utils;
import solutions.common.Point;

import javax.sql.PooledConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
            // This below was an attempt to print it and make sort of real-time
            // didn't look as cool as I thought it would.
//            try {
//                Thread.sleep(250);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
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

    @Override
    public void secondPart(String inputFile) {
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
        for (int i = 0; i < SIMULATE_SECONDS_PART_2; i++) {
            robots.forEach(r -> r.simulateMovement(bathroomWidth, bathroomHeight));
            List<List<Integer>> bathroomMap = initializeEmptyBathroom(bathroomWidth,
                                                                      bathroomHeight);
            traceRobots(bathroomMap, robots);
            // I would hope for the Christmas tree to have its trunk in the middle
            // ok this seems to be too rare
            //boolean trunkFilled = bathroomMap.stream().map(row -> row.get(row.size() / 2)).allMatch(x -> x> 0);

            Map<Robot.Quadrant, List<Robot>> robotsClassifiedIntoQuadrants = robots.stream()
                    .filter(r -> r.determineQuadrant(bathroomWidth, bathroomHeight)
                                 != Robot.Quadrant.MIDDLE)
                    .collect(Collectors.groupingBy(r -> r.determineQuadrant(bathroomWidth,
                                                                            bathroomHeight)));

            boolean isSymmetricallyDistributed = robotsClassifiedIntoQuadrants.get(Robot.Quadrant.TOP_LEFT).size()
                                                == robotsClassifiedIntoQuadrants.get(Robot.Quadrant.TOP_RIGHT).size()
                                                && robotsClassifiedIntoQuadrants.get(Robot.Quadrant.BOTTOM_LEFT).size()
                                                   == robotsClassifiedIntoQuadrants.get(Robot.Quadrant.BOTTOM_RIGHT).size();

            if (isSymmetricallyDistributed) {
                System.out.printf("State after %d seconds elapsed:%n", i + 1);
                System.out.println(IntStream.range(0, bathroomWidth)
                                           .mapToObj(x -> "=")
                                           .collect(Collectors.joining("")));
                System.out.println(printBathroom(bathroomMap));
            }
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

    private boolean isSymmetric(List<List<Integer>> bathroomMap) {
        return bathroomMap.stream().allMatch(Day14::isSymmetricRow);
    }

    private static boolean isSymmetricRow(List<Integer> bathroomRow) {
        List<Integer> reversed = bathroomRow.reversed().stream().toList();
        for (int i = 0; i < bathroomRow.size(); i++) {
            if (!Objects.equals(reversed.get(i), bathroomRow.get(i))) {
                return false;
            }
        }
        return true;
    }

    private String printBathroom(List<List<Integer>> bathroomMap) {
        return bathroomMap.stream()
                .map(row -> row.stream().map(x -> x == 0 ? " " : String.valueOf(x) ).collect(Collectors.joining("")))
                .collect(Collectors.joining("\n"));
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
