package solutions.day6;

import jdk.jshell.execution.Util;
import solutions.Solution;
import solutions.Utils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day6 implements Solution {
    @Override
    public void firstPart(String inputFile) {
        final var grid = parseFromInputStream(Utils.readInputAsStream(inputFile));
        System.out.println(printGrid(grid));
    }

    @Override
    public void secondPart(String inputFile) {

    }

    private List<List<GridCell>> parseFromInputStream(Stream<String> input) {
        return input.map(line -> line.chars().mapToObj(c -> GridCell.fromChar((char) c)).toList())
                .toList();
    }

    private String printGrid(List<List<GridCell>> grid) {
        return grid.stream()
                .map(row -> row.stream().map(Objects::toString).collect(Collectors.joining("")))
                .collect(Collectors.joining("\n"));

    }

    private enum GridCell {
        EMPTY, GUARD, OBSTACLE;

        public static GridCell fromChar(char c) {
            return switch (c) {
                case '.' -> GridCell.EMPTY;
                case '#' -> GridCell.OBSTACLE;
                case '^' -> GridCell.GUARD;
                default ->
                        throw new IllegalArgumentException(("Unable to parse GridCell from char: "
                                                            + "%s").formatted(
                                c));

            };
        }

        @Override
        public String toString() {
            return switch (this) {
                case EMPTY -> ".";
                case GUARD -> "^";
                case OBSTACLE -> "#";
            };
        }
    }
}
