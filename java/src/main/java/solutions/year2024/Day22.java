package solutions.year2024;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;
import solutions.Solution;
import solutions.Utils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day22 implements Solution {
    private static final int EVOLUTION_ITERATIONS = 2000;

    @Override
    public void firstPart(String inputFile) {
        List<SecretNumber> secretNumbers = readSecretNumbers(Utils.readInputAsStream(inputFile));


        int progressLogIterationInterval = 100;
        for (int j = 0; j < secretNumbers.size(); j++) {
            SecretNumber number = secretNumbers.get(j);
            System.out.println("Processing number: %s (%d/%d)".formatted(
                    number.initialValue,
                    j,
                    secretNumbers.size()
            ));
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
        // We need to create a map from the sequence of changes to the sell price and then pick
        // the best one
        List<SecretNumber> secretNumbers = readSecretNumbers(Utils.readInputAsStream(inputFile));

        Map<ChangeSequence, Map<SecretNumber, Integer>> sellPriceByChangeSequence = new HashMap<>();

        int progressLogIterationInterval = 100;
        for (int j = 0; j < secretNumbers.size(); j++) {
            SecretNumber number = secretNumbers.get(j);
            System.out.println("Processing number: %s (%d/%d)".formatted(
                    number.initialValue,
                    j,
                    secretNumbers.size()
            ));
            // First we populate the initial list of four changes
            List<Integer> initialChanges = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                int price = number.getPrice();
                number.evolve();
                int newPrice = number.getPrice();
                int change = newPrice - price;
                //System.out.println("%d : %d (%d) (evolutios: %d)".formatted(number.getValue(), newPrice, change, number.getEvolutionCount()));
                initialChanges.add(change);
            }
            ChangeSequence changeSequence = new ChangeSequence(initialChanges);
            sellPriceByChangeSequence.putIfAbsent(changeSequence, new HashMap<>());
            // First time we always put the number because it is the first time we are processing
            // this number that corresponds to a single buyer
            sellPriceByChangeSequence.get(changeSequence).put(number, number.getPrice());
            for (int i = 0; i < EVOLUTION_ITERATIONS - 4; i++) {
                int price = number.getPrice();
                number.evolve();
                int newPrice = number.getPrice();
                int change = newPrice - price;
                //System.out.println("%d : %d (%d) (evolutios: %d)".formatted(number.getValue(), newPrice, change, number.getEvolutionCount()));
                changeSequence = changeSequence.update(change);
                sellPriceByChangeSequence.putIfAbsent(changeSequence, new HashMap<>());
                // Here we only put if the number is not already there in the map.
                // The reason is that the monkey will sell immediately and then move to
                // the next buyer so if a given change sequence occurs twice for a given buyer,
                // we need to only use the first occurrence.
                sellPriceByChangeSequence.get(changeSequence)
                        .putIfAbsent(number, number.getPrice());

                if (i % progressLogIterationInterval == 0) {
                    System.out.println("Progress: (%d/%d)".formatted(i, EVOLUTION_ITERATIONS-4));
                }
            }
        }

        // Now we sum all prices from the sequences map to find the best one
        Map<ChangeSequence, Integer> totalGainMap = sellPriceByChangeSequence.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().values().stream().reduce(Integer::sum).get()
                ));

        //System.out.println(totalGainMap);

        List<Pair<ChangeSequence, Integer>> sortedTotalGainMap = totalGainMap.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                .toList();

        System.out.println(sortedTotalGainMap.getLast());
        System.out.println(sellPriceByChangeSequence.get(sortedTotalGainMap.getLast().getLeft()));

        System.out.println("The most bananas we can get is: %d".formatted(sortedTotalGainMap.getLast().getRight()));

    }

    private record ChangeSequence(List<Integer> changes) {
        ChangeSequence update(int next) {
            return new ChangeSequence(Stream.concat(changes.subList(1,4).stream(), Stream.of(next)).toList());
        }
    }

    private List<SecretNumber> readSecretNumbers(Stream<String> input) {
        return input.map(SecretNumber::fromString).toList();
    }

    @ToString
    @AllArgsConstructor
    private static class SecretNumber {
        final long initialValue;
        @Getter long value;
        @Getter int evolutionCount;

        static SecretNumber fromString(String input) {
            long value = Long.parseLong(input);
            return new SecretNumber(value, value, 0);
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
            evolutionCount++;
        }

        int getPrice() {
            return (int) (value % 10);
        }

        private void prune() {
            this.value %= 16777216;
        }

        private void mix(long mixingCandidate) {
            this.value ^= mixingCandidate;
        }
    }
}
