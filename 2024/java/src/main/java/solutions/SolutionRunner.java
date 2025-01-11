package solutions;

import solutions.day1.Day1;

public class SolutionRunner {
    public static void main(String[] args) {
        assert (args.length == 3): "You need to specify the day, puzzle part and a path to the input file";

        int day = Integer.parseInt(args[0]);
        int part = Integer.parseInt(args[1]);
        String inputFile = args[2];

        switch (day) {
            case 1:
                Solution solution = new Day1();
                if (part == 1) {
                    solution.firstPart(inputFile);
                } else {
                    solution.secondPart(inputFile);
                }
                break;
            default: throw new RuntimeException("Day %d not implemented".formatted(day));
        }
    }
}
