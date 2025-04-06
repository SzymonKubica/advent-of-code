package solutions.day22;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import solutions.Solution;
import solutions.Utils;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class Day22 implements Solution {
    private static final int EVOLUTION_ITERATIONS = 2000;

    @Override
    public void firstPart(String inputFile) {
        List<SecretNumber> secretNumbers = readSecretNumbers(Utils.readInputAsStream(inputFile));


        int progressLogIterationInterval = 100;
        for (int j = 0; j < secretNumbers.size(); j++) {
            SecretNumber number = secretNumbers.get(j);
            System.out.println("Processing number: %s (%d/%d)".formatted(number.initialValue, j, secretNumbers.size()));
            for (int i = 0; i < EVOLUTION_ITERATIONS; i++) {
                number.evolve();
                if (i % progressLogIterationInterval == 0) {
                    System.out.println("Progress: (%d/%d)".formatted(i, EVOLUTION_ITERATIONS));
                }
            }
        }

        for (SecretNumber number : secretNumbers) {
            System.out.println(number.showCurrentState());
        }

        long secretNumberCurrentValueSum = secretNumbers.stream().map(SecretNumber::getValue)
                .reduce(Long::sum).get();

        System.out.println("Adding up the 2000th new secret number for each buyer produces %d.".formatted(
                secretNumberCurrentValueSum));


    }

    @Override
    public void secondPart(String inputFile) {
        // We need to create a map from the sequence of changes to

    }

    private List<SecretNumber> readSecretNumbers(Stream<String> input) {
        return input.map(SecretNumber::fromString).toList();
    }

    @ToString
    @AllArgsConstructor
    private static class SecretNumber {
        final long initialValue;
        @Getter long value;

        static SecretNumber fromString(String input) {
            long value = Long.parseLong(input);
            return new SecretNumber(value, value);
        }

        String showCurrentState() {
            return "%d: %d".formatted(initialValue, value);
        }


        void evolve() {
            // First evolution stage
            long toMix1 = value * 64;
            this.mix(toMix1);
            this.prune();
            // Second evolution stage
            long toMix2 = value / 32;
            this.mix(toMix2);
            this.prune();
            // Third evolution stage
            long toMix3 = value * 2048;
            this.mix(toMix3);
            this.prune();
        }

        private void prune() {
            this.value %= 16777216;
        }

        private void mix(long mixingCandidate) {
            this.value ^= mixingCandidate;
        }
    }
}
