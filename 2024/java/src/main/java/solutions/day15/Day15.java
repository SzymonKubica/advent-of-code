package solutions.day15;

import org.apache.commons.lang3.tuple.Pair;
import solutions.Solution;
import solutions.Utils;
import solutions.common.Direction;
import solutions.common.Point;
import solutions.common.TerminalScreen;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day15 implements Solution {
    private static final int ROBOT_STEP_MILLIS = 50;
    private static final boolean TERMINAL_RENDERING_ENABLED = Boolean.parseBoolean(System.getenv(
            "USE_LANTERNA"));


    @Override
    public void firstPart(String inputFile) {
        TerminalScreen screen;
        if (inputFile.contains("puzzle-input")) {
            screen = new TerminalScreen(9);
        } else {
            screen = new TerminalScreen();
        }
        final var input = readInput(Utils.readInputAsStream(inputFile));
        var warehouse = input.getLeft();
        final List<Direction> robotMoves = input.getRight();

        System.out.println(Utils.toStringLineByLine(warehouse));
        System.out.println(robotMoves);
        printWarehouse(warehouse, screen);

        Point robotLocation = findRobot(warehouse).get();
        System.out.println(robotLocation);

        for (int i = 0; i < robotMoves.size(); i++) {
            System.out.println("Processing move: (%d/%d)".formatted(i, robotMoves.size()));
            robotLocation = moveRobot(robotLocation, robotMoves.get(i), warehouse);
            if (TERMINAL_RENDERING_ENABLED) {
                printWarehouse(warehouse, screen);
                if (ROBOT_STEP_MILLIS > 0) {
                    try {
                        Thread.sleep(ROBOT_STEP_MILLIS);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        printWarehouse(warehouse, screen);
        List<Pair<Point, Integer>> coordinates =
                findBoxGoodsPositioningSystemCoordinates(warehouse);

        int sum = coordinates.stream().map(Pair::getRight).reduce(Integer::sum).get();
        System.out.println("Sum of boxes' GPS coordinates: %d".formatted(sum));

    }

    @Override
    public void secondPart(String inputFile) {
        TerminalScreen screen;
        if (inputFile.contains("puzzle-input")) {
            screen = new TerminalScreen(9);
        } else {
            screen = new TerminalScreen();
        }
        final var input = readInput(Utils.readInputAsStream(inputFile));

        var warehouse = input.getLeft();
        warehouse = scaleUpWarehouse(warehouse);
        printWarehouse(warehouse, screen);
        final List<Direction> robotMoves = input.getRight();

        Point robotLocation = findRobot(warehouse).get();
        System.out.println(robotLocation);

        for (int i = 0; i < robotMoves.size(); i++) {
            System.out.println("Processing move: (%d/%d) in direction: %s".formatted(i,
                                                                                     robotMoves.size(),
                                                                                     robotMoves.get(
                                                                                             i)));
            robotLocation = moveRobotSecondPart(robotLocation, robotMoves.get(i), warehouse);
            if (TERMINAL_RENDERING_ENABLED) {
                printWarehouse(warehouse, screen);
                if (ROBOT_STEP_MILLIS > 0) {
                    try {
                        Thread.sleep(ROBOT_STEP_MILLIS);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        printWarehouse(warehouse, screen);

        List<Pair<Point, Integer>> coordinates = findBoxGoodsPositioningSystemCoordinatesPart2(
                warehouse);

        int sum = coordinates.stream().map(Pair::getRight).reduce(Integer::sum).get();
        System.out.println("Sum of boxes' GPS coordinates: %d".formatted(sum));
    }


    private List<List<WarehouseLocation>> scaleUpWarehouse(List<List<WarehouseLocation>> warehouse) {
        return warehouse.stream()
                .map(line -> line.stream()
                        .map(this::scaleUpLocation)
                        .flatMap(List::stream)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    private List<WarehouseLocation> scaleUpLocation(WarehouseLocation loc) {
        return switch (loc) {
            case ROBOT -> List.of(WarehouseLocation.ROBOT, WarehouseLocation.EMPTY);
            case WALL -> List.of(WarehouseLocation.WALL, WarehouseLocation.WALL);
            case BOX -> List.of(WarehouseLocation.BOX_LEFT, WarehouseLocation.BOX_RIGHT);
            case EMPTY -> List.of(WarehouseLocation.EMPTY, WarehouseLocation.EMPTY);
            case BOX_LEFT -> List.of();
            case BOX_RIGHT -> List.of();
        };
    }

    private List<Pair<Point, Integer>> findBoxGoodsPositioningSystemCoordinates(List<List<WarehouseLocation>> warehouse) {
        List<Pair<Point, Integer>> output = new ArrayList<>();
        for (int y = 0; y < warehouse.size(); y++) {
            for (int x = 0; x < warehouse.get(0).size(); x++) {
                if (warehouse.get(y).get(x).equals(WarehouseLocation.BOX)) {
                    output.add(Pair.of(new Point(x, y), 100 * y + x));
                }
            }
        }
        return output;
    }

    private List<Pair<Point, Integer>> findBoxGoodsPositioningSystemCoordinatesPart2(List<List<WarehouseLocation>> warehouse) {
        List<Pair<Point, Integer>> output = new ArrayList<>();
        for (int y = 0; y < warehouse.size(); y++) {
            for (int x = 0; x < warehouse.get(0).size(); x++) {
                if (warehouse.get(y).get(x).equals(WarehouseLocation.BOX_LEFT)) {
                    output.add(Pair.of(new Point(x, y), 100 * y + x));
                }
            }
        }
        return output;
    }

    private Point moveRobotSecondPart(
            Point robotLocation,
            Direction move,
            List<List<WarehouseLocation>> warehouse
    ) {
        // Here projecting rays won't work. We need to try and push and if possible do it,
        // If not roll back.
        List<List<WarehouseLocation>> warehouseCopy = Utils.getMutableGridCopy(warehouse);

        Point newLocation = robotLocation.moveInDirection(move);

        // Easy case we simply move forward
        if (newLocation.indexGrid(warehouse).equals(WarehouseLocation.EMPTY)) {
            robotLocation.indexGridAndSet(warehouse, WarehouseLocation.EMPTY);
            newLocation.indexGridAndSet(warehouse, WarehouseLocation.ROBOT);
            return newLocation;
        }

        if (newLocation.indexGrid(warehouse).equals(WarehouseLocation.WALL)) {
            return robotLocation;
        }

        if (move.isHorizontal()) {
            // Handle the easy case
            List<Point> pathToTheClosestWallOrEmpty = projectRayInDirection(move,
                                                                            robotLocation,
                                                                            warehouse);
            final List<WarehouseLocation> stateBeforeMove = pathToTheClosestWallOrEmpty.stream()
                    .map(p -> p.indexGrid(warehouse))
                    .toList();
            final var maybeNewState = simulatePushSecondPart(stateBeforeMove);
            if (maybeNewState.isEmpty()) {
                return robotLocation;
            }

            final List<WarehouseLocation> stateAfterMove = maybeNewState.get();
            for (int i = 0; i < pathToTheClosestWallOrEmpty.size(); i++) {
                if (!Objects.equals(stateAfterMove.get(i), stateBeforeMove.get(i))) {
                    pathToTheClosestWallOrEmpty.get(i)
                            .indexGridAndSet(warehouse, stateAfterMove.get(i));
                }
            }
            return robotLocation.moveInDirection(move);
        }

        // The hard case: pushing vertically.
        assert !move.isHorizontal() :
                "At this point in the code we are only considering pushing up or down";
        // Idea here is to first project a ray, if everything on the ray is filled,
        // no point in trying. Else, we need to simulate a 'waterfall' push by maintaining
        // a queue of blocks that need to be moved.

        List<Point> pathToTheClosestWallOrEmpty = projectRayInDirection(move,
                                                                        robotLocation,
                                                                        warehouse);

        final List<WarehouseLocation> stateBeforeMove = pathToTheClosestWallOrEmpty.stream()
                .map(p -> p.indexGrid(warehouse))
                .toList();

        if (stateBeforeMove.stream()
                .noneMatch(loc -> Objects.equals(loc, WarehouseLocation.EMPTY))) {
            return robotLocation;
        }

        Deque<Pair<Point, WarehouseLocation>> cellsToMove = new ArrayDeque<>();
        cellsToMove.add(Pair.of(newLocation, WarehouseLocation.ROBOT));
        List<Point> updatedLocations = new ArrayList<>();

        do {
            System.out.println(cellsToMove);
            var movedCell = cellsToMove.pollFirst();
            newLocation = movedCell.getLeft();
            var movedCellValue = movedCell.getRight();
            System.out.println("Processing cell %s at %s".formatted(movedCellValue, newLocation));
            if (newLocation.indexGrid(warehouse).equals(WarehouseLocation.WALL)) {
                // Attempt at moving has failed, one box or the robot itself
                // cannot be moved.
                return robotLocation;
            }
            final var impactedCell = newLocation.indexGrid(warehouse);
            newLocation.indexGridAndSet(warehouseCopy, movedCellValue);
            updatedLocations.add(newLocation);
            if (impactedCell.equals(WarehouseLocation.BOX_LEFT)) {
                // The cell that was previously occupied by the right part of the box needs to be
                // emptied but only if it hasn't already been set by other moved box
                if (!updatedLocations.contains(newLocation.moveInDirection(Direction.RIGHT))) {
                    newLocation.moveInDirection(Direction.RIGHT)
                            .indexGridAndSet(warehouseCopy, WarehouseLocation.EMPTY);
                }
                updatedLocations.add(newLocation.moveInDirection(Direction.RIGHT));
                updatedLocations.add(newLocation);
                cellsToMove.add(Pair.of(newLocation.moveInDirection(move),
                                        WarehouseLocation.BOX_LEFT));
                cellsToMove.add(Pair.of(newLocation.moveInDirection(move)
                                                .moveInDirection(Direction.RIGHT),
                                        WarehouseLocation.BOX_RIGHT));
            } else if (impactedCell.equals(WarehouseLocation.BOX_RIGHT)) {
                // The cell that was previously occupied by the left part of the box needs to be
                // emptied
                if (!updatedLocations.contains(newLocation.moveInDirection(Direction.LEFT))) {
                    newLocation.moveInDirection(Direction.LEFT)
                            .indexGridAndSet(warehouseCopy, WarehouseLocation.EMPTY);
                }
                updatedLocations.add(newLocation.moveInDirection(Direction.LEFT));
                cellsToMove.add(Pair.of(newLocation.moveInDirection(move)
                                                .moveInDirection(Direction.LEFT),
                                        WarehouseLocation.BOX_LEFT));
                cellsToMove.add(Pair.of(newLocation.moveInDirection(move),
                                        WarehouseLocation.BOX_RIGHT));
            }
        } while (!cellsToMove.isEmpty());

        for (final var p : updatedLocations) {
            p.indexGridAndSet(warehouse, p.indexGrid(warehouseCopy));
        }


        robotLocation.indexGridAndSet(warehouse, WarehouseLocation.EMPTY);
        return robotLocation.moveInDirection(move);
    }

    private Point moveRobot(
            Point robotLocation,
            Direction move,
            List<List<WarehouseLocation>> warehouse
    ) {
        List<Point> pathToTheClosestWallOrEmpty = projectRayInDirection(move,
                                                                        robotLocation,
                                                                        warehouse);
        final List<WarehouseLocation> stateBeforeMove = pathToTheClosestWallOrEmpty.stream()
                .map(p -> p.indexGrid(warehouse))
                .toList();
        final var maybeNewState = simulatePush(stateBeforeMove);
        if (maybeNewState.isEmpty()) {
            return robotLocation;
        }

        final List<WarehouseLocation> stateAfterMove = maybeNewState.get();
        for (int i = 0; i < pathToTheClosestWallOrEmpty.size(); i++) {
            if (!Objects.equals(stateAfterMove.get(i), stateBeforeMove.get(i))) {
                pathToTheClosestWallOrEmpty.get(i)
                        .indexGridAndSet(warehouse, stateAfterMove.get(i));
            }
        }
        return robotLocation.moveInDirection(move);
    }

    private Optional<List<WarehouseLocation>> simulatePush(List<WarehouseLocation> stateBeforeMove) {
        assert !stateBeforeMove.isEmpty() && stateBeforeMove.get(0)
                .equals(WarehouseLocation.ROBOT) :
                "The ray needs to be projected starting from the current robot position.";

        if (stateBeforeMove.stream()
                .allMatch(location -> !location.equals(WarehouseLocation.EMPTY))) {
            // No move possible as everything is occupied.
            return Optional.empty();
        }

        List<WarehouseLocation> newState = new ArrayList<>(stateBeforeMove);
        newState.set(0, WarehouseLocation.EMPTY);
        newState.set(1, WarehouseLocation.ROBOT);
        // We need to find a spot to move the relocated box
        if (stateBeforeMove.get(1).equals(WarehouseLocation.BOX)) {
            int index = 2;
            while (!newState.get(index).equals(WarehouseLocation.EMPTY)) {
                index++;
            }
            newState.set(index, WarehouseLocation.BOX);
        }
        return Optional.of(newState);
    }

    private Optional<List<WarehouseLocation>> simulatePushSecondPart(List<WarehouseLocation> stateBeforeMove) {
        assert !stateBeforeMove.isEmpty() && stateBeforeMove.get(0)
                .equals(WarehouseLocation.ROBOT) :
                "The ray needs to be projected starting from the current robot position.";

        if (stateBeforeMove.stream()
                .allMatch(location -> !location.equals(WarehouseLocation.EMPTY))) {
            // No move possible as everything is occupied.
            return Optional.empty();
        }

        List<WarehouseLocation> newState = new ArrayList<>(stateBeforeMove);
        newState.set(0, WarehouseLocation.EMPTY);
        newState.set(1, WarehouseLocation.ROBOT);
        // We need to find a spot to move the relocated box
        // This is the case where the box is pushed from the left
        if (stateBeforeMove.get(1).equals(WarehouseLocation.BOX_LEFT)) {
            int index = 2;
            while (!newState.get(index).equals(WarehouseLocation.EMPTY)) {
                index++;
            }
            // We need to ensure that box parts are shifted accordingly
            // this is done by simply writing them again into the array
            for (int j = 2; j <= index; j++) {
                if (j % 2 == 0) {
                    newState.set(j, WarehouseLocation.BOX_LEFT);
                } else {
                    newState.set(j, WarehouseLocation.BOX_RIGHT);
                }
            }
        }
        // This is the case where the box is pushed from the right
        if (stateBeforeMove.get(1).equals(WarehouseLocation.BOX_RIGHT)) {
            int index = 2;
            while (!newState.get(index).equals(WarehouseLocation.EMPTY)) {
                index++;
            }
            // We need to ensure that box parts are shifted accordingly
            // this is done by simply writing them again into the array
            for (int j = 2; j <= index; j++) {
                if (j % 2 == 0) {
                    newState.set(j, WarehouseLocation.BOX_RIGHT);
                } else {
                    newState.set(j, WarehouseLocation.BOX_LEFT);
                }
            }
        }
        System.out.println(stateBeforeMove);
        System.out.println(newState);
        return Optional.of(newState);
    }

    private List<Point> projectRayInDirection(
            Direction direction,
            Point robotLocation,
            List<List<WarehouseLocation>> warehouse
    ) {
        List<Point> ray = new ArrayList<>();
        ray.add(robotLocation);
        Point current = robotLocation.moveInDirection(direction);
        while (current.indexGrid(warehouse) != WarehouseLocation.WALL) {
            ray.add(current);
            // Once the ray has at least one empty box, nothing else after
            // that will be affected by the robot push so we don't need
            // to project any further.
            if (current.indexGrid(warehouse).equals(WarehouseLocation.EMPTY)) {
                return ray;
            }
            current = current.moveInDirection(direction);
        }
        return ray;
    }

    private void printWarehouse(List<List<WarehouseLocation>> warehouse, TerminalScreen screen) {
        try {
            screen.clearScreen();
            screen.resetCursorPosition();
            for (List<WarehouseLocation> line : warehouse) {
                String lineStr = line.stream()
                        .map(WarehouseLocation::toString)
                        .collect(Collectors.joining(""));
                screen.printLine(lineStr);
            }
            screen.flushChanges();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<Point> findRobot(List<List<WarehouseLocation>> warehouse) {
        for (int y = 0; y < warehouse.size(); y++) {
            for (int x = 0; x < warehouse.get(0).size(); x++) {
                if (warehouse.get(y).get(x).equals(WarehouseLocation.ROBOT)) {
                    return Optional.of(new Point(x, y));
                }
            }
        }
        return Optional.empty();
    }


    private Pair<List<List<WarehouseLocation>>, List<Direction>> readInput(Stream<String> input) {
        String fullInput = input.collect(Collectors.joining("\n"));
        String[] wareHouseMapAndMoves = fullInput.split("\n\n");

        assert wareHouseMapAndMoves.length == 2 :
                "Puzzle input needs to contain a grid and a list of moves for the robot.";
        String wareHouseMap = wareHouseMapAndMoves[0];
        String moves = wareHouseMapAndMoves[1];

        return Pair.of(parseWareHouseMap(wareHouseMap), parseMoves(moves));
    }

    private List<Direction> parseMoves(String moves) {
        String allMoves = moves.lines().collect(Collectors.joining(""));
        return allMoves.chars().mapToObj(c -> Direction.fromChar((char) c)).toList();
    }

    private List<List<WarehouseLocation>> parseWareHouseMap(String wareHouseMap) {
        return wareHouseMap.lines()
                .map(l -> l.chars()
                        .mapToObj(c -> WarehouseLocation.fromChar((char) c))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    enum WarehouseLocation {
        ROBOT('@'), WALL('#'), BOX('O'), EMPTY('.'), BOX_LEFT('['), BOX_RIGHT(']');

        private char representation;

        WarehouseLocation(char representation) {
            this.representation = representation;
        }

        static WarehouseLocation fromChar(char c) {
            return switch (c) {
                case '@' -> WarehouseLocation.ROBOT;
                case '#' -> WarehouseLocation.WALL;
                case 'O' -> WarehouseLocation.BOX;
                case '.' -> WarehouseLocation.EMPTY;
                default -> throw new IllegalStateException("Unexpected value: " + c);
            };
        }

        @Override
        public String toString() {
            return String.valueOf(this.representation);
        }
    }
}
