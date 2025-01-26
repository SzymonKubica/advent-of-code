package solutions.day8;

import solutions.CommonStructures;
import solutions.Solution;
import solutions.Utils;

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

        uniqueAntinodeLocations = uniqueAntinodeLocations.stream().filter(antinode -> antinode.isInsideGrid(cityMap)).collect(
                Collectors.toSet());

        System.out.printf("There are %d unique antinodes", uniqueAntinodeLocations.size());
    }

    private Set<CommonStructures.Point> traceAntinodes(List<Antenna> alikeAntennas) {
        Set<CommonStructures.Point> antinodes = new HashSet<>();
        for (int i = 0; i < alikeAntennas.size(); i++) {
            for (int j = 0; j < alikeAntennas.size(); j++) {
                if (i != j) {
                    CommonStructures.Point a1 = alikeAntennas.get(i).location;
                    CommonStructures.Point a2 = alikeAntennas.get(j).location;

                    CommonStructures.Point antinode1 = a1.translateBy(a2.difference(a1).reflectAboutOrigin());
                    CommonStructures.Point antinode2 = a2.translateBy(a1.difference(a2).reflectAboutOrigin());

                    antinodes.add(antinode1);
                    antinodes.add(antinode2);
                }
            }
        }
        return antinodes;

    }

    @Override
    public void secondPart(String inputFile) {

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
