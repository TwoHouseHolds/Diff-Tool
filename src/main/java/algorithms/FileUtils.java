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
     *
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
     * Read a file and return its lines with line numbers
     *
     * @param file File to read
     * @return List of lines in the file
     */
    public static List<String> readFile(File file) {
        FileType fileType = BinaryHeuristics.fileTypeOf(file, false);
        if (fileType == FileType.ERROR) {
            return List.of("Fehler beim Lesen der Datei");
        }
        if (fileType != FileType.TEXT) {
            return List.of((fileType == FileType.BINARY ? "Binäre " : fileType) + " Dateien können (noch) nicht verglichen werden.");
        }
        try {
            List<String> lines = Files.readAllLines(Paths.get(file.toURI()));
            int lineNumber = 1;
            for (String line : lines) {
                line = lineNumber + ":   " + line;
                lines.set(lineNumber - 1, line);
                lineNumber++;
            }
            return lines;
        } catch (IOException e) {
            return List.of("Fehler beim Lesen der Datei");
        }
    }

    /**
     * Represents the result of comparing two files line by line
     * Has a List<String> left and a List<String right to represent the lines of the files
     *
     * @see java.util.List
     */
    public record LineResult(List<String> left, List<String> right, List<SpecificLineChange> specificLineChanges) {
    }

    /**
     * Represents a specific change in a line
     * The lineNumber is the number of the line that has been changed e.g. 5 for the 5th line
     * The index is the index of the character that has been changed e.g. 5 for the 5th character
     * The character is the character that has been changed e.g. 'a' for the character 'a'
     * The displaySide is the side that has the longer line e.g. utils.Side.LEFT if the left line is longer
     *
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
     *
     * @param leftFile  First file to compare
     * @param rightFile Second file to compare
     * @return Result of the comparison as a LineResult object
     * @see java.io.File
     * @see LineResult
     * @see java.util.List
     */
    public static LineResult compareFiles(File leftFile, File rightFile) {

        // make sure file type is text (BinaryHeuristics)
        FileType leftFileType = BinaryHeuristics.fileTypeOf(leftFile, false);
        FileType rightFileType = BinaryHeuristics.fileTypeOf(rightFile, false);
        if (leftFileType != FileType.TEXT || rightFileType != FileType.TEXT) {
            List<String> leftLines = new ArrayList<>();
            List<String> rightLines = new ArrayList<>();
            if (leftFileType == FileType.ERROR) leftLines.add("Fehler beim Lesen der Datei");
            else if (rightFileType == FileType.ERROR) rightLines.add("Fehler beim Lesen der Datei");
            else {
                leftLines.add((leftFileType == FileType.BINARY ? "Binäre " : leftFileType) + " Dateien können (noch) nicht verglichen werden.");
                rightLines.add((rightFileType == FileType.BINARY ? "Binäre " : rightFileType) + " Dateien können (noch) nicht verglichen werden.");
            }
            return new LineResult(leftLines, rightLines, null);
        }

        // get LineTuples (HuntMcIlroy)
        List<HuntMcIlroy.StringTuple> stringTuples;
        try {
            stringTuples = HuntMcIlroy.compare(leftFile, rightFile);
        } catch (IOException e) {
            List<String> errorList = List.of("Fehler beim Lesen einer Datei");
            return new LineResult(errorList, errorList, null);
        }

        return createLineResultFrom(stringTuples);
    }

    private static LineResult createLineResultFrom(List<HuntMcIlroy.StringTuple> stringTuples) {
        // inputs for "return LineResult"
        List<String> leftLines = new ArrayList<>();
        List<String> rightLines = new ArrayList<>();
        List<SpecificLineChange> specificLineChanges = new ArrayList<>();

        int actualLineNumber = 1;
        int displayedLineNumber = 1;
        for (HuntMcIlroy.StringTuple tuple : stringTuples) {
            if (tuple.leftLine() == null) { // line removed => -
                String emptySpaces = " ".repeat(String.valueOf(displayedLineNumber).length());
                leftLines.add(emptySpaces + "  - ");
                rightLines.add(emptySpaces + "    " + tuple.rightLine());
            } else if (tuple.rightLine() == null) { // line added => +
                leftLines.add(displayedLineNumber + ": + " + tuple.leftLine());
                rightLines.add(displayedLineNumber + ":   ");
                displayedLineNumber++;
            } else if (!tuple.sameLine()) { // lines differ => !
                specificLineChanges.addAll(getSpecificLineChanges(tuple, actualLineNumber, displayedLineNumber));
                leftLines.add(displayedLineNumber + ": ! " + tuple.leftLine());
                rightLines.add(displayedLineNumber + ": ! " + tuple.rightLine());
                displayedLineNumber++;
            } else { // same line => " "
                leftLines.add(displayedLineNumber + ":   " + tuple.leftLine());
                rightLines.add(displayedLineNumber + ":   " + tuple.rightLine());
                displayedLineNumber++;
            }
            actualLineNumber++;
        }
        return new LineResult(leftLines, rightLines, specificLineChanges);
    }

    private static List<SpecificLineChange> getSpecificLineChanges(HuntMcIlroy.StringTuple tuple, int actualLineNumber, int displayedLineNumber) {
        List<SpecificLineChange> newSpecificLineChanges = new ArrayList<>();
        String leftString = tuple.leftLine();
        String rightString = tuple.rightLine();
        String[] leftArray = leftString.split("");
        String[] rightArray = rightString.split("");

        Side longerSide = leftArray.length > rightArray.length ? Side.LEFT : Side.RIGHT;
        String longerString = longerSide == Side.LEFT ? leftString : rightString;
        String shorterString = longerSide == Side.LEFT ? rightString : leftString;

        // check if Strings should be compared character by character (LevenshteinDistance)
        if (LevenshteinDistance.of(leftString, rightString) < longerString.length() * 0.3) {
            String diffString = HuntMcIlroy.compareString(longerString, shorterString);
            for (int i = 0; i < diffString.length(); i++) {
                char firstNonWhiteSpaceChar = longerString.trim().charAt(0);
                int offset = longerString.indexOf(firstNonWhiteSpaceChar) - shorterString.indexOf(firstNonWhiteSpaceChar);
                if (diffString.charAt(i) == '!') {
                    newSpecificLineChanges.add(new SpecificLineChange(actualLineNumber, i + String.valueOf(displayedLineNumber).length() + 4 + offset, longerString.charAt(i), longerSide));
                }
            }
        } else {
            for (int i = 0; i < leftString.length(); i++) {
                newSpecificLineChanges.add(new SpecificLineChange(actualLineNumber, i + String.valueOf(displayedLineNumber).length() + 4, leftString.charAt(i), Side.LEFT));
            }
            for (int i = 0; i < rightString.length(); i++) {
                newSpecificLineChanges.add(new SpecificLineChange(actualLineNumber, i + String.valueOf(displayedLineNumber).length() + 4, rightString.charAt(i), Side.RIGHT));
            }
        }
        return newSpecificLineChanges;
    }

    /**
     * Saves the diff of firstFile and secondFile as a .txt File at the Location Path of saveFile.
     * If a lineResult is passed in as the last Parameter the first 2 Parameters should be set to null.
     * Then another Diff won't be executed but the existing one will be used instead.
     *
     * @param firstFile  First file to be used.
     * @param secondFile Second file to be used.
     * @param saveFile   "File" as a representation of the Save-Location
     * @param lineResult Optional: Null if not used. If used firstFile and secondFile should be null
     * @return false if save was unsuccessful. true if successful
     */
    public static boolean saveDiffAsText(File firstFile, File secondFile, File saveFile, LineResult lineResult) {
        if (lineResult != null || (firstFile != null && secondFile != null)) {
            FileUtils.LineResult result = (lineResult == null) ? FileUtils.compareFiles(firstFile, secondFile) : lineResult;

            if(lineResult != null && (lineResult.left() == null || lineResult.right() == null)) {
                return false;
            }

            if (saveFile != null) {
                saveFile = new File(saveFile.getAbsolutePath() + ".txt");
                if (saveFile.exists()) {
                    return false;
                }
                try {
                    List<String> res = result.left();
                    res.add("\n\n\n==============================================================\n\n\n");
                    res.addAll(result.right());

                    Files.write(saveFile.toPath(), res);
                    return true;
                } catch (IOException e) {
                    return false;
                }
            }
        }
        return false;
    }

    public static boolean saveDiffAsHTML(File firstFile, File secondFile, File saveFile, LineResult lineResult) {
        if (lineResult != null || (firstFile != null && secondFile != null)) {
            FileUtils.LineResult result = (lineResult == null) ? FileUtils.compareFiles(firstFile, secondFile) : lineResult;

            if(lineResult != null && (lineResult.left() == null || lineResult.right() == null)) {
                return false;
            }

            if (saveFile != null) {
                saveFile = new File(saveFile.getAbsolutePath() + ".html");
                if (saveFile.exists()) {
                    return false;
                }
                try {

                    StringBuilder html = new StringBuilder();
                    html.append("<html><head><style>table {border-collapse: collapse;} td {border: 1px solid black; padding: 5px;} .yellow {background-color: yellow;} .green {background-color: lightgreen;} .red {background-color: lightcoral;}</style></head><body><table>");
                    if (firstFile != null && secondFile != null) {
                        html.append("<tr><td>").append(firstFile.getName()).append("</td><td>").append(secondFile.getName()).append("</td></tr>");
                        html.append("<tr><td>").append(firstFile.getAbsolutePath()).append("</td><td>").append(secondFile.getAbsolutePath()).append("</td></tr>");
                    }
                    for (int i = 0; i < result.left().size(); i++) {
                        String leftLine = escapeHtml(result.left().get(i));
                        String rightLine = escapeHtml(result.right().get(i));
                        int lineIndex = i + 1;
                        if (leftLine.contains("!") && leftLine.indexOf("!") < String.valueOf(lineIndex).length() + 4) {
                            html.append("<tr><td class=\"yellow\">").append(leftLine).append("</td><td class=\"yellow\">").append(rightLine).append("</td></tr>");
                        } else if (leftLine.contains("+") && leftLine.indexOf("+") < String.valueOf(lineIndex).length() + 4) {
                            html.append("<tr><td class=\"green\">").append(leftLine).append("</td><td class=\"green\">").append(rightLine).append("</td></tr>");
                        } else if (leftLine.contains("-") && leftLine.indexOf("-") < String.valueOf(lineIndex).length() + 4) {
                            html.append("<tr><td class=\"red\">").append(leftLine).append("</td><td class=\"red\">").append(rightLine).append("</td></tr>");
                        } else {
                            html.append("<tr><td>").append(leftLine).append("</td><td>").append(rightLine).append("</td></tr>");
                        }
                    }
                    html.append("</table></body></html>");


                    Files.write(saveFile.toPath(), html.toString().getBytes());
                    return true;
                } catch (IOException e) {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Escape HTML characters (< and >) so they are not interpreted as HTML tags
     *
     * @param str String to escape
     * @return Escaped string
     */
    private static String escapeHtml(String str) {
        return str.replace("<", "&lt;").replace(">", "&gt;");
    }
}