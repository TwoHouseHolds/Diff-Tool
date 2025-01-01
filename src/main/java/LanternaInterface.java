import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DirectoryDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Lanterna interface for comparing two directories
 *
 * @see com.googlecode.lanterna.gui2.BasicWindow
 * @see com.googlecode.lanterna.gui2.Button
 * @see com.googlecode.lanterna.gui2.EmptySpace
 * @see com.googlecode.lanterna.gui2.Label
 * @see com.googlecode.lanterna.gui2.MultiWindowTextGUI
 * @see com.googlecode.lanterna.gui2.Panel
 * @see com.googlecode.lanterna.gui2.TextBox
 * @see com.googlecode.lanterna.gui2.Window
 */
public class LanternaInterface {

    private int selectedLabelIndex = 0;
    private BasicWindow window;
    private WindowBasedTextGUI textGUI;
    private final FileUtils fileUtils = new FileUtils();
    private List<File> leftDir = new ArrayList<>();
    private List<File> rightDir = new ArrayList<>();

    /**
     * Start the Lanterna interface
     *
     * @see com.googlecode.lanterna.screen.Screen
     * @see com.googlecode.lanterna.terminal.DefaultTerminalFactory
     * @see com.googlecode.lanterna.gui2.MultiWindowTextGUI
     */
    public void start() {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Screen screen = null;
        try {
            terminalFactory.setInitialTerminalSize(new TerminalSize(200, 100));
            screen = terminalFactory.createScreen();
            screen.startScreen();
            textGUI = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
            window = new BasicWindow();
            window.setHints(Set.of(Window.Hint.FIT_TERMINAL_WINDOW, Window.Hint.CENTERED));

            getInput(List.of("First Directory", "Second Directory"), this::compareDirectories);

            textGUI.addWindowAndWait(window);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    /**
     * Get input from the user
     *
     * @param labels   Labels for the input fields
     * @param consumer Consumer for the input
     */
    private void getInput(List<String> labels, Consumer<List<String>> consumer) {
        Panel outterPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        List<Panel> panels = new ArrayList<>();
        List<String> output = new ArrayList<>();

        for (String label : labels) {
            Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
            panel.addComponent(new Label(label).addStyle(SGR.BOLD));
            TextBox textBox = new TextBox(new TerminalSize(30, 1));
            panel.addComponent(new Label("Enter a Path:"));
            panel.addComponent(textBox);
            panels.add(panel);
        }

        Button confirmButton = new Button("Confirm", () -> {
            output.clear();
            for (Panel panel : panels) {
                TextBox textBox = (TextBox) panel.getChildren().toArray()[2];
                output.add(textBox.getText());
            }
            consumer.accept(output);
        });

        for(Panel panel : panels) {
            panel.addComponent(new Label("Or select a directory:"));
            panel.addComponent(new Button("Select", new Runnable() {
                @Override
                public void run() {
                    File input = new DirectoryDialogBuilder()
                            .setTitle("Select directory")
                            .setDescription("Choose a directory")
                            .setActionLabel("Select")
                            .build()
                            .showDialog(textGUI);

                    if (input != null) {
                        TextBox textBox = (TextBox) panel.getChildren().toArray()[2];
                        textBox.setText(input.getAbsolutePath());
                    }
                }
            }));
        }

        Panel rightMostPanel = panels.get(panels.size() - 1);
        rightMostPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));
        rightMostPanel.addComponent(confirmButton);

        for (Panel panel : panels) {
            outterPanel.addComponent(panel);
            outterPanel.addComponent(new EmptySpace(new TerminalSize(2, 0)));
        }


        window.setComponent(outterPanel);
    }

    private void compareDirectories(List<String> strings) {
        compareDirectories(strings.toArray(new String[0]));
    }

    /**
     * Compare 2 directories
     * Then show the files in the directories in a side by side view
     * If a directory does not exist or is empty, show an error message
     * Return to the input screen if the user presses the escape key
     *
     * @param output List of directories only the first 2 are used
     */
    private void compareDirectories(String... output) {
        leftDir = fileUtils.getFiles(output[0]);
        rightDir = fileUtils.getFiles(output[1]);

        if (leftDir == null || leftDir.isEmpty()) {
            ((Panel) window.getComponent()).getChildren().stream()
                    .filter(c -> c instanceof Panel)
                    .map(c -> (Panel) c)
                    .filter(p -> p.getChildren().toArray()[1] instanceof TextBox)
                    .map(p -> (TextBox) p.getChildren().toArray()[1])
                    .findFirst()
                    .ifPresent(t -> t.setText("Directory does not exist or is empty"));
            return;
        }

        if (rightDir == null || rightDir.isEmpty()) {
            ((Panel) window.getComponent()).getChildren().stream()
                    .filter(c -> c instanceof Panel)
                    .map(c -> (Panel) c)
                    .filter(p -> p.getChildren().toArray()[1] instanceof TextBox)
                    .map(p -> (TextBox) p.getChildren().toArray()[1])
                    .skip(1)
                    .findFirst()
                    .ifPresent(t -> t.setText("Directory does not exist or is empty"));
            return;
        }

        showFilesAsDirectory(leftDir, rightDir);
    }

    /**
     * Show the files in a side by side view
     * If a file is in both directories, it is marked as such
     * If a file is only in one directory, it is marked as such
     * If the user presses the escape key, return to the input screen
     *
     * @param leftFiles  List of files in the first directory
     * @param rightFiles List of files in the second directory
     * @see java.io.File
     * @see java.io.File
     */
    public void showFilesAsDirectory(List<File> leftFiles, List<File> rightFiles) {
        Panel outterPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        Panel leftPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        Panel rightPanel = new Panel(new LinearLayout(Direction.VERTICAL));

        leftPanel.addComponent(new Label("Erstes Verzeichnis:").addStyle(SGR.BOLD));
        rightPanel.addComponent(new Label("Zweites Verzeichnis:").addStyle(SGR.BOLD));

        ActionListBox leftListBox = new ActionListBox();
        ActionListBox rightListBox = new ActionListBox();

        List<Label> labels = new ArrayList<>();

        for (File file : leftFiles) {
            String fileName = file.getName();
            boolean inBoth = rightFiles.contains(file);
            if (inBoth) {
                fileName += " (in L&R)";
            } else {
                fileName += " (in L)";
            }
            leftListBox.addItem(fileName, () -> {
                handleFileSelect(leftFiles, rightFiles, file);
            });
        }

        for (File file : rightFiles) {
            String fileName = file.getName();
            boolean inBoth = leftFiles.contains(file);
            if (inBoth) {
                fileName += " (in L&R)";
            } else {
                fileName += " (in R)";
            }
            rightListBox.addItem(fileName, () -> {
                handleFileSelect(leftFiles, rightFiles, file);
            });
        }

        leftPanel.addComponent(leftListBox);
        rightPanel.addComponent(rightListBox);
        outterPanel.addComponent(leftPanel);
        outterPanel.addComponent(new EmptySpace(new TerminalSize(2, 0)));
        outterPanel.addComponent(rightPanel);
        window.setComponent(outterPanel);

        window.addWindowListener(new WindowListenerAdapter() {
            @Override
            public void onInput(Window window, KeyStroke keyStroke, AtomicBoolean atomicBoolean) {
                if (keyStroke.getKeyType() == KeyType.Escape) {
                    labels.clear();
                    resetWindow(this);
                    getInput(List.of("Erstes Verzeichnis:", "Zweites Verzeichnis:"), LanternaInterface.this::compareDirectories);
                }
            }
        });

        if (!labels.isEmpty()) {
            labels.get(0).setBackgroundColor(TextColor.ANSI.RED);
        }
    }

    private void handleFileSelect(List<File> leftFiles, List<File> rightFiles, File file) {
        boolean inLeft = false;
        for (File leftFile : leftFiles) {
            if (leftFile.getName().equals(file.getName())) {
                inLeft = true;
                break;
            }
        }
        boolean inBoth = false;
        if (inLeft) {
            for (File rightFile : rightFiles) {
                if (rightFile.getName().equals(file.getName())) {
                    inBoth = true;
                    break;
                }
            }
        } else {
            for (File leftFile : leftFiles) {
                if (leftFile.getName().equals(file.getName())) {
                    inBoth = true;
                    break;
                }
            }
        }

        if (!inBoth) {
            showFileContents(file, file);
            return;
        }

        File leftFile = inLeft ? file : leftFiles.stream().filter(f -> f.getName().equals(file.getName())).findFirst().get();
        File rightFile = inLeft ? rightFiles.stream().filter(f -> f.getName().equals(file.getName())).findFirst().get() : file;

        showFileContents(leftFile, rightFile);
    }

    public void showFileContents(File leftFile, File rightFile) {
        Panel outterPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        Panel leftPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        Panel rightPanel = new Panel(new LinearLayout(Direction.VERTICAL));

        leftPanel.addComponent(new Label("First File:").addStyle(SGR.BOLD));
        rightPanel.addComponent(new Label("Second File").addStyle(SGR.BOLD));

        FileUtils.LineResult result = fileUtils.compareFiles(leftFile, rightFile);

        List<String> leftLines = result.left();
        List<String> rightLines = result.right();

        ColoredTextBox leftTextBox = new ColoredTextBox(new TerminalSize(100, 100));
        ColoredTextBox rightTextBox = new ColoredTextBox(new TerminalSize(100, 100));

        for (String line : leftLines) {
            leftTextBox.addLine(line);
        }

        for (String line : rightLines) {
            rightTextBox.addLine(line);
        }

        leftTextBox.setReadOnly(true);
        rightTextBox.setReadOnly(true);

        leftPanel.addComponent(leftTextBox);
        rightPanel.addComponent(rightTextBox);

        outterPanel.addComponent(leftPanel);
        outterPanel.addComponent(new EmptySpace(new TerminalSize(2, 0)));
        outterPanel.addComponent(rightPanel);


        ScrollBar verticalScrollBar = new ScrollBar(Direction.VERTICAL);
        verticalScrollBar.setViewSize(100);
        verticalScrollBar.setScrollMaximum(100);

        window.setComponent(outterPanel);

        window.addWindowListener(new WindowListenerAdapter() {
            @Override
            public void onInput(Window window, KeyStroke keyStroke, AtomicBoolean atomicBoolean) {
                if (keyStroke.getKeyType() == KeyType.Escape) {
                    resetWindow(this);
                    showFilesAsDirectory(leftDir, rightDir);
                }
            }
        });
    }

    private void resetWindow(WindowListenerAdapter listener) {
        window.setComponent(null);
        window.removeWindowListener(listener);
        selectedLabelIndex = 0;
    }

    public void tryScreenUpdate() {
        try {
            textGUI.updateScreen();
        } catch (IOException e) {
            System.out.println("Error updating screen");
        }
    }
}
