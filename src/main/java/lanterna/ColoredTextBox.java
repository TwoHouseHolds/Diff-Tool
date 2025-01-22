package lanterna;

import algorithms.FileUtils;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.TextBox;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.lanterna.input.KeyStroke;
import utils.Side;

/**
 * A TextBox that uses a custom renderer to colorize the text based on the characters in it
 * (e.g. '+' will be green, '-' will be red)
 * @see ColorBoxRenderer
 * @see TextBox
 */
public class ColoredTextBox extends TextBox {
    private final Side side;
    private List<FileUtils.SpecificLineChange> specificLineChanges = new ArrayList<>();
    ColoredTextBox otherBox = null;
    ColoredTextBox scrollSlave = null;

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

    public void setScrollSlave(ColoredTextBox slave) {
        this.scrollSlave = slave;
    }

    public void setOtherBox(ColoredTextBox otherBox) {
        this.otherBox = otherBox;
    }

    public ColoredTextBox getOtherBox() {
        return this.otherBox;
    }

    @Override
    public synchronized Result handleKeyStroke(KeyStroke keyStroke) {
        if(this.scrollSlave != null) {
            this.scrollSlave.handleKeyStroke(keyStroke);
        }
        return super.handleKeyStroke(keyStroke);
    }
}
