import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.DirectoryDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.gui2.menu.Menu;
import com.googlecode.lanterna.gui2.menu.MenuBar;
import com.googlecode.lanterna.gui2.menu.MenuItem;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
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
     void start() {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Screen screen;
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
            System.out.println("Initalization of Lanterna Interface has failed. Please try again and check the Error message");
            System.out.println(e.getMessage());
            System.out.println(Arrays.toString(e.getStackTrace()));
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

        boolean isFirst = true;

        for (String label : labels) {
            Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
            if(isFirst) {
                addMenu(panel);
                panel.addComponent(new EmptySpace(new TerminalSize(0, 2)));
            }
            if(!isFirst) panel.addComponent(new EmptySpace(new TerminalSize(0, 3)));
            panel.addComponent(new Label(label).addStyle(SGR.BOLD));
            TextBox textBox = new TextBox(new TerminalSize(30, 1));
            panel.addComponent(new Label("Gebe einen Pfad ein:"));
            panel.addComponent(textBox);
            panels.add(panel);
            isFirst = false;
        }

        Button confirmButton = new Button("Confirm", () -> {
            output.clear();
            for (Panel panel : panels) {
                Optional<Object> optBox = Arrays.stream(panel.getChildren().toArray()).filter(TextBox.class::isInstance).findFirst();

                if(optBox.isPresent()) {
                    TextBox textBox = (TextBox) optBox.get();
                    output.add(textBox.getText());
                }
            }
            consumer.accept(output);
        });

        for(Panel panel : panels) {
            panel.addComponent(new Label("Oder wähle ein Verzeichnis aus:"));
            panel.addComponent(new Button("Select", () -> {
                File input = new DirectoryDialogBuilder()
                        .setTitle("Wähle das Verzeichnis")
                        .setDescription("Wähle ein Verzeichnis")
                        .setActionLabel("Select")
                        .build()
                        .showDialog(textGUI);

                if (input != null) {
                    Optional<Object> optBox = Arrays.stream(panel.getChildren().toArray()).filter(TextBox.class::isInstance).findFirst();
                    if(optBox.isPresent()) {
                        TextBox textBox = (TextBox) optBox.get();
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
                    .ifPresent(t -> t.setText("Verzeichnis existiert nicht oder ist leer"));
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
                    .ifPresent(t -> t.setText("Verzeichnis existiert nicht oder ist leer"));
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
    private void showFilesAsDirectory(List<File> leftFiles, List<File> rightFiles) {
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
            boolean inBoth = rightFiles.stream().anyMatch(f -> f.getName().equals(file.getName()));
            File rightFile = inBoth ? rightFiles.stream().filter(f -> f.getName().equals(file.getName())).findFirst().orElseThrow() : file;
            if (inBoth) {
                fileName = getFormattedFileName(file, fileName, rightFile);
            } else {
                fileName += " (in L)";
            }
            leftListBox.addItem(fileName, () -> showFileContents(file, rightFile, Side.LEFT));
        }

        for (File file : rightFiles) {
            String fileName = file.getName();
            boolean inBoth = leftFiles.stream().anyMatch(f -> f.getName().equals(file.getName()));
            File leftFile = inBoth ? leftFiles.stream().filter(f -> f.getName().equals(file.getName())).findFirst().orElseThrow() : file;
            if (inBoth) {
                fileName = getFormattedFileName(file, fileName, leftFile);
            } else {
                fileName += " (in R)";
            }
            rightListBox.addItem(fileName, () -> showFileContents(file, leftFile, Side.RIGHT));
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

    private String getFormattedFileName(File file, String fileName, File rightFile) {
        boolean identical = false;
        try {
            identical = Files.mismatch(file.toPath(), rightFile.toPath()) == -1;
        } catch(IOException ignored) {
        }
        if(!identical) {
            fileName += " (in L&R nicht identisch)";
        } else {
            fileName += " (in L&R identisch)";
        }
        return fileName;
    }

    /**
     * Show the contents of 2 files
     * The contents of the files are shown side by side with differences highlighted
     * If the user presses the escape key, return to the file list
     * @param leftFile  File to show on the left
     * @param rightFile File to show on the right
     * @see java.io.File
     */
    private void showFileContents(File leftFile, File rightFile, Side selectedSide) {
        Panel outterPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        Panel leftPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        Panel rightPanel = new Panel(new LinearLayout(Direction.VERTICAL));

        leftPanel.addComponent(new Label("Ausgewählte Datei " + leftFile.getName() + " :").addStyle(SGR.BOLD));
        rightPanel.addComponent(new Label("Datei in anderem Verzeichnis " + rightFile.getName() + " :").addStyle(SGR.BOLD));

        FileUtils.LineResult result = fileUtils.compareFiles(leftFile, rightFile);

        List<String> leftLines = result.left();
        List<String> rightLines = result.right();

        if(leftFile.equals(rightFile) && selectedSide == Side.LEFT) {
            rightLines.clear();
        }

        if(leftFile.equals(rightFile) && selectedSide == Side.RIGHT) {
            leftLines.clear();
        }

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

    /**
     * Reset the window
     * @param listener Listener to remove
     * @see com.googlecode.lanterna.gui2.WindowListenerAdapter
     */
    private void resetWindow(WindowListenerAdapter listener) {
        window.setComponent(null);
        window.removeWindowListener(listener);
    }

    /**
     * Update the screen
     * @see com.googlecode.lanterna.gui2.MultiWindowTextGUI
     * @noinspection unused
     * */
    private void tryScreenUpdate() {
        try {
            textGUI.updateScreen();
        } catch (IOException e) {
            System.out.println("Error updating screen");
        }
    }

    /**
     * Generate a standard file
     * @see java.io.File
     * @return empty file
     * @noinspection unused
     */
    private File generateStandardFile() {
        return new File("placeholderNA");
    }

    /**
     * Adds a menu with help and exit options to the panel
     * @param panel Panel to add the menu to
     * @see com.googlecode.lanterna.gui2.menu.MenuBar
     */
    private void addMenu(Panel panel) {
        MenuBar menuBar = new MenuBar();

        Menu helpMenu = new Menu("Hilfe");
        menuBar.add(helpMenu);
        helpMenu.add(new MenuItem("Guide", () -> MessageDialog.showMessageDialog(textGUI, "Hilfe",
                """
                        - Wählen Sie die Verzeichnisse aus, die Sie vergleichen möchten.
                        - Die Anwendung zeigt die Unterschiede zwischen den Verzeichnissen an.
                        - Verwenden Sie das Menü, um Hilfe anzuzeigen oder die Anwendung zu beenden.""", MessageDialogButton.OK)));

        helpMenu.add(new MenuItem("Über uns", () -> MessageDialog.showMessageDialog(textGUI, "Über uns",
                """
                Entwickelt im Rahmen der SoftwareProjekt 1 Vorlesung der Hochschule für Technik Stuttgart.
                Beteiligte: Benedikt Belschner, Colin Traub, Daniel Rodean, Finn Wolf
                """, MessageDialogButton.OK)));

        Menu exitMenu = new Menu("Beenden");
        menuBar.add(exitMenu);
        exitMenu.add(new MenuItem("Beende Programm", () -> System.exit(3)));
        panel.addComponent(menuBar);
    }
}
