
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Compare two files using the Hunt-McIlroy algorithm
 * @see java.io.File
 * @see java.io.IOException
 * @see java.nio.file.Files
 * @see java.nio.file.Path
 */
public class HuntMcIlroy {
    List<String> linesLeft;
    List<String> linesRight;

    /**
     * Represents a subsequence of two files
     * @see java.lang.Integer
     * @see java.lang.String
     * @param startLeft
     * @param startRight
     * @param length
     */
    public record Subsequence(int startLeft, int startRight, int length) {
    }

    /**
     * Represents a pair of strings from two files
     * @see java.lang.Integer
     * @see java.lang.String
     * @param leftIndex
     * @param leftText
     * @param rightIndex
     * @param rightText
     */
    public record StringPair(int leftIndex, String leftText, int rightIndex, String rightText) {
    }

    /**
     * Create a new HuntMcIlroy object
     * @param fileLeft First file to compare
     *                 @see java.io.File
     * @param fileRight Second file to compare
     *                  @see java.io.File
     * @throws IOException If an I/O error occurs
     *                    @see java.io.IOException
     */
    public HuntMcIlroy(File fileLeft, File fileRight) throws IOException {

        linesLeft = Files.readAllLines(Path.of(fileLeft.toURI()));
        linesRight = Files.readAllLines(Path.of(fileRight.toURI()));
    }

    public List<Subsequence> getSubsequences() {
        int[][] data = new int[linesLeft.size()][linesRight.size()];
        List<Subsequence> result = new ArrayList<>();
        for (int i = 0; i < linesLeft.size(); i++) {
            for (int j = 0; j < linesRight.size(); j++) {
                data[i][j] = linesLeft.get(i).equals(linesRight.get(j)) ?
                        1 + getData(data, i - 1, j - 1) : Math.max(getData(data, i - 1, j), getData(data, i, j - 1));
            }
        }
        int max = 0;
        for (int i = 0; i < linesLeft.size(); i++) {
            for (int j = 0; j < linesRight.size(); j++) {
                max = Math.max(getData(data, i, j), max);
            }
        }
        int i = linesLeft.size() - 1;
        int j = linesRight.size() - 1;
        while (i >= 0 && j >= 0) {
            int x = i;
            int y = j;
            while (i >= 0 && j >= 0 && linesLeft.get(i).equals(linesRight.get(j))) {
                i--;
                j--;
            }
            result.add(new Subsequence(i + 1, j + 1, x - i));
            x = i;
            while (getData(data, i - 1, j) == getData(data, x, y)) {
                i--;
            }
            while (getData(data, i, j - 1) == getData(data, x, y)) {
                j--;
            }
        }
        for(var v: data) {
            System.out.println(Arrays.toString(v));
        }
        return result;
    }

    public List<StringPair> getStringpairs(List<Subsequence> inputs) {
        List<StringPair> result = new ArrayList<>();
        List<Subsequence> copy = new ArrayList<>();
        copy.add(new Subsequence(linesLeft.size(), linesRight.size(), 0));
        inputs.forEach(x->copy.add(0, x));
        copy.add(0, new Subsequence(-1, -1, 1));
        for (int i = 0; i < copy.size()-1; i++) {
            Subsequence last = copy.get(i);
            Subsequence next = copy.get(i+1);
            int leftStart = last.startLeft()+last.length();
            int rightStart = last.startRight()+last.length();
            int leftLimit = next.startLeft();
            int rightLimit = next.startRight();
            while ( leftStart < leftLimit && rightStart < rightLimit ) {
                result.add(new StringPair(leftStart, linesLeft.get(leftStart),
                        rightStart, linesRight.get(rightStart)));
                leftStart++;
                rightStart++;
            }
            while ( leftStart < leftLimit ) {
                result.add(new StringPair(leftStart, linesLeft.get(leftStart),
                        -1, null));
                leftStart++;
            }
            while ( rightStart < rightLimit ) {
                result.add(new StringPair(-1, null, rightStart,
                        linesRight.get(rightStart)));
                rightStart++;
            }
            for (int j = 0; j < next.length; j++) {
                    result.add(new StringPair(leftStart+j, linesLeft.get(leftStart+j),
                            rightStart+j, linesRight.get(rightStart+j)));
            }
        }
        return result;
    }

    private int getData(int[][] data, int i, int j) {
        if (i < -1 || j < -1) {
            return Integer.MIN_VALUE;
        }
        if (i == -1 || j == -1) {
            return 0;
        }
        return data[i][j];
    }
}
