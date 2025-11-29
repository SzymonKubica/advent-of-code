package solutions.year2024;

import org.apache.commons.lang3.tuple.Pair;
import solutions.Solution;
import solutions.Utils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day19 implements Solution {
    @Override
    public void firstPart(String inputFile) {
        final var patternsAndDesigns = parseTowelPatternsAndRequiredDesigns(Utils.readInputAsStream(inputFile));
        List<Pattern> patterns = patternsAndDesigns.getLeft();
        List<Pattern> designs = patternsAndDesigns.getRight();
        System.out.println("Patterns: ");
        System.out.println(patterns);
        System.out.println("Designs: ");
        System.out.println(designs);

        int possibleDesigns = 0;

        CACHE = new HashMap<>();
        for (int i = 0; i < designs.size(); i++) {
            Pattern design = designs.get(i);
            System.out.println("Checking if design %s is possible. Progress: (%s/%s)".formatted(design, i, designs.size()));
            if (isDesignPossible(design, patterns)) {
                System.out.println("Design can be assembled");
                possibleDesigns += 1;
            }
        }

        System.out.println("Total possible designs: %s".formatted(possibleDesigns));

    }

    @Override
    public void secondPart(String inputFile) {
        final var patternsAndDesigns = parseTowelPatternsAndRequiredDesigns(Utils.readInputAsStream(inputFile));
        List<Pattern> patterns = patternsAndDesigns.getLeft();
        List<Pattern> designs = patternsAndDesigns.getRight();
        System.out.println("Patterns: ");
        System.out.println(patterns);
        System.out.println("Designs: ");
        System.out.println(designs);

        long totalSolutions = 0;

        SOLUTION_COUNT_CACHE = new HashMap<>();
        for (int i = 0; i < designs.size(); i++) {
            Pattern design = designs.get(i);
            System.out.println("Checking if design %s is possible. Progress: (%s/%s)".formatted(design, i, designs.size()));
            long solutionCount = countDesignsPossible(design, patterns);
            if (solutionCount > 0) {
                System.out.println("Design can be assembled");
                totalSolutions += solutionCount;
            }
        }

        System.out.println("Total possible ways to assemble designs: %s".formatted(totalSolutions));

    }

    private boolean isDesignPossible(Pattern design, List<Pattern> availablePatterns) {
        return isStringDesignPossible(design.toString(), availablePatterns.stream().map(Pattern::toString).toList());
    }

    private long countDesignsPossible(Pattern design, List<Pattern> availablePatterns) {
        return countStringDesignsPossible(design.toString(), availablePatterns.stream().map(Pattern::toString).toList());
    }

    Map<String, Boolean> CACHE = new HashMap<>();
    Map<String, Long> SOLUTION_COUNT_CACHE = new HashMap<>();

    private boolean isStringDesignPossible(String design, List<String> availablePatterns) {
        //System.out.println("Checking if design: %s can be formed using %s".formatted(design, availablePatterns));

        if (CACHE.containsKey(design)) {
            return CACHE.get(design);
        }
        boolean output = false;

        for (String pattern : availablePatterns) {
            if (pattern.length() <= design.length() && design.startsWith(pattern)) {
                if (pattern.equals(design)) {
                    CACHE.put(design, true);
                    return true;
                }
                output |= isStringDesignPossible(design.substring(pattern.length()), availablePatterns);
                // We short-circuit if a match is found.
                if (output) {
                    CACHE.put(design, true);
                    return true;
                }
            }
        }
        CACHE.put(design, output);
        return output;
    }

    private long countStringDesignsPossible(String design, List<String> availablePatterns) {
        //System.out.println("Checking if design: %s can be formed using %s".formatted(design, availablePatterns));

        if (SOLUTION_COUNT_CACHE.containsKey(design)) {
            return SOLUTION_COUNT_CACHE.get(design);
        }
        long output = 0;

        for (String pattern : availablePatterns) {
            if (pattern.equals(design)) {
                output += 1;
            }
            if (pattern.length() <= design.length() && design.startsWith(pattern)) {
                output += countStringDesignsPossible(design.substring(pattern.length()), availablePatterns);
            }
        }
        SOLUTION_COUNT_CACHE.put(design, output);
        return output;
    }


    private Pair<List<Pattern>, List<Pattern>> parseTowelPatternsAndRequiredDesigns(Stream<String> input) {
        String allInput = input.collect(Collectors.joining("\n"));

        String[] patternsAndDesigns = allInput.split("\n\n");

        List<Pattern> patterns = Arrays.stream(patternsAndDesigns[0].split(", "))
                .map(String::trim)
                .filter(Predicate.not(String::isBlank))
                .map(Pattern::fromString)
                .toList();

        List<Pattern> designs = Arrays.stream(patternsAndDesigns[1].split("\n"))
                .map(String::trim)
                .filter(Predicate.not(String::isBlank))
                .map(Pattern::fromString)
                .toList();

        return Pair.of(patterns, designs);
    }

    private record Pattern(List<TowelStripeColor> colors) {
        static Pattern fromString(String input) {
            return new Pattern(input.chars()
                                       .mapToObj(c -> String.valueOf((char) c))
                                       .map(TowelStripeColor::parseFromString)
                                       .map(Optional::get)
                                       .toList());
        }
        @Override
        public String toString() {
            return colors.stream().map(c -> c.representation).collect(Collectors.joining(""));
        }
    }

    private enum TowelStripeColor {
        WHITE("w"), BLUE("u"), BLACK("b"), RED("r"), GREEN("g");

        private final String representation;

        TowelStripeColor(String representation) {
            this.representation = representation;
        }

        static Optional<TowelStripeColor> parseFromString(String str) {
            List<TowelStripeColor> matchedColors = Arrays.stream(TowelStripeColor.values())
                    .filter(color -> color.representation.equals(str))
                    .toList();

            return matchedColors.isEmpty() ? Optional.empty() : Optional.of(matchedColors.get(0));
        }
    }
}
