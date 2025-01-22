package solutions.day4;

import lombok.AllArgsConstructor;
import solutions.Solution;
import solutions.Utils;

import java.util.Arrays;
import java.util.List;

public class Day4 implements Solution {
    @Override
    public void firstPart(String inputFile) {
        char[][] wordSearch = getWordSearch(inputFile);

        System.out.println(stringifyMatrix(wordSearch));

        final List<PatternMask> masks = generateAllOrientationPatterns("XMAS").stream()
                .map(PatternMask::fromCharMatrix)
                .toList();

        countMaskMatches(masks, wordSearch);
    }

    @Override
    public void secondPart(String inputFile) {
        char[][] wordSearch = getWordSearch(inputFile);

        System.out.println(stringifyMatrix(wordSearch));

        final List<PatternMask> masks = generateAllXOrientationPatterns("MAS").stream()
                .map(PatternMask::fromCharMatrix)
                .toList();

        countMaskMatches(masks, wordSearch);
    }


    private static void countMaskMatches(List<PatternMask> masks, char[][] wordSearch) {
        for (PatternMask mask : masks) {
            System.out.println(mask);
        }

        long output = 0;
        for (int y = 0; y < wordSearch.length; y++) {
            for (int x = 0; x < wordSearch[0].length; x++) {
                // We need to make the indices effectively final
                // by putting them into temp variables.
                final int x_capture = x;
                final int y_capture = y;
                output += masks.stream()
                        .filter(mask -> mask.matchesWordSearchAt(x_capture, y_capture, wordSearch))
                        .count();
            }
        }
        System.out.printf("Number of matches: %d\n", output);
    }

    private static List<char[][]> generateAllOrientationPatterns(String input) {
        int l = input.length();
        char[][] horizontal = new char[1][l];
        char[][] horizontalReversed = new char[1][l];
        char[][] vertical = new char[l][1];
        char[][] verticalReversed = new char[l][1];
        char[][] diagonal = new char[l][l];
        char[][] diagonalReversed = new char[l][l];
        char[][] diagonalLeftSloping = new char[l][l];
        char[][] diagonalLeftSlopingReversed = new char[l][l];

        // We need to fill the diagonal masks with wildcards
        for (int i = 0; i < l; i++) {
            for (int j = 0; j < l; j++) {
                diagonal[i][j] = '*';
                diagonalReversed[i][j] = '*';
                diagonalLeftSloping[i][j] = '*';
                diagonalLeftSlopingReversed[i][j] = '*';
            }
        }

        // Populate all masks in a single go.
        for (int i = 0; i < l; i++) {
            char c = input.charAt(i);
            horizontal[0][i] = c;
            horizontalReversed[0][l - i - 1] = c;
            vertical[i][0] = c;
            verticalReversed[l - i - 1][0] = c;
            diagonal[i][i] = c;
            diagonalReversed[l - i - 1][l - i - 1] = c;
            diagonalLeftSloping[l-i-1][i] = c;
            diagonalLeftSlopingReversed[i][l-i-1] = c;
        }

        return List.of(horizontal,
                       horizontalReversed,
                       vertical,
                       verticalReversed,
                       diagonal,
                       diagonalReversed,
                       diagonalLeftSloping,
                       diagonalLeftSlopingReversed);
    }

    private static List<char[][]> generateAllXOrientationPatterns(String input) {
        assert input.length() % 2 == 1: "We need to have an odd number of letters for a proper x pattern.";
        int l = input.length();
        char[][] north = new char[l][l];
        char[][] east = new char[l][l];
        char[][] south = new char[l][l];
        char[][] west = new char[l][l];

        // We need to fill the diagonal masks with wildcards
        for (int i = 0; i < l; i++) {
            for (int j = 0; j < l; j++) {
                north[i][j] = '*';
                west[i][j] = '*';
                east[i][j] = '*';
                south[i][j] = '*';
            }
        }

        // First leg of the X
        for (int i = 0; i < l; i++) {
            char c = input.charAt(i);
            // Both words left-to-right, one sloping down the other up
            north[i][i] = c;
            north[l-i-1][i] = c;
            // First word left-to-right the other reversed, one sloping down the other up
            east[i][i] = c;
            east[i][l-i-1] = c;
            // Both words right-to-left, one sloping down the other up
            south[l - i - 1][l - i - 1] = c;
            south[i][l-i-1] = c;
            // Second word left-to-right the other reversed, one sloping down the other up
            west[l - i - 1][l - i - 1] = c;
            west[l-i-1][i] = c;
        }

        return List.of(north,
                       west,
                       east,
                       south);
    }

    @AllArgsConstructor
    private static class PatternMask {
        int width;
        int height;
        char[][] pattern;

        public static PatternMask fromCharMatrix(char[][] matrix) {
            assert matrix.length > 0 : "The input matrix has to be non-empty";
            assert Arrays.stream(matrix).allMatch(row -> row.length == matrix[0].length) :
                    "The input matrix has to be rectangular.";

            return new PatternMask(matrix[0].length, matrix.length, matrix);

        }

        public boolean matchesWordSearchAt(int x, int y, char[][] wordSearch) {
            if (wordSearch.length < y + height) {
                // The pattern does not fit vertically.
                return false;
            }

            if (wordSearch.length == 0 || wordSearch[0].length < x + width) {
                // The pattern does not fit horizontally.
                return false;
            }

            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    if (pattern[j][i] != '*' && pattern[j][i] != wordSearch[y + j][x + i]) {
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public String toString() {
            return stringifyMatrix(pattern);
        }
    }

    private static String stringifyMatrix(char[][] matrix) {
        StringBuilder sb = new StringBuilder();
        for (char[] row : matrix) {
            for (char c : row) {
                sb.append(c);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static char[][] getWordSearch(String inputFile) {
        List<String> rawInput = Utils.readInputAsStream(inputFile).toList();

        assert !rawInput.isEmpty() :
                "The word search needs to be rectangular and needs to have at least one row.";
        char[][] wordSearch = new char[rawInput.size()][rawInput.getFirst().length()];

        for (int i = 0; i < rawInput.size(); i++) {
            for (int j = 0; j < rawInput.getFirst().length(); j++) {
                wordSearch[i][j] = rawInput.get(i).charAt(j);
            }
        }
        return wordSearch;
    }

}
