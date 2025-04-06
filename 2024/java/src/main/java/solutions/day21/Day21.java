package solutions.day21;

import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import solutions.Solution;
import solutions.Utils;
import solutions.common.Point;

import javax.naming.ldap.Control;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day21 implements Solution {
    @Override
    public void firstPart(String inputFile) {
        List<Code> requiredCodes = readInput(Utils.readInputAsStream(inputFile));

        System.out.println("Read codes required to open the door:");
        System.out.println(Utils.toStringLineByLine(requiredCodes));

        List<ControllerInput> firstRobotInputs = requiredCodes.stream()
                .map(code -> findRequiredKeypadSequences(
                        code.buttonSequence,
                        DoorKeypadButton.ACTION.locationOnKeypad,
                        DoorKeypadButton.GAP.locationOnKeypad
                )).toList();
        System.out.println("Inputs required to enter into the first robot: ");
        System.out.println(Utils.toStringLineByLine(firstRobotInputs));

        List<ControllerInput> secondRobotInputs = firstRobotInputs.stream()
                .map(code -> findRequiredKeypadSequences(
                        code.buttonSequence,
                        ControllerKeyPadButton.ACTION.locationOnKeypad,
                        ControllerKeyPadButton.GAP.locationOnKeypad
                )).toList();

        System.out.println("Inputs required to enter into the second robot: ");
        System.out.println(Utils.toStringLineByLine(secondRobotInputs));


        List<ControllerInput> thirdRobotInputs = secondRobotInputs.stream()
                .map(code -> findRequiredKeypadSequences(
                        code.buttonSequence,
                        ControllerKeyPadButton.ACTION.locationOnKeypad,
                        ControllerKeyPadButton.GAP.locationOnKeypad
                )).toList();
        System.out.println("Inputs required to enter into the third robot: ");
        System.out.println(Utils.toStringLineByLine(thirdRobotInputs));


        int totalCodesComplexity = 0;
        for (int i = 0; i < requiredCodes.size(); i++) {
            Code code = requiredCodes.get(i);
            ControllerInput input = thirdRobotInputs.get(i);
            System.out.printf("%s: %s%n", code, input);
            int numericCodePart = Integer.parseInt(code.toString().substring(
                    0,
                    code.toString().length() - 1
            ));
            int shortestSequenceLength = input.buttonSequence.size();
            int complexity = numericCodePart * shortestSequenceLength;
            System.out.printf(
                    "Code complexity: %d * %d = %d%n",
                    shortestSequenceLength,
                    numericCodePart,
                    complexity
            );
            totalCodesComplexity += complexity;
        }

        System.out.printf("Total complexity: %d", totalCodesComplexity);


    }

    private ControllerInput findRequiredKeypadSequencesCacheChunks(
            List<ControllerKeyPadButton> buttonsToPress,
            Point initialLocation,
            Point gapLocation,
            Map<String, List<ControllerKeyPadButton>> cache
    ) {
        // We need to optimize this as the lists are getting too unwieldy
        // the idea is to operate on strings and only convert into the
        // nice button representation on cache miss.
        List<ControllerKeyPadButton> requiredSeq = new ArrayList<>();

        int cacheChunkSize = 100;
        Point currentLocation = initialLocation;
        for (int i = 0; i < buttonsToPress.size(); i += cacheChunkSize) {
            List<ControllerKeyPadButton> currentChunk = buttonsToPress.subList(
                    i,
                    Math.min(
                            i
                            + cacheChunkSize,
                            buttonsToPress.size()
                    )
            );
            String representation = new ControllerInput(currentChunk).toString();

            if (cache.containsKey(representation)) {
                requiredSeq.addAll(cache.get(representation));
            } else {
                List<ControllerKeyPadButton> subOutput = new ArrayList<>();
                for (Button button : currentChunk) {
                    Point translationVector = button.getLocationOnKeypad()
                            .difference(currentLocation);
                    // If the gap on the keypad is on our usual path of
                    // first going horizontally and then vertically,
                    // we need to flip the order
                    if (currentLocation.y() == gapLocation.y()
                        && button.getLocationOnKeypad().x() == gapLocation.x()) {
                        subOutput.addAll(handleVerticalDisplacement(translationVector));
                        subOutput.addAll(handleHorizontalDisplacement(translationVector));
                        subOutput.add(ControllerKeyPadButton.ACTION);
                    } else if (currentLocation.x() == gapLocation.x()
                               && button.getLocationOnKeypad().y() == gapLocation.y()) {
                        subOutput.addAll(handleHorizontalDisplacement(translationVector));
                        subOutput.addAll(handleVerticalDisplacement(translationVector));
                        subOutput.add(ControllerKeyPadButton.ACTION);
                    } else {
                        // Here we need to pick the better approach. For now, we simply default
                        // to the
                        // second one
                        // if we are going left and then down it is faster to do the left way first
                        // not sure why that is the case
                        if (translationVector.x() < 0) {
                            subOutput.addAll(handleHorizontalDisplacement(translationVector));
                            subOutput.addAll(handleVerticalDisplacement(translationVector));
                        } else {
                            subOutput.addAll(handleVerticalDisplacement(translationVector));
                            subOutput.addAll(handleHorizontalDisplacement(translationVector));
                        }
                        subOutput.add(ControllerKeyPadButton.ACTION);
                    }


                    currentLocation = button.getLocationOnKeypad();
                }

                cache.put(representation, subOutput);
                requiredSeq.addAll(subOutput);
            }
        }
        return new ControllerInput(requiredSeq);
    }

    private <T extends Button> ControllerInput findRequiredKeypadSequences(
            List<T> buttonsToPress,
            Point initialLocation,
            Point gapLocation
    ) {
        List<ControllerKeyPadButton> requiredSeq = new ArrayList<>();

        Point currentLocation = initialLocation;
        for (Button button : buttonsToPress) {
            Point translationVector = button.getLocationOnKeypad().difference(currentLocation);

            // If the gap on the keypad is on our usual path of
            // first going horizontally and then vertically,
            // we need to flip the order
            if (currentLocation.y() == gapLocation.y()
                && button.getLocationOnKeypad().x() == gapLocation.x()) {
                requiredSeq.addAll(handleVerticalDisplacement(translationVector));
                requiredSeq.addAll(handleHorizontalDisplacement(translationVector));
                requiredSeq.add(ControllerKeyPadButton.ACTION);
            } else if (currentLocation.x() == gapLocation.x()
                       && button.getLocationOnKeypad().y() == gapLocation.y()) {
                requiredSeq.addAll(handleHorizontalDisplacement(translationVector));
                requiredSeq.addAll(handleVerticalDisplacement(translationVector));
                requiredSeq.add(ControllerKeyPadButton.ACTION);
            } else {
                // Here we need to pick the better approach. For now, we simply default to the
                // second one
                // if we are going left and then down it is faster to do the left way first
                // not sure why that is the case
                if (translationVector.x() < 0) {
                    requiredSeq.addAll(handleHorizontalDisplacement(translationVector));
                    requiredSeq.addAll(handleVerticalDisplacement(translationVector));
                } else {
                    requiredSeq.addAll(handleVerticalDisplacement(translationVector));
                    requiredSeq.addAll(handleHorizontalDisplacement(translationVector));
                }
                requiredSeq.add(ControllerKeyPadButton.ACTION);
            }


            currentLocation = button.getLocationOnKeypad();
        }

        return new ControllerInput(requiredSeq);
    }

    private static List<ControllerKeyPadButton> handleVerticalDisplacement(
            Point translationVector
    ) {
        if (translationVector.y() < 0) {
            return Collections.nCopies(Math.abs(translationVector.y()), ControllerKeyPadButton.UP);
        } else {
            return Collections.nCopies(
                    Math.abs(translationVector.y()),
                    ControllerKeyPadButton.DOWN
            );
        }
    }

    private static List<ControllerKeyPadButton> handleHorizontalDisplacement(
            Point translationVector
    ) {

        if (translationVector.x() > 0) {
            return Collections.nCopies(translationVector.x(), ControllerKeyPadButton.RIGHT);
        } else {
            return Collections.nCopies(
                    Math.abs(translationVector.x()),
                    ControllerKeyPadButton.LEFT
            );
        }
    }

    @Override
    public void secondPart(String inputFile) {
        List<Code> requiredCodes = readInput(Utils.readInputAsStream(inputFile));

        System.out.println("Read codes required to open the door:");
        System.out.println(Utils.toStringLineByLine(requiredCodes));

        // It seems like by going forward we can barely manage to process a chain of 23 robots.
        // anything over that fails because of string builder capacity error.
        // I suspect we must have many duplicates, so instead of representing the input as a
        // sequence of moves,
        // we can represent it as a map from the key type to the number of times it needs to be
        // pressed.
        int numberOfRobots = 3;

        List<Map<ControllerKeyPadButton, Long>> keysPerCode = new ArrayList<>();
        for (Code code : requiredCodes) {
            // The first inputs need to be processed using the old version of the function to
            // get us into the world of keypad sequences. From then onwards we are operating on
            // cached sequences to speed up the process.
            // We try to process only a single key
            ControllerInput currentRobotInput = findRequiredKeypadSequences(
                    code.buttonSequence,
                    DoorKeypadButton.ACTION.locationOnKeypad,
                    DoorKeypadButton.GAP.locationOnKeypad
            );

            var countedUniqueKeyPresses = countUniqueKeyPresses(currentRobotInput);

            for (int i = 0; i < numberOfRobots - 1; i++) {
                countedUniqueKeyPresses = findRequiredKeypadSequencesMapCompaction(
                        countedUniqueKeyPresses,
                        ControllerKeyPadButton.ACTION.locationOnKeypad,
                        ControllerKeyPadButton.GAP.locationOnKeypad
                );

                System.out.println(("Processing key presses required to enter into the robot "
                                    + "number %d: ").formatted(i + 2));
            }
            keysPerCode.add(countedUniqueKeyPresses);
        }


        long totalCodesComplexity = 0;
        for (int i = 0; i < requiredCodes.size(); i++) {
            Code code = requiredCodes.get(i);
            int numericCodePart = Integer.parseInt(code.toString().substring(
                    0,
                    code.toString().length() - 1
            ));
            long shortestSequenceLength = keysPerCode.get(i).values().stream().reduce(Long::sum).get();
            long complexity = numericCodePart * shortestSequenceLength;
            System.out.printf(
                    "Code complexity: %d * %d = %d%n",
                    shortestSequenceLength,
                    numericCodePart,
                    complexity
            );
            totalCodesComplexity += complexity;
        }

        System.out.printf("Total complexity: %d", totalCodesComplexity);


    }

    private static Map<ControllerKeyPadButton, Long> countUniqueKeyPresses(ControllerInput currentRobotInput) {
        return currentRobotInput.buttonSequence.stream()
                .collect(Collectors.groupingBy(ControllerKeyPadButton::getRepresentation))
                .entrySet().stream().collect(Collectors.toMap(
                        entry -> ControllerKeyPadButton.fromString(entry.getKey()),
                        entry -> (long) entry.getValue().size()
                ));
    }

    private Map<ControllerKeyPadButton, Long> findRequiredKeypadSequencesMapCompaction(
            Map<ControllerKeyPadButton, Long> buttonsToPress,
            Point initialLocation,
            Point gapLocation
    ) {
        // In this approach we rely on the fact that producing each key press in the
        // of the same type will require the same key combination because we are generating
        // the optimal inputs. Because of this it makes no sense to process them one by one.
        // Rather, we construct a map from the unique controller inputs to the counts that they need
        // to be pressed. After we process all of them, we compact the map to restore the counts.

        // We store the required press counts in this list, at the end the list will
        // be compacted into the output map
        List<Pair<ControllerKeyPadButton, Long>> requiredPressCounts = new ArrayList<>();

        Point currentLocation = initialLocation;
        for (ControllerKeyPadButton button : buttonsToPress.keySet()) {
            long requiredButtonOccurrences = buttonsToPress.get(button);

            List<ControllerKeyPadButton> pressesForButton = new ArrayList<>();
            Point translationVector = button.getLocationOnKeypad().difference(currentLocation);
            // If the gap on the keypad is on our usual path of
            // first going horizontally and then vertically,
            // we need to flip the order
            if (currentLocation.y() == gapLocation.y()
                && button.getLocationOnKeypad().x() == gapLocation.x()) {
                pressesForButton.addAll(handleVerticalDisplacement(translationVector));
                pressesForButton.addAll(handleHorizontalDisplacement(translationVector));
                pressesForButton.add(ControllerKeyPadButton.ACTION);
            } else if (currentLocation.x() == gapLocation.x()
                       && button.getLocationOnKeypad().y() == gapLocation.y()) {
                pressesForButton.addAll(handleHorizontalDisplacement(translationVector));
                pressesForButton.addAll(handleVerticalDisplacement(translationVector));
                pressesForButton.add(ControllerKeyPadButton.ACTION);
            } else {
                // Here we need to pick the better approach. For now, we simply default
                // to the
                // second one
                // if we are going left and then down it is faster to do the left way first
                // not sure why that is the case
                if (translationVector.x() < 0) {
                    pressesForButton.addAll(handleHorizontalDisplacement(translationVector));
                    pressesForButton.addAll(handleVerticalDisplacement(translationVector));
                } else {
                    pressesForButton.addAll(handleVerticalDisplacement(translationVector));
                    pressesForButton.addAll(handleHorizontalDisplacement(translationVector));
                }
                pressesForButton.add(ControllerKeyPadButton.ACTION);
            }


            currentLocation = button.getLocationOnKeypad();
            final var keyPressCounts = countUniqueKeyPresses(new ControllerInput(pressesForButton));
            requiredPressCounts.addAll(keyPressCounts.entrySet().stream().map(entry -> Pair.of(
                    entry.getKey(),
                    requiredButtonOccurrences
                    * entry.getValue()
            )).toList());
        }

        return requiredPressCounts.stream().collect(Collectors.groupingBy(Pair::getLeft)).entrySet()
                .stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream().map(Pair::getRight).reduce(Long::sum)
                                .get()
                ));
    }

    private String findRequiredKeypadSequencesCacheChunksAsString(
            String buttonsToPress,
            Point initialLocation,
            Point gapLocation,
            Map<String, String> cache
    ) {
        // We need to optimize this as the lists are getting too unwieldy
        // the idea is to operate on strings and only convert into the
        // nice button representation on cache miss. This might help with
        // heap size exceeded issues. (how likely is that though, I don't know)
        StringBuilder requiredSeq = new StringBuilder();

        int cacheChunkSize = 500;
        Point currentLocation = initialLocation;
        for (int i = 0; i < buttonsToPress.length(); i += cacheChunkSize) {
            String currentChunk = buttonsToPress.substring(
                    i,
                    Math.min(
                            i + cacheChunkSize,
                            buttonsToPress.length()
                    )
            );
            if (cache.containsKey(currentChunk)) {
                //System.out.println("Cache hit!");
                requiredSeq.append(cache.get(currentChunk));
            } else {
                List<ControllerKeyPadButton> subOutput = new ArrayList<>();
                for (char buttonSymbol : currentChunk.chars().mapToObj(button -> (char) button)
                        .toList()) {

                    Button button = ControllerKeyPadButton.fromString(String.valueOf(buttonSymbol));
                    Point translationVector = button.getLocationOnKeypad()
                            .difference(currentLocation);
                    // If the gap on the keypad is on our usual path of
                    // first going horizontally and then vertically,
                    // we need to flip the order
                    if (currentLocation.y() == gapLocation.y()
                        && button.getLocationOnKeypad().x() == gapLocation.x()) {
                        subOutput.addAll(handleVerticalDisplacement(translationVector));
                        subOutput.addAll(handleHorizontalDisplacement(translationVector));
                        subOutput.add(ControllerKeyPadButton.ACTION);
                    } else if (currentLocation.x() == gapLocation.x()
                               && button.getLocationOnKeypad().y() == gapLocation.y()) {
                        subOutput.addAll(handleHorizontalDisplacement(translationVector));
                        subOutput.addAll(handleVerticalDisplacement(translationVector));
                        subOutput.add(ControllerKeyPadButton.ACTION);
                    } else {
                        // Here we need to pick the better approach. For now, we simply default
                        // to the
                        // second one
                        // if we are going left and then down it is faster to do the left way first
                        // not sure why that is the case
                        if (translationVector.x() < 0) {
                            subOutput.addAll(handleHorizontalDisplacement(translationVector));
                            subOutput.addAll(handleVerticalDisplacement(translationVector));
                        } else {
                            subOutput.addAll(handleVerticalDisplacement(translationVector));
                            subOutput.addAll(handleHorizontalDisplacement(translationVector));
                        }
                        subOutput.add(ControllerKeyPadButton.ACTION);
                    }


                    currentLocation = button.getLocationOnKeypad();
                }

                cache.put(currentChunk, new ControllerInput(subOutput).toString());
                requiredSeq.append(new ControllerInput(subOutput));
            }
        }
        return requiredSeq.toString();
    }

    private List<Code> readInput(Stream<String> inputStream) {
        return inputStream.map(line -> new Code(line.chars().mapToObj(c -> String.valueOf((char) c))
                                                        .map(DoorKeypadButton::fromString)
                                                        .toList())).toList();

    }
    /* The doors in this puzzle are controlled by a keypad below
       The problem is that
       +---+---+---+
       | 7 | 8 | 9 |
       +---+---+---+
       | 4 | 5 | 6 |
       +---+---+---+
       | 1 | 2 | 3 |
       +---+---+---+
           | 0 | A |
           +---+---+
       The keypad itself is controlled by a robot that is
       controlled by a robot that is controlled by a robot
       that is controlled by a keypad like this:
           +---+---+
           | ^ | A |
       +---+---+---+
       | < | v | > |
       +---+---+---+
       Now the whole trick is for a given code that is to be typed
       on the large keypad, we need to find the shortest (should be trivial
       as we are simply using the manhattan distance) key combination
       that the second robot needs to enter on the keypad that controls
       the first robot that presses buttons on the door.
       Once we have this generic functionality, we need to apply two
       extra levels of indirection as the second robot is controlled by
       another robot that is then controlled by our input.

       This can be achieved generically by looking at the difference in the
       positions between two locations on the keypad and emitting a sequence
       of moves (i.e. Directions) that are required to achieve this.
       We can model a keypad as a map from its button to the location (i.e. Point class)
       on the actual grid.
     */

    /**
     * The enum below models the button layout below.
     * // +---+---+---+
     * // | 7 | 8 | 9 |
     * // +---+---+---+
     * // | 4 | 5 | 6 |
     * // +---+---+---+
     * // | 1 | 2 | 3 |
     * // +---+---+---+
     * //     | 0 | A |
     * //     +---+---+
     */
    @Getter
    private enum DoorKeypadButton implements Button {
        GAP(" ", new Point(0, 3)),
        ACTION("A", new Point(2, 3)),
        ZERO("0", new Point(1, 3)),
        ONE("1", new Point(0, 2)),
        TWO("2", new Point(1, 2)),
        THREE("3", new Point(2, 2)),
        FOUR("4", new Point(0, 1)),
        FIVE("5", new Point(1, 1)),
        SIX("6", new Point(2, 1)),
        SEVEN("7", new Point(0, 0)),
        EIGHT("8", new Point(1, 0)),
        NINE("9", new Point(2, 0));

        private final String representation;
        private final Point locationOnKeypad;

        DoorKeypadButton(String representation, Point locationOnKeypad) {
            this.representation = representation;
            this.locationOnKeypad = locationOnKeypad;
        }

        public static DoorKeypadButton fromString(String s) {
            return switch (s) {
                case "A" -> ACTION;
                case "0" -> ZERO;
                case "1" -> ONE;
                case "2" -> TWO;
                case "3" -> THREE;
                case "4" -> FOUR;
                case "5" -> FIVE;
                case "6" -> SIX;
                case "7" -> SEVEN;
                case "8" -> EIGHT;
                case "9" -> NINE;
                default -> throw new IllegalStateException("Unexpected value: " + s);
            };
        }
    }

    private record Code(List<DoorKeypadButton> buttonSequence) {
        @Override
        public String toString() {
            return buttonSequence.stream().map(DoorKeypadButton::getRepresentation)
                    .collect(Collectors.joining(""));
        }
    }

    private record ControllerInput(List<ControllerKeyPadButton> buttonSequence) {
        @Override
        public String toString() {
            return buttonSequence.stream().map(ControllerKeyPadButton::getRepresentation)
                    .collect(Collectors.joining(""));
        }
    }

    /**
     * The enum below models the button layout below.
     * //     +---+---+
     * //     | ^ | A |
     * // +---+---+---+
     * // | < | v | > |
     * // +---+---+---+
     */
    @Getter
    private enum ControllerKeyPadButton implements Button {
        GAP(" ", new Point(0, 0)),
        ACTION("A", new Point(2, 0)),
        UP("^", new Point(1, 0)),
        LEFT("<", new Point(0, 1)),
        DOWN("v", new Point(1, 1)),
        RIGHT(">", new Point(2, 1));

        private final String representation;
        private final Point locationOnKeypad;

        ControllerKeyPadButton(String representation, Point locationOnKeypad) {
            this.representation = representation;
            this.locationOnKeypad = locationOnKeypad;
        }

        static ControllerKeyPadButton fromString(String representation) {
            return switch (representation) {
                case "A" -> ACTION;
                case "^" -> UP;
                case "<" -> LEFT;
                case "v" -> DOWN;
                case ">" -> RIGHT;
                default -> throw new IllegalStateException("Unexpected value: " + representation);
            };
        }
    }

    private interface Button {
        String getRepresentation();

        Point getLocationOnKeypad();
    }

}

