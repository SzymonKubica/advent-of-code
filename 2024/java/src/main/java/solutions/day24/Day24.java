package solutions.day24;

import lombok.EqualsAndHashCode;
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
        List<Wire> wiresToEvaluate = allWires.stream().filter(Predicate.not(gatesDependingOnWire::containsKey)).filter(Wire::isOutput).collect(Collectors.toList());

        while (!wiresToEvaluate.isEmpty()) {
            Wire current = wiresToEvaluate.removeFirst();
            // We pick a wire and try to evaluate it, if the inputs are not available, we add them to the queue
            // and put the wire at the end
            if (current.value.isPresent()) {
                continue;
            }

            Gate gateDefiningWireValue = wireValueGateMap.get(current);
            System.out.println(gateDefiningWireValue);

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
            System.out.println(wire);
        }

        List<Wire> outputWires = allWires.stream().filter(Wire::isOutput).sorted(Comparator.comparing(wire -> wire.name)).toList();

        System.out.println("Output wires: ");
        System.out.println(Utils.toStringLineByLine(outputWires));

        long output = processOutputBits(outputWires);

        System.out.println("Output of the circuit: %d".formatted(output));


    }

    private long processOutputBits(List<Wire> outputWires) {
        return outputWires.stream().map(wire -> wire.value.get() ? (long) Math.pow(2, outputWires.indexOf(wire)) : 0).reduce(Long::sum).get();
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

        System.out.println(gates);

        return Pair.of(wireNameToWireMap, gates);
    }

    @Override
    public void secondPart(String inputFile) {

        Pair<Map<String, Wire>, List<Gate>> wireMapAndGates = processInput(Utils.readInputAsStream(
                inputFile).collect(Collectors.joining("\n")));

        Map<String, Wire> wireMap = wireMapAndGates.getLeft();
        List<Gate> gates = wireMapAndGates.getRight();

        System.out.println(gates.size());

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
        Wire outputWire = wireNameToWireMap.getOrDefault(output, new Wire(output, Optional.empty()));
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

    record Gate(Wire left, Wire right, Wire output, Operation operation) {
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
    }

    enum Operation {
        AND,
        OR,
        XOR,
    }
}
