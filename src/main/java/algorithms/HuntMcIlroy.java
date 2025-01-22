package algorithms;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HuntMcIlroy {
    private static List<String> leftLines;
    private static List<String> rightLines;

    public static List<StringTuple> compare(File leftFile, File rightFile) throws IOException {
        int[][] hmiMatrix = huntMcIlroyMatrix(leftFile, rightFile);

        List<MatchingLineSequence> matchingLineSequences = getMatchingLineSequences(hmiMatrix);
        Collections.reverse(matchingLineSequences); // sameLineSequences were read in reversed order
        matchingLineSequences.add(0, new MatchingLineSequence(-1, -1, 1)); // startmarker (place filler)

        List<StringTuple> result = new ArrayList<>();
        for (int i = 0; i < matchingLineSequences.size() - 1; i++) { // iterate over subsequences
            MatchingLineSequence last = matchingLineSequences.get(i);
            MatchingLineSequence next = matchingLineSequences.get(i + 1);
            int leftStart = last.startLeft() + last.length();
            int rightStart = last.startRight() + last.length();
            int leftLimit = next.startLeft();
            int rightLimit = next.startRight();
            while (leftStart < leftLimit && rightStart < rightLimit) { // add DIFFERENT lines that are in both sides
                result.add(new StringTuple(leftStart, leftLines.get(leftStart), rightStart, rightLines.get(rightStart), false));
                leftStart++;
                rightStart++;
            }
            while (leftStart < leftLimit) { // add remaining DIFFERENT lines (only in left)
                result.add(new StringTuple(leftStart, leftLines.get(leftStart), -1, null, false));
                leftStart++;
            }
            while (rightStart < rightLimit) { // OR add DIFFERENT remaining lines (only in right)
                result.add(new StringTuple(-1, null, rightStart, rightLines.get(rightStart), false));
                rightStart++;
            }
            for (int j = 0; j < next.length; j++) { // add SAME lines of next block
                result.add(new StringTuple(leftStart + j, leftLines.get(leftStart + j), rightStart + j, rightLines.get(rightStart + j), true));
            }
        }
        return result;
    }

    public record StringTuple(int leftIndex, String leftLine, int rightIndex, String rightLine, boolean sameLine) {
    }

    private static int[][] huntMcIlroyMatrix(File leftFile, File rightFile) throws IOException {
        //UTF 8
        BufferedReader leftReader = Files.newBufferedReader(leftFile.toPath(), StandardCharsets.UTF_8);
        BufferedReader rightReader = Files.newBufferedReader(rightFile.toPath(), StandardCharsets.UTF_8);
        leftLines = leftReader.lines().toList();
        rightLines = rightReader.lines().toList();

        int[][] hmiMatrix = new int[leftLines.size()][rightLines.size()];

        // fill hmiMatrix
        for (int row = 0; row < leftLines.size(); row++) {
            for (int col = 0; col < rightLines.size(); col++) {
                String leftLine = leftLines.get(row).trim();
                String rightLine = rightLines.get(col).trim();
                hmiMatrix[row][col] = leftLine.equals(rightLine) ? // lines are equal?
                        getMatrixData(hmiMatrix, row - 1, col - 1) + 1 :// ggZ wächst um 1
                        max( // ggz bleibt bei einseitigem Anhängen eines Buchstaben gleich (→ größtmöglicher Wert)
                                getMatrixData(hmiMatrix, row - 1, col), //
                                getMatrixData(hmiMatrix, row, col - 1));
            }
        }
        return hmiMatrix;
    }

    private static List<MatchingLineSequence> getMatchingLineSequences(int[][] hmiMatrix) {
        List<MatchingLineSequence> result = new ArrayList<>();
        int row = leftLines.size() - 1; // bottom row
        int col = rightLines.size() - 1; // most right column
        int currentExpected = hmiMatrix[hmiMatrix.length - 1][hmiMatrix[0].length - 1]; // bottom right field
        while (row >= 0 && col >= 0) {
            int oldRow = row;
            int currentEntry = getMatrixData(hmiMatrix, row, col);
            // replacement for leftLines.get(row).equals(rightLines.get(col)) (expensive)
            boolean leftDiagonalTopAllLower = currentEntry > max(getMatrixData(hmiMatrix, row, col - 1), // left
                    getMatrixData(hmiMatrix, row - 1, col), // above
                    getMatrixData(hmiMatrix, row - 1, col - 1) // diagonal
            );
            while (row >= 0 && col >= 0 && leftDiagonalTopAllLower && currentEntry == currentExpected) {
                // skip through one subsequence of equal lines
                row--;
                col--;
                currentExpected--;
            }
            result.add(new MatchingLineSequence(row + 1, col + 1, oldRow - row));
            int temp = getMatrixData(hmiMatrix, row, col);
            while (getMatrixData(hmiMatrix, row - 1, col) == temp) row--; // move up as far as possible
            while (getMatrixData(hmiMatrix, row, col - 1) == temp) col--; // move left as far as possible
        }
        return result;
    }

    private record MatchingLineSequence(int startLeft, int startRight, int length) {
    }

    private static int getMatrixData(int[][] hmiMatrix, int row, int col) {
        if (row < -1 || col < -1) return Integer.MIN_VALUE;
        if (row == -1 || col == -1) return 0;
        return hmiMatrix[row][col];
    }

    private static int max(int... numbers) {
        return Arrays.stream(numbers).reduce(Math::max).orElseThrow();
    }


    /**
     * Compare two strings and return a string with differences marked with '!' and matches marked with 'O'
     * Using the Longest Common Subsequence algorithm / Hunt-McIlroy algorithm
     *
     * @param longerString  First string to compare
     * @param shorterString Second string to compare
     */
    public static String compareString(String longerString, String shorterString) {
        int[][] hmiMatrix = HuntMcIlroy.huntMcIlroyMatrixString(longerString, shorterString);

        int row = hmiMatrix.length - 1;
        int col = hmiMatrix[0].length - 1;
        StringBuilder result = new StringBuilder(longerString.length());

        while (row > 0 && col > 0) {
            if (longerString.charAt(row - 1) == shorterString.charAt(col - 1)) {
                result.insert(0, "O");
                row--;
                col--;
            } else {
                while (hmiMatrix[row - 1][col] == hmiMatrix[row][col]) {
                    row--;
                    result.insert(0, "!");
                }
                while (hmiMatrix[row][col-1] == hmiMatrix[row][col]) col--;
            }
        }
        return result.toString();
    }

    /**
     * Build the Longest Common Subsequence matrix for two strings
     *
     * @param s1 First string to compare
     * @param s2 Second string to compare
     * @return Longest Common Subsequence matrix
     */
    private static int[][] huntMcIlroyMatrixString(String s1, String s2) {
        int[][] hmiMatrix = new int[s1.length() + 1][s2.length() + 1];
        for (int row = 1; row <= hmiMatrix.length - 1; row++) {
            for (int col = 1; col <= hmiMatrix[0].length - 1; col++) {
                if (s1.charAt(row - 1) == s2.charAt(col - 1)) {
                    hmiMatrix[row][col] = hmiMatrix[row - 1][col - 1] + 1;
                } else {
                    hmiMatrix[row][col] = Math.max(hmiMatrix[row - 1][col], hmiMatrix[row][col - 1]);
                }
            }
        }
        return hmiMatrix;
    }
}
