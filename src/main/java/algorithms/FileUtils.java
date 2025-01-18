package algorithms;

import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
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
        try {
            List<String> lines = Files.readAllLines(Paths.get(file.toURI()));
            int lineNumber = 1;
            for(String line : lines) {
                String prefix = lineNumber + ":   ";
                line = prefix + line;
                lines.set(lineNumber - 1, line);
                lineNumber++;
            }
            return lines;
        } catch (IOException e) {
            return List.of("Fehler beim Lesen der Datei");
        }
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

    /**
     * Saves the diff of {@link firstFile} and {@link secondFile} as a .txt File at the Location Path of {@link saveFile}.
     * If a {@link lineResult} is passed in as the last Parameter the first 2 Parameters should be set to null.
     * Then another Diff won't be executed but the existing one will be used instead.
     * @param firstFile First file to be used.
     * @param secondFile Second file to be used.
     * @param saveFile "File" as a representation of the Save-Location
     * @param lineResult Optional: Null if not used. If used firstFile and secondFile should be null
      * @return false if save was unsuccessful. true if successful
     */
    public static boolean saveDiffAsText(File firstFile, File secondFile, File saveFile, LineResult lineResult) {
        if (lineResult != null || (firstFile != null && secondFile != null)) {
            FileUtils.LineResult result = (lineResult == null) ? FileUtils.compareFiles(firstFile, secondFile) : lineResult;

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

            if (saveFile != null) {
                saveFile = new File(saveFile.getAbsolutePath() + ".html");
                if (saveFile.exists()) {
                    return false;
                }
                try {

                    StringBuilder html = new StringBuilder();
                    html.append("<html><head><style>table {border-collapse: collapse;} td {border: 1px solid black; padding: 5px;} .yellow {background-color: yellow;} .green {background-color: lightgreen;} .red {background-color: lightcoral;}</style></head><body><table>");
                    if(firstFile != null && secondFile != null) {
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
     * Escape HTML characters: < and > so they are not interpreted as HTML tags
     * @param str String to escape
     * @return Escaped string
     */
    public static String escapeHtml(String str) {
        return str.replace("<", "&lt;").replace(">", "&gt;");
    }
}