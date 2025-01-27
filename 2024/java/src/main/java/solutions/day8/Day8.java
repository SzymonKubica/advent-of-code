package solutions.day8;

import solutions.CommonStructures;
import solutions.Solution;
import solutions.Utils;

import javax.swing.text.html.Option;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day8 implements Solution {
    @Override
    public void firstPart(String inputFile) {
        final var cityMap = getCityMap(Utils.readInputAsStream(inputFile));
        System.out.println(printCityMap(cityMap));

        // Need to group antennas into sets of same frequency

        List<Antenna> allAntennas = cityMap.stream()
                .map(row -> row.stream().filter(Optional::isPresent).map(Optional::get).toList())
                .flatMap(List::stream)
                .toList();

        Map<Character, List<Antenna>> sameFrequencyAntennaMap = allAntennas.stream()
                .collect(Collectors.groupingBy(antenna -> antenna.frequency));

        Set<CommonStructures.Point> uniqueAntinodeLocations = new HashSet<>();
        for (final var alikeAntennas : sameFrequencyAntennaMap.values()) {
            Set<CommonStructures.Point> points = traceAntinodes(alikeAntennas);
            uniqueAntinodeLocations.addAll(points);
        }

        uniqueAntinodeLocations = uniqueAntinodeLocations.stream()
                .filter(antinode -> antinode.isInsideGrid(cityMap))
                .collect(Collectors.toSet());

        System.out.printf("There are %d unique antinodes\n", uniqueAntinodeLocations.size());

        markAntinodes(cityMap, uniqueAntinodeLocations);
        System.out.println(printCityMap(cityMap));
    }

    private void markAntinodes(
            List<List<Optional<Antenna>>> cityMap,
            Set<CommonStructures.Point> uniqueAntinodeLocations
    ) {
        for (final var p : uniqueAntinodeLocations) {
            cityMap.get(p.y()).set(p.x(), Optional.of(ANTINODE));
        }
    }

    @Override
    public void secondPart(String inputFile) {
        final var cityMap = getCityMap(Utils.readInputAsStream(inputFile));
        System.out.println(printCityMap(cityMap));

        // Need to group antennas into sets of same frequency

        List<Antenna> allAntennas = cityMap.stream()
                .map(row -> row.stream().filter(Optional::isPresent).map(Optional::get).toList())
                .flatMap(List::stream)
                .toList();

        Map<Character, List<Antenna>> sameFrequencyAntennaMap = allAntennas.stream()
                .collect(Collectors.groupingBy(antenna -> antenna.frequency));

        Set<CommonStructures.Point> uniqueAntinodeLocations = new HashSet<>();
        for (final var alikeAntennas : sameFrequencyAntennaMap.values()) {
            Set<CommonStructures.Point> points = traceAntinodesPart2(alikeAntennas, cityMap);
            uniqueAntinodeLocations.addAll(points);
        }

        uniqueAntinodeLocations = uniqueAntinodeLocations.stream()
                .filter(antinode -> antinode.isInsideGrid(cityMap))
                .collect(Collectors.toSet());

        System.out.printf("There are %d unique antinodes\n", uniqueAntinodeLocations.size());
        markAntinodes(cityMap, uniqueAntinodeLocations);
        System.out.println(printCityMap(cityMap));
    }

    private Set<CommonStructures.Point> traceAntinodesPart2(
            List<Antenna> alikeAntennas,
            List<List<Optional<Antenna>>> grid
    ) {
        Set<CommonStructures.Point> antinodes = new HashSet<>();
        for (int i = 0; i < alikeAntennas.size(); i++) {
            for (int j = 0; j < alikeAntennas.size(); j++) {
                if (i != j) {
                    CommonStructures.Point a1 = alikeAntennas.get(i).location;
                    CommonStructures.Point a2 = alikeAntennas.get(j).location;

                    CommonStructures.Point differenceVector = a2.difference(a1);
                    int u = differenceVector.x();
                    int v = differenceVector.y();

                    int divisor = gcd(u, v);
                    CommonStructures.Point diffScaled = new CommonStructures.Point(u / divisor,
                                                                                   v / divisor);
                    CommonStructures.Point diffScaledNegative = diffScaled.reflectAboutOrigin();

                    CommonStructures.Point antinode1 = a1;
                    while (antinode1.isInsideGrid(grid)) {
                        antinodes.add(antinode1);
                        antinode1 = antinode1.translateBy(diffScaled);
                    }

                    CommonStructures.Point antinode2 = a1;
                    while (antinode2.isInsideGrid(grid)) {
                        antinodes.add(antinode2);
                        antinode2 = antinode2.translateBy(diffScaledNegative);
                    }

                    antinodes.add(antinode1);
                    antinodes.add(antinode2);
                }
            }
        }
        return antinodes;

    }

    private static int gcd(int a, int b) {
        BigInteger b1 = BigInteger.valueOf(a);
        BigInteger b2 = BigInteger.valueOf(b);
        BigInteger gcd = b1.gcd(b2);
        return gcd.intValue();
    }

    private Set<CommonStructures.Point> traceAntinodes(List<Antenna> alikeAntennas) {
        Set<CommonStructures.Point> antinodes = new HashSet<>();
        for (int i = 0; i < alikeAntennas.size(); i++) {
            for (int j = 0; j < alikeAntennas.size(); j++) {
                if (i != j) {
                    CommonStructures.Point a1 = alikeAntennas.get(i).location;
                    CommonStructures.Point a2 = alikeAntennas.get(j).location;

                    CommonStructures.Point antinode1 = a1.translateBy(a2.difference(a1)
                                                                              .reflectAboutOrigin());
                    CommonStructures.Point antinode2 = a2.translateBy(a1.difference(a2)
                                                                              .reflectAboutOrigin());

                    antinodes.add(antinode1);
                    antinodes.add(antinode2);
                }
            }
        }
        return antinodes;

    }


    private List<List<Optional<Antenna>>> getCityMap(Stream<String> input) {
        List<String> inputCollected = input.toList();
        List<List<Optional<Antenna>>> cityMap = new ArrayList<>();
        for (int y = 0; y < inputCollected.size(); y++) {
            List<Character> chars = inputCollected.get(y).chars().mapToObj(x -> (char) x).toList();
            List<Optional<Antenna>> row = new ArrayList<>();
            for (int x = 0; x < chars.size(); x++) {
                row.add(Antenna.fromCharAndLocation(chars.get(x),
                                                    new CommonStructures.Point(x, y)));
            }
            cityMap.add(row);
        }
        return cityMap;
    }

    // Used only for printing below
    public static final Antenna NO_ANTENNA = new Antenna('.', null);
    public static final Antenna ANTINODE = new Antenna('#', null);

    private String printCityMap(List<List<Optional<Antenna>>> cityMap) {
        return cityMap.stream()
                .map(row -> row.stream()
                        .map(maybeAntenna -> String.valueOf(maybeAntenna.orElseGet(() -> NO_ANTENNA).frequency))
                        .collect(Collectors.joining("")))
                .collect(Collectors.joining("\n"));
    }

    public record Antenna(char frequency, CommonStructures.Point location) {
        public static Optional<Antenna> fromCharAndLocation(
                char c,
                CommonStructures.Point location
        ) {
            return switch (c) {
                case '.' -> Optional.empty();
                default -> Optional.of(new Antenna(c, location));
            };
        }
    }
}
