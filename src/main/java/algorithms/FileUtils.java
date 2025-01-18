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
     * Get the files in a directory
     * Public Service Info: File Names can be obtained by calling the getName() method on the File object ;)
     * @param path Path of the directory
     * @return List of files in the directory or null if the directory is empty or does not exist
     * @see java.io.File
     * @see java.nio.file.Path
     * @see java.util.List
     */
    public static List<File> getFiles(String path) {
        Path p = Path.of(path);
        Directory dir = new Directory(p);

        return dir.getFiles();
    }

    /**
     * Check which FileType a file is
     * @param file File to check
     * @param extensive If true, check the entire file. If false, check the first 1MB (1048576 bytes). If possible only check the magic number
     * @return FileType of the file
     * @see java.io.File
     * @see BinaryHeuristics
     */
    public static FileType getFileType(File file, boolean extensive) {
        return BinaryHeuristics.fileTypeOf(file, extensive);
    }

    /**
     * Read a file and return its lines with line numbers
     * @param file File to read
     * @return List of lines in the file
    */
    public static List<String> readFile(File file) {
        FileType fileType = getFileType(file, false);
        if(fileType == FileType.ERROR) {
            return List.of("Fehler beim Lesen der Datei");
        }
        if(fileType != FileType.TEXT) {
            return List.of((fileType == FileType.BINARY ? "Binäre " : fileType) + " Dateien können (noch) nicht verglichen werden.");
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
     * Compare two files using the Hunt-McIlroy algorithm
     * @param fileLeft First file to compare
     * @param fileRight Second file to compare
     * @return Result of the comparison as a List<HuntMcIlroy.LineTuple> object
     * @see HuntMcIlroy
     * @see java.io.File
     */
    private static List<HuntMcIlroy.LineTuple> huntCompare(File fileLeft, File fileRight) {
        try {
            return HuntMcIlroy.compare(fileLeft, fileRight);
        } catch (IOException e) {
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
     * If the files are the sameLine, the lines will be present in both List<String> objects
     * @param leftFile First file to compare
     * @param rightFile Second file to compare
     * @return Result of the comparison as a LineResult object
     * @see java.io.File
     * @see LineResult
     * @see java.util.List
     */
    public static LineResult compareFiles(File leftFile, File rightFile) {
        List<HuntMcIlroy.LineTuple> lineTuples = huntCompare(leftFile, rightFile);
        List<String> leftLines = new ArrayList<>();
        List<String> rightLines = new ArrayList<>();
        List<SpecificLineChange> specificLineChanges = new ArrayList<>();

        FileType fileTypeLeft = getFileType(leftFile, false);
        FileType fileTypeRight = getFileType(rightFile, false);

        int lineNumber = 1;

        if(lineTuples == null) {
            if(fileTypeLeft  == FileType.ERROR) {
                leftLines.add("Fehler beim Lesen der Datei");
            } else if(fileTypeRight == FileType.ERROR) {
                rightLines.add("Fehler beim Lesen der Datei");
            } else {
                leftLines.add((fileTypeLeft == FileType.BINARY ? "Binäre " : fileTypeLeft) + " Dateien können (noch) nicht verglichen werden.");
                rightLines.add((fileTypeRight == FileType.BINARY ? "Binäre " : fileTypeRight) + " Dateien können (noch) nicht verglichen werden.");
            }
            return new LineResult(leftLines, rightLines, null);
        }

        int lineCounterLeft = 1;

        for (HuntMcIlroy.LineTuple tuple : lineTuples) {
            if(tuple.leftLine() == null) {
                String emptySpaces = " ".repeat(String.valueOf(lineCounterLeft).length());
                leftLines.add(emptySpaces + "  - ");
                rightLines.add(emptySpaces + "    " + tuple.rightLine());
                lineNumber++;
                continue;
            }

            if(tuple.rightLine() == null) {
                leftLines.add(lineCounterLeft + ": + " + tuple.leftLine());
                rightLines.add(lineCounterLeft + ":   ");
                lineCounterLeft++;
                lineNumber++;
                continue;
            }

            if(!tuple.sameLine()) {
                String[] leftText = tuple.leftLine().split("");
                String[] rightText = tuple.rightLine().split("");

                String leftString = tuple.leftLine();
                String rightString = tuple.rightLine();

                Side longerSide = leftText.length > rightText.length ? Side.LEFT : Side.RIGHT;

                String longerString = longerSide == Side.LEFT ? tuple.leftLine() : tuple.rightLine();
                String shorterString = longerSide == Side.LEFT ? tuple.rightLine() : tuple.leftLine();

                int distance = LevenshteinDistance.of(leftString, rightString);

                if(distance < leftString.length() * 0.3 || distance < rightString.length() * 0.3) {

                    String diffString = HuntMcIlroy.compareString(longerString, shorterString);

                    for (int i = 0; i < diffString.length(); i++) {
                        if (diffString.charAt(i) == '!') {
                            specificLineChanges.add(new SpecificLineChange(lineNumber, i + String.valueOf(lineNumber).length() + 4, longerString.charAt(i), longerSide));
                        }
                    }

                } else {

                    for(int i = 0; i < leftString.length(); i++) {
                        specificLineChanges.add(new SpecificLineChange(lineNumber, i + String.valueOf(lineCounterLeft).length() + 4, leftString.charAt(i), Side.LEFT));
                    }

                    for(int i = 0; i < rightString.length(); i++) {
                        specificLineChanges.add(new SpecificLineChange(lineNumber, i + String.valueOf(lineCounterLeft).length() + 4, rightString.charAt(i), Side.RIGHT));
                    }

                }
                leftLines.add(lineCounterLeft + ": ! " + tuple.leftLine());
                rightLines.add(lineCounterLeft + ": ! " + tuple.rightLine());
                lineCounterLeft++;
                lineNumber++;
                continue;
            }

            leftLines.add(lineCounterLeft + ":   " + tuple.leftLine());
            rightLines.add(lineCounterLeft  + ":   " + tuple.rightLine());
            lineNumber++;
            lineCounterLeft++;
        }

        return new LineResult(leftLines, rightLines, specificLineChanges);

    }
}