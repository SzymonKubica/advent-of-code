package solutions.day24;

import org.apache.commons.lang3.tuple.Pair;
import solutions.Solution;
import solutions.Utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Day24 implements Solution {
    @Override
    public void firstPart(String inputFile) {
        Pair<Map<String, Wire>, List<Gate>> wireMapAndGates = processInput(Utils.readInputAsStream(
                inputFile).collect(Collectors.joining("\n")));
    }

    private Pair<Map<String, Wire>, List<Gate>> processInput(String input) {
        System.out.println(input);
        String[] wiresAndGates = input.split("\n\n");

        List<Wire> wires = wiresAndGates[0].lines().map(Wire::fromString).toList();

        Map<String, Wire> wireNameToWireMap = wires.stream()
                .collect(Collectors.groupingBy(wire -> wire.name)).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get(0)));

        List<Gate> gates = wiresAndGates[1].lines().map(line -> parseGate(line, wireNameToWireMap)).toList();

        System.out.println(gates);

        return Pair.of(wireNameToWireMap, gates);
    }

    @Override
    public void secondPart(String inputFile) {

    }

    private Gate parseGate(String input, Map<String, Wire> wireNameToWireMap) {
        String[] inputAndOutput = input.split(" -> ");
        String[] leftOperatorRight = inputAndOutput[0].split(" ");
        String output = inputAndOutput[1].trim();

        Wire left = wireNameToWireMap.getOrDefault(leftOperatorRight[0], new Wire(leftOperatorRight[0], false));
        Wire right = wireNameToWireMap.getOrDefault(leftOperatorRight[2], new Wire(leftOperatorRight[2], false));
        Wire outputWire = wireNameToWireMap.getOrDefault(output, new Wire(output, false));

        Operation operation = Operation.valueOf(leftOperatorRight[1]);

        return new Gate(left, right, outputWire, operation);
    }

    record Wire(String name, boolean value) {
        @Override
        public String toString() {
            return "%s: %d".formatted(name, value ? 1 : 0);
        }

        public static Wire fromString(String input) {
            String[] parts = input.split(": ");
            return new Wire(parts[0], parts[1].trim().equals("1"));
        }
    }

    record Gate(Wire left, Wire right, Wire output, Operation operation) {
    }

    enum Operation {
        AND,
        OR,
        XOR,
    }
}
