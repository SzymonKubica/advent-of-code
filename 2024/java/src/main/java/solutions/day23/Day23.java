package solutions.day23;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import solutions.Solution;
import solutions.Utils;

import java.util.*;
import java.util.stream.Collectors;

public class Day23 implements Solution {
    @Override
    public void firstPart(String inputFile) {
        Map<Computer, List<Computer>> connectionMap = getConnectionMap(inputFile);

        // Now that we have the map, we need to go over the keys and see for which starting
        // points we can find a loop of 3 nodes.

        Set<ConnectedTriple> interConnectedTriples = connectionMap.keySet().stream()
                .map(computer -> tryToTraverse(computer, connectionMap)).flatMap(List::stream)
                .collect(Collectors.toSet());

        Set<ConnectedTriple> triplesWithChiefHistorian = interConnectedTriples.stream()
                .filter(triple -> triple.connectedComputers.stream()
                        .anyMatch(Computer::belongsToChiefHistorian)).collect(Collectors.toSet());

        System.out.println(
                "There are %d triples that contain a computer owned by the chief historian.".formatted(
                        triplesWithChiefHistorian.size()));


    }

    @Override
    public void secondPart(String inputFile) {
        Map<Computer, List<Computer>> connectionMap = getConnectionMap(inputFile);

        // The size of the largest set is limited by the max number of connections
        // going out of one node.

        int max = connectionMap.values().stream().map(List::size)
                .max(Comparator.comparingInt(x -> x)).get();

        System.out.println("Maximum possible size of the fully-connected set: %d".formatted(max));

        int[][] adjacencyMatrix = new int[connectionMap.size()][connectionMap.size()];

        List<Computer> allComputers = connectionMap.keySet().stream().sorted(Comparator.comparing(computer -> computer.name)).toList();
        System.out.println("There are %s distinct computers".formatted(allComputers.size()));
        Map<Computer, Integer> matrixKeyMap = allComputers.stream()
                .collect(Collectors.toMap(entry -> entry, allComputers::indexOf));

        for (final Computer computer : allComputers) {
            for (final var nb : connectionMap.get(computer)) {
                adjacencyMatrix[matrixKeyMap.get(computer)][matrixKeyMap.get(nb)] = 1;
            }
        }

        System.out.println(printAdjacencyMatrix(adjacencyMatrix));


    }

    private String printAdjacencyMatrix(int[][] matrix) {
        return Arrays.stream(matrix).map(row -> Arrays.stream(row).mapToObj(x -> x == 0 ? " " : String.valueOf(x)).collect(Collectors.joining())).collect(
                Collectors.joining("\n"));
    }

    private Map<Computer, List<Computer>> getConnectionMap(String inputFile) {
        List<Pair<Computer, Computer>> connections = Utils.readInputAsStream(inputFile)
                .map(this::parseComputerConnection).toList();

        Map<Computer, List<Computer>> connectionMap = new HashMap<>();

        for (final var connection : connections) {
            // We insert both directions as connections are not directed
            connectionMap.putIfAbsent(connection.getKey(), new ArrayList<>());
            connectionMap.get(connection.getKey()).add(connection.getValue());

            connectionMap.putIfAbsent(connection.getValue(), new ArrayList<>());
            connectionMap.get(connection.getValue()).add(connection.getKey());
        }
        return connectionMap;
    }

    private List<ConnectedTriple> tryToTraverse(
            Computer start,
            Map<Computer, List<Computer>> connectionMap
    ) {
        List<Computer> neighbours = connectionMap.get(start);

        final List<ConnectedTriple> output = new ArrayList<>();
        for (final Computer nb : neighbours) {
            for (final Computer nb2 : connectionMap.get(nb)) {
                if (connectionMap.get(nb2).contains(start)) {
                    output.add(new ConnectedTriple(List.of(start, nb, nb2)));
                }
            }
        }
        return output;
    }


    record ConnectedTriple(List<Computer> connectedComputers) {

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ConnectedTriple that = (ConnectedTriple) o;
            for (Computer c : that.connectedComputers) {
                if (!connectedComputers.contains(c)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            return connectedComputers.stream().map(Objects::hashCode).reduce(Integer::sum).get();
        }
    }

    record Computer(String name) {
        boolean belongsToChiefHistorian() {
            return this.name.charAt(0) == 't';
        }
    }

    private Pair<Computer, Computer> parseComputerConnection(String input) {
        String[] parts = input.split("-");
        assert parts.length == 2;
        return Pair.of(new Computer(parts[0]), new Computer(parts[1]));
    }
}
