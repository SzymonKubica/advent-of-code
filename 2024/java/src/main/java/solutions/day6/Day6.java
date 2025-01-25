package solutions.day6;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import solutions.CommonStructures;
import solutions.Solution;
import solutions.Utils;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day6 implements Solution {
    @Override
    public void firstPart(String inputFile) {
        final var grid = parseFromInputStream(Utils.readInputAsStream(inputFile));
        System.out.println(printGrid(grid));

        Guard guard = findGuard(grid).get();

        do {
            guard.visitCell(grid);
        } while (guard.takeStepOnGrid(grid).isPresent());

        int visitedCells = grid.stream()
                .map(row -> row.stream().filter(cell -> cell == GridCell.VISITED).count())
                .reduce(Long::sum)
                .get()
                .intValue();

        System.out.println(printGrid(grid));
        System.out.printf("Visited cells: %d\n", visitedCells);
    }


    @Override
    public void secondPart(String inputFile) {
        final var grid = parseFromInputStream(Utils.readInputAsStream(inputFile));
        System.out.println(printGrid(grid));

        Guard guard = findGuard(grid).get();
        var clearGrid = cloneGrid(grid);
        var clearGuard = new Guard(guard.direction, guard.location);

        Set<CommonStructures.Point> newObstructionPositions = new HashSet<>();
        do {
            // We need to do the following:
            // - Find all possible places for obstacles that won't result in the guard leaving the
            //   board immediately.
            // - Once we have found all of those positions, we insert the obstacle one-by-one
            //   into different copies of the grid and run the search to check if it results in a
            //   cycle.
            //final var maybeNewObstruction = searchForObstaclesToTheRight(guard, grid);
            // Turns out a brute-force trying of all location positions is fast enough.
            // The whole idea about projecting rays was unnecessary.
            newObstructionPositions.add(guard.location);
            //maybeNewObstruction.ifPresent(newObstructionPositions::add);
            guard.visitCell(grid);
        } while (guard.takeStepOnGrid(grid).isPresent());

        System.out.printf("New possible obstruction positions: %d\n", newObstructionPositions.size());
        int cyclesFound = 0;
        int iterations = 1;
        for (var position : newObstructionPositions) {
            System.out.println("Checking obstruction position: %s (%d/%s)".formatted(position, iterations, newObstructionPositions.size()));
            var newPossibleGrid = cloneGrid(grid);
            newPossibleGrid.get(position.y()).set(position.x(), GridCell.OBSTACLE_SPECIAL);
            var newGuard = new Guard(clearGuard.direction, clearGuard.location);

            Set<Pair<CommonStructures.Point, CommonStructures.Direction>> guardStates = new HashSet<>();

            do {
                if (guardStates.contains(Pair.of(newGuard.location, newGuard.direction))) {
                    cyclesFound += 1;
                    break;
                }
                newGuard.visitCell(newPossibleGrid);
                guardStates.add(Pair.of(newGuard.location, newGuard.direction));
            } while (newGuard.takeStepOnGrid(newPossibleGrid).isPresent());

            iterations += 1;
        }

        System.out.println("Positions that cause cycles found: %s".formatted(cyclesFound));

    }

    private Optional<CommonStructures.Point> searchForObstaclesToTheRight(Guard guard,
                                                                          List<List<GridCell>> grid) {
        // copy the current state of the guard
        Guard projectionRay = new Guard(guard.direction, guard.location);
        List<List<GridCell>> projectionGrid = cloneGrid(grid);
        // save the new location as we only count the new obstruction if it is within the grid.
        CommonStructures.Point possibleObstructionLocation =
                projectionRay.location.translateInDirection(
                guard.direction);
        if (!possibleObstructionLocation.isInsideGrid(grid)
            || possibleObstructionLocation.indexGrid(grid) == GridCell.OBSTACLE) {
            return Optional.empty();
        }

        projectionRay.turnRight();
        while (projectionRay.location.isInsideGrid(projectionGrid)) {
            if (projectionRay.location.indexGrid(projectionGrid) == GridCell.OBSTACLE) {
                // Obstacle found
                return Optional.of(possibleObstructionLocation);
            }
            projectionRay.visitCell(projectionGrid);
            projectionRay.location =
                    projectionRay.location.translateInDirection(projectionRay.direction);
        }

        return Optional.empty();
    }

    private static List<List<GridCell>> cloneGrid(List<List<GridCell>> grid) {
        List<List<GridCell>> projectionGrid = grid.stream()
                .map(ArrayList::new)
                .map(l -> (List<GridCell>) l)
                .toList();
        return projectionGrid;
    }

    private Optional<Guard> findGuard(List<List<GridCell>> grid) {
        for (int y = 0; y < grid.size(); y++) {
            for (int x = 0; x < grid.get(0).size(); x++) {
                if (grid.get(y).get(x) == GridCell.GUARD) {
                    return Optional.of(new Guard(CommonStructures.Direction.UP,
                                                 new CommonStructures.Point(x, y)));
                }
            }
        }
        return Optional.empty();
    }

    private List<List<GridCell>> parseFromInputStream(Stream<String> input) {
        return input.map(line -> (List<GridCell>) new ArrayList<>(line.chars()
                                                                          .mapToObj(c -> GridCell.fromChar(
                                                                                  (char) c))
                                                                          .toList())).toList();
    }

    private String printGrid(List<List<GridCell>> grid) {
        return "==========\n" + grid.stream()
                .map(row -> row.stream().map(Objects::toString).collect(Collectors.joining("")))
                .collect(Collectors.joining("\n")) + "\n==========\n";
    }

    @AllArgsConstructor
    private class Guard {
        private CommonStructures.Direction direction;
        private CommonStructures.Point location;

        /**
         * The guard will try to take the next step on the grid according to the puzzle
         * logic, if the next location falls outside of the grid, the returned
         * optional will be empty indicating that the main 'game' loop
         * is over.
         */
        public Optional<CommonStructures.Point> takeStepOnGrid(List<List<GridCell>> grid) {
            CommonStructures.Point nextLocation = location.translateInDirection(direction);
            if (!nextLocation.isInsideGrid(grid)) {
                return Optional.empty();
            }
            while (nextLocation.indexGrid(grid) == GridCell.OBSTACLE || nextLocation.indexGrid(grid) == GridCell.OBSTACLE_SPECIAL) {
                turnRight();
                nextLocation = location.translateInDirection(direction);
                if (!nextLocation.isInsideGrid(grid)) {
                    return Optional.empty();
                }
            }
            location = nextLocation;
            return Optional.of(location);
        }

        private void turnRight() {
            direction = switch (direction) {
                case UP -> CommonStructures.Direction.RIGHT;
                case DOWN -> CommonStructures.Direction.LEFT;
                case LEFT -> CommonStructures.Direction.UP;
                case RIGHT -> CommonStructures.Direction.DOWN;
            };
        }

        public void visitCell(List<List<GridCell>> grid) {
            assert location.isInsideGrid(grid) :
                    "The guard cannot visit a cell that is outside of the grid.";
            grid.get(location.y()).set(location.x(), GridCell.VISITED);
        }
    }

    private enum GridCell {
        EMPTY, GUARD, OBSTACLE, OBSTACLE_SPECIAL, VISITED, NEW_OBSTRUCTION_LOCATION; 
        public static GridCell fromChar(char c) {
            return switch (c) {
                case '.' -> GridCell.EMPTY;
                case '#' -> GridCell.OBSTACLE;
                case '$' -> GridCell.OBSTACLE_SPECIAL;
                case '^' -> GridCell.GUARD;
                case 'O' -> GridCell.NEW_OBSTRUCTION_LOCATION;
                default ->
                        throw new IllegalArgumentException(("Unable to parse GridCell from char: "
                                                            + "%s").formatted(c));

            };
        }

        @Override
        public String toString() {
            return switch (this) {
                case EMPTY -> ".";
                case GUARD -> "^";
                case OBSTACLE -> "#";
                case OBSTACLE_SPECIAL -> "$";
                case VISITED -> "X";
                case NEW_OBSTRUCTION_LOCATION -> "O";
            };
        }
    }
}
