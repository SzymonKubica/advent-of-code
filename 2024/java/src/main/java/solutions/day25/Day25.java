package solutions.day25;

import solutions.Solution;
import solutions.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Day25 implements Solution {
    @Override
    public void firstPart(String inputFile) {
        final List<LockPart> locksAndKeys = readInput(inputFile);

        final List<LockPart> locks = locksAndKeys.stream().filter(Predicate.not(LockPart::isKey))
                .toList();
        final List<LockPart> keys = locksAndKeys.stream().filter(LockPart::isKey).toList();

        System.out.printf("There are %d locks.%n", locks.size());
        System.out.printf("There are %d keys.%n", keys.size());

        System.out.println(locks);
        System.out.println(keys);

        int matchingPairs = 0;
        for (final var lock : locks) {
            for (final var key : keys) {
                if (LockPart.lockAndKeyFitTogether(lock, key)) {
                    matchingPairs++;
                }
            }
        }

        System.out.println("There are %d unique lock-key matching pairs.".formatted(matchingPairs));
    }

    private List<LockPart> readInput(String inputFile) {
        String allInput = Utils.readInputAsStream(inputFile).collect(Collectors.joining("\n"));
        return Arrays.stream(allInput.split("\n\n")).map(LockPart::fromString).toList();
    }

    @Override
    public void secondPart(String inputFile) {

    }

    private record LockPart(List<Integer> heights, boolean isKey) {
        private static int LOCK_HEIGHT = 7;
        private static int LOCK_WIDTH = 5;

        static LockPart fromString(String input) {
            List<List<LockCrossSectionCell>> insideOfTheLock = parseLockInternals(input);

            assert insideOfTheLock.size() == LOCK_HEIGHT
                   && insideOfTheLock.get(0).size() == LOCK_WIDTH :
                    "Lock dimensions need to match %sx%d".formatted(LOCK_WIDTH, LOCK_HEIGHT);

            boolean isKey = insideOfTheLock.get(0).get(0) == LockCrossSectionCell.EMPTY;

            List<Integer> heights = new ArrayList<>();
            if (isKey) {
                // For keys, we need to iterate from the top
                for (int x = 0; x < insideOfTheLock.get(0).size(); x++) {
                    for (int y = 0; y < insideOfTheLock.size(); y++) {
                        LockCrossSectionCell curr = insideOfTheLock.get(y).get(x);
                        if (curr == LockCrossSectionCell.LOCK_PART) {
                            // The extra -1 is because we have the convention that the bottom of
                            // the key will be always full
                            // so we start counting from 1.
                            heights.add(LOCK_HEIGHT - y - 1);
                            break;
                        }
                    }
                }
            } else {
                // For locks, we need to iterate from the top
                for (int x = 0; x < insideOfTheLock.get(0).size(); x++) {
                    for (int y = insideOfTheLock.size() - 1; y >= 0; y--) {
                        LockCrossSectionCell curr = insideOfTheLock.get(y).get(x);
                        if (curr == LockCrossSectionCell.LOCK_PART) {
                            // The extra -1 is because we have the convention that the bottom of
                            // the key will be always full
                            // so we start counting from 1.
                            heights.add(y);
                            break;
                        }
                    }
                }
            }

            return new LockPart(heights, isKey);
        }

        @Override
        public String toString() {
            return "%s: %s".formatted(isKey ? "Key" : "Lock", heights);
        }

        private static List<List<LockCrossSectionCell>> parseLockInternals(String input) {
            return input.lines().map(line -> line.chars().mapToObj(c -> String.valueOf((char) c))
                    .map(LockCrossSectionCell::fromString).toList()).toList();
        }

        static boolean lockAndKeyFitTogether(LockPart lock, LockPart key) {
            assert !lock.isKey() : "The first argument into this function has to be a lock.";
            assert key.isKey() : "The second argument into this function has to be a key.";

            for (int i = 0; i < LOCK_WIDTH; i++) {
                if (lock.heights.get(i) + key.heights.get(i) >= LOCK_HEIGHT - 1) {
                    System.out.println("Overlap detected in %d column. %s %s".formatted(
                            i + 1,
                            lock,
                            key
                    ));
                    return false;
                }
            }

            return true;
        }
    }

    private enum LockCrossSectionCell {
        EMPTY("."),
        LOCK_PART("#");

        private String representation;

        LockCrossSectionCell(String representation) {
            this.representation = representation;
        }

        static LockCrossSectionCell fromString(String str) {
            return switch (str) {
                case "." -> EMPTY;
                case "#" -> LOCK_PART;
                default -> throw new IllegalStateException("Unexpected value: " + str);
            };
        }
    }

    private record Key(List<Integer> latchSizes) {
    }
}