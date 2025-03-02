package solutions;

import solutions.day1.Day1;
import solutions.day10.Day10;
import solutions.day11.Day11;
import solutions.day12.Day12;
import solutions.day13.Day13;
import solutions.day14.Day14;
import solutions.day15.Day15;
import solutions.day16.Day16;
import solutions.day17.Day17;
import solutions.day18.Day18;
import solutions.day19.Day19;
import solutions.day2.Day2;
import solutions.day3.Day3;
import solutions.day4.Day4;
import solutions.day5.Day5;
import solutions.day6.Day6;
import solutions.day7.Day7;
import solutions.day8.Day8;
import solutions.day9.Day9;

import java.util.List;

public class SolutionRunner {
    public static void main(String[] args) {
        assert (args.length == 3) :
                "You need to specify the day, puzzle part and a path to the input file";

        int day = Integer.parseInt(args[0]);
        int part = Integer.parseInt(args[1]);
        String inputFile = args[2];

        List<Solution> solutions = List.of(new Day1(),
                                           new Day2(),
                                           new Day3(),
                                           new Day4(),
                                           new Day5(),
                                           new Day6(),
                                           new Day7(),
                                           new Day8(),
                                           new Day9(),
                                           new Day10(),
                                           new Day11(),
                                           new Day12(),
                                           new Day13(),
                                           new Day14(),
                                           new Day15(),
                                           new Day16(),
                                           new Day17(),
                                           new Day18(),
                                           new Day19()
                                           );

        int day_idx = day - 1;
        if (day_idx > solutions.size()) {
            throw new RuntimeException("Day %d not implemented".formatted(day));
        }

        Solution solution = solutions.get(day_idx);

        if (part == 1) {
            solution.firstPart(inputFile);
        } else {
            solution.secondPart(inputFile);
        }
    }
}
