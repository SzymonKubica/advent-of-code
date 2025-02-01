package solutions.day12;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import solutions.Solution;
import solutions.Utils;
import solutions.common.Point;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day12 implements Solution {
    @Override
    public void firstPart(String inputFile) {
        final var farmMap = readFarmMap(Utils.readInputAsStream(inputFile));
        System.out.println("Farm map:");
        System.out.println(printFarmMap(farmMap));

        final var farmMapDilated = dilate(farmMap);
        System.out.println("Dilated farm map:");
        System.out.println(printFarmMap(farmMapDilated));
        final var farmMapClassified = classifyFencesAndBetweenLand(farmMapDilated);
        System.out.println("Farm map with fences and in-land boundaries:");
        System.out.println(printFarmMap(farmMapClassified));

        List<List<Pair<FarmLand, Point>>> distinctRegions = findDistinctRegions(farmMapClassified);

        int totalPrice = distinctRegions.stream()
                .map(region -> region.stream().map(Pair::getLeft).toList())
                .map(Day12::calculateAreaAndPerimeter)
                .map(pair -> getPrice(pair.getLeft(), pair.getRight()))
                .reduce(Integer::sum)
                .get();

        System.out.printf("Total price of the garden fence: %d\n", totalPrice);

    }


    @Override
    public void secondPart(String inputFile) {

    }


    private List<List<FarmLand>> dilate(List<List<FarmLand>> farmMap) {
        assert !farmMap.isEmpty() : "Farm land map cannot be empty";

        List<List<FarmLand>> farmMapDilated = new ArrayList<>();
        int width = farmMap.get(0).size();
        final List<FarmLand> emptyRow = IntStream.range(0, 2 * width - 1)
                .mapToObj(_i -> (FarmLand) new Empty())
                .toList();
        for (int i = 0; i < farmMap.size(); i++) {
            List<FarmLand> dilatedRow = new ArrayList<>();
            for (int j = 0; j < width; j++) {
                dilatedRow.add(farmMap.get(i).get(j));
                if (j < width - 1) {
                    dilatedRow.add(new Empty());
                }
            }
            farmMapDilated.add(dilatedRow);
            if (i < farmMap.size() - 1) {
                farmMapDilated.add(new ArrayList<>(emptyRow));
            }
        }
        return farmMapDilated;
    }

    private List<List<FarmLand>> classifyFencesAndBetweenLand(List<List<FarmLand>> farmMap) {
        List<List<FarmLand>> mapCopy = getFarmMapCopy(farmMap);
        int width = farmMap.get(0).size();
        for (int i = 0; i < mapCopy.size(); i++) {
            for (int j = 0; j < width; j++) {
                Point location = new Point(j, i);
                List<FarmLand> neighbours = location.getNeighboursInsideGrid(mapCopy);
                Set<Character> neighbourTypes = neighbours.stream()
                        .filter(nb -> nb instanceof GardenPlot)
                        .map(g -> ((GardenPlot) g).type)
                        .collect(Collectors.toSet());
                if (location.indexGrid(farmMap) instanceof Empty) {
                    if (neighbourTypes.size() != 1) {
                        mapCopy.get(i).set(j, new Fence());
                    } else {
                        mapCopy.get(i)
                                .set(j,
                                     new BetweenLand(neighbourTypes.stream().toList().getFirst().toString().toLowerCase().charAt(0)));
                    }
                }
            }
        }
        return mapCopy;
    }

    private List<List<Pair<FarmLand, Point>>> findDistinctRegions(List<List<FarmLand>> farmMapClassified) {
        List<List<Pair<FarmLand, Point>>> distinctRegions = new ArrayList<>();
        LinkedHashSet<Point> pointsToVisit = new LinkedHashSet<>();
        int width = farmMapClassified.get(0).size();
        for (int i = 0; i < farmMapClassified.size(); i++) {
            for (int j = 0; j < width; j++) {
                Point location = new Point(j, i);
                FarmLand land = location.indexGrid(farmMapClassified);
                if (land instanceof GardenPlot || land instanceof BetweenLand) {
                    pointsToVisit.add(location);
                }
            }
        }

        List<Pair<FarmLand, Point>> currentRegion = new ArrayList<>();
        while (!pointsToVisit.isEmpty()) {
            System.out.println(pointsToVisit.size());
            final var regionStart = pointsToVisit.removeFirst();
            currentRegion.add(Pair.of(regionStart.indexGrid(farmMapClassified), regionStart));

            List<Point> neighboursToVisit = regionStart.getNeighbourLocationsInsideGrid(
                            farmMapClassified)
                    .stream()
                    .filter(pointsToVisit::contains)
                    .toList();

            Deque<Point> pointsToTraverse = new ArrayDeque<>(neighboursToVisit);

            while (!pointsToTraverse.isEmpty()) {
                final var curr = pointsToTraverse.removeFirst();
                currentRegion.add(Pair.of(curr.indexGrid(farmMapClassified), curr));
                pointsToVisit.remove(curr);
                List<Point> neighbours = curr.getNeighbourLocationsInsideGrid(farmMapClassified)
                        .stream()
                        .filter(pointsToVisit::contains)
                        .filter(Predicate.not(pointsToTraverse::contains))
                        .toList();
                pointsToTraverse.addAll(neighbours);
            }

            distinctRegions.add(currentRegion);
            currentRegion = new ArrayList<>();
        }
        return distinctRegions;
    }

    private int getPrice(int area, int perimeter) {
        return area * perimeter;
    }

    static private Pair<Integer, Integer> calculateAreaAndPerimeter(Collection<FarmLand> region) {
        int area = (int) region.stream().filter(farmLand -> farmLand instanceof GardenPlot).count();
        int betweenPlotsCount = (int) region.stream()
                .filter(farmLand -> farmLand instanceof BetweenLand)
                .count();

        int perimeter = 4 * area - 2 * betweenPlotsCount;
        return Pair.of(area, perimeter);
    }

    private List<List<FarmLand>> getFarmMapCopy(List<List<FarmLand>> grid) {
        List<List<FarmLand>> copy = new ArrayList<>();
        for (final var row : grid) {
            copy.add(new ArrayList<>(row.stream().map(FarmLand::scuffedClone).toList()));
        }
        return copy;
    }


    private String printFarmMap(List<List<FarmLand>> farmMap) {
        return farmMap.stream()
                .map(row -> row.stream().map(FarmLand::toString).collect(Collectors.joining("")))
                .collect(Collectors.joining("\n"));
    }

    private List<List<FarmLand>> readFarmMap(Stream<String> input) {
        return input.map(row -> row.chars()
                .mapToObj(c -> (FarmLand) new GardenPlot((char) c))
                .toList()).toList();
    }

    private abstract sealed class FarmLand permits GardenPlot, Fence, BetweenLand, Empty {
        public FarmLand scuffedClone() {
            return switch (this) {
                case BetweenLand betweenLand -> new BetweenLand(betweenLand.type);
                case Empty empty -> new Empty();
                case Fence fence -> new Fence();
                case GardenPlot gardenPlot -> new GardenPlot(gardenPlot.type);
            };
        }
    }

    @AllArgsConstructor
    private final class GardenPlot extends FarmLand {
        public final char type;

        @Override
        public String toString() {
            return String.valueOf(type);
        }
    }

    private final class Fence extends FarmLand {
        @Override
        public String toString() {
            return "#";
        }
    }

    @AllArgsConstructor
    private final class BetweenLand extends FarmLand {
        public final char type;

        @Override
        public String toString() {
            return String.valueOf(type).toLowerCase();
        }
    }

    private final class Empty extends FarmLand {
        @Override
        public String toString() {
            return "_";
        }
    }
}
