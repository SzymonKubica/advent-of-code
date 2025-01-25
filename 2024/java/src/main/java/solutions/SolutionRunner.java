package solutions;

import solutions.day1.Day1;
import solutions.day2.Day2;
import solutions.day3.Day3;
import solutions.day4.Day4;
import solutions.day5.Day5;
import solutions.day6.Day6;
import solutions.day7.Day7;

public class SolutionRunner {
    public static void main(String[] args) {
        assert (args.length == 3): "You need to specify the day, puzzle part and a path to the input file";

        int day = Integer.parseInt(args[0]);
        int part = Integer.parseInt(args[1]);
        String inputFile = args[2];

        Solution solution;
        switch (day) {
            case 1:
                solution = new Day1();
                break;
            case 2:
                solution = new Day2();
                break;
            case 3:
                solution = new Day3();
                break;
            case 4:
                solution = new Day4();
                break;
            case 5:
                solution = new Day5();
                break;
            case 6:
                solution = new Day6();
                break;
            case 7:
                solution = new Day7();
                break;
            default: throw new RuntimeException("Day %d not implemented".formatted(day));
        }

        if (part == 1) {
            solution.firstPart(inputFile);
        } else {
            solution.secondPart(inputFile);
        }
    }
}
