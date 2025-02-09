package solutions.day17;

import jdk.jshell.execution.Util;
import org.apache.commons.lang3.tuple.Pair;
import solutions.Solution;
import solutions.Utils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day17 implements Solution {
    @Override
    public void firstPart(String inputFile) {
        final var computerConfiguration =
                getComputerConfiguration(Utils.readInputAsStream(inputFile));

        final Map<Register, Integer> registerMap = computerConfiguration.getLeft();
        final Program program = computerConfiguration.getRight();

        System.out.println(registerMap);
        System.out.println(program);

        ProgramState state = new ProgramState(registerMap, program);

        int counter = 0;
        while (executeInstruction(state)) {
            counter++;
            System.out.println("Executed instructions: %d".formatted(counter));
        }

        System.out.println("Output: %s".formatted(state.output.stream()
                                                          .map(String::valueOf)
                                                          .collect(Collectors.joining(","))));
    }

    @Override
    public void secondPart(String inputFile) {
        final var computerConfiguration =
                getComputerConfiguration(Utils.readInputAsStream(inputFile));

        boolean matchFound = false;
        int registerAOverride = 0;
        while (!matchFound) {
            System.out.println(
                    "Testing if overriding A to %d will make the program reproduce itself.".formatted(
                            registerAOverride));
            final Map<Register, Integer> registerMap = computerConfiguration.getLeft();
            final Program program = computerConfiguration.getRight();

            System.out.println(registerMap);
            System.out.println(program);

            ProgramState state = new ProgramState(registerMap, program);
            registerMap.put(Register.A, registerAOverride);

            while (executeInstruction(state)) {
            }

            System.out.println("Output: %s".formatted(printOutput(state.output)));

            if (Objects.equals(printOutput(state.output), printOutput(state.program.bits))) {
                matchFound = true;
                System.out.println("Match found!");
            }

            registerAOverride++;
        }
    }

    private static String printOutput(List<Integer> output) {
        return output.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private boolean executeInstruction(ProgramState state) {
        if (state.instructionPointer >= state.program.bits.size()) {
            System.out.println("Halting the CPU");
            return false;
        }
        int opcode = state.program.bits.get(state.instructionPointer);
        int operand = state.program.bits.get(state.instructionPointer + 1);
        Instruction instruction = Instruction.fromOpcode(opcode);
        //System.out.println("Parsed instruction: %s".formatted(instruction.name().toLowerCase()));

        switch (instruction) {
            case ADV -> {
                int numerator = state.registerMap.get(Register.A);
                int denominator = (int) Math.pow(2, resolveComboOperand(state, operand));
                state.registerMap.put(Register.A, numerator / denominator);
            }
            case BXL ->
                    state.registerMap.put(Register.B, state.registerMap.get(Register.B) ^ operand);
            case BST -> state.registerMap.put(Register.B, resolveComboOperand(state, operand) % 8);
            case JNZ -> {
                boolean shouldJump = state.registerMap.get(Register.A) != 0;
                if (shouldJump) {
                    state.instructionPointer = operand;
                    return true;
                }
            }
            case BXC -> {
                state.registerMap.put(Register.B,
                                      state.registerMap.get(Register.B) ^ state.registerMap.get(
                                              Register.C));
            }
            case OUT -> {
                state.output.add(resolveComboOperand(state, operand) % 8);
            }
            case BDV -> {
                int numerator = state.registerMap.get(Register.A);
                int denominator = (int) Math.pow(2, resolveComboOperand(state, operand));
                state.registerMap.put(Register.B, numerator / denominator);
            }
            case CDV -> {
                int numerator = state.registerMap.get(Register.A);
                int denominator = (int) Math.pow(2, resolveComboOperand(state, operand));
                state.registerMap.put(Register.C, numerator / denominator);
            }
        }

        state.instructionPointer += 2;
        return true;
    }

    private int resolveComboOperand(ProgramState state, int operandValue) {
        return switch (operandValue) {
            case 0 -> 0;
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 3;
            case 4 -> state.registerMap.get(Register.A);
            case 5 -> state.registerMap.get(Register.B);
            case 6 -> state.registerMap.get(Register.C);
            default -> throw new IllegalStateException("Unexpected value: " + operandValue);
        };
    }


    private class ProgramState {
        final Map<Register, Integer> registerMap;
        final Program program;
        int instructionPointer;
        List<Integer> output;

        private ProgramState(Map<Register, Integer> registerMap, Program program) {
            this.registerMap = registerMap;
            this.program = program;
            this.instructionPointer = 0;
            this.output = new ArrayList<>();
        }
    }

    private enum Instruction {
        ADV(0), BXL(1), BST(2), JNZ(3), BXC(4), OUT(5), BDV(6), CDV(7);

        int opcode;

        Instruction(int opcode) {
            this.opcode = opcode;
        }

        static Instruction fromOpcode(int opcode) {
            assert 0 <= opcode && opcode <= 7 : "Opcode needs to be between 0 and 7 inclusive";
            return Arrays.stream(Instruction.values())
                    .filter(instr -> instr.opcode == opcode)
                    .toList()
                    .get(0);
        }
    }


    private Pair<Map<Register, Integer>, Program> getComputerConfiguration(Stream<String> input) {
        String allInput = input.collect(Collectors.joining("\n"));
        String[] inputSections = allInput.split("\n\n");
        return Pair.of(getRegisterMap(inputSections[0].lines().toList()),
                       Program.fromString(inputSections[1]));
    }

    private Map<Register, Integer> getRegisterMap(List<String> input) {
        assert input.size() == 3;

        Map<Register, Integer> output = new HashMap<>();
        output.put(Register.A, parseRegisterConfig(input.get(0)));
        output.put(Register.B, parseRegisterConfig(input.get(1)));
        output.put(Register.C, parseRegisterConfig(input.get(2)));
        return output;
    }

    private Integer parseRegisterConfig(String s) {
        return Integer.parseInt(s.split(" ")[2].trim());
    }

    enum Register {
        A, B, C
    }


    record Program(List<Integer> bits) {
        static Program fromString(String input) {
            assert input.matches("Program: \\d+(,\\d)") :
                    "The input has to look like 'Program: 1,3,4,'";
            return new Program(Arrays.stream(input.split(" ")[1].split(","))
                                       .map(String::trim)
                                       .map(Integer::parseInt)
                                       .toList());
        }
    }


}
