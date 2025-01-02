import com.googlecode.lanterna.gui2.*;

import java.util.Random;
import java.util.Set;

public class TicTacToeMinigame {

    private final Window window;
    private final TextGUI textGUI;
    private final Random random;
    Panel panel;

    public TicTacToeMinigame(WindowBasedTextGUI textGUI) {
        this.textGUI = textGUI;
        this.random = new Random();
        window = new BasicWindow("Blackjack Minigame");
        window.setHints(Set.of(Window.Hint.CENTERED));
        panel = new Panel(new LinearLayout(Direction.VERTICAL));
    }

}