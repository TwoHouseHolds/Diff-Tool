package lanterna;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlackjackMinigame {

    private final Window window;
    private final Random random;
    List<Integer> playerHand = new ArrayList<>();
    List<Integer> dealerHand = new ArrayList<>();
    Panel panel;

    /**
     * Funny Blackjack minigame :)
     * Just a simple text based badly implemented Blackjack game
     * @param textGUI TextGUI to add the window to
     */
    public BlackjackMinigame(WindowBasedTextGUI textGUI) {
        this.random = new Random();
        window = new BasicWindow("Blackjack Minigame");
        window.setHints(Set.of(Window.Hint.CENTERED));
        panel = new Panel(new LinearLayout(Direction.VERTICAL));

        window.addWindowListener(new WindowListenerAdapter() {
            @Override
            public void onInput(Window window, KeyStroke keyStroke, AtomicBoolean atomicBoolean) {
                if (keyStroke.getKeyType() == KeyType.Escape) {
                    window.close();
                }
            }
        });

        resetGame();

        window.setComponent(panel);
        textGUI.addWindow(window);
    }

    private void resetGame() {
        playerHand.clear();
        dealerHand.clear();
        playerHand.add(random.nextInt(10) + 1);
        playerHand.add(random.nextInt(10) + 1);
        dealerHand.add(random.nextInt(10) + 1);
        dealerHand.add(random.nextInt(10) + 1);
        resetToStandardLayout();
    }

    private void resetToStandardLayout() {
        panel.removeAllComponents();
        panel.addComponent(new Label("Player Hand: " + playerHand));
        panel.addComponent(new Label("Dealer Hand: " + dealerHand));
        panel.addComponent(new Button("Hit", this::hit));
        panel.addComponent(new Button("Stand", this::stand));
        panel.addComponent(new Button("Exit", window::close));
    }

    private void hit() {
        playerHand.add(random.nextInt(10) + 1);
        resetToStandardLayout();
        if (playerHand.stream().mapToInt(Integer::intValue).sum() > 21) {
            panel.removeAllComponents();
            panel.addComponent(new Label("Player Hand: " + playerHand));
            panel.addComponent(new Label("Dealer Hand: " + dealerHand));
            panel.addComponent(new Label("Bust!"));
            panel.addComponent(new Button("Play Again", this::resetGame));
            panel.addComponent(new Button("Exit", window::close));
        }
    }

    private void stand() {
        while (dealerHand.stream().mapToInt(Integer::intValue).sum() < 17) {
            dealerHand.add(random.nextInt(10) + 1);
        }
        panel.removeAllComponents();
        panel.addComponent(new Label("Player Hand: " + playerHand));
        panel.addComponent(new Label("Dealer Hand: " + dealerHand));
        if (dealerHand.stream().mapToInt(Integer::intValue).sum() > 21) {
            panel.addComponent(new Label("Dealer Busts!"));
            panel.addComponent(new Button("Play Again", this::resetGame));
            panel.addComponent(new Button("Exit", window::close));
        } else if (dealerHand.stream().mapToInt(Integer::intValue).sum() > playerHand.stream().mapToInt(Integer::intValue).sum()) {
            panel.addComponent(new Label("Dealer Wins!"));
            panel.addComponent(new Button("Play Again", this::resetGame));
            panel.addComponent(new Button("Exit", window::close));
        } else {
            panel.addComponent(new Label("Player Wins!"));
            panel.addComponent(new Button("Play Again", this::resetGame));
            panel.addComponent(new Button("Exit", window::close));
        }
    }

}