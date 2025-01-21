package solutions.day3;

import org.checkerframework.checker.units.qual.C;
import solutions.Solution;
import solutions.Utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
        String programMemory = String.join("", inputLines);
        System.out.println(programMemory);

        List<Character> programMemoryChars = new ArrayList<>();
        for (byte c : programMemory.getBytes(StandardCharsets.UTF_8)) {
            programMemoryChars.add((char) c);
        }

        final var state = new ParsingState(programMemoryChars, 0);

        // this design is bad.
        List<Token> tokensToParse = List.of(new Mul(), new RightParen(), new LeftParen(),
                new Comma(), new ShortNumber(0), new Bad());

        List<Token> parsedTokens = new ArrayList<>();

        while (!state.noMoreToParse()) {
            for (Token tokenType : tokensToParse) {
                final var maybeToken = tokenType.tryToParse(state);
                if (maybeToken.isPresent()) {
                    parsedTokens.add(maybeToken.get());
                    break;
                }
            }
        }

        String programMemoryAfterParsing = parsedTokens.stream()
                .map(Objects::toString)
                .collect(Collectors.joining(""));

        System.out.printf("Program memory after parsing: \n%s\n", programMemoryAfterParsing);

        int mulPatternLength = 6;
        long output = 0;
        for (int i = 0; i < parsedTokens.size() + 1 - mulPatternLength; ) {
            if (parsedTokens.get(i) instanceof Mul && parsedTokens.get(
                    i + 1) instanceof LeftParen && parsedTokens.get(
                    i + 2) instanceof ShortNumber s1 && parsedTokens.get(
                    i + 3) instanceof Comma && parsedTokens.get(
                    i + 4) instanceof ShortNumber s2 && parsedTokens.get(
                    i + 5) instanceof RightParen) {
                output += (((long) s1.value) * ((long) s2.value));
                System.out.printf("mul(%d,%d)\n", s1.value, s2.value);
                i += 6;
                continue;
            }
            i += 1;
        }
        System.out.printf("Output: %d", output);


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

        public boolean noMoreToParse() {
            return index >= programMemory.size();
        }

        public char peekNext() {
            assert (index <= programMemory.size()) :
                    "The index needs to be within the program " + "memory";
            return programMemory.get(index);
        }

        public char popShift() {
            assert (index <= programMemory.size()) :
                    "The index needs to be within the program " + "memory";
            return programMemory.get(index++);
        }

        public int charsLeft() {
            return programMemory.size() - (index + 1);
        }
    }

    private sealed interface Token permits Mul, LeftParen, RightParen, Comma, ShortNumber, Bad {
        Optional<Token> tryToParse(ParsingState state);
    }

    private final static class Mul implements Token {
        @Override
        public Optional<Token> tryToParse(ParsingState state) {
            if (state.charsLeft() >= 3 && state.getPrefix(3).equals("mul")) {
                state.index += 3;
                return Optional.of(new Mul());
            }

            return Optional.empty();
        }

        @Override
        public String toString() {
            return "mul";
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

        @Override
        public String toString() {
            return "(";
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

        @Override
        public String toString() {
            return ")";
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

        @Override
        public String toString() {
            return ",";
        }
    }

    private final static class Bad implements Token {
        @Override
        public Optional<Token> tryToParse(ParsingState state) {
            if (state.noMoreToParse()) {
                return Optional.empty();
            }
            state.index += 1;
            return Optional.of(new Bad());
        }

        @Override
        public String toString() {
            return "#";
        }
    }

    private final static class ShortNumber implements Token {
        final int value;

        private ShortNumber(int value) {
            this.value = value;
        }

        @Override
        public Optional<Token> tryToParse(ParsingState state) {
            List<Character> numberDigits = new ArrayList<>();
            while (state.charsLeft() >= 1 && numberDigits.size() < 3) {
                char next = state.peekNext();
                if (!Character.isDigit(next)) {
                    break;
                }
                numberDigits.add(state.popShift());
            }

            if (numberDigits.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(new ShortNumber(Integer.parseInt(
                    numberDigits.stream().map(String::valueOf).collect(Collectors.joining("")))));
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}
