package solutions.day1;

import com.google.common.collect.Streams;
import org.apache.commons.lang3.tuple.Pair;
import solutions.Solution;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class Day1 implements Solution {
    @Override
    public void firstPart(String inputFile) {
        Pair<List<Integer>, List<Integer>> inputLists = parseInputLists(readInput(inputFile));
        var sortedInputLists = Pair.of(inputLists.getLeft().stream().sorted().toList(), inputLists.getRight().stream().sorted().toList());
        int difference = Streams.zip(sortedInputLists.getLeft().stream(), sortedInputLists.getRight().stream(),
                Pair::of).map(pair -> Math.abs(pair.getLeft() - pair.getRight())).reduce(Integer::sum).get();
        System.out.printf("Difference: %s\n", difference);
    }

    @Override
    public void secondPart(String inputFile) {

    }

    private Pair<List<Integer>, List<Integer>> parseInputLists(Stream<String> input) {
        List<Integer> left = new ArrayList<>();
        List<Integer> right = new ArrayList<>();

        input.forEach(line -> {
                    var parts = line.split("   ");
                    assert (parts.length == 2) : "Each line in the input should contain entries " +
                            "for both lists.";
                    left.add(Integer.parseInt(parts[0]));
                    right.add(Integer.parseInt(parts[1]));
                }
        );
        return Pair.of(left, right);
    }

    private Stream<String> readInput(String inputFile) {
        try {
            var reader = new BufferedReader(new FileReader(inputFile));
            return reader.lines();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
