import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.DirectoryDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.gui2.menu.Menu;
import com.googlecode.lanterna.gui2.menu.MenuBar;
import com.googlecode.lanterna.gui2.menu.MenuItem;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
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
    private Screen screen;

    /**
     * Start the Lanterna interface
     *
     * @see com.googlecode.lanterna.screen.Screen
     * @see com.googlecode.lanterna.terminal.DefaultTerminalFactory
     * @see com.googlecode.lanterna.gui2.MultiWindowTextGUI
     */
     void start() {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
         terminalFactory.setPreferTerminalEmulator(true);
        try {
            terminalFactory.setInitialTerminalSize(new TerminalSize(200, 100));
            screen = terminalFactory.createScreen();
            screen.startScreen();
            textGUI = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));

            window = new BasicWindow();
            window.setHints(Set.of(Window.Hint.FIT_TERMINAL_WINDOW, Window.Hint.CENTERED));

            getInput(List.of("Erstes Verzeichnis", "Zweites Verzeichnis"), this::compareDirectories);

            textGUI.addWindowAndWait(window);

        } catch (Exception e) {
            System.out.println("Initialization of Lanterna Interface has failed. Please try again and check the Error message");
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

        TextBox leftTextBox = ((Panel) window.getComponent()).getChildren().stream()
                .filter(Panel.class::isInstance)
                .map(c -> (Panel) c)
                .flatMap(p -> p.getChildren().stream())
                .filter(TextBox.class::isInstance)
                .map(c -> (TextBox) c)
                .findFirst()
                .orElseThrow();

        TextBox rightTextBox = ((Panel) window.getComponent()).getChildren().stream()
                .filter(Panel.class::isInstance)
                .map(c -> (Panel) c)
                .flatMap(p -> p.getChildren().stream())
                .filter(TextBox.class::isInstance)
                .map(c -> (TextBox) c)
                .skip(1)
                .findFirst()
                .orElseThrow();

        if (leftDir == null) {
            MessageDialog.showMessageDialog(textGUI, "Fehler", "Linkes Verzeichnis existiert nicht oder ist leer", MessageDialogButton.OK);
            if (rightDir == null) {
                MessageDialog.showMessageDialog(textGUI, "Fehler", "Rechtes Verzeichnis existiert nicht oder ist leer", MessageDialogButton.OK);
                return;
            }
            return;
        }

        if (rightDir == null) {
            MessageDialog.showMessageDialog(textGUI, "Fehler", "Rechtes Verzeichnis existiert nicht oder ist leer", MessageDialogButton.OK);

            if(leftDir == null) {
                MessageDialog.showMessageDialog(textGUI, "Fehler", "Linkes Verzeichnis existiert nicht oder ist leer", MessageDialogButton.OK);
                return;
            }
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

        if((leftFiles == null || rightFiles == null) || (leftFiles.isEmpty() && rightFiles.isEmpty())) {
            getInput(List.of("Erstes Verzeichnis:", "Zweites Verzeichnis:"), LanternaInterface.this::compareDirectories);
            return;
        }

        Panel outterPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        Panel leftPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        Panel rightPanel = new Panel(new LinearLayout(Direction.VERTICAL));

        leftPanel.addComponent(new Label("Erstes Verzeichnis:").addStyle(SGR.BOLD));
        rightPanel.addComponent(new Label("Zweites Verzeichnis:").addStyle(SGR.BOLD));

        ActionListBox leftListBox = new ActionListBox();
        ActionListBox rightListBox = new ActionListBox();

        for (File leftFile : leftFiles) {
            String fileName = leftFile.getName();
            boolean inBoth = rightFiles.stream().anyMatch(f -> f.getName().equals(leftFile.getName()));
            File rightFile = inBoth ? rightFiles.stream().filter(f -> f.getName().equals(leftFile.getName())).findFirst().orElseThrow() : leftFile;
            if (inBoth) {
                fileName = getFormattedFileName(leftFile, fileName, rightFile);
            } else {
                fileName += " (in L)";
            }
            leftListBox.addItem(fileName, () -> showFileContents(leftFile, rightFile, Side.LEFT));
        }

        for (File rightFile : rightFiles) {
            String fileName = rightFile.getName();
            boolean inBoth = leftFiles.stream().anyMatch(f -> f.getName().equals(rightFile.getName()));
            File leftFile = inBoth ? leftFiles.stream().filter(f -> f.getName().equals(rightFile.getName())).findFirst().orElseThrow() : rightFile;
            if (inBoth) {
                fileName = getFormattedFileName(rightFile, fileName, leftFile);
            } else {
                fileName += " (in R)";
            }
            rightListBox.addItem(fileName, () -> showFileContents(rightFile, leftFile, Side.RIGHT));
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
                    resetWindow(this);
                    getInput(List.of("Erstes Verzeichnis:", "Zweites Verzeichnis:"), LanternaInterface.this::compareDirectories);
                }
            }
        });
    }

    private String getFormattedFileName(File leftFile, String fileName, File rightFile) {
        boolean identical = false;
        try {
            identical = Files.mismatch(leftFile.toPath(), rightFile.toPath()) == -1;
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

        ColoredTextBox leftTextBox = new ColoredTextBox(new TerminalSize(100, 100), Side.LEFT);
        ColoredTextBox rightTextBox = new ColoredTextBox(new TerminalSize(100, 100), Side.RIGHT);

        for (String line : leftLines) {
            leftTextBox.addLine(line);
        }

        for (String line : rightLines) {
            rightTextBox.addLine(line);
        }

        leftTextBox.setSpecificLineChanges(result.specificLineChanges());
        rightTextBox.setSpecificLineChanges(result.specificLineChanges());

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
                        - Die Anwendung zeigt die Unterschiede zwischen 2 Verzeichnissen an.
                          Zusätzlich können Sie die Unterschiede zwischen 2 Dateien anzeigen lassen.
                          Hierfür wählen Sie die Dateien aus, die Sie vergleichen möchten, indem
                          Sie den Pfad in der TextBox eingeben oder das Verzeichnis mit dem Button "Select" auswählen.
                          Danach auf "Confirm" klicken.
                        - Jederzeit können Sie mit der Escape-Taste zum vorherigen Menü zurückkehren.
                        - Um diese Anleitung erneut anzuzeigen, wählen Sie im Menü "Hilfe" -> "Guide".
                        - Um Informationen über die Entwickler zu erhalten, wählen Sie im Menü "Hilfe" -> "Über uns".
                        - Um ein Blackjack-Minispiel zu starten, wählen Sie im Menü "Hilfe" -> "Blackjack".
                        - Um ein TicTacToe-Minispiel zu starten, wählen Sie im Menü "Hilfe" -> "TicTacToe".
                        - Um 2 Dateien manuell zu vergleichen, wählen Sie im Menü "Datei" -> "Manueller Vergleich von 2 Dateien".
                        - Um die Differenz von 2 Dateien zu speichern, wählen Sie im Menü "Datei" -> "Differenz von 2 Dateien speichern".
                        - Um eine Datei zu bearbeiten, wählen Sie im Menü "Datei" -> "Datei editieren".
                        - Um das Programm zu beenden, wählen Sie im Menü "Beenden" -> "Beende Programm".
                        """, MessageDialogButton.OK)));

        helpMenu.add(new MenuItem("Über uns", () -> MessageDialog.showMessageDialog(textGUI, "Über uns",
                """
                Entwickelt im Rahmen der SoftwareProjekt 1 Vorlesung der Hochschule für Technik Stuttgart.
                Beteiligte: Benedikt Belschner, Colin Traub, Daniel Rodean, Finn Wolf
                """, MessageDialogButton.OK)));

        helpMenu.add(new MenuItem("GUI", () -> {
            if(GraphicsEnvironment.isHeadless()) {
                MessageDialog.showMessageDialog(textGUI, "Fehler", "Das Programm kann nicht in einer GUI-Umgebung ausgeführt werden.\nDas System läuft im Headless Modus", MessageDialogButton.OK);
                return;
            }
            window.close();
            try {
                screen.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            SwingInterface swingInterface = new SwingInterface();
            swingInterface.start();
        }));

        Menu fileMenu = new Menu("Datei");
        menuBar.add(fileMenu);
        fileMenu.add(new MenuItem("Manueller Vergleich von 2 Dateien", this::diffFilesManually));

        fileMenu.add(new MenuItem("Differenz von 2 Dateien speichern - Rohtext", this::compAndSaveText));

        fileMenu.add(new MenuItem("Differenz von 2 Dateien speichern - HTML", this::compAndSaveHtml));

        fileMenu.add(new MenuItem("Datei editieren", this::editFile));

        Menu additionalMenu = new Menu("Zusätzliches");
        menuBar.add(additionalMenu);

        additionalMenu.add(new MenuItem("Blackjack", () -> new BlackjackMinigame(textGUI)));

        additionalMenu.add(new MenuItem("TicTacToe", () -> new LanternaTicTacToeMinigame(textGUI)));

        additionalMenu.add(new MenuItem("GUI-TicTacToe", () -> {
            if(GraphicsEnvironment.isHeadless()) {
                MessageDialog.showMessageDialog(textGUI, "Fehler", "Das Spiel kann nicht in einer GUI-Umgebung ausgeführt werden.\nDas System läuft im Headless Modus", MessageDialogButton.OK);
                return;
            }
            SwingTicTacToeMinigame swingTicTacToeMinigame = new SwingTicTacToeMinigame();
            JFrame frame = new JFrame("GUI - TicTacToe");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(swingTicTacToeMinigame);
            frame.pack();
            frame.setVisible(true);
        }));

        Menu exitMenu = new Menu("Beenden");
        menuBar.add(exitMenu);
        exitMenu.add(new MenuItem("Beende Programm", () -> System.exit(3)));
        panel.addComponent(menuBar);
    }

    /**
     * Let the user manually compare 2 files
     * @see com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder
     * @see com.googlecode.lanterna.gui2.dialogs.MessageDialog
     * @see com.googlecode.lanterna.gui2.dialogs.MessageDialogButton
     * @see java.io.File
     */
    private void diffFilesManually() {
        File file1 = new FileDialogBuilder()
                .setTitle("Wähle die 1 Datei")
                .setDescription("Wähle eine Datei")
                .setActionLabel("Select")
                .build()
                .showDialog(textGUI);

        File file2 = new FileDialogBuilder()
                .setTitle("Wähle die 2 Datei")
                .setDescription("Wähle eine Datei")
                .setActionLabel("Select")
                .build()
                .showDialog(textGUI);

        if (file1 != null && file2 != null) {
            leftDir.clear();
            rightDir.clear();
            showFileContents(file1, file2, Side.LEFT);
        }
    }

    /**
     * Let the User compare 2 files and save the differences as a text file
     * @see com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder
     * @see com.googlecode.lanterna.gui2.dialogs.MessageDialog
     * @see com.googlecode.lanterna.gui2.dialogs.MessageDialogButton
     * @see java.io.File
     */
    private void compAndSaveText() {
        File file1 = new FileDialogBuilder()
                .setTitle("Wähle die 1 Datei")
                .setDescription("Wähle eine Datei")
                .setActionLabel("Select")
                .build()
                .showDialog(textGUI);

        File file2 = new FileDialogBuilder()
                .setTitle("Wähle die 2 Datei")
                .setDescription("Wähle eine Datei")
                .setActionLabel("Select")
                .build()
                .showDialog(textGUI);

        if (file1 != null && file2 != null) {
            FileUtils.LineResult result = fileUtils.compareFiles(file1, file2);

            File file =  new FileDialogBuilder()
                    .setTitle("Speichere die Differenz")
                    .setDescription("Wähle einen Speicherort")
                    .setActionLabel("Speichern")
                    .build()
                    .showDialog(textGUI);

            if(file != null) {
                if(file.exists()) {
                    MessageDialog.showMessageDialog(textGUI, "Fehler", "Die Datei existiert bereits", MessageDialogButton.OK);
                    return;
                }
                try {
                    Files.write(file.toPath(), result.left());
                    Files.write(file.toPath(), result.right());
                    MessageDialog.showMessageDialog(textGUI, "Erfolg", "Die Datei wurde erfolgreich gespeichert", MessageDialogButton.OK);
                } catch (IOException e) {
                    MessageDialog.showMessageDialog(textGUI, "Fehler", "Die Datei konnte nicht gespeichert werden", MessageDialogButton.OK);
                }
            }
        }
    }

    /**
     * Let the user edit a file
     * @see com.googlecode.lanterna.gui2.dialogs.FileDialogBuilder
     * @see com.googlecode.lanterna.gui2.dialogs.MessageDialog
     * @see com.googlecode.lanterna.gui2.dialogs.MessageDialogButton
     * @see java.io.File
     */
    private void editFile() {
        File file = new FileDialogBuilder()
                .setTitle("Wähle die Datei die bearbeitet werden soll")
                .setDescription("Wähle eine Datei")
                .setActionLabel("Select")
                .build()
                .showDialog(textGUI);

        if (file != null) {
            try {
                List<String> lines = Files.readAllLines(file.toPath());
                StringBuilder content = new StringBuilder();
                for (String line : lines) {
                    content.append(line).append("\n");
                }
                TextBox textBox = new TextBox(new TerminalSize(100, 40));
                textBox.setText(content.toString());
                textBox.setTheme(new SimpleTheme(TextColor.ANSI.BLACK, TextColor.ANSI.WHITE));
                Panel editPanel = new Panel(new LinearLayout(Direction.VERTICAL));
                editPanel.addComponent(new Label("Bearbeite die Datei: " + file.getName()).addStyle(SGR.BOLD));
                editPanel.addComponent(textBox);
                editPanel.addComponent(new Button("Speichern", () -> {
                    try {
                        Files.write(file.toPath(), textBox.getText().getBytes());
                        MessageDialog.showMessageDialog(textGUI, "Erfolg", "Die Datei wurde erfolgreich gespeichert", MessageDialogButton.OK);
                        getInput(List.of("Erstes Verzeichnis:", "Zweites Verzeichnis:"), LanternaInterface.this::compareDirectories);
                    } catch (IOException e) {
                        MessageDialog.showMessageDialog(textGUI, "Fehler", "Die Datei konnte nicht gespeichert werden", MessageDialogButton.OK);
                        getInput(List.of("Erstes Verzeichnis:", "Zweites Verzeichnis:"), LanternaInterface.this::compareDirectories);
                    }
                }));
                editPanel.addComponent(new Button("Abbrechen", () -> getInput(List.of("Erstes Verzeichnis:", "Zweites Verzeichnis:"), LanternaInterface.this::compareDirectories)));
                window.setComponent(editPanel);
            } catch (IOException e) {
                MessageDialog.showMessageDialog(textGUI, "Fehler", "Die Datei konnte nicht gelesen werden oder existiert nicht", MessageDialogButton.OK);
                getInput(List.of("Erstes Verzeichnis:", "Zweites Verzeichnis:"), LanternaInterface.this::compareDirectories);
            }
        }
    }

    private void compAndSaveHtml() {
        File file1 = new FileDialogBuilder()
                .setTitle("Wähle die 1 Datei")
                .setDescription("Wähle eine Datei")
                .setActionLabel("Select")
                .build()
                .showDialog(textGUI);

        File file2 = new FileDialogBuilder()
                .setTitle("Wähle die 2 Datei")
                .setDescription("Wähle eine Datei")
                .setActionLabel("Select")
                .build()
                .showDialog(textGUI);

        if (file1 != null && file2 != null) {
            FileUtils.LineResult result = fileUtils.compareFiles(file1, file2);

            File file =  new FileDialogBuilder()
                    .setTitle("Speichere die Differenz")
                    .setDescription("Wähle einen Speicherort")
                    .setActionLabel("Speichern")
                    .build()
                    .showDialog(textGUI);

            if(file != null) {

                file = new File(file.getAbsolutePath() + ".html");

                if(file.exists()) {
                    MessageDialog.showMessageDialog(textGUI, "Fehler", "Die Datei existiert bereits", MessageDialogButton.OK);
                    return;
                }
                try {
                    StringBuilder html = new StringBuilder();
                    html.append("<html><head><style>table {border-collapse: collapse;} td {border: 1px solid black; padding: 5px;} .yellow {background-color: yellow;} .green {background-color: lightgreen;} .red {background-color: lightcoral;}</style></head><body><table>");
                    html.append("<tr><td>").append(file1.getName()).append("</td><td>").append(file2.getName()).append("</td></tr>");
                    html.append("<tr><td>").append(file1.getAbsolutePath()).append("</td><td>").append(file2.getAbsolutePath()).append("</td></tr>");
                    for (int i = 0; i < result.left().size(); i++) {
                        String leftLine = escapeHtml(result.left().get(i));
                        String rightLine = escapeHtml(result.right().get(i));
                        int lineIndex = i + 1;
                        if (leftLine.contains("!") && leftLine.indexOf("!") < String.valueOf(lineIndex).length() + 4) {
                            html.append("<tr><td class=\"yellow\">").append(leftLine).append("</td><td class=\"yellow\">").append(rightLine).append("</td></tr>");
                        } else if (leftLine.contains("+") && leftLine.indexOf("+") < String.valueOf(lineIndex).length() + 4) {
                            html.append("<tr><td class=\"green\">").append(leftLine).append("</td><td class=\"green\">").append(rightLine).append("</td></tr>");
                        } else if (leftLine.contains("-") && leftLine.indexOf("-") < String.valueOf(lineIndex).length() + 4) {
                            html.append("<tr><td class=\"red\">").append(leftLine).append("</td><td class=\"red\">").append(rightLine).append("</td></tr>");
                        } else {
                            html.append("<tr><td>").append(leftLine).append("</td><td>").append(rightLine).append("</td></tr>");
                        }
                    }
                    html.append("</table></body></html>");


                    Files.write(file.toPath(), html.toString().getBytes());
                    MessageDialog.showMessageDialog(textGUI, "Erfolg", "Die Datei wurde erfolgreich gespeichert", MessageDialogButton.OK);
                } catch (IOException e) {
                    MessageDialog.showMessageDialog(textGUI, "Fehler", "Die Datei konnte nicht gespeichert werden", MessageDialogButton.OK);
                }
            }
        }
    }

    /**
     * Escape HTML characters: < and > so they are not interpreted as HTML tags
     * @param str String to escape
     * @return Escaped string
     */
    private String escapeHtml(String str) {
        return str.replace("<", "&lt;").replace(">", "&gt;");
    }
}
