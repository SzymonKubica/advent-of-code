package solutions.day10;

import com.google.common.collect.Streams;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import solutions.Solution;
import solutions.Utils;
import solutions.common.Point;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day10 implements Solution {
    @Override
    public void firstPart(String inputFile) {
        List<List<HikingTrailPart>> topographicMap = readTopographicMap(Utils.readInputAsStream(
                inputFile));
        System.out.println("Topographic map: ");
        System.out.println(printTopographicMap(topographicMap));
        Set<HikingTrailPart> trailheads = topographicMap.stream()
                .map(row -> row.stream().filter(HikingTrailPart::isTrailHead).toList())
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        System.out.println("Trailhead locations: ");
        System.out.println(trailheads.stream().map(HikingTrailPart::position).toList());

        int totalScore = 0;
        for (final HikingTrailPart trailhead : trailheads) {
            System.out.println("Exploring trailhead at: " + trailhead.position);
            final Pair<Integer, List<List<HikingTrailPart>>> trailheadScoreAndTraceMap =
                    exploreTrailhead(
                    trailhead,
                    topographicMap);
            int trailHeadScore = trailheadScoreAndTraceMap.getLeft();
            System.out.printf("Trailhead score: %d%n", trailHeadScore);
            totalScore += trailHeadScore;
        }

        System.out.printf("Total score: %d\n", totalScore);
    }

    @Override
    public void secondPart(String inputFile) {
        List<List<HikingTrailPart>> topographicMap = readTopographicMap(Utils.readInputAsStream(
                inputFile));
        System.out.println("Topographic map: ");
        System.out.println(printTopographicMap(topographicMap));
        Set<HikingTrailPart> trailheads = topographicMap.stream()
                .map(row -> row.stream().filter(HikingTrailPart::isTrailHead).toList())
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        System.out.println("Trailhead locations: ");
        System.out.println(trailheads.stream().map(HikingTrailPart::position).toList());

        int totalScore = 0;
        for (final HikingTrailPart trailhead : trailheads) {
            System.out.println("Exploring trailhead at: " + trailhead.position);
            final Pair<Integer, List<List<HikingTrailPart>>> trailheadScoreAndTraceMap =
                    exploreTrailheadDistinctTrails(
                    trailhead,
                    topographicMap);
            int trailHeadScore = trailheadScoreAndTraceMap.getLeft();
            System.out.printf("Trailhead score: %d%n", trailHeadScore);
            totalScore += trailHeadScore;
        }

        System.out.printf("Total score: %d\n", totalScore);
    }

    /**
     * @param trailhead      Starting point of the exploration
     * @param topographicMap map of the mountain
     * @return pair of (trailhead score, trace on the map after exploration
     */
    private Pair<Integer, List<List<HikingTrailPart>>> exploreTrailhead(
            HikingTrailPart trailhead,
            List<List<HikingTrailPart>> topographicMap
    ) {
        List<List<HikingTrailPart>> mapCopy = getMutableMapCopy(topographicMap);
        // This may contain duplicates
        List<Point> reachablePeaks = traverse(trailhead, mapCopy, topographicMap, new HashSet<>());
        Set<Point> uniqueReachablePeaks = new HashSet<>(reachablePeaks);
        //System.out.println(printTopographicMap(mapCopy));
        return Pair.of(uniqueReachablePeaks.size(), mapCopy);
    }

    private Pair<Integer, List<List<HikingTrailPart>>> exploreTrailheadDistinctTrails(HikingTrailPart trailhead,
            List<List<HikingTrailPart>> topographicMap
    ) {
        List<List<HikingTrailPart>> mapCopy = getMutableMapCopy(topographicMap);
        // This may contain duplicates
        List<Point> reachablePeaks = traverse(trailhead, mapCopy, topographicMap, new HashSet<>());
        //System.out.println(printTopographicMap(mapCopy));
        return Pair.of(reachablePeaks.size(), mapCopy);
    }

    private List<Point> traverse(
            HikingTrailPart start,
            List<List<HikingTrailPart>> mapCopy,
            List<List<HikingTrailPart>> topographicMap,
            Set<Point> visited
    ) {
        if (start.isPeak()) {
            return List.of(start.position);
        }
        mapCopy.get(start.position.y())
                .set(start.position.x(), new HikingTrailPart(-1, start.position));

        List<HikingTrailPart> possiblePaths = start.position.getNeighboursInsideGrid(topographicMap)
                .stream()
                .filter(nb -> nb.height == start.height + 1)
                .filter(nb -> !visited.contains(nb.position))
                .toList();

        if (possiblePaths.isEmpty()) {
            return List.of();
        }

        return possiblePaths.stream()
                .map(p -> traverse(p,
                                   mapCopy,
                                   topographicMap,
                                   Streams.concat(visited.stream(), Stream.of(start.position))
                                           .collect(Collectors.toSet())))
                .flatMap(List::stream)
                .toList();
    }

    private List<List<HikingTrailPart>> getMutableMapCopy(List<List<HikingTrailPart>> topographicMap) {
        List<List<HikingTrailPart>> copy = new ArrayList<>();
        for (final var row : topographicMap) {
            copy.add(new ArrayList<>(row));
        }
        return copy;
    }

    private String printTopographicMap(List<List<HikingTrailPart>> topographicMap) {
        return topographicMap.stream()
                .map(r -> r.stream().map(HikingTrailPart::toString).collect(Collectors.joining("")))
                .collect(Collectors.joining("\n"));

    }


    private static List<List<HikingTrailPart>> readTopographicMap(Stream<String> input) {
        // Surprisingly java does not have a nice way to iterate over a stream with indices
        // so we need to use this cursed code
        AtomicInteger rowIndex = new AtomicInteger();
        AtomicInteger columnIndex = new AtomicInteger();
        // we use the length to increment the row index module row length
        return input.map(str -> Triple.of(str.chars().mapToObj(c -> (char) c),
                                          columnIndex.getAndAdd(1),
                                          str.length()))
                .map(t -> Triple.of(t.getLeft()
                                            .map(String::valueOf)
                                            .map(Integer::parseInt)
                                            .toList(), t.getMiddle(), t.getRight()))
                .map(t -> t.getLeft()
                        .stream()
                        .map(h -> new HikingTrailPart(h,
                                                      new Point(rowIndex.getAndSet((rowIndex.get()
                                                                                    + 1)
                                                                                   % t.getRight()),
                                                                t.getMiddle())))
                        .toList())
                .toList();

    }

    record HikingTrailPart(int height, Point position) {
        boolean isTrailHead() {
            return height == 0;
        }

        boolean isPeak() {
            return height == 9;
        }

        boolean isVisited() {
            return height == -1;
        }

        @Override
        public String toString() {
            if (isTrailHead()) {
                return "_";
            }
            if (isPeak()) {
                return "*";
            }
            if (isVisited()) {
                return "x";
            }
            return String.valueOf(height);
        }
    }
}
