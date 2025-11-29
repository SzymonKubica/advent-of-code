package solutions.year2024;

import solutions.Solution;
import solutions.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day11 implements Solution {
    @Override
    public void firstPart(String inputFile) {
        long iterations = 25;
        transformForIterations(iterations, inputFile);
    }

    @Override
    public void secondPart(String inputFile) {
        long iterations = 75;
        transformForIterations(iterations, inputFile);
    }

    private void transformForIterations(long iterations, String inputFile) {
        List<MagicStone> stones = readStones(Utils.readInputAsStream(inputFile));

        System.out.println(printStones(stones));

        Map<MagicStone, Long> duplicatesMap = stones.stream()
                .collect(Collectors.toMap(stone -> stone, _stone -> 1L));

        long totalStones = stones.size();
        for (long i = 0; i < iterations; i++) {
            duplicatesMap = duplicatesMap.entrySet()
                    .stream()
                    .map(entry -> entry.getKey()
                            .transform()
                            .stream()
                            .map(stone -> Map.entry(stone, entry.getValue()))
                            .toList())
                    .flatMap(List::stream)
                    .collect(Collectors.groupingBy(Map.Entry::getKey))
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey,
                                              entry -> entry.getValue()
                                                      .stream()
                                                      .map(Map.Entry::getValue)
                                                      .reduce(Long::sum)
                                                      .get()));
            long stonesBefore = totalStones;
            totalStones = duplicatesMap.values().stream().reduce(Long::sum).get();
            System.out.println(("Performing iteration: (%d/%d), stones before: %d, stones after: "
                                + "%d").formatted(
                    i + 1,
                    iterations,
                    stonesBefore,
                    totalStones));

        }

        System.out.printf("Total stones after %d iterations: %d\n", iterations, totalStones);
    }


    private List<MagicStone> readStones(Stream<String> input) {
        return input.map(str -> Arrays.stream(str.split(" ")).toList())
                .flatMap(List::stream)
                .filter(Predicate.not(String::isBlank))
                .map(String::trim)
                .map(x -> new MagicStone(Long.parseLong(x)))
                .toList();
    }

    private String printStones(List<MagicStone> stones) {
        return stones.stream().map(MagicStone::toString).collect(Collectors.joining(" "));
    }

    record MagicStone(long value) {
        public List<MagicStone> transform() {
            if (value == 0) {
                return List.of(new MagicStone(1));
            }

            String valueStr = String.valueOf(value);
            if (valueStr.length() % 2 == 0) {
                int midpoint = valueStr.length() / 2;
                long firstPart = Long.parseLong(valueStr.substring(0, midpoint));
                long secondPart = Long.parseLong(valueStr.substring(midpoint));
                return List.of(new MagicStone(firstPart), new MagicStone(secondPart));
            }

            return List.of(new MagicStone(2024 * value));
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}
