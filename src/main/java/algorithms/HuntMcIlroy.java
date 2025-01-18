package algorithms;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HuntMcIlroy {
    private static List<String> leftLines;
    private static List<String> rightLines;

    public static List<LineTuple> compare(File leftFile, File rightFile) throws IOException {
        int[][] hmiMatrix = huntMcIllroyMatrix(leftFile, rightFile);

        List<MatchingLineSequence> matchingLineSequences = getMatchingLineSequences(hmiMatrix);
        Collections.reverse(matchingLineSequences); // sameLineSequences were read in reversed order
        matchingLineSequences.add(0, new MatchingLineSequence(-1, -1, 1)); // startmarker (place filler)

        List<LineTuple> result = new ArrayList<>();
        for (int i = 0; i < matchingLineSequences.size() - 1; i++) { // iterate over subsequences
            MatchingLineSequence last = matchingLineSequences.get(i);
            MatchingLineSequence next = matchingLineSequences.get(i + 1);
            int leftStart = last.startLeft() + last.length();
            int rightStart = last.startRight() + last.length();
            int leftLimit = next.startLeft();
            int rightLimit = next.startRight();
            while (leftStart < leftLimit && rightStart < rightLimit) { // add DIFFERENT lines that are in both sides
                result.add(new LineTuple(leftStart, leftLines.get(leftStart), rightStart, rightLines.get(rightStart), false));
                leftStart++;
                rightStart++;
            }
            while (leftStart < leftLimit) { // add remaining DIFFERENT lines (only in left)
                result.add(new LineTuple(leftStart, leftLines.get(leftStart), -1, null, false));
                leftStart++;
            }
            while (rightStart < rightLimit) { // OR add DIFFERENT remaining lines (only in right)
                result.add(new LineTuple(-1, null, rightStart, rightLines.get(rightStart), false));
                rightStart++;
            }
            for (int j = 0; j < next.length; j++) { // add SAME lines of next block
                result.add(new LineTuple(leftStart + j, leftLines.get(leftStart + j), rightStart + j, rightLines.get(rightStart + j), true));
            }
        }
        return result;
    }

    public record LineTuple(int leftIndex, String leftLine, int rightIndex, String rightLine, boolean sameLine) {
    }

    private static int[][] huntMcIllroyMatrix(File leftFile, File rightFile) throws IOException {
        leftLines = Files.readAllLines(leftFile.toPath());
        rightLines = Files.readAllLines(rightFile.toPath());
        int[][] hmiMatrix = new int[leftLines.size()][rightLines.size()];

        // fill hmiMatrix
        for (int row = 0; row < leftLines.size(); row++) {
            for (int col = 0; col < rightLines.size(); col++) {
                hmiMatrix[row][col] = leftLines.get(row).equals(rightLines.get(col)) ? // lines are equal?
                        getMatrixData(hmiMatrix, row - 1, col - 1) + 1 :// ggZ wächst um 1
                        max( // ggz bleibt bei einseitigem Anhängen eines Buchstaben gleich (=> größtmögl. Wert)
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
            boolean leftDiagonalTopAllLower = currentEntry > max(
                    getMatrixData(hmiMatrix, row, col - 1), // left
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
     * @param s1 First string to compare
     * @param s2 Second string to compare
     */
    public static String compareString(String s1, String s2) {
        int[][] lcs = HuntMcIlroy.huntMcIllroyMatrixString(s1, s2);
        List<Integer> diff = HuntMcIlroy.buildDiffString(s1, s2, lcs);
        StringBuilder sb = new StringBuilder(s1.length());
        int matchPos = 0;
        for (int i = 0; i < s1.length(); i++) {
            if (matchPos < diff.size() && diff.get(matchPos) == i) {
                sb.append('O');
                matchPos++;
            } else {
                sb.append('!');
            }
        }
        return sb.toString();
    }

    /**
     * Build a list of indices of matching characters between two strings
     *
     * @param s1 First string to compare
     * @param s2 Second string to compare
     * @param c  Longest Common Subsequence matrix
     */
    private static List<Integer> buildDiffString(String s1, String s2, int[][] c) {
        int row = c.length - 1;
        int col = c[0].length - 1;

        List<Integer> matchedIndices = new ArrayList<>();

        while (row > 0 && col > 0) {
            if (s1.charAt(row - 1) == s2.charAt(col - 1)) {
                matchedIndices.add(row - 1);
                row--;
                col--;
            } else {
                if (c[row - 1][col] > c[row][col - 1]) {
                    row--;
                } else {
                    col--;
                }
            }
        }
        java.util.Collections.reverse(matchedIndices);
        return matchedIndices;
    }

    /**
     * Build the Longest Common Subsequence matrix for two strings
     *
     * @param s1 First string to compare
     * @param s2 Second string to compare
     * @return Longest Common Subsequence matrix
     */
    private static int[][] huntMcIllroyMatrixString(String s1, String s2) {
        int[][] lcs = new int[s1.length() + 1][s2.length() + 1];
        for (int row = 1; row <= lcs.length - 1; row++) {
            for (int col = 1; col <= lcs[0].length - 1; col++) {
                if (s1.charAt(row - 1) == s2.charAt(col - 1)) {
                    lcs[row][col] = lcs[row - 1][col - 1] + 1;
                } else {
                    lcs[row][col] = Math.max(lcs[row - 1][col], lcs[row][col - 1]);
                }
            }
        }
        return lcs;
    }
}
