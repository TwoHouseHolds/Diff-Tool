package lanterna;

import algorithms.FileUtils.SpecificLineChange;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;

/**
 * A custom renderer for {@link TextBox} that colorizes the text based on the characters in it
 * (e.g. '+' will be green, '-' will be red)
 * @see ColorBoxRenderer
 * @see TextBox
 * @see TextColor
 * @see TextGUIGraphics
 */
public class ColorBoxRenderer extends TextBox.DefaultTextBoxRenderer {

    @Override
    public void drawComponent(TextGUIGraphics graphics, TextBox textBox) {
        super.drawComponent(graphics, textBox);

        ColoredTextBox coloredTextBox = (ColoredTextBox) textBox;
        ColoredTextBox otherTextBox = coloredTextBox.getOtherBox();

        String boxText = textBox.getText();
        String[] lines = boxText.split("\\r?\\n|\\r");

        String otherBoxText = otherTextBox.getText();
        String[] otherLines = otherBoxText.split("\\r?\\n|\\r");

        int xScrollOffset = getViewTopLeft().getColumn();
        int yScrollOffset = getViewTopLeft().getRow();

        int lineNumber = 1;

        for(int i = 0; i < yScrollOffset; i++) {
            if (i >= lines.length) break;
            String line = lines[i];
            String otherLine = otherLines[i];
            if(line.isEmpty()) continue;
            int symbolLocation = String.valueOf(lineNumber).length() + 2;
            char symbol = line.charAt(symbolLocation);
            char otherSymbol = otherLine.charAt(symbolLocation);
            if(symbol != '-' && otherSymbol != '-') lineNumber++;
        }

        for (int i = yScrollOffset; i < yScrollOffset + textBox.getSize().getRows() - 1; i++) {
            if (i >= lines.length) break;
            String line = lines[i];
            String otherLine = otherLines[i];
            if(line.isEmpty()) continue;
            int yPos = i - yScrollOffset;
            int symbolLocation = String.valueOf(lineNumber).length() + 2;
            int xPos = symbolLocation - xScrollOffset;
            char symbol = line.charAt(symbolLocation);
            char otherSymbol = otherLine.charAt(symbolLocation);

            if((symbol == '+' || symbol == '-' || symbol == '!')) {
                //
                TextColor.ANSI colorOfSymbol = symbol == '+' ? TextColor.ANSI.GREEN //
                        : symbol == '-' ? TextColor.ANSI.RED : TextColor.ANSI.YELLOW;
                graphics.setBackgroundColor(colorOfSymbol);
                graphics.setForegroundColor(TextColor.ANSI.BLACK);
                graphics.putString(xPos, yPos, String.valueOf(symbol));

                graphics.setBackgroundColor(TextColor.ANSI.BLUE);
                graphics.setForegroundColor(TextColor.ANSI.WHITE);
            }
            if(symbol != '-' && otherSymbol != '-') lineNumber++;

            if(coloredTextBox.getSpecificLineChanges() != null) {
                for(SpecificLineChange c : coloredTextBox.getSpecificLineChanges()) {
                    if(coloredTextBox.getSide() == c.displaySide()) {
                        xPos = c.index() - xScrollOffset;
                        if(c.lineNumber() == i) {
                            graphics.setBackgroundColor(TextColor.ANSI.YELLOW);
                            graphics.setForegroundColor(TextColor.ANSI.BLACK);
                            try {
                                graphics.putString(xPos, yPos, String.valueOf(c.character()));
                            } catch (Exception e) {
                                //Not a valid character
                                graphics.putString(xPos, yPos, "?");
                            }
                            graphics.setBackgroundColor(TextColor.ANSI.BLUE);
                            graphics.setForegroundColor(TextColor.ANSI.WHITE);
                        }
                    }
                }
            }
        }
    }
}