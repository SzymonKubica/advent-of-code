package solutions.day20;

import org.checkerframework.checker.units.qual.A;
import solutions.Solution;
import solutions.Utils;
import solutions.common.Point;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day20 implements Solution {
    @Override
    public void firstPart(String inputFile) {
        final var raceTrack = readRaceTrack(Utils.readInputAsStream(inputFile));

        Point start = findStart(raceTrack);
        Point end = findEnd(raceTrack);

        // first find the usual shortest path with no cheats
        //HashMap<Point, Point> shortestPathParentMap = findShortestPath(startLocation, raceTrack);
        HashMap<Point, Integer> costMap = new HashMap<>();
        HashMap<Point, Point> shortestPathParentMap = findShortestLinearPath(start,
                                                                             end,
                                                                             raceTrack,
                                                                             costMap);

        assert shortestPathParentMap.containsKey(end) : "Shortest path to the end has to exist";

        System.out.println(start);

        System.out.println(printRaceTrack(raceTrack));

        final List<List<RaceTrackCell>> raceTrackWithShortestPath = traceShortestPath(raceTrack,
                                                                                      shortestPathParentMap,
                                                                                      start,
                                                                                      end);
        /*
         This whole business of finding the shortest path using Dijkstra seems a bit overkill.
         We need to check the actual puzzle input and determine if the path is linear and always
         ends at the end of the racetrack, if so there is no need to perform path finding
         algorithms.
         If however, the track has dead ends, we need to find a proper path.

         If however the linear path assumption is true, we can solve the puzzle as follows:
         - step along the only possible path, counting the number of steps at each location
         - then do a second pass and try to 'look through the wall'
         - if within a 5x5 rim around the current position there are cells that are empty and
           have a higher cost (i.e. they lie on a later part of the path), those will be classified
           as shortcuts and given that we have stored costs of all locations we can take the
           difference and immediately determine how many steps would be saved by executing the
           cheat.
           By 'rim' I refer to the outer row of the 5x5 square centered at the current location.
         - the solution described above would be both easier to implement and also allow us to
           find the answer faster.
         */

        // Now we need to iterate over the shortest path and scan for the possible shortcuts to take

        final List<Shortcut> shortcuts = walkShortestPathAndFindShortcuts(raceTrack,
                                                                          shortestPathParentMap,
                                                                          costMap,
                                                                          start,
                                                                          end);

        final Map<Integer, List<Shortcut>> timeSavedToShortcut = shortcuts.stream()
                .collect(Collectors.groupingBy(shortcut -> shortcut.costSaved));

        int eligibleShortcutCount = 0;
        for (final var entry : timeSavedToShortcut.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(entry -> entry.getValue().size()))
                .toList()) {
            if (entry.getKey() >= 100) {
                eligibleShortcutCount += entry.getValue().size();
            }
            if (entry.getValue().size() > 1) {
                System.out.println("There are %d cheats that save %d picoseconds.".formatted(entry.getValue()
                                                                                                     .size(),
                                                                                             entry.getKey()));
            } else {
                System.out.println("There is one cheat that saves %d picoseconds.".formatted(entry.getKey()));
            }
        }

        System.out.println("There are %d shortcuts that save at least 100 picoseconds.".formatted(
                eligibleShortcutCount));

    }

    private HashMap<Point, Point> findShortestLinearPath(
            Point startLocation,
            Point endLocation,
            List<List<RaceTrackCell>> raceTrack,
            HashMap<Point, Integer> costMap
    ) {

        Point current = startLocation;
        HashSet<Point> visited = new HashSet<>();
        HashMap<Point, Point> pathParentMap = new HashMap<>();
        int cost = 0;
        Set<RaceTrackCell> traversableTrackTypes = Set.of(RaceTrackCell.TRACK,
                                                          RaceTrackCell.START,
                                                          RaceTrackCell.END);
        do {
            costMap.put(current, cost);
            visited.add(current);
            List<Point> neighbours = current.getNeighbourLocationsInsideGrid(raceTrack)
                    .stream()
                    .filter(nb -> traversableTrackTypes.contains(nb.indexGrid(raceTrack)))
                    .filter(Predicate.not(visited::contains))
                    .toList();

            assert neighbours.size() == 1 :
                    "The path is assumed to be linear and contain no crossroads";

            pathParentMap.put(neighbours.getFirst(), current);
            current = neighbours.getFirst();
            cost += 1;

        } while (!current.equals(endLocation));
        costMap.put(endLocation, cost);

        return pathParentMap;
    }

    private record Shortcut(Point from, Point to, int costSaved) {
    }

    private List<Shortcut> walkShortestPathAndFindShortcutsPart2(
            List<List<RaceTrackCell>> raceTrack,
            HashMap<Point, Point> shortestPathParentMap,
            HashMap<Point, Integer> costMap,
            Point startLocation,
            Point endLocation
    ) {
        Point current = startLocation;

        // We need to traverse from start to end to find shortcuts.
        Map<Point, Point> shortestPathChildMap = shortestPathParentMap.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        List<Shortcut> accessibleShortcuts = new ArrayList<>();
        do {
            // Find all accessible shortcut
            List<Point> possibleShortcuts = findShortcutLocations(current, raceTrack, 20);
            for (Point shortcutDestination : possibleShortcuts) {
                int currentCost = costMap.get(current);
                int shortcutDestinationCost = costMap.get(shortcutDestination);
                int shortcutLength = current.getManhattanDistanceTo(shortcutDestination);

                if (shortcutDestinationCost > currentCost) {
                    // Taking the shortcut takes 2 picoseconds, hence we subtract it from the
                    // total time saved.
                    int timeSaved = shortcutDestinationCost - currentCost - shortcutLength;
                    accessibleShortcuts.add(new Shortcut(current, shortcutDestination, timeSaved));
                }
            }

            current = shortestPathChildMap.get(current);
        } while (!current.equals(endLocation));

        return accessibleShortcuts;
    }

    private List<Shortcut> walkShortestPathAndFindShortcuts(
            List<List<RaceTrackCell>> raceTrack,
            HashMap<Point, Point> shortestPathParentMap,
            HashMap<Point, Integer> costMap,
            Point startLocation,
            Point endLocation
    ) {
        Point current = startLocation;

        // We need to traverse from start to end to find shortcuts.
        Map<Point, Point> shortestPathChildMap = shortestPathParentMap.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        List<Shortcut> accessibleShortcuts = new ArrayList<>();
        do {
            // Find all accessible shortcut
            List<Point> possibleShortcuts = findShortcutLocations(current, raceTrack);
            for (Point shortcutDestination : possibleShortcuts) {
                int currentCost = costMap.get(current);
                int shortcutDestinationCost = costMap.get(shortcutDestination);

                if (shortcutDestinationCost > currentCost) {
                    // Taking the shortcut takes 2 picoseconds, hence we subtract it from the
                    // total time saved.
                    int timeSaved = shortcutDestinationCost - currentCost - 2;
                    accessibleShortcuts.add(new Shortcut(current, shortcutDestination, timeSaved));
                }
            }

            current = shortestPathChildMap.get(current);
        } while (!current.equals(endLocation));

        return accessibleShortcuts;
    }

    private List<Point> findShortcutLocations(Point current, List<List<RaceTrackCell>> raceTrack) {
        List<Point> accessibleShortcuts = new ArrayList<>();

        for (Point neighbour : current.getNeighbourLocationsInsideGrid(raceTrack)) {
            if (neighbour.indexGrid(raceTrack).equals(RaceTrackCell.WALL)) {
                Point shortcutDestination = neighbour.translateBy(neighbour.difference(current));
                if (shortcutDestination.isInsideGrid(raceTrack) && List.of(RaceTrackCell.TRACK,
                                                                           RaceTrackCell.END)
                        .contains(shortcutDestination.indexGrid(raceTrack))) {
                    accessibleShortcuts.add(shortcutDestination);
                }
            }
        }
        return accessibleShortcuts;
    }

    private List<Point> findShortcutLocations(Point current, List<List<RaceTrackCell>> raceTrack, int shortcutLength) {
        List<Point> accessibleShortcuts = new ArrayList<>();

        for (int yDisplacement = -shortcutLength; yDisplacement <= shortcutLength; yDisplacement++) {
            for (int xDisplacement = -shortcutLength; xDisplacement <= shortcutLength; xDisplacement++) {
                if (Math.abs(xDisplacement)+ Math.abs(yDisplacement) <= shortcutLength) {
                    Point destination = new Point(current.x() + xDisplacement, current.y() + yDisplacement);
                    if (destination.isInsideGrid(raceTrack) && List.of(RaceTrackCell.TRACK,
                                                                       RaceTrackCell.END).contains(destination.indexGrid(raceTrack))) {
                        accessibleShortcuts.add(destination);
                    }
                }
            }
        }

        return accessibleShortcuts;
    }

    private List<List<RaceTrackCell>> traceShortestPath(
            List<List<RaceTrackCell>> raceTrack,
            HashMap<Point, Point> shortestPathParentMap,
            Point startLocation,
            Point endLocation
    ) {
        List<List<RaceTrackCell>> raceTrackCopy = Utils.getMutableGridCopy(raceTrack);
        Point current = endLocation;

        do {
            current.indexGridAndSet(raceTrackCopy, RaceTrackCell.PATH);
            current = shortestPathParentMap.get(current);
        } while (!current.equals(startLocation));

        return raceTrackCopy;
    }

    private HashMap<Point, Point> findShortestPath(
            Point startLocation,
            List<List<RaceTrackCell>> raceTrack
    ) {
        List<Point> toVisitQueue = findCellsToVisit(raceTrack);
        // Find all cells that can be visited:

        HashMap<Point, Integer> costMap = new HashMap<>();
        HashMap<Point, Point> shortestPath = new HashMap<>();
        costMap.put(startLocation, 0);
        do {
            toVisitQueue.sort(Comparator.comparing(p -> costMap.getOrDefault(p,
                                                                             Integer.MAX_VALUE)));
            Point current = toVisitQueue.removeFirst();

            List<Point> neighbours = current.getNeighbourLocationsInsideGrid(raceTrack)
                    .stream()
                    .filter(p -> !p.indexGrid(raceTrack).equals(RaceTrackCell.WALL))
                    .toList();

            for (Point neighbour : neighbours) {
                int costThroughCurrent = costMap.get(current) + 1;
                int neighbourCurrentCost = costMap.getOrDefault(neighbour, Integer.MAX_VALUE);

                if (costThroughCurrent < neighbourCurrentCost) {
                    shortestPath.put(neighbour, current);
                    costMap.put(neighbour, costThroughCurrent);
                }
            }

        } while (!toVisitQueue.isEmpty());

        return shortestPath;
    }

    private List<Point> findCellsToVisit(List<List<RaceTrackCell>> raceTrack) {
        List<Point> cellsToVisit = new ArrayList<>();
        for (int i = 0; i < raceTrack.size(); i++) {
            for (int j = 0; j < raceTrack.get(0).size(); j++) {
                Point currentLocation = new Point(j, i);
                if (!currentLocation.indexGrid(raceTrack).equals(RaceTrackCell.WALL)) {
                    cellsToVisit.add(currentLocation);
                }
            }
        }
        return cellsToVisit;
    }

    private Point findStart(List<List<RaceTrackCell>> raceTrack) {
        return findLocationSatisfying(raceTrack,
                                      point -> point.indexGrid(raceTrack)
                                              .equals(RaceTrackCell.START));
    }

    private Point findEnd(List<List<RaceTrackCell>> raceTrack) {
        return findLocationSatisfying(raceTrack,
                                      point -> point.indexGrid(raceTrack)
                                              .equals(RaceTrackCell.END));
    }

    private Point findLocationSatisfying(
            List<List<RaceTrackCell>> raceTrack,
            Predicate<Point> test
    ) {
        for (int i = 0; i < raceTrack.size(); i++) {
            for (int j = 0; j < raceTrack.get(0).size(); j++) {
                Point currentLocation = new Point(j, i);
                if (test.test(currentLocation)) {
                    return currentLocation;
                }
            }
        }
        throw new IllegalStateException(
                "Point satisfying predicate not found of the race track not found");
    }

    private String printRaceTrack(List<List<RaceTrackCell>> raceTrack) {
        return raceTrack.stream()
                .map(row -> row.stream()
                        .map(RaceTrackCell::toString)
                        .collect(Collectors.joining("")))
                .collect(Collectors.joining("\n"));
    }

    @Override
    public void secondPart(String inputFile) {
        final var raceTrack = readRaceTrack(Utils.readInputAsStream(inputFile));

        Point start = findStart(raceTrack);
        Point end = findEnd(raceTrack);

        // first find the usual shortest path with no cheats
        //HashMap<Point, Point> shortestPathParentMap = findShortestPath(startLocation, raceTrack);
        HashMap<Point, Integer> costMap = new HashMap<>();
        HashMap<Point, Point> shortestPathParentMap = findShortestLinearPath(start,
                                                                             end,
                                                                             raceTrack,
                                                                             costMap);

        assert shortestPathParentMap.containsKey(end) : "Shortest path to the end has to exist";

        System.out.println(start);

        System.out.println(printRaceTrack(raceTrack));

        final List<Shortcut> shortcuts = walkShortestPathAndFindShortcutsPart2(raceTrack,
                                                                          shortestPathParentMap,
                                                                          costMap,
                                                                          start,
                                                                          end);

        final Map<Integer, List<Shortcut>> timeSavedToShortcut = shortcuts.stream()
                .collect(Collectors.groupingBy(shortcut -> shortcut.costSaved));

        long eligibleShortcutCount = 0;
        for (final var entry : timeSavedToShortcut.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(entry -> entry.getKey()))
                .toList()) {
            if (entry.getKey() >= 100) {
                eligibleShortcutCount += entry.getValue().size();
            }
            if (entry.getValue().size() > 1) {
                System.out.println("There are %d cheats that save %d picoseconds.".formatted(entry.getValue()
                                                                                                     .size(),
                                                                                             entry.getKey()));
            } else {
                System.out.println("There is one cheat that saves %d picoseconds.".formatted(entry.getKey()));
            }
        }

        System.out.println("There are %d shortcuts that save at least 100 picoseconds.".formatted(
                eligibleShortcutCount));

    }

    private List<List<RaceTrackCell>> readRaceTrack(Stream<String> inputStream) {
        return inputStream.map(line -> line.chars()
                .mapToObj(c -> String.valueOf((char) c))
                .map(RaceTrackCell::fromString)
                .toList()).toList();
    }

    enum RaceTrackCell {
        WALL("#"), TRACK("."), START("S"), END("E"), PATH("*");

        private final String representation;

        RaceTrackCell(String representation) {
            this.representation = representation;
        }

        @Override
        public String toString() {
            return representation;
        }

        public static RaceTrackCell fromString(String representation) {
            return switch (representation) {
                case "#" -> RaceTrackCell.WALL;
                case "." -> RaceTrackCell.TRACK;
                case "S" -> RaceTrackCell.START;
                case "E" -> RaceTrackCell.END;
                case "*" -> RaceTrackCell.PATH;
                default -> throw new IllegalStateException("Unexpected value: " + representation);
            };
        }
    }
}
