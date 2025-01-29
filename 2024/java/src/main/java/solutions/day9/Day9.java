package solutions.day9;

import org.checkerframework.checker.units.qual.A;
import solutions.Solution;
import solutions.Utils;

import java.util.*;
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

    @Override
    public void secondPart(String inputFile) {
        final var diskLayout = readDiskLayout(Utils.readInputAsStream(inputFile).toList().get(0));
        System.out.println(printDiskLayout(diskLayout));
        final var compactedDisk = performCompactionChunked(diskLayout);
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

    private List<Optional<DiskBlock>> performCompactionChunked(List<Optional<DiskBlock>> diskLayout) {
        List<Optional<File>> diskLayoutFilesInChunks = assembleFiles(diskLayout);
        System.out.println(printChunkedFiles(diskLayoutFilesInChunks));

        List<Optional<DiskBlock>> output = new ArrayList<>(diskLayout);

        Map<Integer, Integer> candidatesOriginalPositions = new HashMap<>();


        List<File> candidatesToMove = new ArrayList<>(diskLayoutFilesInChunks.stream().filter(Optional::isPresent).map(Optional::get).toList());
        for (File candidate: candidatesToMove) {
            for (int i = 0; i < output.size(); i++) {
                if (output.get(i).isPresent() && output.get(i).get().id == candidate.id) {
                    candidatesOriginalPositions.put(candidate.id, i);
                }
            }
        }
        Collections.reverse(candidatesToMove);
        for (File candidate: candidatesToMove) {
            for (int i = 0; i < candidatesOriginalPositions.get(candidate.id); i++) {
                if (i+candidate.length <= output.size() && output.subList(i, i+candidate.length).stream().allMatch(Optional::isEmpty)) {
                    for(int j = i; j < output.size(); j++){
                        if (j < i+candidate.length) {
                            output.set(j, Optional.of(new DiskBlock(candidate.id)));
                        } else if (output.get(j).isPresent() && output.get(j).get().id == candidate.id) {
                            // Clean up the old location of the block.
                            output.set(j, Optional.empty());
                        }
                    }
                    break;
                }
            }
        }
        return output;
    }

    private String printChunkedFiles(List<Optional<File>> files) {
        return files.stream().map(maybeFile -> maybeFile.isPresent() ? String.valueOf(maybeFile.get().id).repeat(maybeFile.get().length) : ".").collect(
                Collectors.joining(""));
    }

    private List<Optional<File>> assembleFiles(List<Optional<DiskBlock>> diskLayout) {
        List<Optional<File>> output = new ArrayList<>();

        Optional<DiskBlock> lastBlock = Optional.empty();
        int streak = 0;
        for (final var maybeFileBlock : diskLayout) {
            if (maybeFileBlock.isEmpty()) {
                if (lastBlock.isPresent()) {
                    output.add(Optional.of(new File(lastBlock.get().id, streak)));
                }
                output.add(Optional.empty());
                lastBlock = maybeFileBlock;
                streak = 0;
            } else {
                if (lastBlock.isEmpty() || lastBlock.get().id == maybeFileBlock.get().id) {
                    streak++;
                    lastBlock = maybeFileBlock;
                } else if (lastBlock.get().id != maybeFileBlock.get().id) {
                    output.add(Optional.of(new File(lastBlock.get().id, streak)));
                    streak = 1;
                    lastBlock = maybeFileBlock;
                }
            }
        }
        if (streak > 0 && lastBlock.isPresent()) {
            output.add(Optional.of(new File(lastBlock.get().id, streak)));
        }
        return output;
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

    record File(int id, int length) {}

    record DiskBlock(int id) {

    }
}
