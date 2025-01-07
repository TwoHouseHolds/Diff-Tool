import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import java.io.IOException;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class LanternaTicTacToeMinigame {

    private final Window window;
    private final WindowBasedTextGUI textGUI;
    private final Random random;
    int[][] board = new int[3][3];
    Panel panel;

    int currentPlayer = 0;

    int playerOne = 0;
    int playerTwo = 1;

    int selectedX = 0;
    int selectedY = 0;

    /**
     * Funny TicTacToe minigame :)
     * Just a simple text based badly implemented TicTacToe game
     *
     * @param textGUI TextGUI to add the window to
     */
    public LanternaTicTacToeMinigame(WindowBasedTextGUI textGUI) {
        this.textGUI = textGUI;
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
                if(keyStroke.getKeyType() == KeyType.Tab) {
                    selectedY++;
                    if(selectedX > 2) {
                        selectedX = 0;
                        selectedY = 0;
                    }
                    if(selectedY > 2) {
                        selectedY = 0;
                        selectedX++;
                    }
                    refreshBoard();
                }
                if(keyStroke.getKeyType() == KeyType.Backspace) {
                    setBoard(selectedX, selectedY, currentPlayer);
                    currentPlayer = currentPlayer == playerOne ? playerTwo : playerOne;
                    refreshBoard();
                }
            }
        });

        resetGame();

        window.setComponent(panel);
        textGUI.addWindow(window);
    }

    private void resetGame() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = -1;
            }
        }
        currentPlayer = random.nextInt(2);
        refreshBoard();
    }


    private void refreshBoard() {
        panel.removeAllComponents();
        for(int i = 0; i < 3; i++) {
            final StringBuilder row = getCurrentBoard(i);
            panel.addComponent(new Label(row.toString()));
            if(i < 2) {
                panel.addComponent(new Label("-----"));
            }
        }

        panel.addComponent(new Label("Current player: " + (currentPlayer == playerOne ? "O" : "X")));
        panel.addComponent(new Button("Exit", window::close));

        try {
            textGUI.updateScreen();
        } catch (IOException e) {
            System.out.println("Error updating screen");
            System.out.println(e.getMessage());
        }
    }

    private StringBuilder getCurrentBoard(int i) {
        StringBuilder row = new StringBuilder();
        for(int j = 0; j < 3; j++) {
            if(board[i][j] == -1) {
                if(selectedX == i && selectedY == j) {
                    row.append("*");
                } else {
                    row.append(" ");
                }
            } else if(board[i][j] == 0) {
                row.append("O");
            } else {
                row.append("X");
            }
            if(j < 2) {
                row.append("|");
            }
        }
        return row;
    }

    private void setBoard(int x, int y, int player) {
        if(board[x][y] == -1) {
            board[x][y] = player;
            checkWin();
        } else {
            MessageDialog.showMessageDialog(textGUI, "Invalid move", "This cell is already taken");
        }
    }

    private void checkWin() {
        for(int i = 0; i < 3; i++) {
            if(board[i][0] == board[i][1] && board[i][1] == board[i][2] && board[i][0] != -1) {
                MessageDialog.showMessageDialog(textGUI, "Game Over", "Player " + (board[i][0] == 0 ? "O" : "X") + " wins!");
                resetGame();
                return;
            }
            if(board[0][i] == board[1][i] && board[1][i] == board[2][i] && board[0][i] != -1) {
                MessageDialog.showMessageDialog(textGUI, "Game Over", "Player " + (board[0][i] == 0 ? "O" : "X") + " wins!");
                resetGame();
                return;
            }
        }
        if(board[0][0] == board[1][1] && board[1][1] == board[2][2] && board[0][0] != -1) {
            MessageDialog.showMessageDialog(textGUI, "Game Over", "Player " + (board[0][0] == 0 ? "O" : "X") + " wins!");
            resetGame();
            return;
        }
        if(board[0][2] == board[1][1] && board[1][1] == board[2][0] && board[0][2] != -1) {
            MessageDialog.showMessageDialog(textGUI, "Game Over", "Player " + (board[0][2] == 0 ? "O" : "X") + " wins!");
            resetGame();
        }
    }
}