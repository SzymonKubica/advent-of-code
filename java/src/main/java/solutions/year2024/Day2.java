package solutions.year2024;

import solutions.Solution;
import solutions.Utils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Day2 implements Solution {
    @Override
    public void firstPart(String inputFile) {
        List<Report> reports = parseReports(Utils.readInputAsStream(inputFile));
        long result = reports.stream()
                .filter(r -> (r.isAllIncreasing() || r.isAllDecreasing()) && r.satisfiesAdjacentDifferenceBounds())
                .count();
        System.out.printf("There are %d safe reports.%n", result);
    }

    @Override
    public void secondPart(String inputFile) {
        List<Report> reports = parseReports(Utils.readInputAsStream(inputFile));
        List<List<Report>> allPossibilities = generateAllPossibilitiesWithProblemDampener(reports);
        long result = allPossibilities.stream()
                .filter(rs -> rs.stream()
                        .anyMatch(
                                r -> (r.isAllIncreasing() || r.isAllDecreasing()) && r.satisfiesAdjacentDifferenceBounds()))
                .count();
        System.out.printf("There are %d safe reports.%n", result);
    }

    @Nonnull
    private List<List<Report>> generateAllPossibilitiesWithProblemDampener(List<Report> reports) {
        return reports.stream().map(Day2::generateAllPossibleSubReports).toList();
    }

    private static List<Report> generateAllPossibleSubReports(Report report) {
        List<Report> allPossibilities = new ArrayList<>();
        for (int i = 0; i < report.levels.size(); i++) {
            var copy = new ArrayList<>(report.levels);
            copy.remove(i);
            allPossibilities.add(new Report(copy));
        }
        return allPossibilities;
    }

    private List<Report> parseReports(Stream<String> input) {
        return input.map(line -> new Report(
                        Arrays.stream(line.split(" ")).map(x -> new Level(Integer.parseInt(x))).toList()))
                .toList();
    }

    record Report(List<Level> levels) {
        public boolean isAllIncreasing() {
            for (int i = 0; i < levels.size() - 1; i++) {
                if (levels.get(i + 1).value <= levels.get(i).value) {
                    return false;
                }
            }
            return true;
        }

        public boolean isAllDecreasing() {
            for (int i = 0; i < levels.size() - 1; i++) {
                if (levels.get(i + 1).value >= levels.get(i).value) {
                    return false;
                }
            }
            return true;
        }

        public boolean satisfiesAdjacentDifferenceBounds() {
            for (int i = 0; i < levels.size() - 1; i++) {
                int diff = Math.abs(levels.get(i + 1).value - levels.get(i).value);
                if (diff < 1 || 3 < diff) {
                    return false;
                }
            }
            return true;
        }
    }

    record Level(int value) {
    }
}
