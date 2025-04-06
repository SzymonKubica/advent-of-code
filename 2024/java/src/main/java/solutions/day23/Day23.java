package solutions.day23;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import solutions.Solution;
import solutions.Utils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        List<Computer> allComputers = connectionMap.keySet().stream()
                .sorted(Comparator.comparing(computer -> computer.name)).toList();
        System.out.println("There are %s distinct computers".formatted(allComputers.size()));
        Map<Computer, Integer> matrixKeyMap = allComputers.stream()
                .collect(Collectors.toMap(entry -> entry, allComputers::indexOf));

        Map<Integer, Computer> matrixKeyReverseMap = allComputers.stream()
                .collect(Collectors.toMap( allComputers::indexOf, entry -> entry));

        for (final Computer computer : allComputers) {
            for (final var nb : connectionMap.get(computer)) {
                adjacencyMatrix[matrixKeyMap.get(computer)][matrixKeyMap.get(nb)] = 1;
            }
        }

        // The idea is that if we take a subset of n nodes from the graph and count the edges
        // that are there
        // if we see that the sum of the edges is  n * (n-1) (each edge is present twice in the
        // adjacency matrix),
        // we know that the graph is fully connected

        List<List<Integer>> completeSubGraphs = new ArrayList<>();
        for (final Computer computer : allComputers) {
            System.out.println("Processing computer %s. Progress (%d,%d)".formatted(computer, allComputers.indexOf(computer), allComputers.size()));
            final List<Computer> neighbours = connectionMap.get(computer);
            // We need to consider all subsets of neighbours plus the current computer
            // Note that this is bounded by 2^(max set size) which in case of our puzzle input is
            // 2^14 (should be doable)
            final List<Computer> subNetwork = Stream.concat(
                    Stream.of(computer),
                    neighbours.stream()
            ).toList();
            System.out.println("Subnetwork starting from this computer has size %d".formatted(subNetwork.size()));
            final List<List<Integer>> completeSubsets = getCompleteSubsets(subNetwork.stream().map(matrixKeyMap::get).toList(), adjacencyMatrix, new HashSet<>());
            completeSubGraphs.addAll(completeSubsets);
            System.out.println("Found %d complete subsets".formatted(completeSubsets.size()));
        }

        List<List<Integer>> sortedSubGraphs = completeSubGraphs.stream()
                .sorted(Comparator.comparingInt(List::size)).toList();

        List<Integer> largestCompleteGraph = sortedSubGraphs.getLast();

        System.out.println(largestCompleteGraph);

        List<Computer> largestCompleteSubNetwork = largestCompleteGraph.stream().map(matrixKeyReverseMap::get).toList();

        String password = largestCompleteSubNetwork.stream().sorted(Comparator.comparing(computer -> computer.name)).map(Computer::name).collect(
                Collectors.joining(","));

        System.out.println("The password is: %s".formatted(password));
    }

    private boolean isComplete(
            List<Integer> subset,
            int[][] adjacencyMatrix
    ) {
        processedSubsets ++;
        //System.out.println("%d: Checking if %s is complete".formatted(processedSubsets, subset));
        int n = subset.size();
        int totalEdges = 0;
        for (int index : subset) {
            int[] row = adjacencyMatrix[index];
            for (int rowIndex : subset) {
                totalEdges += row[rowIndex];
            }
        }

        return totalEdges == n * (n - 1);
    }

    static int processedSubsets = 0;

    private List<List<Integer>> getCompleteSubsets(List<Integer> subNetwork, int[][] adjacencyMatrix, Set<List<Integer>> alreadyProcessed) {
        if (alreadyProcessed.contains(subNetwork)) {
            return List.of();
        }
        alreadyProcessed.add(subNetwork);
        if (subNetwork.size() == 1) {
            return List.of();
        }
        if (isComplete(subNetwork, adjacencyMatrix)) {
            return List.of(subNetwork);
        }

        List<List<Integer>> output = new ArrayList<>();
        for (int i = 0; i < subNetwork.size() - 1; i++) {
            output.addAll(getCompleteSubsets(Stream.concat(
                    subNetwork.subList(0, i).stream(),
                    subNetwork.subList(i + 1, subNetwork.size()).stream()
            ).toList(), adjacencyMatrix, alreadyProcessed));
        }
        return output;
    }

    private String printAdjacencyMatrix(int[][] matrix) {
        return Arrays.stream(matrix)
                .map(row -> Arrays.stream(row).mapToObj(x -> x == 0 ? " " : String.valueOf(x))
                        .collect(Collectors.joining())).collect(Collectors.joining("\n"));
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
