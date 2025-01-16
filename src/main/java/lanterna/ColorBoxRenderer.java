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

        String boxText = textBox.getText();
        String[] lines = boxText.split("\n");

        int xScrollOffset = getViewTopLeft().getColumn();
        int yScrollOffset = getViewTopLeft().getRow();

        for (int i = yScrollOffset; i < yScrollOffset + textBox.getSize().getRows() - 1; i++) {
            if (i >= lines.length) {
                break;
            }
            String line = lines[i];
            int yPos = i - yScrollOffset;
            int xPos = line.indexOf("+");
            xPos = xPos - xScrollOffset;

            if(xPos < String.valueOf(i).length() + 4) {
                graphics.setBackgroundColor(TextColor.ANSI.GREEN);
                graphics.setForegroundColor(TextColor.ANSI.BLACK);
                graphics.putString(xPos, yPos, "+");

                graphics.setBackgroundColor(TextColor.ANSI.BLUE);
                graphics.setForegroundColor(TextColor.ANSI.WHITE);
            }

            xPos = line.indexOf("-");
            xPos = xPos - xScrollOffset;

            if(xPos < String.valueOf(i).length() + 4) {
                graphics.setBackgroundColor(TextColor.ANSI.RED);
                graphics.setForegroundColor(TextColor.ANSI.BLACK);
                graphics.putString(xPos, yPos, "-");

                graphics.setBackgroundColor(TextColor.ANSI.BLUE);
                graphics.setForegroundColor(TextColor.ANSI.WHITE);
            }

            xPos = line.indexOf("!");
            xPos = xPos - xScrollOffset;

            if(xPos < String.valueOf(i).length() + 4) {
                graphics.setBackgroundColor(TextColor.ANSI.YELLOW);
                graphics.setForegroundColor(TextColor.ANSI.BLACK);
                graphics.putString(xPos, yPos, "!");

                graphics.setBackgroundColor(TextColor.ANSI.BLUE);
                graphics.setForegroundColor(TextColor.ANSI.WHITE);
            }

            if(coloredTextBox.getSpecificLineChanges() == null) {
                continue;
            }

            for(SpecificLineChange c : coloredTextBox.getSpecificLineChanges()) {

                if(coloredTextBox.getSide() != c.displaySide()) {
                    continue;
                }

                xPos = c.index() - xScrollOffset;

                if(c.lineNumber() == i) {
                    graphics.setBackgroundColor(TextColor.ANSI.YELLOW);
                    graphics.setForegroundColor(TextColor.ANSI.BLACK);
                    graphics.putString(xPos, yPos, String.valueOf(c.character()));
                    graphics.setBackgroundColor(TextColor.ANSI.BLUE);
                    graphics.setForegroundColor(TextColor.ANSI.WHITE);
                }
            }
        }
    }
}
