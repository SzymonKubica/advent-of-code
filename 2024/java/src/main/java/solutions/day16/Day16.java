package solutions.day16;

import org.apache.commons.lang3.tuple.MutablePair;
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

public class Day16 implements Solution {
    private static final boolean USE_LANTERNA = Boolean.parseBoolean(System.getenv("USE_LANTERNA"));
    private static final int SIMULATION_TICK_MILLIS = System.getenv("SIMULATION_TICK_MILLIS")
                                                      == null ? 0 : Integer.parseInt(System.getenv(
            "SIMULATION_TICK_MILLIS"));

    @Override
    public void firstPart(String inputFile) {
        final var maze = readMazeMap(Utils.readInputAsStream(inputFile));

        System.out.println(Utils.toStringLineByLine(maze));

        // Here is the strat: find all paths to the end
        // then evaluate their cost by counting turns and lengths
        // we need to use fancy Dijkstra here

        final var maybeStart = findStartingPoint(maze);
        if (maybeStart.isEmpty()) {
            System.out.println("No starting point found, unable to solve the puzzle.");
            return;
        }

        Point start = maybeStart.get();


        // Map with references to the created points allowing for updating
        // distance to the neighbours.
        Map<Point, Integer> costMap = new HashMap<>();
        Map<Point, Direction> directionMap = new HashMap<>();
        HashSet<Point> alreadyVisited = new HashSet<>();
        List<Point> unvisitedSet = new ArrayList<>();

        for (int y = 0; y < maze.size(); y++) {
            for (int x = 0; x < maze.get(0).size(); x++) {
                final var curr = maze.get(y).get(x);
                final var point = new Point(x, y);
                if (!curr.equals(MazeCell.WALL) && !curr.equals(MazeCell.START)) {
                    costMap.put(point, Integer.MAX_VALUE);
                    unvisitedSet.add(point);
                }
            }
        }

        costMap.put(start, 0);
        unvisitedSet.add(start);
        directionMap.put(start, Direction.RIGHT);

        while (!unvisitedSet.isEmpty()) {
            unvisitedSet.sort(Comparator.comparingInt(costMap::get));
            Point curr = unvisitedSet.removeFirst();
            int currCost = costMap.get(curr);
            if (curr.indexGrid(maze).equals(MazeCell.END)) {
                System.out.println("End found, cost: %s".formatted(currCost));
                return;
            }
            System.out.println("Processing point: %s, current cost: %s".formatted(curr, currCost));
            alreadyVisited.add(curr);
            if (currCost == Integer.MAX_VALUE) {
                System.out.println("No more nodes reachable.");
                return;
            }
            Direction currDirection = directionMap.get(curr);
            List<Pair<Point, Direction>> neighbours = getAvailableNeighbourLocations(curr,
                                                                                     maze,
                                                                                     alreadyVisited);
            System.out.println("Accessible neighbours: ");
            System.out.println(Utils.toStringLineByLine(neighbours));

            // update costs based on direction relationship
            for (final var nb : neighbours) {
                int nbCost = costMap.get(nb.getLeft());
                int stepCost = currDirection == nb.getRight() ? 1 : 1001;
                int costThroughCurrent = currCost + stepCost;
                System.out.println("Cost through current node: %s".formatted(costThroughCurrent));
                System.out.println("Cost of neighbour: %s".formatted(nbCost));
                costMap.put(nb.getLeft(), Math.min(nbCost, costThroughCurrent));
                System.out.println(costMap.get(nb.getLeft()));
                directionMap.put(nb.getLeft(), nb.getRight());
            }
        }
    }

    @Override
    public void secondPart(String inputFile) {
        final var maze = readMazeMap(Utils.readInputAsStream(inputFile));

        System.out.println(Utils.toStringLineByLine(maze));


        final var maybeStart = findStartingPoint(maze);
        if (maybeStart.isEmpty()) {
            System.out.println("No starting point found, unable to solve the puzzle.");
            return;
        }

        Point start = maybeStart.get();


        // Map with references to the created points allowing for updating
        // distance to the neighbours.
        Map<Pair<Point, Direction>, Integer> costMap = new HashMap<>();
        Map<Point, Direction> directionMap = new HashMap<>();
        List<Pair<Point, Direction>> unvisitedSet = new ArrayList<>();


        for (int y = 0; y < maze.size(); y++) {
            for (int x = 0; x < maze.get(0).size(); x++) {
                final var curr = maze.get(y).get(x);
                final var point = new Point(x, y);
                if (!curr.equals(MazeCell.WALL) && !curr.equals(MazeCell.START)) {
                    for (var dir : Direction.values()) {
                        costMap.put(Pair.of(point, dir), Integer.MAX_VALUE);
                        unvisitedSet.add(Pair.of(point, dir));
                    }
                }
            }
        }

        costMap.put(Pair.of(start, Direction.RIGHT), 0);
        unvisitedSet.add(Pair.of(start, Direction.RIGHT));
        Map<Pair<Point, Direction>, List<Pair<Point, Direction>>> shortestPathParentMap = new HashMap<>();

        TerminalScreen screen = new TerminalScreen();

        if (USE_LANTERNA) {
            visualiseVisited(screen, unvisitedSet.stream().map(Pair::getLeft).toList(), maze);
        }

        while (!unvisitedSet.isEmpty()) {

            unvisitedSet.sort(Comparator.comparingInt(costMap::get));
            Pair<Point, Direction> curr = unvisitedSet.removeFirst();

            // We need to mark the cost of turning in place
            for (Direction d : Arrays.stream(Direction.values()).filter(dir -> !dir.equals(curr.getRight())).toList()) {
                if (d.isOpposite(curr.getRight())) {
                    costMap.put(Pair.of(curr.getLeft(), d),  costMap.get(curr) + 2000);
                } else {
                    costMap.put(Pair.of(curr.getLeft(), d),  costMap.get(curr) + 1000);
                }
            }
            if (USE_LANTERNA) {
                try {
                    Thread.sleep(SIMULATION_TICK_MILLIS);
                    visualiseVisited(screen, unvisitedSet.stream().map(Pair::getLeft).toList(), maze);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            int currCost = costMap.get(curr);
            System.out.println("Processing point: %s, current cost: %s".formatted(curr, currCost));
            if (currCost == Integer.MAX_VALUE) {
                System.out.println("No more nodes reachable.");
                break;
            }
            Direction currDirection = curr.getRight();
            List<Pair<Point, Direction>> neighbours = getAvailableNeighbourLocations(curr.getLeft(),
                                                                                     maze,
                                                                                     HashSet.newHashSet(0));
            System.out.println("Accessible neighbours: ");
            System.out.println(Utils.toStringLineByLine(neighbours));

            // update costs based on direction relationship
            for (final var nb : neighbours) {
                int nbCost = costMap.get(nb);
                int stepCost = currDirection == nb.getRight() ? 1 : 1001;
                // If the direction changes we need to update the cost map as we are turning in place
                int costThroughCurrent = currCost + stepCost;
                System.out.println("Cost through current node: %s".formatted(costThroughCurrent));
                System.out.println("Cost of neighbour: %s".formatted(nbCost));
                // The idea is that all best paths will have the same cost of the
                // current node, so if the current cost of nb and the new cost
                // is the same, it means that we have two paths that are equally good
                if (costThroughCurrent == nbCost && shortestPathParentMap.containsKey(nb)) {
                    shortestPathParentMap.get(nb).add(curr);
                } else if (costThroughCurrent < nbCost){
                    shortestPathParentMap.put(nb, new ArrayList<>(List.of(curr)));
                }
                costMap.put(nb, Math.min(nbCost, costThroughCurrent));
                System.out.println(costMap.get(nb.getLeft()));
                directionMap.put(nb.getLeft(), nb.getRight());
            }
        }
        // After this is done, we have traversed all possible paths and so the end should have an
        // entry
        // in the parent map.
        Point end = findEndingPoint(maze).get();
        List<Integer> endCosts = costMap.entrySet().stream().filter(entry -> entry.getKey().getLeft().equals(end)).map(entry -> entry.getValue()).toList();
        System.out.println("Cost at the end: %s".formatted(endCosts.stream().min(Integer::compareTo)));

        System.out.println(Utils.toStringLineByLine(shortestPathParentMap.entrySet().stream().toList()));
        if (false) {
            return;
        }
        // Now we trace back all paths that end at the end
        List<Pair<Point, Direction>> pointsToTraceBack = shortestPathParentMap.get(Pair.of(end, Direction.UP));
        HashSet<Point> uniquePointsOnBestPaths = new HashSet<>();
        while (!pointsToTraceBack.isEmpty()) {
            final var curr = pointsToTraceBack.removeFirst();
            uniquePointsOnBestPaths.add(curr.getLeft());
            System.out.println("Tracing point: %s".formatted(curr));
            final var parents = shortestPathParentMap.getOrDefault(curr, List.of());
            System.out.println("Parents: %s".formatted(parents));
            pointsToTraceBack.addAll(parents);
        }

        System.out.println("Total unique locations: %s".formatted(uniquePointsOnBestPaths.size()));
    }

    private void visualiseVisited(
            TerminalScreen screen,
            List<Point> unvisitedSet,
            List<List<MazeCell>> maze
    ) {
        List<List<MazeCell>> mazeCopy = Utils.getMutableGridCopy(maze);
        for (Point p : unvisitedSet) {
            p.indexGridAndSet(mazeCopy, MazeCell.UNVISITED);
        }
        try {
            screen.clearScreen();
            for (List<MazeCell> row : mazeCopy) {
                String repr = row.stream().map(MazeCell::toString).collect(Collectors.joining(""));
                screen.printLine(repr);
            }
            screen.flushChanges();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Pair<Point, Direction>> getAvailableNeighbourLocations(
            Point currLoc,
            List<List<MazeCell>> maze,
            HashSet<Point> alreadyVisited
    ) {
        return Arrays.stream(Direction.values())
                .map(dir -> Pair.of(currLoc.moveInDirection(dir), dir))
                .filter(p -> !p.getLeft().indexGrid(maze).equals(MazeCell.WALL))
                .filter(p -> !p.getLeft().indexGrid(maze).equals(MazeCell.START))
                .filter(p -> !alreadyVisited.contains(p.getLeft()))
                .toList();
    }

    private Optional<Point> findStartingPoint(List<List<MazeCell>> maze) {
        return findPointOfType(maze, MazeCell.START);
    }

    private Optional<Point> findEndingPoint(List<List<MazeCell>> maze) {
        return findPointOfType(maze, MazeCell.END);
    }

    private Optional<Point> findPointOfType(List<List<MazeCell>> maze, MazeCell type) {
        for (int y = 0; y < maze.size(); y++) {
            for (int x = 0; x < maze.get(0).size(); x++) {
                if (maze.get(y).get(x).equals(type)) {
                    return Optional.of(new Point(x, y));
                }
            }
        }
        return Optional.empty();
    }


    private List<List<MazeCell>> readMazeMap(Stream<String> input) {
        return input.map(line -> line.chars().mapToObj(c -> MazeCell.fromChar((char) c)).toList())
                .toList();
    }

    enum MazeCell {
        EMPTY('.'), START('S'), END('E'), WALL('#'), UNVISITED('_');

        private char representation;

        MazeCell(char representation) {
            this.representation = representation;
        }

        static MazeCell fromChar(char c) {
            return switch (c) {
                case '.' -> EMPTY;
                case 'S' -> START;
                case 'E' -> END;
                case '#' -> WALL;
                default -> throw new IllegalStateException("Unexpected value: " + c);
            };
        }

        @Override
        public String toString() {
            return String.valueOf(representation);
        }
    }
}
