package swing;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;

public enum SwingTheme {
    LIGHT(Color.BLACK, new FlatMacLightLaf()), //
    DARK(Color.LIGHT_GRAY, new FlatMacDarkLaf()), //
    HACKER(Color.GREEN, new FlatMacDarkLaf());

    public final ColorUIResource textColor;
    public final LookAndFeel laf;

    SwingTheme(Color textColor, LookAndFeel laf) {
        this.textColor = textColor != null ? new ColorUIResource(textColor) : null;
        this.laf = laf;
    }
}
