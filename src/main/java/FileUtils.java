import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    /**
     * Create a new FileUtils object
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
        return Boolean.TRUE.equals(BinaryHeuristics.isBinary(file, extensive));
    }

    /** @noinspection unused*/
    public List<String> readFile(File file) {
        List<String> lines = new ArrayList<>();
        try {
            lines = Files.readAllLines(Paths.get(file.toURI()));
        } catch (IOException e) {
            System.out.println("File-Read has failed");
            System.out.println(e.getMessage());
        }
        return lines;
    }

    /**
     * Represents the result of the Hunt-McIlroy algorithm for comparing two files
     * Has a List<HuntMcIlroy.Subsequence> subsequences and a List<HuntMcIlroy.StringPair> stringPairs
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
    public record LineResult(List<String> left, List<String> right) {
    }

    /**
     * Compare two files line by line
     * Lines come pre-modified with a line-number
     * They also come pre-modified with a + or - to represent if the line is present in the left or right file
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

        boolean leftBinary;
        boolean rightBinary;

        if(leftFile.equals(rightFile)) {
            leftBinary = isBinary(leftFile, true);
            rightBinary = leftBinary;
        } else {
            leftBinary = isBinary(leftFile, true);
            rightBinary = isBinary(rightFile, true);
        }

        int lineNumber = 0;

        if(result == null) {
            leftLines.add(leftBinary ? "Cannot compare binary files yet" : "Cannot compare this filetype yet");
            rightLines.add(rightBinary ? "Cannot compare binary files yet" : "Cannot compare this filetype yet");
            return new LineResult(leftLines, rightLines);
        }

        for (HuntMcIlroy.StringPair pair : result.stringPairs()) {

            if(pair.leftText() == null) {
                leftLines.add(lineNumber + ": -");
                rightLines.add(lineNumber + ": + " + pair.rightText());
                lineNumber++;
                continue;
            }
            if(pair.rightText() == null) {
                leftLines.add(lineNumber + ": + " + pair.leftText());
                rightLines.add(lineNumber + ": -");
                lineNumber++;
                continue;
            }
            if(!pair.leftText().equals(pair.rightText())) {
                leftLines.add(lineNumber + ": + " + pair.leftText());
                rightLines.add(lineNumber + ": + " + pair.rightText());
                lineNumber++;
                continue;
            }
            leftLines.add(lineNumber + ": " + pair.leftText());
            rightLines.add(lineNumber  + ": " + pair.rightText());
            lineNumber++;
        }

        return new LineResult(leftLines, rightLines);

    }
}
