package solutions.day9;

import org.checkerframework.checker.units.qual.A;
import solutions.Solution;
import solutions.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day9 implements Solution {
    @Override
    public void firstPart(String inputFile) {
        final var diskLayout = readDiskLayout(Utils.readInputAsStream(inputFile).toList().get(0));
        System.out.println(printDiskLayout(diskLayout));
        final var compactedDisk = performCompaction(diskLayout);
        System.out.println(printDiskLayout(compactedDisk));
        long checksum = calculateChecksum(compactedDisk);
        System.out.printf("Disk checksum: %d", checksum);

    }

    private long calculateChecksum(List<Optional<DiskBlock>> compactedDisk) {
        long checksum = 0;
        for (int i = 0; i < compactedDisk.size(); i++) {
            final var maybeBlock = compactedDisk.get(i);
            if (maybeBlock.isPresent()) {
                checksum += i * maybeBlock.get().id;
            }
        }
        return checksum;
    }

    private List<Optional<DiskBlock>> performCompaction(List<Optional<DiskBlock>> diskLayout) {
        List<DiskBlock> replacementBlocks = new ArrayList<>(diskLayout.stream()
                                                                    .filter(Optional::isPresent)
                                                                    .map(Optional::get)
                                                                    .toList());
        List<Optional<DiskBlock>> compactedDiskLayout = new ArrayList<>();
        int totalBlocks = replacementBlocks.size();
        // Reverse replacement blocks as we need to start from the back.
        Collections.reverse(replacementBlocks);
        for (int i = 0; i < totalBlocks; i++) {
            if (diskLayout.get(i).isPresent()) {
                compactedDiskLayout.add(diskLayout.get(i));
            } else {
                compactedDiskLayout.add(Optional.of(replacementBlocks.removeFirst()));
            }
        }
        // After all replacements, we need to append empty blocks
        while (compactedDiskLayout.size() < diskLayout.size()) {
            compactedDiskLayout.add(Optional.empty());
        }
        return compactedDiskLayout;
    }

    @Override
    public void secondPart(String inputFile) {

    }

    private static List<Optional<DiskBlock>> readDiskLayout(String input) {
        List<Character> chars = input.chars().mapToObj(c -> (char) c).toList();
        List<Optional<DiskBlock>> diskLayout = new ArrayList<>();
        for (int i = 0; i < chars.size(); i++) {
            if (i % 2 == 0) {
                int count = Integer.parseInt(String.valueOf(chars.get(i)));
                int id = i / 2;
                IntStream.range(0, count)
                        .forEach(_x -> diskLayout.add(Optional.of(new DiskBlock(id))));
            } else {
                int count = Integer.parseInt(String.valueOf(chars.get(i)));
                IntStream.range(0, count).forEach(_x -> diskLayout.add(Optional.empty()));
            }
        }
        return diskLayout;
    }

    private static String printDiskLayout(List<Optional<DiskBlock>> diskLayout) {
        return diskLayout.stream()
                .map(maybeBlock -> maybeBlock.isPresent() ? String.valueOf(maybeBlock.get().id) :
                        ".")
                .collect(Collectors.joining(""));

    }

    record DiskBlock(int id) {

    }
}
