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
        final var farmMap = readFarmMap(Utils.readInputAsStream(inputFile));
        System.out.println("Farm map:");
        System.out.println(printFarmMap(farmMap));

        // We first need to identify distinct regions and make their types unique
        final var farmMapDilated = dilate(farmMap);
        System.out.println("Dilated farm map:");
        System.out.println(printFarmMap(farmMapDilated));
        final var farmMapClassified = classifyFencesAndBetweenLand(farmMapDilated);
        System.out.println("Farm map with fences and in-land boundaries:");
        System.out.println(printFarmMap(farmMapClassified));

        List<List<Pair<FarmLand, Point>>> distinctRegions = findDistinctRegions(farmMapClassified);
        // We stamp the distinct region ids on the classified map so that we can then differentiate
        // between wall counts of distinct regions of the same type
        stampDistinctRegionIds(farmMapClassified, distinctRegions);
        List<List<GardenPlot>> farmLandCompressed = compress(farmMapClassified);
        System.out.println(printFarmMap(farmLandCompressed));

        Map<Integer, Integer> horizontal = performWallAnalysis(farmLandCompressed);
        List<List<GardenPlot>> farmMapTransposed = transpose(farmLandCompressed);
        System.out.println("Transposed Farm map:");
        System.out.println(printFarmMap(farmMapTransposed));
        Map<Integer, Integer> vertical = performWallAnalysis(farmMapTransposed);

        System.out.println("Horizontal walls: ");
        System.out.println(horizontal);
        System.out.println("Vertical walls: ");
        System.out.println(vertical);


        int totalPrice = distinctRegions.stream()
                .map(region -> region.stream().map(Pair::getLeft).toList())
                // find the type for each region
                .map(lands -> {
                    FarmLand land = lands.getFirst();
                    if (land instanceof GardenPlot gp) {
                        return Pair.of(gp.distinctRegionId, lands);
                    } else {
                        return Pair.of(((BetweenLand) land).distinctRegionId, lands);
                    }
                })
                .map(p -> Pair.of(p.getLeft(), calculateAreaAndPerimeter(p.getRight())))
                .map(p -> {
                    int area = p.getRight().getLeft();
                    int horizontalWalls = horizontal.get(p.getLeft());
                    int verticalWalls = vertical.get(p.getLeft());
                    System.out.printf(
                            "Type: %s, area: %d, horizontal walls: %d, vertical walls: %d\n",
                            p.getLeft(),
                            area,
                            horizontalWalls,
                            verticalWalls);
                    return getPriceWithWallCount(area, horizontalWalls + verticalWalls);
                })
                .reduce(Integer::sum)
                .get();

        System.out.printf("Total price of the garden fence: %d\n", totalPrice);

    }

    private List<List<GardenPlot>> compress(List<List<FarmLand>> farmMapClassified) {
        return farmMapClassified.stream()
                .map(row -> row.stream()
                        .filter(land -> land instanceof GardenPlot)
                        .map(land -> (GardenPlot) land)
                        .toList())
                .filter(Predicate.not(List::isEmpty))
                .toList();
    }

    private void stampDistinctRegionIds(
            List<List<FarmLand>> farmMapClassified,
            List<List<Pair<FarmLand, Point>>> distinctRegions
    ) {
        int id = 1;
        for (final var region : distinctRegions) {
            for (final var pair : region) {
                Point location = pair.getRight();
                FarmLand land = location.indexGrid(farmMapClassified);
                if (land instanceof GardenPlot gp) {
                    gp.distinctRegionId = id;
                    continue;
                }
                if (land instanceof BetweenLand b) {
                    b.distinctRegionId = id;
                }
            }
            id++;
        }
    }

    private List<List<GardenPlot>> transpose(List<List<GardenPlot>> farmMap) {
        List<List<GardenPlot>> transposed = new ArrayList<>();
        for (int i = 0; i < farmMap.get(0).size(); i++) {
            List<GardenPlot> column = new ArrayList<>();
            for (int j = 0; j < farmMap.size(); j++) {
                column.add(farmMap.get(j).get(i));
            }
            transposed.add(column);
        }
        return transposed;
    }

    private Map<Integer, Integer> performWallAnalysis(List<List<GardenPlot>> farmMap) {
        // We scan the grid row-by-row, then we look at contiguous blocks
        // of the same type, if the blocks change in length, we have detected a wall

        // We map from the distinct region id to the count of walls for that region
        Map<Integer, Integer> wallCountMap = new HashMap<>();
        for (int i = 0; i < farmMap.size(); i++) {
            List<GardenPlot> currentRow = farmMap.get(i);
            if (i == 0) {
                // Analyze wall from the top of the grid
                analyzeOutsideWall(currentRow, wallCountMap);
                System.out.println("After top border analysis");
                System.out.println(wallCountMap);
                continue;
            }
            if (i == farmMap.size() - 1) {
                // Analyze wall from the bottom of the grid
                analyzeOutsideWall(currentRow, wallCountMap);
                System.out.println("After bottom border analysis");
                System.out.println(wallCountMap);
            }

            // Analyze by looking at the boundary between the row
            // directly above the current row and the current row
            List<GardenPlot> rowAbove = farmMap.get(i - 1);

            GardenPlot prevAbove = rowAbove.getFirst();
            GardenPlot prevCurrent = currentRow.getFirst();

            if (prevCurrent.type != prevAbove.type) {
                incrementWallCount(prevAbove.distinctRegionId, wallCountMap);
                incrementWallCount(prevCurrent.distinctRegionId, wallCountMap);
            }
            for (int j = 1; j < currentRow.size(); j++) {
                GardenPlot currAbove = rowAbove.get(j);
                GardenPlot currCurrent = currentRow.get(j);

                Set<Integer> typesWithNewWalls = analyzeWindow(prevAbove,
                                                               currAbove,
                                                               prevCurrent,
                                                               currCurrent);
                System.out.printf("New walls detected: %s\n", typesWithNewWalls);
                typesWithNewWalls.forEach(type -> incrementWallCount(type, wallCountMap));
                System.out.print("After inspecting window: ");
                System.out.println(wallCountMap);
                prevAbove = currAbove;
                prevCurrent = currCurrent;
            }
            System.out.print("After full row analysis: ");
            System.out.println(wallCountMap);
        }

        return wallCountMap;
    }

    private Set<Integer> analyzeWindow(
            GardenPlot prevAbove,
            GardenPlot currAbove,
            GardenPlot prevCurrent,
            GardenPlot currCurrent
    ) {

        char prevCharAbove = prevAbove.type;
        char currCharAbove = currAbove.type;
        char prevCharCurrent = prevCurrent.type;
        char currCharCurrent = currCurrent.type;
        System.out.printf("Looking at window:\n%s%s\n%s%s\n",
                          prevCharAbove,
                          currCharAbove,
                          prevCharCurrent,
                          currCharCurrent);

        /*
        If we have a window of type:
        AA
        _-
        AC
        We want to add new the new wall (marked with - above) for both C and A
         */
        if (prevCharAbove == prevCharCurrent && currCharAbove != currCharCurrent) {
            return Set.of(currAbove.distinctRegionId, currCurrent.distinctRegionId);
        }

        /*
        If we have a window of type:
        AA
        _-
        BA
        The walls for B and A already have been added when we looked at the previous window
        XA
        XB
        So in this case we don't add anything
         */
        if (currCharAbove == currCharCurrent) {
            return Set.of();
        }

        /* If we have a window of type:
        AA
        BC
        The A and B walls already have been counted, so
        we only add a wall for c
        */
        if (prevCharAbove == currCharAbove && prevCharCurrent != currCharCurrent) {
            return Set.of(currCurrent.distinctRegionId);
        }

        // As above but we are going along the wall in the bottom two cells.
        if (prevCharCurrent == currCharCurrent && prevCharAbove != currCharAbove) {
            return Set.of(currAbove.distinctRegionId);
        }

        // All adjacent are different
        if (prevCharAbove != prevCharCurrent
            && currCharAbove != currCharCurrent
            && prevCharAbove != currCharAbove
            && prevCurrent != currCurrent) {
            return Set.of(currAbove.distinctRegionId, currCurrent.distinctRegionId);
        }

        // Nothing has changed, still tracing the same wall.
        return Set.of();
    }

    private static void analyzeOutsideWall(
            List<GardenPlot> currentRow,
            Map<Integer, Integer> wallCountMap
    ) {
        int prev = currentRow.get(0).distinctRegionId;
        incrementWallCount(prev, wallCountMap);
        for (int j = 1; j < currentRow.size(); j++) {
            int curr = currentRow.get(j).distinctRegionId;
            if (curr != prev) {
                incrementWallCount(curr, wallCountMap);
            }
            prev = curr;
        }
    }

    private static void incrementWallCount(int key, Map<Integer, Integer> wallCountMap) {
        wallCountMap.put(key, wallCountMap.getOrDefault(key, 0) + 1);
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
                                     new BetweenLand(neighbourTypes.stream()
                                                             .toList()
                                                             .getFirst()
                                                             .toString()
                                                             .toLowerCase()
                                                             .charAt(0)));
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
                    farmMapClassified).stream().filter(pointsToVisit::contains).toList();

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

    private int getPriceWithWallCount(int area, int wallCount) {
        return area * wallCount;
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


    private <T extends FarmLand> String printFarmMap(List<List<T>> farmMap) {
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
        public int distinctRegionId;

        public GardenPlot(char type) {
            this.type = type;
        }

        @Override
        public String toString() {
            if (distinctRegionId != 0) {
                return "(%s, %d)".formatted(type, distinctRegionId);
            }
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
        public int distinctRegionId;

        public BetweenLand(char type) {
            this.type = type;
        }

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
