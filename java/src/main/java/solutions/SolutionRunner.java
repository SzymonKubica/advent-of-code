package solutions;

import solutions.year2024.*;

import java.lang.reflect.InvocationTargetException;

public class SolutionRunner {
    public static void main(String[] args) {
        assert (args.length == 4) :
                "You need to specify the year, day, puzzle part and a path to the input file";

        int year = Integer.parseInt(args[0]);
        int day = Integer.parseInt(args[1]);
        int part = Integer.parseInt(args[2]);
        String inputFile = args[3];

        Solution solution;
        try {
            solution = (Solution) Class.forName("solutions.year%d.Day%d".formatted(year, day))
                    .getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                 IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        if (part == 1) {
            solution.firstPart(inputFile);
        } else {
            solution.secondPart(inputFile);
        }
    }
}
