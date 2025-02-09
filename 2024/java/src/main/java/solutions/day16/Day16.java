package solutions.day16;

import solutions.Solution;
import solutions.Utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day16 implements Solution {
    @Override
    public void firstPart(String inputFile) {
        final var maze = readMazeMap(Utils.readInputAsStream(inputFile));

        System.out.println(Utils.toStringLineByLine(maze));

    }

    @Override
    public void secondPart(String inputFile) {

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
