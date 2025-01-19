package solutions.day3;

import solutions.Solution;
import solutions.Utils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class Day3 implements Solution {
    @Override
    public void firstPart(String inputFile) {

        List<String> inputLines = Utils.readInputAsStream(inputFile).toList();

        assert (inputLines.size() == 1) : "Program memory input for this puzzle should be a " +
                "single long line.";
        String programMemory = inputLines.get(0);
        System.out.println(programMemory);

    }

    @Override
    public void secondPart(String inputFile) {

    }

    /**
     * Record maintaining information about the current state of parsing the
     * program memory. The idea is that while parsing we use functions that output
     * tokens and shift the index in the parsing state to avoid
     * copying / modifying the parsed list.
     */
    static final class ParsingState {
        private final List<Character> programMemory;
        public int index;

        /**
         * @param programMemory Program memory that we are trying to parse.
         * @param index         Current index at which we are parsing.
         */
        ParsingState(List<Character> programMemory, int index) {
            this.programMemory = programMemory;
            this.index = index;
        }

        public String getPrefix(int prefixLength) {
            assert (programMemory.size() >= prefixLength) :
                    "String needs to be at least of the " + "size of the requested prefix";
            return programMemory.subList(index, index + prefixLength)
                    .stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(""));

        }
    }

    private sealed interface Token permits Mul, LeftParen, RightParen, Comma, ShortNumber {
        Optional<Token> tryToParse(ParsingState state);
    }

    private final static class Mul implements Token {
        @Override
        public Optional<Token> tryToParse(ParsingState state) {
            if (state.getPrefix(3).equals("mul")) {
                state.index += 3;
                return Optional.of(new Mul());
            }

            return Optional.empty();
        }
    }


    private final static class LeftParen implements Token {
        @Override
        public Optional<Token> tryToParse(ParsingState state) {
            if (state.getPrefix(1).equals("(")) {
                state.index += 1;
                return Optional.of(new LeftParen());
            }
            return Optional.empty();
        }
    }

    private final static class RightParen implements Token {
        @Override
        public Optional<Token> tryToParse(ParsingState state) {
            if (state.getPrefix(1).equals(")")) {
                state.index += 1;
                return Optional.of(new RightParen());
            }
            return Optional.empty();
        }
    }

    private final static class Comma implements Token {
        @Override
        public Optional<Token> tryToParse(ParsingState state) {
            if (state.getPrefix(1).equals(",")) {
                state.index += 1;
                return Optional.of(new Comma());
            }
            return Optional.empty();
        }
    }

    private final static class ShortNumber implements Token {
        @Override
        public Optional<Token> tryToParse(ParsingState state) {
            return Optional.empty();
        }
    }
}
