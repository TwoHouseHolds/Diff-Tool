package algorithms;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class BinaryHeuristics {
    /**
     * Check if a file is binary
     * @param file File to check
     * @param extensive If true, check the entire file. If false, check the first 1MB
     *                  (1048576 bytes)
     * @return True if the file is binary, false otherwise
     * @see java.io.File
     */
    public static Boolean isBinary(File file, boolean extensive) {
        try(FileInputStream fis = new FileInputStream(file)){
            long length = extensive ? file.length() : Math.min(file.length()/10, 1048576);
            long binaryChars = 0;
            long nonUtf8Chars = 0;
            long nonIsoChars = 0;
            int followChars=0;
            for (long l = 0; l < length; l++) {
                int c = fis.read();
                if ( c == -1 ) {
                    break;
                }
                if ( c<' ' && !Character.isWhitespace(c) ) {
                    binaryChars++;
                }
                if ( c < 0x80 ) {
                    continue;
                }
                if ( c>0x80 && c<=0x9F ) {
                    nonIsoChars++;
                }
                if ( (c&0xC0) == 0x80 ) {
                    if ( followChars > 0 ) {
                        followChars--;
                    }
                    else {
                        nonUtf8Chars++;
                    }
                }
                if ( (c&0xE0) == 0xC0 ) {
                    if ( followChars > 0 ) {
                        nonUtf8Chars++;
                    }
                    followChars = 1;
                }
                else if ( (c&0xF0) == 0xE0 ) {
                    if ( followChars > 0 ) {
                        nonUtf8Chars++;
                    }
                    followChars = 2;
                }
                else if ( (c&0xF8) == 0xF0 ) {
                    if ( followChars > 0 ) {
                        nonUtf8Chars++;
                    }
                    followChars = 3;
                }
                else if ( (c&0xFC) == 0xF8 ) {
                    if ( followChars > 0 ) {
                        nonUtf8Chars++;
                    }
                    followChars = 4;
                }

            }
            return binaryChars > length/10 || nonUtf8Chars > length/10 ||
                    nonIsoChars > length/10;
        }
        catch (IOException ex) {
            return null;
        }
    }

}
