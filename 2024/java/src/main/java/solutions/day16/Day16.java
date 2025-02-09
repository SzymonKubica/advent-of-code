package solutions.day16;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import solutions.Solution;
import solutions.Utils;
import solutions.common.Direction;
import solutions.common.Point;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day16 implements Solution {
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

        // Here we need to use a different algo that will find all paths.
    }

    private List<Pair<Point, Direction>> getAvailableNeighbourLocations(
            Point currLoc,
            List<List<MazeCell>> maze,
            HashSet<Point> alreadyVisited
    ) {
        return Arrays.stream(Direction.values())
                .map(dir -> Pair.of(currLoc.moveInDirection(dir), dir))
                .filter(p -> !p.getLeft().indexGrid(maze).equals(MazeCell.WALL))
                .filter(p -> !alreadyVisited.contains(p.getLeft()))
                .toList();
    }

    private Optional<Point> findStartingPoint(List<List<MazeCell>> maze) {
        for (int y = 0; y < maze.size(); y++) {
            for (int x = 0; x < maze.get(0).size(); x++) {
                if (maze.get(y).get(x).equals(MazeCell.START)) {
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
        EMPTY('.'), START('S'), END('E'), WALL('#');

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
