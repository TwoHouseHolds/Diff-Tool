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

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            int yPos = i - yScrollOffset;
            int index = line.indexOf("+");
            index = index - xScrollOffset;

            while (index >= 0 && index + xScrollOffset < String.valueOf(index).length() + 4) {
                graphics.setBackgroundColor(TextColor.ANSI.GREEN);
                graphics.setForegroundColor(TextColor.ANSI.BLACK);
                graphics.putString(index, yPos, "+");

                graphics.setBackgroundColor(TextColor.ANSI.BLUE);
                graphics.setForegroundColor(TextColor.ANSI.WHITE);

                index = line.indexOf("+", index + line.length());
            }

            index = line.indexOf("-");
            index = index - xScrollOffset;

            while (index >= 0 && index + xScrollOffset < String.valueOf(index).length() + 4) {
                //Check if - is outside of view due to scrolling
                graphics.setBackgroundColor(TextColor.ANSI.RED);
                graphics.setForegroundColor(TextColor.ANSI.BLACK);
                graphics.putString(index, yPos, "-");

                graphics.setBackgroundColor(TextColor.ANSI.BLUE);
                graphics.setForegroundColor(TextColor.ANSI.WHITE);

                index = line.indexOf("-", index + line.length());
            }

            index = line.indexOf("!");
            index = index - xScrollOffset;

            while (index >= 0 && index + xScrollOffset < String.valueOf(index).length() + 4) {
                graphics.setBackgroundColor(TextColor.ANSI.YELLOW);
                graphics.setForegroundColor(TextColor.ANSI.BLACK);
                graphics.putString(index, yPos, "!");

                graphics.setBackgroundColor(TextColor.ANSI.BLUE);
                graphics.setForegroundColor(TextColor.ANSI.WHITE);

                index = line.indexOf("!", index + line.length());
            }

            if(coloredTextBox.getSpecificLineChanges() == null) {
                continue;
            }

            for(FileUtils.SpecificLineChange c : coloredTextBox.getSpecificLineChanges()) {

                if(coloredTextBox.getSide() != c.longerSide()) {
                    continue;
                }

                index = c.index() - xScrollOffset;

                if(c.lineNumber() == i) {
                    graphics.setBackgroundColor(TextColor.ANSI.YELLOW);
                    graphics.setForegroundColor(TextColor.ANSI.BLACK);
                    graphics.putString(index, yPos, String.valueOf(c.character()));
                    graphics.setBackgroundColor(TextColor.ANSI.BLUE);
                    graphics.setForegroundColor(TextColor.ANSI.WHITE);
                }
            }
        }
    }
}
