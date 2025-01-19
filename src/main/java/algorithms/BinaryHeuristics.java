package algorithms;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;

// TODO UTF16 = Text? (wird oft falsch erkannt
public class BinaryHeuristics {

    public static FileType fileTypeOf(File file, boolean extensive) {
        try {
            FileType fileType = getFileTypeFromMagicNumber(file);
            if (fileType != null) {
                return fileType;
            } else if (isBinary(file, extensive)) {
                return FileType.BINARY;
            } else {
                return FileType.TEXT;
            }
        } catch (IOException e) {
            return FileType.ERROR;
        }
    }

    private static FileType getFileTypeFromMagicNumber(File file) throws IOException {
        FileInputStream fis1 = new FileInputStream(file);
        int[] actual8FirstMN = nextNMagicNumbers(fis1, 8);
        for (FileType fileType : FileType.values()) {
            if (fileType.magicNumbersWithOffset.isEmpty()) { // in most cases magic numbers do not have an offset
                int[] currentMN = fileType.magicNumbers;
                if (magicNumbersMatch(actual8FirstMN, currentMN)) return fileType;
                // check alternative magic numbers if applicable
                Set<int[]> currentAltMN = fileType.alternativeMagicNumbers;
                if (!currentAltMN.isEmpty()) for (int[] amn : currentAltMN) {
                    if (magicNumbersMatch(actual8FirstMN, amn)) return fileType;
                }
            } else { // if magic numbers have offset
                for (FileType.MagicNumberWithOffset mnWithOffset : fileType.magicNumbersWithOffset) {
                    FileInputStream fis2 = new FileInputStream(file);
                    if(mnWithOffset.offsetFromLeft()) fis2.skipNBytes(mnWithOffset.offset()); // skip to location of magic number
                    else fis2.skipNBytes(file.length() - mnWithOffset.offset()); // skip to location of magic number (offset from right)
                    int[] currentMN = mnWithOffset.magicNumber();
                    int[] actualMN = nextNMagicNumbers(fis2, currentMN.length);
                    fis2.close();
                    if(magicNumbersMatch(actualMN, currentMN)) return fileType;
                }
            }
        }
        return null; // no file type matches
    }

    private static int[] nextNMagicNumbers(FileInputStream fis, int n) throws IOException {
        int[] firstMN = new int[n];
        for (int i = 0; i < n; i++) firstMN[i] = fis.read();
        fis.close();
        return firstMN;
    }

    private static boolean magicNumbersMatch(int[] actualMN, int[] currentMN) {
        for (int i = 0; i < currentMN.length; i++) {
            if (currentMN[i] != actualMN[i]) return false;// mismatch
        }
        return true; // all match
    }

    /**
     * Check if a file is binary
     *
     * @param file      File to check
     * @param extensive If true, check the entire file. If false, check the first 1MB
     *                  (1048576 bytes)
     * @return True if the file is binary, false otherwise
     * @see java.io.File
     */
    private static Boolean isBinary(File file, boolean extensive) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        long length = extensive ? file.length() : Math.min(file.length() / 10, 1048576);
        long binaryChars = 0;
        long nonUtf8Chars = 0;
        long nonIsoChars = 0;
        int followChars = 0;
        for (long l = 0; l < length; l++) {
            int c = fis.read();
            if (c == -1) break; // end of input

            // binaryChars = non-printable, non-whitespace control characters
            if (c < ' ' && !Character.isWhitespace(c)) binaryChars++;

            // non-ASCII characters (0x00 to 0x7F) => nonIso/nonUtf8?
            if (!(c < 0x80)) {

                // nonIsoChars = non-ISO control characters (0x81 to 0x9F)
                if (c > 0x80 && c <= 0x9F) nonIsoChars++;

                // nonUtf8Chars
                if ((c & 0xC0) == 0x80) { // UTF-8 continuation bytes (0x80 to 0xBF)
                    if (followChars > 0) followChars--;
                    else nonUtf8Chars++;
                }
                if ((c & 0xE0) == 0xC0) { // start of UTF-8 2-byte-sequence (0xC0 to 0xDF)
                    if (followChars > 0) nonUtf8Chars++;
                    followChars = 1;
                } else if ((c & 0xF0) == 0xE0) { // start of UTF-8 3-byte-sequence (0xE0 to 0xEF)
                    if (followChars > 0) nonUtf8Chars++;
                    followChars = 2;
                } else if ((c & 0xF8) == 0xF0) {// start of UTF-8 4-byte-sequence (0xF0 to 0xF7)
                    if (followChars > 0) nonUtf8Chars++;
                    followChars = 3;
                } else if ((c & 0xFC) == 0xF8) { // start of UTF-8 5-byte-sequence (invalid in modern UTF-8)
                    if (followChars > 0) nonUtf8Chars++;
                    followChars = 4;
                }
            }
        }
        fis.close();
        return binaryChars > length / 10 || nonUtf8Chars > length / 10 || nonIsoChars > length / 10;
    }
}