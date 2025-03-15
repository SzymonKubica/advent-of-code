package solutions.day17;

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

        final Map<Register, Long> registerMap = computerConfiguration.getLeft();
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
        // Strategy: look at what the input program actually does and try to be smart about it.
        // Here is the input program after being interpreted:
        // 1. BST Register: A,     // Instruction BST: take the value of combo operand (in this
        // case resolved to reg A) modulo 8 and save in register B
        // 2. BXL Literal: 7,      // Instruction BXL: Register B := Register B XOR 7
        // 3. CDV Register: B,     // Instruction CDV: Register C := Register A / (2 ^ Register B)
        // 4. ADV Literal: 3,      // Instruction ADV: Register A := Register A / (2 ^ 3)
        // 5. BXL Literal: 7,      // Instruction BXL: Register B := Register B XOR 7
        // 6. BXC Literal: 1,      // Instruction BXC: Register B := Register B XOR Register C
        // 7. OUT Register: B,     // Output the value modulo 8
        // 8. JNZ Literal: 0,      // Jump back to start if A is not 0

        // Key observation: A is divided by 2 ^ 3 in each step, so in order to produce the output
        // 16 characters long,
        // we need to do at least 16 iterations and so A has to be at least 2 ^ (3 * 16) = 2 ^ 48

        // Note that registers B and C are zero at the start, which gives us an idea of how the
        // first iteration is going to behave:
        // Also, B always gets overwritten at the start, so its value does not matter. We can
        // therefore infer that
        // if our desired output is Program[bytes=[2, 4, 1, 7, 7, 5, 0, 3, 1, 7, 4, 1, 5, 5, 3, 0]]
        // We need that... <- this looks like it is not going anywhere. I think the best strategy
        // is to implement the loop
        // above in the native code and try to iterate.

        // One observation: our program has 0 at the end, whereas all of the outputs that I'm
        // getting
        // look like they have a 1 at the end, this indicates that we might want to try and match
        // that
        // one first.

        // Note that this number is the last output of the program, and it is the value of
        // B after it has been xored with C  which is defined as
        // (A / (2 ^ ((A % 8) XOR 7))) % 8
        // And for that to be equal to 1 (modulo 8) we need that:
        // B ^ (A / (2 ^ ((A % 8) XOR 7))) % 8 =
        // = (A % 8) ^ (A / (2 ^ ((A % 8) XOR 7)))
        // Note that for a 3 bit number a XOR with 7 is effectively a bit flip.

        // Key observation: when we divide A by 8 we effectively get rid of the last 3 bits of
        // the program
        // also given that above we are doing modulo 8, we care only about the last 3 bits anyway.
        // Hence, in order to solve the puzzle, we need to find inputs that give the respective
        // output
        // digits in reverse order and then assemble the final number.

        // I think I found it: basically: the only way to get the last digit to be 0 is for A to
        // be a power of 2 this is because of how the calculation flows and the search below
        // demonstrates it:
        /*
        // After running the simulation below I found that only 0, 7 values of the lowest 3 bits
        of A
        // give us 0 as the output.
        // Hence, in order for A to be good, we need to have that when we divide it by 8 16 times
         (with truncation)
        // then it is either 0 or 7 modulo 8. Can we create all such numbers?
        // Yes, because that lossy division by 8 is effectively the truncation of the last 3
        bits, so in order for
        // our program to produce any good outputs, we need to have 111 in the top 3 bits (we
        cannot have 000)
        // because it would be too low and then not produce 16 iterations

        Map<Integer, Integer> outputToInputMap = new HashMap<>();
        for (int aOverride = 0; aOverride <= 8; aOverride++) {
            long A = aOverride;
            long B = 0;
            long C = 0;

            B = A % 8;
            B = B ^ 7;
            C = A / ((long) Math.pow(2, B));
            A = A / 8;
            B = B ^ 7;
            B = B ^ C;

            int output = (int) B % 8;
            outputToInputMap.put(output, aOverride);

            System.out.println("Given input A: %d, the program produces: %d".formatted(aOverride,
             output));
        }
         */

        // The idea is that we take the initial list of promising candidates. Then for each of them
        // we iteratively try to find the next digit. If we find it, we proceed.
        // The process continues as long as we get stuck (i.e. it is not possible to find the next
        // 3 bits that would make the output match
        final var computerConfiguration =
                getComputerConfiguration(Utils.readInputAsStream(inputFile));

        final Program program = computerConfiguration.getRight();


        // Running the code above got us the following 4 numbers that make the first part of the
        // output match the expectation:
        // 15801536, 15801544, 15801550, 15801568, 15834113, 15834121, 15834126, 15834304,
        // 15834312, 15834318, 15834336, 15932608, 15932616, 15932622, 15932640
        // We can use those as a starting point for our search.
        // All of those look quite close together, which indicates that we need to start with the
        // lowest one.
        List<Integer> promisingCandidates = List.of(15801536,
                                                    15801544,
                                                    15801550,
                                                    15801568,
                                                    15834113,
                                                    15834121,
                                                    15834126,
                                                    15834304,
                                                    15834312,
                                                    15834318,
                                                    15834336,
                                                    15932608,
                                                    15932616,
                                                    15932622,
                                                    15932640);

        for (int candidate : promisingCandidates) {

            long shiftedCandidate = ((long) candidate) << 3;
            int matchedOutputLength = 9;
            int steps = 0;

            boolean unableToMatch = false;
            System.out.println(shiftedCandidate);
            while (!unableToMatch) {
                System.out.println("Register override: " + shiftedCandidate);
                List<Integer> output = performHardCodedProgramCalculation(shiftedCandidate);

                if (steps > 8) {
                    unableToMatch = true;
                }

                System.out.println(printOutput(output));
                System.out.println(printOutput(program.bytes.subList(program.bytes.size()
                                                                     - (matchedOutputLength),
                                                                     program.bytes.size())));
                if (Objects.equals(printOutput(output),
                                   printOutput(program.bytes.subList(program.bytes.size()
                                                                     - (matchedOutputLength),
                                                                     program.bytes.size())))) {
                    //if (Objects.equals(printOutput(output), printOutput(program.bytes))) {
                    System.out.println("Match found!");
                    System.out.println(shiftedCandidate);
                    System.out.println(output);
                    matchedOutputLength += 1;
                    shiftedCandidate <<= 3;
                }

                steps++;
                shiftedCandidate++;
            }

            System.out.println("Finished processing candidate: %d. Final output: %s".formatted(shiftedCandidate, printOutput(performHardCodedProgramCalculation(shiftedCandidate))));
        }

        if (true) {
            boolean matchFound = false;
            int index = 0;
            long registerAOverride = ((long) promisingCandidates.stream()
                    .min(Comparator.comparingInt(e -> e))
                    .get()) << 3;
            // We can repeatedly keep finding the required beginnings
            registerAOverride = 1011298328L << 3;
            registerAOverride = 8090386630L << 3;
            registerAOverride = 64856528481L << 3;
            registerAOverride = 518852228497L << 3;
            registerAOverride = 4150817827976L << 3;
            registerAOverride = 33206542623812L << 3;
            // TODO: automate this

            //registerAOverride = 126412291 << 3;
            //registerAOverride = ((long) promisingCandidates.get(index)) << 24;
            //long registerAOverride = (long) 0b111 << 45;
            //long registerAOverride = (long) 246852 << 30;
            //long registerAOverride = (long) 3760 << 36;

            // We implement the calc in the native code:
            System.out.println();
            while (!matchFound) {
                //registerAOverride = ((long) promisingCandidates.get(index)) << 24;
                System.out.println("Register override: " + registerAOverride);
                List<Integer> output = performHardCodedProgramCalculation(registerAOverride);
                //if (output.size() > 10) {
                //if (output.size() > 11) {
                if (output.size() > 16) {
                    matchFound = true;
                }

                if (true) {
                    //return;
                }

                //System.out.println(registerAOverride >> 24);
                System.out.println(output);

                //System.out.println(program.bytes);
                if (Objects.equals(printOutput(output),
                                   printOutput(program.bytes.subList(program.bytes.size() - 16,
                                                                     program.bytes.size())))) {
                    //if (Objects.equals(printOutput(output), printOutput(program.bytes))) {
                    System.out.println("Match found!");
                    System.out.println(registerAOverride);
                    System.out.println(output);
                    promisingCandidates.add((int) registerAOverride);
                }

                registerAOverride++;
            }
            System.out.println(promisingCandidates);


            while (!matchFound) {
                System.out.println(
                        "Testing if overriding A to %d will make the program reproduce itself.".formatted(
                                registerAOverride));
                final Map<Register, Long> registerMap = computerConfiguration.getLeft();

                System.out.println(registerMap);
                System.out.println(program);

                ProgramState state = new ProgramState(registerMap, program);

                System.out.println(state.visualizeProgram());

                registerMap.put(Register.A, registerAOverride);

                while (executeInstruction(state)) {
                }

                System.out.println("Output: %s".formatted(printOutput(state.output)));

                if (Objects.equals(printOutput(state.output), printOutput(state.program.bytes))) {
                    matchFound = true;
                    System.out.println("Match found!");
                }

                registerAOverride++;
            }
        }
    }

    private static List<Integer> performHardCodedProgramCalculation(long registerAOverride) {
        long A = registerAOverride;
        long B = 0;
        long C = 0;
        List<Integer> output = new ArrayList<>();

        //System.out.println("Testing %s...".formatted(registerAOverride));
        while (A != 0) {
            B = A % 8;
            B = B ^ 7;
            C = A / ((long) Math.pow(2, B));
            A = A / 8;
            B = B ^ 7;
            B = B ^ C;
            output.add((int) (B % 8));
        }
        return output;
    }

    private static <T> String printOutput(List<T> output) {
        return output.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private boolean executeInstruction(ProgramState state) {
        if (state.instructionPointer >= state.program.bytes.size()) {
            System.out.println("Halting the CPU");
            return false;
        }
        int opcode = state.program.bytes.get(state.instructionPointer);
        int operand = state.program.bytes.get(state.instructionPointer + 1);
        Instruction instruction = Instruction.fromOpcode(opcode);
        //System.out.println("Parsed instruction: %s".formatted(instruction.name().toLowerCase()));

        switch (instruction) {
            case ADV -> {
                long numerator = state.registerMap.get(Register.A);
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
                long numerator = state.registerMap.get(Register.A);
                int denominator = (int) Math.pow(2, resolveComboOperand(state, operand));
                state.registerMap.put(Register.B, numerator / denominator);
            }
            case CDV -> {
                long numerator = state.registerMap.get(Register.A);
                int denominator = (int) Math.pow(2, resolveComboOperand(state, operand));
                state.registerMap.put(Register.C, numerator / denominator);
            }
        }

        state.instructionPointer += 2;
        return true;
    }

    private long resolveComboOperand(ProgramState state, int operandValue) {
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

    private String visualizeComboOperand(int operandValue) {
        return switch (operandValue) {
            case 0, 1, 2, 3 -> "Literal: " + operandValue;
            case 4 -> "Register: A";
            case 5 -> "Register: B";
            case 6 -> "Register: C";
            default -> throw new IllegalStateException("Unexpected value: " + operandValue);
        };
    }


    private class ProgramState {
        final Map<Register, Long> registerMap;
        final Program program;
        int instructionPointer;
        List<Long> output;

        private ProgramState(Map<Register, Long> registerMap, Program program) {
            this.registerMap = registerMap;
            this.program = program;
            this.instructionPointer = 0;
            this.output = new ArrayList<>();
        }

        public String visualizeProgram() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < program.bytes.size(); i += 2) {
                int opcode = program.bytes.get(i);
                Instruction instruction = Instruction.fromOpcode(opcode);

                sb.append(instruction.name()).append(" ");
                int operand = program.bytes.get(i + 1);
                if (List.of(Instruction.ADV,
                            Instruction.BST,
                            Instruction.BDV,
                            Instruction.CDV,
                            Instruction.OUT).contains(instruction)) {
                    sb.append(visualizeComboOperand(program.bytes.get(i + 1)));
                } else {
                    sb.append("Literal: %d".formatted(operand));
                }
                sb.append(", ");
            }
            return sb.toString();
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


    private Pair<Map<Register, Long>, Program> getComputerConfiguration(Stream<String> input) {
        String allInput = input.collect(Collectors.joining("\n"));
        String[] inputSections = allInput.split("\n\n");
        return Pair.of(getRegisterMap(inputSections[0].lines().toList()),
                       Program.fromString(inputSections[1]));
    }

    private Map<Register, Long> getRegisterMap(List<String> input) {
        assert input.size() == 3;

        Map<Register, Long> output = new HashMap<>();
        output.put(Register.A, parseRegisterConfig(input.get(0)));
        output.put(Register.B, parseRegisterConfig(input.get(1)));
        output.put(Register.C, parseRegisterConfig(input.get(2)));
        return output;
    }

    private Long parseRegisterConfig(String s) {
        return Long.parseLong(s.split(" ")[2].trim());
    }

    enum Register {
        A, B, C
    }


    record Program(List<Integer> bytes) {
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
