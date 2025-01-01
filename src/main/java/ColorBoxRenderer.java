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

        String boxText = textBox.getText();
        String[] lines = boxText.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            int index = line.indexOf("+");
            while (index >= 0 && index < String.valueOf(index).length() + 4) {
                graphics.setBackgroundColor(TextColor.ANSI.GREEN);
                graphics.setForegroundColor(TextColor.ANSI.BLACK);
                graphics.putString(index, i, "+");

                graphics.setBackgroundColor(TextColor.ANSI.BLUE);
                graphics.setForegroundColor(TextColor.ANSI.WHITE);

                index = line.indexOf("+", index + line.length());
            }

            index = line.indexOf("-");
            while (index >= 0 && index < String.valueOf(index).length() + 4) {
                graphics.setBackgroundColor(TextColor.ANSI.RED);
                graphics.setForegroundColor(TextColor.ANSI.BLACK);
                graphics.putString(index, i, "-");

                graphics.setBackgroundColor(TextColor.ANSI.BLUE);
                graphics.setForegroundColor(TextColor.ANSI.WHITE);

                index = line.indexOf("-", index + line.length());
            }

        }
    }
}
