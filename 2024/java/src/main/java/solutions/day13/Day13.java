package solutions.day13;

import com.google.common.base.Functions;
import org.apache.commons.lang3.tuple.Triple;
import solutions.Solution;
import solutions.Utils;
import solutions.common.Point;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day13 implements Solution {

    @Override
    public void firstPart(String inputFile) {

        List<Triple<Button, Button, Point>> machines = parseClawMachines(Utils.readInputAsStream(
                inputFile));
        System.out.println(Utils.toStringLineByLine(machines));

        AtomicInteger progress = new AtomicInteger();
        AtomicInteger solutionsFound = new AtomicInteger();
        long totalCost = machines.stream()
                .map(Day13::findCheapestSolutionCost)
                .peek(maybeCost -> {
                    int curr = progress.incrementAndGet();
                    System.out.printf("Processed (%d/%d) machines.\n", curr, machines.size());
                    if (maybeCost.isPresent()) {
                        solutionsFound.incrementAndGet();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Long::valueOf)
                .reduce(Long::sum)
                .get();


        System.out.printf("Solutions found: %d%n", solutionsFound.get());
        System.out.printf("Minimum tokens required to win all possible prices: %d", totalCost);
    }

    @Override
    public void secondPart(String inputFile) {
        List<Triple<Button, Button, Point>> machines = parseClawMachines(Utils.readInputAsStream(
                inputFile));
        System.out.println(Utils.toStringLineByLine(machines));

        AtomicInteger progress = new AtomicInteger();
        AtomicInteger solutionsFound = new AtomicInteger();
        long totalCost = machines.stream()
                .map(triple -> Triple.of(triple.getLeft().intoBigButton(),
                                         triple.getMiddle().intoBigButton(),
                                         getPartTwoDistantPoint(triple.getRight())))
                .map(Day13::findCheapestSolutionClosedForm)
                .peek(maybeCost -> {
                    int curr = progress.incrementAndGet();
                    System.out.printf("Processed (%d/%d) machines.\n", curr, machines.size());
                    if (maybeCost.isPresent()) {
                        solutionsFound.incrementAndGet();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(Long::sum)
                .get();

        System.out.printf("Solutions found: %d%n", solutionsFound.get());
        System.out.printf("Minimum tokens required to win all possible prices: %d", totalCost);
    }

    private static Point.BigPoint getPartTwoDistantPoint(Point originalPrize) {
        long prizeShift = 10000000000000L;
        return new Point.BigPoint(prizeShift + originalPrize.x(), prizeShift + originalPrize.y());
    }

    private static Optional<Long> findCheapestSolutionClosedForm(Triple<BigButton, BigButton,
            Point.BigPoint> machineConfiguration) {
        BigButton buttonA = machineConfiguration.getLeft();
        BigButton buttonB = machineConfiguration.getMiddle();
        Point.BigPoint prize = machineConfiguration.getRight();

        long xA = buttonA.clawMovement.x();
        long yA = buttonA.clawMovement.y();
        long xB = buttonB.clawMovement.x();
        long yB = buttonB.clawMovement.y();

        long xP = prize.x();
        long yP = prize.y();

        long determinant = xA * yB - xB * yA;

        if (determinant == 0) {
            return Optional.empty();
        }
        if ((xP * yB - xB * yP) % determinant != 0) {
            return Optional.empty();
        }
        if ((xA * yP - xP * yA) % determinant != 0) {
            return Optional.empty();
        }

        long aPresses = (xP * yB - xB * yP) / determinant;
        long bPresses = (xA * yP - xP * yA) / determinant;

        long cost = 3 * aPresses + bPresses;
        System.out.printf("Total cost using the closed-form solution: %d%n", cost);
        return Optional.of(cost);
    }

    private static final int MAXIMUM_BUTTON_PRESSES = 100;

    private static Optional<Integer> findCheapestSolutionCost(Triple<Button, Button, Point> machineConfiguration) {
        Button buttonA = machineConfiguration.getLeft();
        Button buttonB = machineConfiguration.getMiddle();
        Point prize = machineConfiguration.getRight();

        // Given that we cannot press any button more than 100 times,
        // we use this bound to do the following:
        // - iterate from 1 to min(100, maximum button presses not to move past the prize)
        // - if 100 * (A_x + B_x) < P_x or same in y direction, we short circuit and do
        // not even search at all as the prize is not reachable.

        // Start the brute-force for the button that has the lower number of maximum presses
        int maxPressA = buttonA.findMaximumPossiblePresses(prize);
        int maxPressB = buttonB.findMaximumPossiblePresses(prize);

        Button bruteForceCandidate = maxPressA <= maxPressB ? buttonA : buttonB;
        // the second button that we pick once the number of presses
        // of the first one is determined by force.
        Button constrainedButton = maxPressA > maxPressB ? buttonA : buttonB;
        int maxPressBruteForceConstraint = Math.min(maxPressA, maxPressB);

        int searchSpaceBound = Math.min(maxPressBruteForceConstraint, MAXIMUM_BUTTON_PRESSES);


        System.out.printf(
                "Initiating brute-force for button %s over key-press search space: [1,%d]\n",
                bruteForceCandidate,
                searchSpaceBound);

        Set<Integer> solutionCosts = new HashSet<>();
        for (int pressCount = 0; pressCount <= searchSpaceBound; pressCount++) {
            Point locationAfterPressingFirst =
                    Point.ORIGIN.translateBy(bruteForceCandidate.clawMovement,
                                                                        pressCount);

            // In order for a solution to be feasible, now the displacement that
            // we need to take to reach the prize needs to be divisible by the
            // translation vector on the second button.

            Point remainingDisplacement =
                    prize.translateBy(locationAfterPressingFirst.reflectAboutOrigin());

            if (remainingDisplacement.x() % constrainedButton.clawMovement.x() == 0
                && remainingDisplacement.y() % constrainedButton.clawMovement.y() == 0
                && remainingDisplacement.x() >= 0
                && remainingDisplacement.y() >= 0) {
                // We need to ensure that it is equally divided.
                int secondButtonPresses = remainingDisplacement.x()
                                          / constrainedButton.clawMovement.x();
                int secondButtonPressesY = remainingDisplacement.y()
                                           / constrainedButton.clawMovement.y();

                if (secondButtonPressesY == secondButtonPresses) {
                    int cost = bruteForceCandidate.cost * pressCount
                               + constrainedButton.cost * secondButtonPresses;
                    System.out.printf(
                            "Found solution to reach %s:\nPress %s %d times.\nPress %s %d times"
                            + ".\nCost: %d\nCheck X: %d\nCheck Y: %d\n",
                            prize,
                            bruteForceCandidate,
                            pressCount,
                            constrainedButton,
                            secondButtonPresses,
                            cost,
                            bruteForceCandidate.clawMovement.x() * pressCount
                            + constrainedButton.clawMovement.x() * secondButtonPresses,
                            bruteForceCandidate.clawMovement.y() * pressCount
                            + constrainedButton.clawMovement.y() * secondButtonPresses);
                    solutionCosts.add(cost);
                }

            }
        }

        if (solutionCosts.isEmpty()) {
            return Optional.empty();
        }

        int minimumCost = solutionCosts.stream().min(Comparator.comparingInt(x -> x)).get();
        System.out.printf("Minimum cost: %d\n", minimumCost);
        return Optional.of(minimumCost);
    }


    private List<Triple<Button, Button, Point>> parseClawMachines(Stream<String> input) {
        String allInput = input.collect(Collectors.joining("\n"));

        String[] machineConfigurations = allInput.split("\n\n");

        List<Triple<Button, Button, Point>> parsedConfigurations = new ArrayList<>();
        for (String config : machineConfigurations) {

            parsedConfigurations.add(parseConfiguration(config));
        }
        return parsedConfigurations;
    }

    private Triple<Button, Button, Point> parseConfiguration(String config) {
        List<String> lines = config.lines().toList();
        assert (lines.size() == 3) :
                "A correct claw machine config specifies buttons A, B and the prize location";
        String buttonAConfig = lines.get(0);
        String buttonBConfig = lines.get(1);
        String prizeConfig = lines.get(2);

        return Triple.of(parseButton(buttonAConfig),
                         parseButton(buttonBConfig),
                         parsePrize(prizeConfig));
    }

    private Button parseButton(String buttonConfig) {
        String[] parts = buttonConfig.split(" ");
        assert (parts.length == 4) :
                "Button config needs to be of the form: 'Button B: X+27, Y+71'";

        int xDisplacement = Integer.parseInt(parts[2].split("\\+")[1].split(",")[0]);
        int yDisplacement = Integer.parseInt(parts[3].split("\\+")[1]);

        String buttonType = parts[1].split(":")[0];

        int cost = buttonType.equals("A") ? 3 : 1;
        return new Button(cost, new Point(xDisplacement, yDisplacement));
    }

    private Point parsePrize(String prizeConfig) {
        String[] parts = prizeConfig.split(" ");
        assert (parts.length == 3) :
                "Prize config needs to be of the form: 'Prize: X=18641, Y=10279'";

        int x = Integer.parseInt(parts[1].split("=")[1].split(",")[0]);
        int y = Integer.parseInt(parts[2].split("=")[1]);

        return new Point(x, y);
    }


    record Button(int cost, Point clawMovement) {
        /**
         * Determines the maximum number of times the button
         * can be pressed until one of its coordinates overtakes
         * the prize and we cannot move back.
         */
        int findMaximumPossiblePresses(Point prize) {
            Point origin = new Point(0, 0);
            Point current = origin;
            int presses = 0;
            while (current.x() < prize.x() && current.y() < prize.y()) {
                presses++;
                current = current.translateBy(clawMovement);
            }

            return presses;
        }

        BigButton intoBigButton() {
            return new BigButton(cost,
                                 new Point.BigPoint((long) clawMovement.x(),
                                                    (long) clawMovement.y()));
        }

    }

    record BigButton(int cost, Point.BigPoint clawMovement) {
    }
}
