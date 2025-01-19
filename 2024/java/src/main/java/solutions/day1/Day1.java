package solutions.day1;

import com.google.common.collect.Streams;
import org.apache.commons.lang3.tuple.Pair;
import solutions.Solution;
import solutions.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Day1 implements Solution {
    @Override
    public void firstPart(String inputFile) {
        Pair<List<Integer>, List<Integer>> inputLists = parseInputLists(
                Utils.readInputAsStream(inputFile));
        var sortedInputLists = Pair.of(inputLists.getLeft().stream().sorted().toList(),
                inputLists.getRight().stream().sorted().toList());

        int difference = Streams.zip(sortedInputLists.getLeft().stream(),
                        sortedInputLists.getRight().stream(), Pair::of)
                .map(pair -> Math.abs(pair.getLeft() - pair.getRight()))
                .reduce(Integer::sum)
                .get();

        System.out.printf("Difference: %s\n", difference);
    }

    @Override
    public void secondPart(String inputFile) {
        Pair<List<Integer>, List<Integer>> inputLists = parseInputLists(
                Utils.readInputAsStream(inputFile));
        Map<Integer, Integer> counts = new HashMap<>();
        for (int x : inputLists.getLeft()) {
            counts.put(x, (int) inputLists.getRight().stream().filter(y -> y == x).count());
        }
        int similarityScore = counts.entrySet()
                .stream()
                .map(entry -> entry.getKey() * entry.getValue())
                .reduce(Integer::sum)
                .get();

        System.out.printf("Similarity score: %s\n", similarityScore);
    }


    private Pair<List<Integer>, List<Integer>> parseInputLists(Stream<String> input) {
        List<Integer> left = new ArrayList<>();
        List<Integer> right = new ArrayList<>();

        input.forEach(line -> {
            var parts = line.split("   ");
            assert (parts.length == 2) : "Each line in the input should contain entries " + "for "
                    + "both lists.";
            left.add(Integer.parseInt(parts[0]));
            right.add(Integer.parseInt(parts[1]));
        });
        return Pair.of(left, right);
    }

}
