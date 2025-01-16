package algorithms;

import utils.Side;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    /**
     * Create a new algorithms.FileUtils object
     * @see java.io.File
     * @see java.nio.file.Path
     * @see java.util.List
     */
    public FileUtils() {
        super();
    }

    /**
     * Get the files in a directory
     * Public Service Info: File Names can be obtained by calling the getName() method on the File object ;)
     * @param path Path of the directory
     * @return List of files in the directory or null if the directory is empty or does not exist
     * @see java.io.File
     * @see java.nio.file.Path
     * @see java.util.List
     */
    public List<File> getFiles(String path) {
        Path p = Path.of(path);
        Directory dir = new Directory(p);

        return dir.getFiles();
    }

    /**
     * Check if a file is binary
     * @param file File to check
     * @param extensive If true, check the entire file. If false, check the first 1MB (1048576 bytes)
     * @return True if the file is binary, false otherwise
     * @see java.io.File
     * @see BinaryHeuristics
     */
    public boolean isBinary(File file, boolean extensive) {
        return BinaryHeuristics.isBinary(file, extensive);
    }

    /**
     * Read a file and return its lines with line numbers
     * @param file File to read
     * @return List of lines in the file
    */
    public List<String> readFile(File file) {
        if(isBinary(file, false)) {
            return List.of("Cannot read binary files yet");
        }
        List<String> lines = new ArrayList<>();
        try {
            lines = Files.readAllLines(Paths.get(file.toURI()));
            int lineNumber = 1;
            for(String line : lines) {
                String prefix = lineNumber + ":   ";
                line = prefix + line;
                lines.set(lineNumber - 1, line);
                lineNumber++;
            }
        } catch (IOException e) {
            System.out.println("File-Read has failed");
            System.out.println(e.getMessage());
        }
        return lines;
    }

    /**
     * Represents the result of the Hunt-McIlroy algorithm for comparing two files
     * Has a List<algorithms.HuntMcIlroy.Subsequence> subsequences and a List<algorithms.HuntMcIlroy.StringPair> stringPairs
     * @see HuntMcIlroy
     * @see java.util.List
     * @see HuntMcIlroy.Subsequence
     * @see HuntMcIlroy.StringPair
     */
    public record huntIllroyResult(List<HuntMcIlroy.Subsequence> subsequences, List<HuntMcIlroy.StringPair> stringPairs) {
    }

    /**
     * Compare two files using the Hunt-McIlroy algorithm
     * @param fileLeft First file to compare
     * @param fileRight Second file to compare
     * @return Result of the comparison as a huntIllroyResult object
     * @see HuntMcIlroy
     * @see huntIllroyResult
     * @see java.io.File
     */
    private huntIllroyResult huntCompare(File fileLeft, File fileRight) {
        try {
            HuntMcIlroy hm = new HuntMcIlroy(fileLeft, fileRight);
            List<HuntMcIlroy.Subsequence> subsequences = hm.getSubsequences();
            List<HuntMcIlroy.StringPair> stringPairs;
            stringPairs = hm.getStringpairs(subsequences);
            return new huntIllroyResult(subsequences, stringPairs);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Represents the result of comparing two files line by line
     * Has a List<String> left and a List<String right to represent the lines of the files
     * @see java.util.List
     */
    public record LineResult(List<String> left, List<String> right, List<SpecificLineChange> specificLineChanges) {
    }

    /**
     * Represents a specific change in a line
     * The lineNumber is the number of the line that has been changed e.g. 5 for the 5th line
     * The index is the index of the character that has been changed e.g. 5 for the 5th character
     * The character is the character that has been changed e.g. 'a' for the character 'a'
     * The longerSide is the side that has the longer line e.g. utils.Side.LEFT if the left line is longer
     * @param lineNumber
     * @param index
     * @param character
     * @param displaySide
     */
    public record SpecificLineChange(int lineNumber, int index, char character, Side displaySide) {
    }

    /**
     * Compare two files line by line
     * Lines come pre-modified with a line-number
     * They also come pre-modified with a + or - to represent if the line is present in the left or right file
     * If the files are the same, the lines will be present in both List<String> objects
     * @param leftFile First file to compare
     * @param rightFile Second file to compare
     * @return Result of the comparison as a LineResult object
     * @see java.io.File
     * @see LineResult
     * @see java.util.List
     */
    public LineResult compareFiles(File leftFile, File rightFile) {
        huntIllroyResult result = this.huntCompare(leftFile, rightFile);
        List<String> leftLines = new ArrayList<>();
        List<String> rightLines = new ArrayList<>();
        List<SpecificLineChange> specificLineChanges = new ArrayList<>();

        boolean leftBinary;
        boolean rightBinary;

        if(leftFile.equals(rightFile)) {
            leftBinary = isBinary(leftFile, false);
            rightBinary = leftBinary;
        } else {
            leftBinary = isBinary(leftFile, false);
            rightBinary = isBinary(rightFile, false);
        }

        int lineNumber = 1;

        if(result == null) {
            leftLines.add(leftBinary ? "Cannot compare binary files yet" : "Cannot compare this filetype yet");
            rightLines.add(rightBinary ? "Cannot compare binary files yet" : "Cannot compare this filetype yet");
            return new LineResult(leftLines, rightLines, null);
        }

        int lineCounterLeft = 1;

        for (HuntMcIlroy.StringPair pair : result.stringPairs()) {
            if(pair.leftText() == null) {
                String emptySpaces = " ".repeat(String.valueOf(lineCounterLeft).length());
                leftLines.add(emptySpaces + "  - ");
                rightLines.add(emptySpaces + "    " + pair.rightText());
                lineNumber++;
                continue;
            }

            if(pair.rightText() == null) {
                leftLines.add(lineCounterLeft + ": + " + pair.leftText());
                rightLines.add(lineCounterLeft + ":   ");
                lineCounterLeft++;
                lineNumber++;
                continue;
            }

            if(!pair.leftText().equals(pair.rightText())) {
                String[] leftText = pair.leftText().split("");
                String[] rightText = pair.rightText().split("");

                String leftString = pair.leftText();
                String rightString = pair.rightText();

                Side longerSide = leftText.length > rightText.length ? Side.LEFT : Side.RIGHT;

                String longerString = longerSide == Side.LEFT ? pair.leftText() : pair.rightText();
                String shorterString = longerSide == Side.LEFT ? pair.rightText() : pair.leftText();

                int distance = LevenshteinDistance.of(leftString, rightString);

                if(distance < leftString.length() * 0.3 || distance < rightString.length() * 0.3) {

                    String diffString = compareString(longerString, shorterString);

                    for (int i = 0; i < diffString.length(); i++) {
                        if (diffString.charAt(i) == '!') {
                            specificLineChanges.add(new SpecificLineChange(lineNumber, i + String.valueOf(lineNumber).length() + 4, longerString.charAt(i), longerSide));
                        }
                    }

                    leftLines.add(lineCounterLeft + ": ! " + pair.leftText());
                    rightLines.add(lineCounterLeft + ": ! " + pair.rightText());
                    lineCounterLeft++;
                    lineNumber++;
                    continue;
                } else {

                    for(int i = 0; i < leftString.length(); i++) {
                        specificLineChanges.add(new SpecificLineChange(lineNumber, i + String.valueOf(lineCounterLeft).length() + 4, leftString.charAt(i), Side.LEFT));
                    }

                    for(int i = 0; i < rightString.length(); i++) {
                        specificLineChanges.add(new SpecificLineChange(lineNumber, i + String.valueOf(lineCounterLeft).length() + 4, rightString.charAt(i), Side.RIGHT));
                    }

                    leftLines.add(lineCounterLeft + ": ! " + pair.leftText());
                    rightLines.add(lineCounterLeft + ": ! " + pair.rightText());
                    lineCounterLeft++;
                    lineNumber++;
                    continue;
                }
            }

            leftLines.add(lineCounterLeft + ":   " + pair.leftText());
            rightLines.add(lineCounterLeft  + ":   " + pair.rightText());
            lineNumber++;
            lineCounterLeft++;
        }

        return new LineResult(leftLines, rightLines, specificLineChanges);

    }

    /**
     * Compare two strings and return a string with differences marked with '!' and matches marked with 'O'
     * Using the Longest Common Subsequence algorithm / Hunt-McIlroy algorithm
     * @param s1 First string to compare
     * @param s2 Second string to compare
     */
    public String compareString(String s1, String s2) {
        int[][] lcs = buildLcs(s1, s2);
        List<Integer> diff = buildDiff(s1, s2, lcs);
        return diffString(s1, diff);
    }

    /**
     * Build a string with differences marked with '!' and matches marked with 'O'
     * Using the Longest Common Subsequence algorithm / Hunt-McIlroy algorithm
     * @param s1 First string to compare
     * @param matches List of matching indices
     */
    private String diffString(String s1, List<Integer> matches) {
        StringBuilder sb = new StringBuilder(s1.length());

        int matchPos = 0;
        for (int i = 0; i < s1.length(); i++) {
            if (matchPos < matches.size() && matches.get(matchPos) == i) {
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
     * @param s1 First string to compare
     * @param s2 Second string to compare
     * @param c Longest Common Subsequence matrix
     */
    private List<Integer> buildDiff(String s1, String s2, int[][] c) {
        int i = s1.length();
        int j = s2.length();

        List<Integer> matchedIndices = new ArrayList<>();

        while (i > 0 && j > 0) {
            if (s1.charAt(i-1) == s2.charAt(j-1)) {
                matchedIndices.add(i-1);
                i--;
                j--;
            } else {
                if (c[i-1][j] > c[i][j-1]) {
                    i--;
                } else {
                    j--;
                }
            }
        }

        java.util.Collections.reverse(matchedIndices);
        return matchedIndices;
    }

    /**
     * Build the Longest Common Subsequence matrix for two strings
     * @param s1 First string to compare
     * @param s2 Second string to compare
     * @return Longest Common Subsequence matrix
     */
    private int[][] buildLcs(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        int[][] c = new int[m+1][n+1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (s1.charAt(i-1) == s2.charAt(j-1)) {
                    c[i][j] = c[i-1][j-1] + 1;
                } else {
                    c[i][j] = Math.max(c[i-1][j], c[i][j-1]);
                }
            }
        }
        return c;
    }
}
