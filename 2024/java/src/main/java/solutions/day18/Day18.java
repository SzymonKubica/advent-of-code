package solutions.day18;

import lombok.SneakyThrows;
import solutions.Solution;
import solutions.Utils;
import solutions.common.Point;
import solutions.common.TerminalScreen;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day18 implements Solution {

    private record PuzzleConfiguration(int gridSize, int fallingBytesToSimulate) {
    }

    private static final boolean FANCY_VISUALIZATION = Boolean.parseBoolean(System.getenv(
            "FANCY_VISUALIZATION"));
    private static final int SIMULATION_TICK_MILLIS = Integer.parseInt((System.getenv(
            "SIMULATION_TICK_MILLIS") == null ? "100" : System.getenv("SIMULATION_TICK_MILLIS")));

    /**
     * For this puzzle the grid size and the number of falling bytes that needs to be
     * simulated differs for the example and the real puzzle input. Because
     * of this we need to resolve it based on the file name.
     */
    private PuzzleConfiguration resolvePuzzleConfiguration(String inputFileName) {
        int gridSize;
        int bytesFalling;
        if (inputFileName.contains("example")) {
            gridSize = 7;
            bytesFalling = 12;
        } else {
            gridSize = 71;
            bytesFalling = 1024;
        }
        return new PuzzleConfiguration(gridSize, bytesFalling);
    }

    @Override
    public void firstPart(String inputFile) {
        List<Point> fallingBytes = parseFallingStones(Utils.readInputAsStream(inputFile));
        final var config = resolvePuzzleConfiguration(inputFile);

        System.out.println("Using puzzle config: %s".formatted(config));
        if (FANCY_VISUALIZATION) {
            System.out.println("Using Lanterna-based visualization. Tick milliseconds: %d".formatted(
                    SIMULATION_TICK_MILLIS));
        }
        List<List<MemoryCell>> grid = initializeGrid(config.gridSize());

        System.out.println("Grid after initialization: ");
        System.out.println(printGrid(grid));

        TerminalScreen screen = null;
        if (FANCY_VISUALIZATION) {
            screen = new TerminalScreen(6);
        }

        simulateFallingBytes(grid, fallingBytes.subList(0, config.fallingBytesToSimulate()));

        System.out.println("Grid after simulating falling bytes: ");
        System.out.println(printGrid(grid));

        Point start = new Point(0, 0);
        Point end = new Point(config.gridSize() - 1, config.gridSize() - 1);

        Map<Point, Point> parentMap = findShortestPaths(screen, start, end, grid);
        traceBackPathToExit(screen, end, grid, parentMap, start);
    }

    private void traceBackPathToExit(
            TerminalScreen screen,
            Point end,
            List<List<MemoryCell>> grid,
            Map<Point, Point> parentMap,
            Point start
    ) {
        Point current = end;

        List<Point> pathFromExit = new ArrayList<>();
        do {
            current.indexGridAndSet(grid,
                                    new MemoryCell(MemoryType.PART_OF_SHORTEST_PATH, current));
            current = parentMap.get(current);
            pathFromExit.add(current);

            if (FANCY_VISUALIZATION) {
                visualizeTraceBack(screen, grid);
            }
        } while (!current.equals(start));

        System.out.println("Shortest path found: ");
        System.out.println(printGrid(grid));
    }


    private static Map<Point, Point> findShortestPaths(
            TerminalScreen screen,
            Point start,
            Point end,
            List<List<MemoryCell>> grid
    ) {
        Map<Point, Integer> costMap = new HashMap<>();
        List<Point> pointsToVisit = new ArrayList<>();

        // This set is not needed by the algorithm, it is
        // only used for visualisation
        HashSet<Point> visited = new HashSet<>();

        costMap.put(start, 0);
        pointsToVisit.add(start);


        grid.stream().forEach(row -> row.forEach(cell -> {
            if (!cell.location.equals(start) && cell.type.equals(MemoryType.HEALTHY)) {
                costMap.put(cell.location, Integer.MAX_VALUE);
                pointsToVisit.add(cell.location);
            }
        }));

        Map<Point, Point> parentMap = new HashMap<>();

        while (!pointsToVisit.isEmpty()) {
            pointsToVisit.sort(Comparator.comparingInt(costMap::get));
            Point curr = pointsToVisit.removeFirst();
            visited.add(curr);
            if (curr.equals(end)) {
                System.out.println("Exit found");
                System.out.println("Steps required to reach the exit: %s".formatted(costMap.get(curr)));
                break;
            }

            for (Point nb : curr.getNeighbourLocationsInsideGrid(grid)) {
                if (pointsToVisit.contains(nb)) {
                    int currentNeighbourCost = costMap.get(nb);
                    int costThroughCurr = costMap.get(curr) + 1;

                    if (costThroughCurr <= currentNeighbourCost) {
                        costMap.put(nb, costThroughCurr);
                        parentMap.put(nb, curr);
                    }
                }
            }

            if (FANCY_VISUALIZATION) {
                visualizeAlgorithmStep(screen, grid, visited, costMap);
            }
        }
        return parentMap;
    }

    private static boolean isExitReachable(
            TerminalScreen screen,
            Point start,
            Point end,
            List<List<MemoryCell>> grid
    ) {
        Map<Point, Integer> costMap = new HashMap<>();
        List<Point> pointsToVisit = new ArrayList<>();

        // This set is not needed by the algorithm, it is
        // only used for visualisation
        HashSet<Point> visited = new HashSet<>();

        costMap.put(start, 0);
        pointsToVisit.add(start);


        grid.stream().forEach(row -> row.forEach(cell -> {
            if (!cell.location.equals(start) && cell.type.equals(MemoryType.HEALTHY)) {
                costMap.put(cell.location, Integer.MAX_VALUE);
                pointsToVisit.add(cell.location);
            }
        }));

        Map<Point, Point> parentMap = new HashMap<>();

        while (!pointsToVisit.isEmpty()) {
            pointsToVisit.sort(Comparator.comparingInt(costMap::get));
            Point curr = pointsToVisit.removeFirst();
            visited.add(curr);
            if (curr.equals(end)) {
                System.out.println("Exit found");
                System.out.println("Steps required to reach the exit: %d".formatted(costMap.get(curr)));
                return costMap.get(curr) != Integer.MAX_VALUE;
            }

            for (Point nb : curr.getNeighbourLocationsInsideGrid(grid)) {
                if (pointsToVisit.contains(nb)) {
                    int currentNeighbourCost = costMap.get(nb);
                    int costThroughCurr = costMap.get(curr) + 1;

                    if (costThroughCurr <= currentNeighbourCost && costMap.get(curr) != Integer.MAX_VALUE) {
                        costMap.put(nb, costThroughCurr);
                        parentMap.put(nb, curr);
                    }
                }
            }

            if (FANCY_VISUALIZATION) {
                visualizeAlgorithmStep(screen, grid, visited, costMap);
            }
        }
        return false;
    }


    /**
     * Responsible for drawing the algorithm as it progresses using lanterna
     */
    @SneakyThrows
    private static void visualizeAlgorithmStep(
            TerminalScreen screen,
            List<List<MemoryCell>> grid,
            HashSet<Point> visited,
            Map<Point, Integer> costMap
    ) {
        List<List<MemoryCell>> gridCopy = Utils.getMutableGridCopy(grid);

        Set<Point> candidatesToVisit = costMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue() < Integer.MAX_VALUE)
                .filter(entry -> !visited.contains(entry.getKey()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());


        visited.forEach(p -> p.indexGridAndSet(gridCopy, new MemoryCell(MemoryType.VISITED, p)));
        candidatesToVisit.forEach(p -> p.indexGridAndSet(gridCopy,
                                                         new MemoryCell(MemoryType.CANDIDATE_TO_VISIT,
                                                                        p)));

        try {
            screen.clearScreen();
            screen.resetCursorPosition();
            for (final var row : gridCopy) {
                String line = row.stream()
                        .map(MemoryCell::type)
                        .map(MemoryType::toString)
                        .collect(Collectors.joining(""));
                screen.printLine(line);

            }
            screen.flushChanges();
            Thread.sleep(SIMULATION_TICK_MILLIS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void visualizeTraceBack(
            TerminalScreen screen,
            List<List<MemoryCell>> grid
    ) {

        try {
            screen.clearScreen();
            screen.resetCursorPosition();
            for (final var row : grid) {
                String line = row.stream()
                        .map(MemoryCell::type)
                        .map(MemoryType::toString)
                        .collect(Collectors.joining(""));
                screen.printLine(line);

            }
            screen.flushChanges();
            Thread.sleep(SIMULATION_TICK_MILLIS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void simulateFallingBytes(List<List<MemoryCell>> grid, List<Point> fallingBytes) {
        for (Point p : fallingBytes) {
            p.indexGridAndSet(grid, new MemoryCell(MemoryType.CORRUPTED, p));
        }
    }

    private static String printGrid(List<List<MemoryCell>> grid) {
        return grid.stream()
                .map(row -> row.stream()
                        .map(memoryCell -> memoryCell.type.toString())
                        .collect(Collectors.joining("")))
                .collect(Collectors.joining("\n"));
    }

    private List<List<MemoryCell>> initializeGrid(int gridSize) {
        List<List<MemoryCell>> output = new ArrayList<>();
        for (int y = 0; y < gridSize; y++) {
            List<MemoryCell> row = new ArrayList<>();
            for (int x = 0; x < gridSize; x++) {
                row.add(new MemoryCell(MemoryType.HEALTHY, new Point(x, y)));
            }
            output.add(row);
        }
        return output;
    }

    @Override
    public void secondPart(String inputFile) {
        List<Point> fallingBytes = parseFallingStones(Utils.readInputAsStream(inputFile));
        final var config = resolvePuzzleConfiguration(inputFile);

        System.out.println("Using puzzle config: %s".formatted(config));
        if (FANCY_VISUALIZATION) {
            System.out.println("Using Lanterna-based visualization. Tick milliseconds: %d".formatted(
                    SIMULATION_TICK_MILLIS));
        }
        List<List<MemoryCell>> grid = initializeGrid(config.gridSize());

        System.out.println("Grid after initialization: ");
        System.out.println(printGrid(grid));

        TerminalScreen screen = null;
        if (FANCY_VISUALIZATION) {
            screen = new TerminalScreen(6);
        }

        simulateFallingBytes(grid, fallingBytes.subList(0, config.fallingBytesToSimulate()));
        System.out.println("Grid after simulating initial falling bytes: ");
        System.out.println(printGrid(grid));

        int nextFallingByteIndex = config.fallingBytesToSimulate();
        boolean blockingByteFound = false;
        while (!blockingByteFound) {
            System.out.println("Simulating falling byte: %s (%d/%d)".formatted(fallingBytes.get(nextFallingByteIndex), nextFallingByteIndex, fallingBytes.size()));
            simulateFallingBytes(grid, List.of(fallingBytes.get(nextFallingByteIndex)));
            Point start = new Point(0, 0);
            Point end = new Point(config.gridSize() - 1, config.gridSize() - 1);
            blockingByteFound |= !isExitReachable(screen, start, end, grid);
            nextFallingByteIndex++;
        }
        System.out.println("Found a byte that blocks the exit.");


    }

    private List<Point> parseFallingStones(Stream<String> input) {
        return input.map(line -> Arrays.stream(line.split(",")).map(Integer::parseInt).toList())
                .map(tup -> new Point(tup.get(0), tup.get(1)))
                .toList();
    }

    private record MemoryCell(MemoryType type, Point location) {
    }

    private enum MemoryType {
        HEALTHY("."), CORRUPTED("#"), PART_OF_SHORTEST_PATH("0"), VISITED("v"), CANDIDATE_TO_VISIT(
                "_");

        private final String representation;

        MemoryType(String representation) {
            this.representation = representation;
        }

        @Override
        public String toString() {
            return this.representation;
        }
    }


}
