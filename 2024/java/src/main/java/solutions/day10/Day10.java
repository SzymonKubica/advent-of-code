package solutions.day10;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import solutions.Solution;
import solutions.Utils;
import solutions.common.Point;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day10 implements Solution {
    @Override
    public void firstPart(String inputFile) {
        List<List<HikingTrailPart>> topographicMap = readTopographicMap(Utils.readInputAsStream(
                inputFile));
        Set<HikingTrailPart> trailheads = topographicMap.stream()
                .map(row -> row.stream().filter(HikingTrailPart::isTrailHead).toList())
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        System.out.println(topographicMap);
        System.out.println(printTopographicMap(topographicMap));
    }

    private String printTopographicMap(List<List<HikingTrailPart>> topographicMap) {
        return topographicMap.stream().map(r -> r.stream().map(HikingTrailPart::toString).collect(
                Collectors.joining(""))).collect(Collectors.joining("\n"));

    }

    @Override
    public void secondPart(String inputFile) {

    }

    private static List<List<HikingTrailPart>> readTopographicMap(Stream<String> input) {
        // Surprisingly java does not have a nice way to iterate over a stream with indices
        AtomicInteger rowIndex = new AtomicInteger();
        AtomicInteger columnIndex = new AtomicInteger();
        // we use the length to increment the row index module row length
        return input.map(str -> Triple.of(str.chars().mapToObj(c -> (char) c), columnIndex.getAndAdd(1), str.length()))
                .map(t -> Triple.of(t.getLeft()
                                            .map(String::valueOf)
                                            .map(Integer::parseInt)
                                            .toList(), t.getMiddle(), t.getRight()))
                .map(t -> t.getLeft()
                        .stream()
                        .map(h -> new HikingTrailPart(h,
                                                      new Point(
                                                                rowIndex.getAndSet((rowIndex.get() + 1)
                                                                                   % t.getRight()), t.getMiddle())))
                        .toList())
                .toList();

    }

    record HikingTrailPart(int height, Point position) {
        boolean isTrailHead() {
            return height == 0;
        }

        @Override
        public String toString() {
            if (height == 0) {
                return "_";
            }
            if (height == 9) {
                return "*";
            }
            return String.valueOf(height);
        }
    }
}
