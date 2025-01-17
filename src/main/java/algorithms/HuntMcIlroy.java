package algorithms;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Compare two files using the Hunt-McIlroy algorithm
 *
 * @see java.io.File
 * @see java.io.IOException
 * @see java.nio.file.Files
 * @see java.nio.file.Path
 */
public class HuntMcIlroy {
    private static List<String> leftLines;
    private static List<String> rightLines;

    /**
     * Get the strings from the two files
     *
     * @param fileLeft  First file to compare
     * @param fileRight Second file to compare
     * @return List of StringPairs
     * @throws IOException If an I/O error occurs
     * @see File
     * @see File
     * @see IOException
     */
    public static List<StringPair> compare(File fileLeft, File fileRight) throws IOException {
        List<Subsequence> subsequences = huntMcIllroy(fileLeft, fileRight);
        // Collections.reverse(subsequences);
        List<Subsequence> reversed = new ArrayList<>();
        // reversed.add(new Subsequence(leftLines.size(), rightLines.size(), 0)); // WHAT IS THIS FOR???
        subsequences.forEach(x -> reversed.add(0, x));
        reversed.add(0, new Subsequence(-1, -1, 1)); // endmarker
        //reversed.addAll(subsequences);
        List<StringPair> result = new ArrayList<>();
        // // why does subsequences.reversed() not work???

        for (int i = 0; i < reversed.size() - 1; i++) {
            Subsequence last = reversed.get(i);
            Subsequence next = reversed.get(i + 1);
            int leftStart = last.startLeft() + last.length();
            int rightStart = last.startRight() + last.length();
            int leftLimit = next.startLeft();
            int rightLimit = next.startRight();
            while (leftStart < leftLimit && rightStart < rightLimit) {
                result.add(new StringPair(leftStart, leftLines.get(leftStart), rightStart, rightLines.get(rightStart)));
                leftStart++;
                rightStart++;
            }
            while (leftStart < leftLimit) {
                result.add(new StringPair(leftStart, leftLines.get(leftStart), -1, null));
                leftStart++;
            }
            while (rightStart < rightLimit) {
                result.add(new StringPair(-1, null, rightStart, rightLines.get(rightStart)));
                rightStart++;
            }
            for (int j = 0; j < next.length; j++) {
                result.add(new StringPair(leftStart + j, leftLines.get(leftStart + j), rightStart + j, rightLines.get(rightStart + j)));
            }
        }
        return result;
    }

    /**
     * Represents a pair of strings from two files
     *
     * @param leftIndex
     * @param leftText
     * @param rightIndex
     * @param rightText
     * @see java.lang.Integer
     * @see java.lang.String
     */
    public record StringPair(int leftIndex, String leftText, int rightIndex, String rightText) {
    }

    private static List<Subsequence> huntMcIllroy(File fileLeft, File fileRight) throws IOException {
        leftLines = Files.readAllLines(fileLeft.toPath());
        rightLines = Files.readAllLines(fileRight.toPath());
        int[][] hmiMatrix = new int[leftLines.size()][rightLines.size()];

        // fill hmiMatrix
        for (int row = 0; row < leftLines.size(); row++) {
            for (int col = 0; col < rightLines.size(); col++) {
                hmiMatrix[row][col] = leftLines.get(row).equals(rightLines.get(col)) ? // lines are equal?
                        getMatrixData(hmiMatrix, row - 1, col - 1) + 1 : // ggZ wächst um 1
                        Math.max( // // ggz bleibt bei einseitigem Anhängen eines Buchstaben gleich (=> größtmögl. Wert)
                                getMatrixData(hmiMatrix, row - 1, col), //
                                getMatrixData(hmiMatrix, row, col - 1));
            }
        }
        return getSubsequences(hmiMatrix);
    }

    private static List<Subsequence> getSubsequences(int[][] hmiMatrix) {
        List<Subsequence> result = new ArrayList<>();
        int row = leftLines.size() - 1; // bottom row
        int col = rightLines.size() - 1; // most right column
        while (row >= 0 && col >= 0) {
            int oldRow = row;
            int oldCol = col;
            while (row >= 0 && col >= 0 && leftLines.get(row).equals(rightLines.get(col))) {
                // skip through one subsequence of equal lines
                row--;
                col--;
            }
            result.add(new Subsequence(row + 1, col + 1, oldRow - row));
            oldRow = row;
            while (getMatrixData(hmiMatrix, row - 1, col) == getMatrixData(hmiMatrix, oldRow, oldCol)) {
                row--;
            }
            while (getMatrixData(hmiMatrix, row, col - 1) == getMatrixData(hmiMatrix, oldRow, oldCol)) {
                col--;
            }
        }
        return result;
    }

    /**
     * Represents a subsequence of two files
     *
     * @param startLeft
     * @param startRight
     * @param length
     * @see java.lang.Integer
     * @see java.lang.String
     */
    private record Subsequence(int startLeft, int startRight, int length) {
    }

    private static int getMatrixData(int[][] hmiMatrix, int i, int j) {
        if (i < -1 || j < -1) return Integer.MIN_VALUE;
        if (i == -1 || j == -1) return 0;
        return hmiMatrix[i][j];
    }
}
