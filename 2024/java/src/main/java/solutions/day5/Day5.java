package solutions.day5;

import org.apache.commons.lang3.tuple.Pair;
import solutions.Solution;
import solutions.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day5 implements Solution {
    @Override
    public void firstPart(String inputFile) {

        final var rulesAndUpdates = parseInput(Utils.readInputAsStream(inputFile));

        final List<OrderingRule> rules = rulesAndUpdates.getLeft();
        final List<Update> updates = rulesAndUpdates.getRight();

        System.out.printf("Rules:\n%s\n", Utils.toStringLineByLine(rules));
        System.out.printf("Updates:\n%s\n", Utils.toStringLineByLine(updates));

        // For each page number find all updates that need to precede it
        final Map<Integer, List<Integer>> happensBeforeMap = getUpdateRulesMap(rules);

        for (final var entry : happensBeforeMap.entrySet()) {
            System.out.println("%s -> %s".formatted(entry.getKey(), entry.getValue()));
        }

        final List<Update> validUpdates = updates.stream()
                .filter(update -> update.isValidUpdate(happensBeforeMap))
                .toList();

        System.out.printf("Valid updates:\n%s\n", Utils.toStringLineByLine(updates));

        final int middlePageNumberSum = validUpdates.stream()
                .map(Update::getMiddlePageNumber)
                .reduce(Integer::sum)
                .get();
        System.out.printf("Sum of middle page numbers for valid updates: %s\n",
                          middlePageNumberSum);
    }

    @Override
    public void secondPart(String inputFile) {
        final var rulesAndUpdates = parseInput(Utils.readInputAsStream(inputFile));

        final List<OrderingRule> rules = rulesAndUpdates.getLeft();
        final List<Update> updates = rulesAndUpdates.getRight();

        System.out.printf("Rules:\n%s\n", Utils.toStringLineByLine(rules));
        System.out.printf("Updates:\n%s\n", Utils.toStringLineByLine(updates));

        // For each page number find all updates that need to precede it
        final Map<Integer, List<Integer>> happensBeforeMap = getUpdateRulesMap(rules);

        final List<Update> invalidUpdates = updates.stream()
                .filter(update -> !update.isValidUpdate(happensBeforeMap))
                .toList();

        System.out.printf("Invalid updates:\n%s\n", Utils.toStringLineByLine(updates));

        final var reorderedInvalidUpdates = invalidUpdates.stream().map(update -> update.reorderAccordingTo(rules)).toList();

        final int middlePageNumberSum = reorderedInvalidUpdates.stream()
                .map(Update::getMiddlePageNumber)
                .reduce(Integer::sum)
                .get();
        System.out.printf("Sum of middle page numbers for invalid updates after reordering: %s\n",
                          middlePageNumberSum);

    }

    private static Map<Integer, List<Integer>> getUpdateRulesMap(List<OrderingRule> rules) {
        final Map<Integer, List<Integer>> happensBeforeMap = rules.stream()
                .map(OrderingRule::happensAfter)
                .collect(Collectors.toSet())
                .stream()
                .map(val -> Map.entry(val,
                                      rules.stream()
                                              .filter(rule -> rule.happensAfter == val)
                                              .map(OrderingRule::happensBefore)
                                              .toList()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return happensBeforeMap;
    }


    private Pair<List<OrderingRule>, List<Update>> parseInput(Stream<String> input) {
        List<OrderingRule> rules = new ArrayList<>();
        List<Update> updates = new ArrayList<>();

        // As we process the input lines stream, this controls
        // whether we are still parsing the rules section or the
        // updates section.
        boolean currentlyParsingRules = true;

        for (String line : input.toList()) {
            if (line.isBlank()) {
                currentlyParsingRules = false;
                continue;
            }
            if (currentlyParsingRules) {
                rules.add(parseRule(line));
            } else {
                updates.add(parseUpdate(line));
            }
        }
        return Pair.of(rules, updates);
    }

    private Update parseUpdate(String line) {
        return new Update(Arrays.stream(line.split(",")).map(Integer::parseInt).toList());
    }

    private OrderingRule parseRule(String line) {
        List<Integer> orderingConfig = Arrays.stream(line.split("\\|"))
                .map(Integer::parseInt)
                .toList();
        assert orderingConfig.size() == 2 :
                "There can only be two pages forming the page ordering config";
        return new OrderingRule(orderingConfig.get(0), orderingConfig.get(1));
    }

    private record OrderingRule(int happensBefore, int happensAfter) {
    }

    private record Update(List<Integer> pages) {
        boolean isValidUpdate(Map<Integer, List<Integer>> happensBeforeMap) {
            for (int i = 0; i < pages.size() - 1; i++) {
                final int i_idx = i;
                if (pages.subList(i + 1, pages.size())
                        .stream()
                        .anyMatch(value -> happensBeforeMap.getOrDefault(pages.get(i_idx), List.of())
                                .contains(value))) {
                    return false;
                }
            }
            return true;
        }

        public int getMiddlePageNumber() {
            return pages.get(pages.size() / 2);
        }

        public Update reorderAccordingTo(List<OrderingRule> rules) {
            List<PageUpdate> pageUpdates = new ArrayList<>(pages.stream().map(p -> new PageUpdate(p, rules)).toList());
            pageUpdates.sort(PageUpdate::compareTo);
            return new Update(pageUpdates.stream().map(PageUpdate::pageNumber).toList());
        }

        /* Records required to do the reordering for the second part */
        private record PageUpdate(int pageNumber, List<OrderingRule> rules) implements Comparable<PageUpdate>{
            @Override
            public int compareTo(PageUpdate that) {
                OrderingRule thisBeforeThat = new OrderingRule(pageNumber, that.pageNumber);
                OrderingRule thatBeforeThis = new OrderingRule(that.pageNumber, pageNumber);

                if (rules.contains(thisBeforeThat)) {
                    return -1;
                }
                if (rules.contains(thatBeforeThis)) {
                    return 1;
                }
                return 0;
            }
        }
    }

}
