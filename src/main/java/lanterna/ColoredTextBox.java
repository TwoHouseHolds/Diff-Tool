package lanterna;

import algorithms.FileUtils;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.TextBox;

import java.util.ArrayList;
import java.util.List;

/**
 * A TextBox that uses a custom renderer to colorize the text based on the characters in it
 * (e.g. '+' will be green, '-' will be red)
 * @see ColorBoxRenderer
 * @see TextBox
 */
public class ColoredTextBox extends TextBox {
    private final Side side;
    private List<FileUtils.SpecificLineChange> specificLineChanges = new ArrayList<>();
    public ColoredTextBox(TerminalSize initialSize, Side side) {
        super(initialSize, Style.MULTI_LINE);
        this.side = side;
        setRenderer(new ColorBoxRenderer());
    }

    public void setSpecificLineChanges(List<FileUtils.SpecificLineChange> specificLineChanges) {
        this.specificLineChanges = specificLineChanges;
    }

    public List<FileUtils.SpecificLineChange> getSpecificLineChanges() {
        return specificLineChanges;
    }

    public Side getSide() {
        return side;
    }

}
