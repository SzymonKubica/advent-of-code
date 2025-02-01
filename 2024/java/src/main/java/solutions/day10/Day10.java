package solutions.day10;

import solutions.Solution;
import solutions.Utils;
import solutions.common.Point;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class Day10 implements Solution {
    @Override
    public void firstPart(String inputFile) {
        List<List<HikingTrailPart>> topographicMap = readTopographicMap(Utils.readInputAsStream(inputFile));
        Set<Point>
    }

    @Override
    public void secondPart(String inputFile) {

    }

    private static List<List<HikingTrailPart>> readTopographicMap(Stream<String> input) {
        return input.map(str -> str.chars()
                .mapToObj(String::valueOf)
                .map(Integer::parseInt)
                .map(HikingTrailPart::new)
                .toList()).toList();

    }

    record HikingTrailPart(int height) {
    }
}
