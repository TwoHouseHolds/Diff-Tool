package algorithms;

import java.util.Arrays;

public class LevenshteinDistance {

    public static int of(String s1, String s2) {
        int[][] matrix = new int[s2.length() + 1][s1.length() + 1];
        for (int i = 0; i < matrix.length; i++) matrix[i][0] = i; // fill first column with ascending values
        for (int i = 0; i < matrix[0].length; i++) matrix[0][i] = i; // fill first row with ascending values
        // iterate over matrix, starting at field (1,1)
        for (int s2Index = 0; s2Index < s2.length(); s2Index++) {
            for (int s1Index = 0; s1Index < s1.length(); s1Index++) {
                int matrixRow = s2Index + 1;
                int matrixColumn = s1Index + 1;
                int insert = matrix[matrixRow - 1][matrixColumn] + 1; // Feld darüber
                int delete = matrix[matrixRow][matrixColumn - 1] + 1; // Feld links daneben
                int substitute = matrix[matrixRow - 1][matrixColumn - 1] //Feld diagonal links oben
                        + (s1.charAt(s1Index) == s2.charAt(s2Index) ? 0 : 1); // wenn gleicher Buchstabe: "keine Kosten"
                matrix[matrixRow][matrixColumn] = Math.min(insert, Math.min(delete, substitute)); // Minimum der 3 Werte
            }
        }
        return matrix[matrix.length - 1][matrix[0].length - 1]; // Feld ganz unten rechts
    }

    /*    k i t t e n

        0 1 2 3 4 5 6
    s   1 1 2 3 4 5 6
    i   2 2 1 2 3 4 5
    t   3 3 2 1 2 3 4
    t   4 4 3 2 1 2 3
    i   5 5 4 3 2 2 3
    n   6 6 5 4 3 3 2
    g   7 7 6 5 4 4 3 */
}
