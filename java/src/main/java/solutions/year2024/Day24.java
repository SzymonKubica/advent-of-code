package solutions.year2024;

import org.apache.commons.lang3.tuple.Pair;
import solutions.Solution;
import solutions.Utils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Day24 implements Solution {
    @Override
    public void firstPart(String inputFile) {
        Pair<Map<String, Wire>, List<Gate>> wireMapAndGates = processInput(Utils.readInputAsStream(
                inputFile).collect(Collectors.joining("\n")));

        Map<String, Wire> wireMap = wireMapAndGates.getLeft();
        List<Gate> gates = wireMapAndGates.getRight();

        // Find all wires that something else depends on
        Set<Wire> allWires = runCircuit(gates, wireMap);

        List<Wire> outputWires = allWires.stream().filter(Wire::isOutput)
                .sorted(Comparator.comparing(wire -> wire.name)).toList();

        System.out.println("Output wires: ");
        System.out.println(Utils.toStringLineByLine(outputWires));

        long output = processOutputBits(outputWires);

        System.out.println("Output of the circuit: %d".formatted(output));


    }

    private static Set<Wire> runCircuit(List<Gate> gates, Map<String, Wire> wireMap) {
        Map<Wire, List<Gate>> gatesDependingOnWire = new HashMap<>();

        for (final var gate : gates) {
            // If the wire is found for the first time, we need to initialize an entry in the map.
            gatesDependingOnWire.putIfAbsent(gate.left, new ArrayList<>());
            gatesDependingOnWire.putIfAbsent(gate.right, new ArrayList<>());

            // Mark the current gate as depending on that wire
            gatesDependingOnWire.get(gate.left).add(gate);
            gatesDependingOnWire.get(gate.right).add(gate);
        }

        // Now we need to find the map from wire to the output gate that defines its value

        Map<Wire, Gate> wireValueGateMap = new HashMap<>();

        for (final var gate : gates) {
            wireValueGateMap.put(gate.output, gate);
        }

        // Now we get the set of all wires
        Set<Wire> allWires = new HashSet<>();
        allWires.addAll(wireMap.values());

        for (final var gate : gates) {
            allWires.add(gate.left);
            allWires.add(gate.right);
            allWires.add(gate.output);
        }

        // Now we start evaluating from the wires that nothing depends on.
        List<Wire> wiresToEvaluate = allWires.stream()
                .filter(Predicate.not(gatesDependingOnWire::containsKey)).filter(Wire::isOutput)
                .collect(Collectors.toList());

        while (!wiresToEvaluate.isEmpty()) {
            Wire current = wiresToEvaluate.removeFirst();
            // We pick a wire and try to evaluate it, if the inputs are not available, we add
            // them to the queue
            // and put the wire at the end
            if (current.value.isPresent()) {
                continue;
            }

            Gate gateDefiningWireValue = wireValueGateMap.get(current);
            //System.out.println(gateDefiningWireValue);

            if (gateDefiningWireValue.isReady()) {
                gateDefiningWireValue.evaluate();
            } else {
                Wire left = gateDefiningWireValue.left;
                Wire right = gateDefiningWireValue.right;
                if (left.value.isEmpty() && !wiresToEvaluate.contains(left)) {
                    wiresToEvaluate.add(gateDefiningWireValue.left);
                }
                if (right.value.isEmpty() && !wiresToEvaluate.contains(right)) {
                    wiresToEvaluate.add(gateDefiningWireValue.right);
                }
                wiresToEvaluate.add(current);
            }
        }

        for (final var wire : allWires) {
            //System.out.println(wire);
        }
        return allWires;
    }

    private long processOutputBits(List<Wire> outputWires) {
        return outputWires.stream()
                .map(wire -> wire.value.get() ? (long) Math.pow(2, outputWires.indexOf(wire)) : 0)
                .reduce(Long::sum).get();
    }

    private Pair<Map<String, Wire>, List<Gate>> processInput(String input) {
        System.out.println(input);
        String[] wiresAndGates = input.split("\n\n");

        List<Wire> wires = wiresAndGates[0].lines().map(Wire::fromString).toList();

        Map<String, Wire> wireNameToWireMap = wires.stream()
                .collect(Collectors.groupingBy(wire -> wire.name)).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get(0)));

        List<Gate> gates = wiresAndGates[1].lines().map(line -> parseGate(line, wireNameToWireMap))
                .toList();

        //System.out.println(gates);

        return Pair.of(wireNameToWireMap, gates);
    }

    @Override
    public void secondPart(String inputFile) {

        Pair<Map<String, Wire>, List<Gate>> wireMapAndGates = processInput(Utils.readInputAsStream(
                inputFile).collect(Collectors.joining("\n")));

        Map<String, Wire> wireMap = wireMapAndGates.getLeft();
        List<Gate> gates = new ArrayList<>(wireMapAndGates.getRight());

        List<Wire> outputWires = getWiresStartingWith(wireMap, "z");


        System.out.println();
        System.out.println(wireMap.keySet().stream().filter(s -> s.startsWith("z")).toList()
                                   .size());

        gates.sort(Gate::compareTo);
        //System.out.println(Utils.toStringLineByLine(gates));

        System.out.println(gates.size());


        // The idea is to walk the wire tree and identify constituent components of the adder
        // circuit, then identify places where it breaks.

        // For better debugging, lets visualize for each z gate what inputs flow into it

        Map<String, Gate> outputWireToGateMap = gates.stream()
                .collect(Collectors.groupingBy(gate -> gate.output.name)).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getFirst()));


        Set<Wire> allWires = runCircuit(gates, wireMap);

        List<Wire> xInputWires = getWiresStartingWith(wireMap, "x");
        List<Wire> yInputWires = getWiresStartingWith(wireMap, "y");

        String xInput = getBitString(xInputWires);
        String yInput = getBitString(yInputWires);
        String output = getBitString(outputWires);

        String formatString = "%%%ds".formatted(Math.max(xInput.length(), output.length()));
        System.out.println(formatString.formatted(xInput));
        System.out.println(formatString.formatted(yInput));
        System.out.println(output);

        // validate addition correctness
        int carry = 0;
        for (int i = 0; i < xInputWires.size(); i++) {
            int expected = extractBooleanIntValue(outputWires, i);
            int x = extractBooleanIntValue(xInputWires, i);
            int y = extractBooleanIntValue(yInputWires, i);

            int sum = x + y + carry;

            if (sum == 2) {
                carry = 1;
                sum = 0;
            } else if (sum == 3) {
                carry = 1;
                sum = 1;
            } else {
                // Reset the carry
                carry = 0;
            }

            if (sum != expected) {
                System.out.println("Mismatch found at bit %d (index %d)".formatted(i+1, i));
                System.out.println("Bit sum: %d, expected: %d".formatted(sum, expected));
            }
        }
        System.out.println("Carry: %d, final bit of output: %d".formatted(carry, extractBooleanIntValue(outputWires, xInputWires.size())));

        // First found the error where z07 should be mapped to rts
        // Second found the error where z12 should be mapped to jpj
        // Third found the error where chv should be mapped to vvw
        // Fourth found the error where kgj should be mapped to z26
        // The last bit is a carry so we cannot validate it in the same way
        for (final Wire outputWire : outputWires.subList(3, outputWires.size() -1)) {
            System.out.println("Tracing wire %s".formatted(outputWire.name));
            traceInputs(outputWire, outputWireToGateMap);
            validateAdderShape(outputWire, outputWireToGateMap);
        }

        List<String> gatesToSwap = List.of("z07", "rts", "z12", "jpj", "chv", "vvw", "kgj", "z26");

        gatesToSwap = gatesToSwap.stream().sorted().toList();

        System.out.println("Final output: %s".formatted(String.join(",", gatesToSwap)));



        // we need to narrow down the search space and only find the bits that are wrong.

    }

    private void validateAdderShape(Wire outputWire, Map<String, Gate> outputWireToGateMap) {
        Gate gate = outputWireToGateMap.get(outputWire.name);
        System.out.println(gate);

        assert gate.operation == Operation.XOR: "The top level operation of the output gate has to be a xor";

        Gate leftInputGate = outputWireToGateMap.get(gate.left.name);
        Gate rightInputGate = outputWireToGateMap.get(gate.right.name);

        boolean leftXorRightOr = leftInputGate.operation == Operation.XOR && rightInputGate.operation == Operation.OR;
        boolean rightXorLeftOr = leftInputGate.operation == Operation.OR && rightInputGate.operation == Operation.XOR;

        assert leftXorRightOr || rightXorLeftOr;

        if (leftXorRightOr) {
            validateAdderInputBitsXor(leftInputGate, outputWireToGateMap, outputWire);
            validateAdderCarryBitsOr(rightInputGate, outputWireToGateMap, outputWire);
        }
        if (rightXorLeftOr) {
            validateAdderInputBitsXor(rightInputGate, outputWireToGateMap, outputWire);
            validateAdderCarryBitsOr(leftInputGate, outputWireToGateMap, outputWire);
        }
    }

    /**
     * This validates that the second xor into the adder gate does a xor of
     * the input bits for the current output bit (i.e. for z02 we would expect to xor x02 and y02)
     */
    private void validateAdderCarryBitsOr(Gate gate,
                                          Map<String, Gate> outputWireToGateMap,
                                          Wire outputWire
    ) {
        System.out.println(gate);
        System.out.println(outputWire);
        System.out.println(gate);
        Gate leftInputGate = outputWireToGateMap.get(gate.left.name);
        Gate rightInputGate = outputWireToGateMap.get(gate.right.name);

        assert leftInputGate.operation == Operation.AND && rightInputGate.operation == Operation.AND : "OR expression from the carry bits needs to compose results from two AND gates. Happened at: %s".formatted(gate);

        // The rest of the checks will be done if this doesn't catch the issue
        boolean inputBitsHandledOnTheLeft = andLowerLevelInputBits(leftInputGate, outputWireToGateMap, outputWire);
        assert inputBitsHandledOnTheLeft ||  andLowerLevelInputBits(rightInputGate, outputWireToGateMap, outputWire);

        if (inputBitsHandledOnTheLeft) {
            assert xorPlusCarry(rightInputGate, outputWireToGateMap) : "Left gate handles input bits, right one needs to handle carry and xor.";
        } else {
            assert xorPlusCarry(leftInputGate, outputWireToGateMap) : "Right gate handles input bits, right one needs to handle carry and xor.";
        }

    }

    /**
     * This validates that the carry bit is correctly set up.
     * I.e. we would expect this gate to be a combination of an AND of input bits one level below
     * plus the carry bit from the lower level anded with the XOR of the input bits.
     */
    private void validateAdderInputBitsXor(Gate gate, Map<String, Gate> outputWireToGateMap, Wire outputWire) {
        assert gate.left.name.substring(1).equals(outputWire.name.substring(1));
        assert gate.right.name.substring(1).equals(outputWire.name.substring(1));
    }

    private boolean xorPlusCarry(Gate gate, Map<String, Gate> outputWireToGateMap) {
        Gate leftInputGate = outputWireToGateMap.get(gate.left.name);
        Gate rightInputGate = outputWireToGateMap.get(gate.right.name);

        if (leftInputGate.operation != Operation.XOR && rightInputGate.operation != Operation.XOR) {
            System.out.println("None of the input gates is a XOR");
            return false;
        }

        if (leftInputGate.operation != Operation.OR && rightInputGate.operation != Operation.OR) {
            System.out.println("None of the input gates is a OR required for carry bit.");
            return false;
        }

        return true;
    }

    private boolean andLowerLevelInputBits(
            Gate gate,
            Map<String, Gate> outputWireToGateMap,
            Wire outputWire
    ) {
        if (!(gate.left.name.startsWith("x") || gate.left.name.startsWith("y"))) {
            System.out.println("Left gate doesn't read input bit");
            return false;
        }

        if (!(gate.right.name.startsWith("x") || gate.right.name.startsWith("y"))) {
            System.out.println("Right gate doesn't read input bit");
            return false;
        }

        int level = Integer.parseInt(outputWire.name.substring(1));
        if (Integer.parseInt(gate.left.name.substring(1)) != level -1) {
            System.out.println("Left gate input bit has incorrect level");
            return false;
        }

        if (Integer.parseInt(gate.right.name.substring(1)) != level -1) {
            System.out.println("Right gate input bit has incorrect level");
            return false;
        }
        return true;
    }

    private static int extractBooleanIntValue(List<Wire> outputWires, int i) {
        return outputWires.get(i).value.get() ? 1 : 0;
    }

    private static String getBitString(List<Wire> wires) {
        String xInput = wires.reversed().stream().map(wire -> wire.value.get()).map(value -> value ? 1 : 0)
                .map(String::valueOf).collect(Collectors.joining());
        return xInput;
    }

    private static List<Wire> getWiresStartingWith(Map<String, Wire> wireMap, String letter) {
        List<Wire> outputWires = wireMap.keySet().stream().filter(s -> s.startsWith(letter))
                .toList().stream().map(wireMap::get)
                .sorted(Comparator.comparingInt(wire -> Integer.parseInt(wire.name.substring(1))))
                .collect(Collectors.toList());
        return outputWires;
    }

    private void traceInputs(Wire outputWire, Map<String, Gate> outputWireToGateMap) {
        if (outputWireToGateMap.containsKey(outputWire.name)) {
            Gate gate = outputWireToGateMap.get(outputWire.name);
            System.out.println(gate);
            traceInputs(gate.left, outputWireToGateMap);
            traceInputs(gate.right, outputWireToGateMap);
        }
    }

    private Gate parseGate(String input, Map<String, Wire> wireNameToWireMap) {
        String[] inputAndOutput = input.split(" -> ");
        String[] leftOperatorRight = inputAndOutput[0].split(" ");
        String output = inputAndOutput[1].trim();

        Wire left = wireNameToWireMap.getOrDefault(
                leftOperatorRight[0],
                new Wire(leftOperatorRight[0], Optional.empty())
        );
        wireNameToWireMap.put(left.name, left);
        Wire right = wireNameToWireMap.getOrDefault(
                leftOperatorRight[2],
                new Wire(leftOperatorRight[2], Optional.empty())
        );
        wireNameToWireMap.put(right.name, right);
        Wire outputWire = wireNameToWireMap.getOrDefault(
                output,
                new Wire(output, Optional.empty())
        );
        wireNameToWireMap.put(outputWire.name, outputWire);

        Operation operation = Operation.valueOf(leftOperatorRight[1]);

        return new Gate(left, right, outputWire, operation);
    }

    static final class Wire {
        private final String name;
        private Optional<Boolean> value;

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Wire wire = (Wire) o;
            return Objects.equals(name, wire.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        Wire(String name, Optional<Boolean> value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return "%s: %s".formatted(name, value.isPresent() ? (value.get() ? 1 : 0) : "_");
        }

        public static Wire fromString(String input) {
            String[] parts = input.split(": ");
            return new Wire(parts[0], Optional.of(parts[1].trim().equals("1")));
        }

        public boolean isOutput() {
            return name.startsWith("z");
        }
    }

    record Gate(Wire left, Wire right, Wire output, Operation operation)
            implements Comparable<Gate> {
        @Override
        public int compareTo(Gate gate) {
            return gate.left.name.compareTo(this.left.name);
        }

        public boolean isReady() {
            return left.value.isPresent() && right.value.isPresent();
        }

        public void evaluate() {
            assert isReady();

            boolean left = this.left.value.get();
            boolean right = this.right.value.get();
            output.value = Optional.of(switch (operation) {
                case AND -> left && right;
                case OR -> left || right;
                case XOR -> left ^ right;
            });
        }

        @Override
        public String toString() {
            return "%s <- %s %s %s".formatted(
                    output.name,
                    left.name,
                    operation.name(),
                    right.name
            );
        }
    }

    enum Operation {
        AND,
        OR,
        XOR,
    }
}
