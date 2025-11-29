package solutions.year2024;

import com.google.common.collect.Streams;
import solutions.Solution;
import solutions.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class Day7 implements Solution {
    @Override
    public void firstPart(String inputFile) {
        final Set<BinaryOperation> availableOperations = Set.of(BinaryOperation.ADD,
                                                                BinaryOperation.MULTIPLY);
        runParameterizableSolution(inputFile, availableOperations);
    }

    @Override
    public void secondPart(String inputFile) {
        final Set<BinaryOperation> availableOperations = Set.of(BinaryOperation.ADD,
                                                                BinaryOperation.MULTIPLY,
                                                                BinaryOperation.CONCATENATE);
        runParameterizableSolution(inputFile, availableOperations);
    }

    private static void runParameterizableSolution(
            String inputFile,
            Set<BinaryOperation> availableOperations
    ) {
        final List<CalibrationEquation> equations = Utils.readInputAsStream(inputFile)
                .map(CalibrationEquation::fromString)
                .toList();

        System.out.printf("All equations:\n%s\n", Utils.toStringLineByLine(equations));

        List<CalibrationEquation> satisfiableEquations = equations.stream()
                .filter(eq -> eq.canBeTrueWith(availableOperations))
                .toList();

        System.out.printf("Satisfiable equations:\n%s\n",
                          Utils.toStringLineByLine(satisfiableEquations));

        long sumOfTestValues = satisfiableEquations.stream()
                .map(CalibrationEquation::result)
                .reduce(Long::sum)
                .get();

        System.out.printf("Sum of test values: %d\n", sumOfTestValues);
    }


    public static List<List<BinaryOperation>> getAllPossibleBinaryOperationAssignmentsOfLength(
            Set<BinaryOperation> availableOperations,
            int length
    ) {
        if (length == 1) {
            return availableOperations.stream().map(List::of).toList();
        }

        return getAllPossibleBinaryOperationAssignmentsOfLength(availableOperations,
                                                                length - 1).stream()
                .map(assignment -> availableOperations.stream()
                        .map(op -> Streams.concat(assignment.stream(), Stream.of(op)).toList())
                        .toList())
                .flatMap(List::stream)
                .toList();
    }

    record CalibrationEquation(long result, List<Long> parameters) {
        public static CalibrationEquation fromString(String line) {
            assert line.contains(":") :
                    "Calibration equation string needs to be of the format: 'result: x1 x2 x3 ....";

            List<String> resultAndParams = Arrays.stream(line.split(": ")).toList();
            assert resultAndParams.size() == 2 :
                    "Both the equation result and the parameters need to be provided";
            return new CalibrationEquation(Long.parseLong(resultAndParams.get(0)),
                                           Arrays.stream(resultAndParams.get(1).split(" "))
                                                   .filter(s -> !s.isBlank())
                                                   .map(Long::parseLong)
                                                   .toList());
        }

        public boolean canBeTrueWith(Set<BinaryOperation> availableOperations) {
            List<List<BinaryOperation>> possibleAssignments =
                    getAllPossibleBinaryOperationAssignmentsOfLength(
                    availableOperations,
                    parameters.size() - 1);
            return possibleAssignments.stream()
                    .anyMatch(assignment -> result == evaluateAssignment(assignment));
        }

        public long evaluateAssignment(List<BinaryOperation> operations) {
            assert operations.size() == parameters.size() - 1;
            long first = parameters.getFirst();
            for (int i = 1; i < parameters.size(); i++) {
                long second = parameters.get(i);
                BinaryOperation op = operations.get(i - 1);
                first = op.apply(first, second);
            }
            return first;
        }
    }

    public enum BinaryOperation {
        ADD, MULTIPLY, CONCATENATE;

        public long apply(long x1, long x2) {
            return switch (this) {
                case ADD -> x1 + x2;
                case MULTIPLY -> x1 * x2;
                case CONCATENATE -> Long.parseLong(String.valueOf(x1) + x2);
            };
        }
    }
}
