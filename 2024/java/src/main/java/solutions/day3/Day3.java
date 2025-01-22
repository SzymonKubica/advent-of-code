package solutions.day3;

import lombok.extern.java.Log;
import org.checkerframework.checker.units.qual.C;
import solutions.Solution;
import solutions.Utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Log
public class Day3 implements Solution {
    @Override
    public void firstPart(String inputFile) {

        List<String> inputLines = Utils.readInputAsStream(inputFile).toList();
        final var state = initializeParsingState(inputLines);

        // this design is bad.
        List<Token> tokensAvailable = List.of(new Mul(),
                                            new RightParen(),
                                            new LeftParen(),
                                            new Comma(),
                                            new ShortNumber(0),
                                            new Bad());

        List<Token> parsedTokens = parseTokens(state, tokensAvailable);

        int mulPatternLength = 6;
        int output = 0;
        for (int i = 0; i < parsedTokens.size() + 1 - mulPatternLength; ) {
            if (multiplicationFound(parsedTokens, i)) {
                int firstArgument = ((ShortNumber) parsedTokens.get(i + 2)).value;
                int secondArgument = ((ShortNumber) parsedTokens.get(i + 4)).value;
                output += firstArgument * secondArgument;
                i += mulPatternLength;
                continue;
            }
            i += 1;
        }
        log.info("Output: %d".formatted(output));
    }

    @Override
    public void secondPart(String inputFile) {

        List<String> inputLines = Utils.readInputAsStream(inputFile).toList();
        final var state = initializeParsingState(inputLines);

        // this design is bad.
        List<Token> tokensAvailable = List.of(new Mul(),
                                              new RightParen(),
                                              new LeftParen(),
                                              new Comma(),
                                              new ShortNumber(0),
                                              new DoNot(),
                                              new Do(),
                                              new Bad());

        List<Token> parsedTokens = parseTokens(state, tokensAvailable);

        int mulPatternLength = 6;
        int doDoNotPatternLength = 3;
        int output = 0;
        boolean areMultiplicationsEnabled = true;
        for (int i = 0; i < parsedTokens.size() + 1 - mulPatternLength; ) {
            if (multiplicationFound(parsedTokens, i)) {
                int firstArgument = ((ShortNumber) parsedTokens.get(i + 2)).value;
                int secondArgument = ((ShortNumber) parsedTokens.get(i + 4)).value;
                if (areMultiplicationsEnabled) {
                    output += firstArgument * secondArgument;
                }
                i += mulPatternLength;
                continue;
            } else if (doFound(parsedTokens, i)) {
                areMultiplicationsEnabled = true;
                i += doDoNotPatternLength;
                continue;
            } else if (doNotFound(parsedTokens, i)) {
                areMultiplicationsEnabled = false;
                i += doDoNotPatternLength;
                continue;
            }
            i += 1;
        }
        log.info("Output: %d".formatted(output));
    }


    private static boolean multiplicationFound(List<Token> parsedTokens, int index) {
        return parsedTokens.get(index) instanceof Mul
        && parsedTokens.get(index + 1) instanceof LeftParen
        && parsedTokens.get(index + 2) instanceof ShortNumber
        && parsedTokens.get(index + 3) instanceof Comma
        && parsedTokens.get(index + 4) instanceof ShortNumber
        && parsedTokens.get(index + 5) instanceof RightParen;
    }

    private boolean doNotFound(List<Token> parsedTokens, int index) {
        return parsedTokens.get(index) instanceof DoNot
               && parsedTokens.get(index + 1) instanceof LeftParen
               && parsedTokens.get(index + 2) instanceof RightParen;
    }

    private boolean doFound(List<Token> parsedTokens, int index) {
        return parsedTokens.get(index) instanceof Do
               && parsedTokens.get(index + 1) instanceof LeftParen
               && parsedTokens.get(index + 2) instanceof RightParen;
    }

    private static List<Token> parseTokens(ParsingState state, List<Token> tokensToParse) {
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
        log.info("Program memory after parsing: \n%s\n".formatted(programMemoryAfterParsing));
        return parsedTokens;
    }


    private static ParsingState initializeParsingState(List<String> inputLines) {
        assert (inputLines.size() == 1) :
                "Program memory input for this puzzle should be a " + "single long line.";
        String programMemory = String.join("", inputLines);
        log.info("Read program memory: \n" + programMemory);

        List<Character> programMemoryChars = new ArrayList<>();
        for (byte c : programMemory.getBytes(StandardCharsets.UTF_8)) {
            programMemoryChars.add((char) c);
        }

        return new ParsingState(programMemoryChars, 0);
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
                    "The index needs to be within the program memory";
            return programMemory.get(index);
        }

        public char popShift() {
            assert (index <= programMemory.size()) :
                    "The index needs to be within the program memory";
            return programMemory.get(index++);
        }

        public int charsLeft() {
            return programMemory.size() - (index + 1);
        }
    }

    private sealed interface Token
            permits Mul, LeftParen, RightParen, Comma, ShortNumber, Bad, Do, DoNot {
        Optional<Token> tryToParse(ParsingState state);
    }

    private static final class Mul implements Token {
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


    private static final class LeftParen implements Token {
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

    private static final class RightParen implements Token {
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

    private static final class Comma implements Token {
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

    private static final class Bad implements Token {
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

    private static final class ShortNumber implements Token {
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

            return Optional.of(new ShortNumber(Integer.parseInt(numberDigits.stream()
                                                                        .map(String::valueOf)
                                                                        .collect(Collectors.joining(
                                                                                "")))));
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    private static final class Do implements Token {
        @Override
        public Optional<Token> tryToParse(ParsingState state) {
            if (state.charsLeft() >= 2 && state.getPrefix(2).equals("do")) {
                state.index += 2;
                return Optional.of(new Do());
            }

            return Optional.empty();
        }

        @Override
        public String toString() {
            return "do";
        }
    }

    private static final class DoNot implements Token {
        @Override
        public Optional<Token> tryToParse(ParsingState state) {
            if (state.charsLeft() >= 5 && state.getPrefix(5).equals("don't")) {
                state.index += 5;
                return Optional.of(new DoNot());
            }

            return Optional.empty();
        }

        @Override
        public String toString() {
            return "don't";
        }
    }
}
