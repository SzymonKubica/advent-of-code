package solutions.day6;

import jdk.jshell.execution.Util;
import lombok.AllArgsConstructor;
import solutions.CommonStructures;
import solutions.Solution;
import solutions.Utils;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
        return grid.stream()
                .map(row -> row.stream().map(Objects::toString).collect(Collectors.joining("")))
                .collect(Collectors.joining("\n"));
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
            while (nextLocation.indexGrid(grid) == GridCell.OBSTACLE) {
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
        EMPTY, GUARD, OBSTACLE, VISITED;

        public static GridCell fromChar(char c) {
            return switch (c) {
                case '.' -> GridCell.EMPTY;
                case '#' -> GridCell.OBSTACLE;
                case '^' -> GridCell.GUARD;
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
                case VISITED -> "X";
            };
        }
    }
}
