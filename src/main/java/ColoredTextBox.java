import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.TextBox;

/**
 * A TextBox that uses a custom renderer to colorize the text based on the characters in it
 * (e.g. '+' will be green, '-' will be red)
 * @see ColorBoxRenderer
 * @see TextBox
 */
public class ColoredTextBox extends TextBox {
    public ColoredTextBox(TerminalSize initialSize) {
        super(initialSize);
        setRenderer(new ColorBoxRenderer());
    }
}
