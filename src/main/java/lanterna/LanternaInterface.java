package lanterna;

import algorithms.FileUtils;
import algorithms.FileUtils.SpecificLineChange;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.graphics.Theme;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.*;
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
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import swing.SwingInterface;
import swing.SwingTicTacToeMiniGaming;
import utils.Side;
import utils.SortType;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * Lanterna interface for comparing two directories
 *
 * @see BasicWindow
 * @see Button
 * @see EmptySpace
 * @see Label
 * @see MultiWindowTextGUI
 * @see Panel
 * @see TextBox
 * @see Window
 */
public class LanternaInterface {
    private final InterfaceState interfaceState = InterfaceState.getInterfaceState();
    private BasicWindow window;
    private WindowBasedTextGUI textGUI;
    private TerminalScreen screen;
    List<SpecificLineChange> lineChanges = new ArrayList<>();

    /**
     * Start the Lanterna interface
     *
     * @see Screen
     * @see DefaultTerminalFactory
     * @see MultiWindowTextGUI
     */
    public void start() {
        try {
            DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
            terminalFactory.setInitialTerminalSize(new TerminalSize(150, 40));
            terminalFactory.setPreferTerminalEmulator(true);
            screen = terminalFactory.createScreen();
            screen.startScreen();
            textGUI = new MultiWindowTextGUI(screen);

            //Load Prefs
            Preferences prefs = Preferences.userNodeForPackage(LanternaInterface.class);
            String theme = prefs.get("theme", "default");
            Theme lanternaTheme = LanternaThemes.getRegisteredTheme(theme);
            textGUI.setTheme(lanternaTheme);

            window = new BasicWindow();
            window.setHints(Set.of(Window.Hint.FIT_TERMINAL_WINDOW, Window.Hint.CENTERED));

            getInput(List.of("Erstes Verzeichnis", "Zweites Verzeichnis"), this::compareDirectories);

            TerminalSize screenSize = textGUI.getScreen().getTerminalSize();
            int x = (screenSize.getColumns() / 2);
            int y = (screenSize.getRows()) / 2;
            window.setPosition(new TerminalPosition(x, y));

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
        if (interfaceState.getCurrentListener() != null) resetWindow(interfaceState.getCurrentListener());
        interfaceState.setState(LanternaState.DIRECTORYSELECT);
        Panel outterPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        List<Panel> panels = new ArrayList<>();
        List<String> output = new ArrayList<>();

        boolean isFirst = true;

        for (String label : labels) {
            Panel panel = new Panel(new LinearLayout(Direction.VERTICAL));
            if (isFirst) {
                addMenu(panel);
                panel.addComponent(new EmptySpace(new TerminalSize(0, 2)));
            }
            if (!isFirst) panel.addComponent(new EmptySpace(new TerminalSize(0, 3)));
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

                if (optBox.isPresent()) {
                    TextBox textBox = (TextBox) optBox.get();
                    output.add(textBox.getText());
                }
            }
            interfaceState.setCurrentDirectorys(output);
            consumer.accept(output);
        });

        for (Panel panel : panels) {
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
                    if (optBox.isPresent()) {
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

        WindowListenerAdapter listener = new WindowListenerAdapter() {
            @Override
            public void onInput(Window window, KeyStroke keyStroke, AtomicBoolean atomicBoolean) {
                if (keyStroke.getKeyType() == KeyType.F2) {
                    handleForwards(this);
                }
            }
        };

        interfaceState.setCurrentListener(listener);
        window.addWindowListener(interfaceState.getCurrentListener());
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
        interfaceState.setLeftDir(FileUtils.getFiles(output[0]));
        interfaceState.setRightDir(FileUtils.getFiles(output[1]));

        if (interfaceState.getLeftDir() == null) {
            MessageDialog.showMessageDialog(textGUI, "Fehler", "Linkes Verzeichnis existiert nicht oder ist leer", MessageDialogButton.OK);
            if (interfaceState.getRightDir() == null) {
                MessageDialog.showMessageDialog(textGUI, "Fehler", "Rechtes Verzeichnis existiert nicht oder ist leer", MessageDialogButton.OK);
                return;
            }
            return;
        }

        if (interfaceState.getRightDir() == null) {
            MessageDialog.showMessageDialog(textGUI, "Fehler", "Rechtes Verzeichnis existiert nicht oder ist leer", MessageDialogButton.OK);

            if (interfaceState.getLeftDir() == null) {
                MessageDialog.showMessageDialog(textGUI, "Fehler", "Linkes Verzeichnis existiert nicht oder ist leer", MessageDialogButton.OK);
                return;
            }
            return;
        }

        showFilesAsDirectory(interfaceState.getLeftDir(), interfaceState.getRightDir());
    }

    /**
     * Show the files in a side by side view
     * If a file is in both directories, it is marked as such
     * If a file is only in one directory, it is marked as such
     * If the user presses the escape key, return to the input screen
     *
     * @param leftFiles  List of files in the first directory
     * @param rightFiles List of files in the second directory
     * @see File
     * @see File
     */
    private void showFilesAsDirectory(List<File> leftFiles, List<File> rightFiles) {
        if (interfaceState.getCurrentListener() != null) resetWindow(interfaceState.getCurrentListener());
        interfaceState.setLeftDir(leftFiles);
        interfaceState.setRightDir(rightFiles);
        interfaceState.setState(LanternaState.FILESELECT);

        if ((leftFiles == null || rightFiles == null) || (leftFiles.isEmpty() && rightFiles.isEmpty())) {
            getInput(List.of("Erstes Verzeichnis:", "Zweites Verzeichnis:"), LanternaInterface.this::compareDirectories);
            return;
        }

        Panel menuPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        Panel outterPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        Panel leftPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        Panel rightPanel = new Panel(new LinearLayout(Direction.VERTICAL));

        leftPanel.addComponent(new Label("Erstes Verzeichnis:").addStyle(SGR.BOLD));
        rightPanel.addComponent(new Label("Zweites Verzeichnis:").addStyle(SGR.BOLD));

        ActionListBox leftListBox = new ActionListBox();
        ActionListBox rightListBox = new ActionListBox();

        ComboBox<String> leftComboBox = new ComboBox<>();
        leftComboBox.addItem("Unsortiert");
        leftComboBox.addItem("Alphabetisch");
        leftComboBox.addItem("Größe");
        leftComboBox.addItem("Datum");

        ComboBox<String> rightComboBox = new ComboBox<>();
        rightComboBox.addItem("Unsortiert");
        rightComboBox.addItem("Alphabetisch");
        rightComboBox.addItem("Größe");
        rightComboBox.addItem("Datum");

        CheckBox leftReverseBox = new CheckBox("Umgekehrte Reihenfolge");
        CheckBox rightReverseBox = new CheckBox("Umgekehrte Reihenfolge");

        TextBox leftSearchBox = new TextBox(new TerminalSize(30, 1));
        TextBox rightSearchBox = new TextBox(new TerminalSize(30, 1));

        leftReverseBox.setChecked(interfaceState.isSortLeftReversed());
        rightReverseBox.setChecked(interfaceState.isSortRightReversed());

        manageSortingBox(leftComboBox, leftListBox, leftFiles, rightFiles, Side.LEFT, leftReverseBox, leftSearchBox);
        manageSortingBox(rightComboBox, rightListBox, rightFiles, leftFiles, Side.RIGHT, rightReverseBox, rightSearchBox);

        leftComboBox.setSelectedIndex(interfaceState.getSortTypeLeft());
        rightComboBox.setSelectedIndex(interfaceState.getSortTypeRight());

        leftPanel.addComponent(leftComboBox);
        leftPanel.addComponent(leftReverseBox);
        leftPanel.addComponent(leftSearchBox);
        rightPanel.addComponent(rightComboBox);
        rightPanel.addComponent(rightReverseBox);
        rightPanel.addComponent(rightSearchBox);

        leftPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));
        rightPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        leftPanel.addComponent(leftListBox);
        rightPanel.addComponent(rightListBox);
        outterPanel.addComponent(leftPanel);

        outterPanel.addComponent(new EmptySpace(new TerminalSize(2, 0)));

        outterPanel.addComponent(rightPanel);
        addMenu(menuPanel);

        menuPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        menuPanel.addComponent(outterPanel);
        window.setComponent(menuPanel);

        leftSearchBox.setTextChangeListener((s, s1) -> leftComboBox.setSelectedIndex(leftComboBox.getSelectedIndex()));
        rightSearchBox.setTextChangeListener((s, s1) -> rightComboBox.setSelectedIndex(rightComboBox.getSelectedIndex()));

        WindowListenerAdapter listener = new WindowListenerAdapter() {
            @Override
            public void onInput(Window window, KeyStroke keyStroke, AtomicBoolean atomicBoolean) {
                if (keyStroke.getKeyType() == KeyType.Escape || keyStroke.getKeyType() == KeyType.F1) {
                    handleBackwards(this);
                }
                if (keyStroke.getKeyType() == KeyType.F2) {
                    handleForwards();
                }
            }
        };

        //Trigger comboBox event
        leftReverseBox.addListener((b) -> {
            leftComboBox.setSelectedIndex(leftComboBox.getSelectedIndex());
            interfaceState.setSortLeftReversed(leftReverseBox.isChecked());
        });
        rightReverseBox.addListener((b) -> {
            rightComboBox.setSelectedIndex(rightComboBox.getSelectedIndex());
            interfaceState.setSortRightReversed(rightReverseBox.isChecked());
        });

        interfaceState.setCurrentListener(listener);
        window.addWindowListener(listener);
    }

    private void manageSortingBox(ComboBox<String> comboBox, ActionListBox listBox, List<File> firstFiles, List<File> secondFiles, Side side, CheckBox reverseBox, TextBox searchBox) {
        comboBox.addListener((i, i2, i3) -> {
            listBox.clearItems();
            listBox.addItem("Lade Daten...", () -> {
            });
            comboBox.setEnabled(false);
            reverseBox.setEnabled(false);
            //noinspection rawtypes
            SwingWorker worker = new SwingWorker() {
                @Override
                protected Object doInBackground() {
                    Comparator<File> comparator = switch (i) {
                        case 1 -> Comparator.comparing(File::getName);
                        case 2 -> Comparator.comparing(File::length);
                        case 3 -> Comparator.comparing(File::lastModified);
                        default -> Comparator.comparing(File::exists);
                    };

                    SortType sortType = switch (i) {
                        case 1 -> SortType.ALPHABETICAL;
                        case 2 -> SortType.SIZE;
                        case 3 -> SortType.DATE;
                        default -> SortType.UNSORTED;
                    };

                    if (side == Side.LEFT) interfaceState.setSortTypeLeft(sortType);
                    else interfaceState.setSortTypeRight(sortType);

                    if (reverseBox.isChecked()) {
                        comparator = comparator.reversed();
                    }

                    manageSortedList(comparator, listBox, firstFiles, secondFiles, side, searchBox);
                    return null;
                }

                @Override
                protected void done() {
                    super.done();
                    tryScreenUpdate();
                    comboBox.setEnabled(true);
                    reverseBox.setEnabled(true);
                }
            };

            worker.execute();
        });
    }

    private void displayList(List<File> firstFiles, List<File> secondFiles, Side side, ActionListBox listBox) {
        listBox.clearItems();
        for (File file : firstFiles) {
            String fileName = file.getName();
            boolean inBoth = secondFiles.stream().anyMatch(f -> f.getName().equals(file.getName()));
            File otherFile = inBoth ? secondFiles.stream().filter(f -> f.getName().equals(file.getName())).findFirst().orElseThrow() : null;
            if (inBoth) {
                fileName = getFormattedFileName(file, fileName, otherFile);
            } else {
                fileName += side == Side.LEFT ? " (in L)" : " (in R)";
            }
            if(side == Side.RIGHT && otherFile == null) {
                listBox.addItem(fileName, () -> showFileContents(null, file, side));
            } else if(side == Side.LEFT && otherFile == null) {
                listBox.addItem(fileName, () -> showFileContents(file, null, side));
            } else {
                listBox.addItem(fileName, () -> showFileContents(file, otherFile, side));
            }
        }
        tryScreenUpdate();
    }

    private void manageSortedList(Comparator<File> comparing, ActionListBox listBox, List<File> firstFiles, List<File> secondFiles, Side side, TextBox searchBox) {
        List<File> sortedFiles = new ArrayList<>(firstFiles);
        sortedFiles = sortedFiles.stream().filter(f -> f.getName().toLowerCase().contains(searchBox.getText().toLowerCase())).collect(Collectors.toList());
        sortedFiles.sort(comparing);
        displayList(sortedFiles, secondFiles, side, listBox);
    }

    private String getFormattedFileName(File leftFile, String fileName, File rightFile) {
        boolean identical = false;
        try {
            identical = Files.mismatch(leftFile.toPath(), rightFile.toPath()) == -1;
        } catch (IOException ignored) {
        }
        if (!identical) {
            fileName += " (in L&R verschieden)";
        } else {
            fileName += " (in L&R identisch)";
        }
        return fileName;
    }

    /**
     * Show the contents of 2 files
     * The contents of the files are shown side by side with differences highlighted
     * If the user presses the escape key, return to the file list
     *
     * @param leftFile  File to show on the left
     * @param rightFile File to show on the right
     * @see File
     */
    private void showFileContents(File leftFile, File rightFile, Side selectedSide) {
        if (interfaceState.getCurrentListener() != null) resetWindow(interfaceState.getCurrentListener());
        interfaceState.setState(LanternaState.FILECOMPARE);
        interfaceState.setCurrentLeftFile(leftFile);
        interfaceState.setCurrentRightFile(rightFile);
        interfaceState.setCurrentSide(selectedSide);

        Panel menuPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        Panel outterPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));

        Panel leftPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        Panel rightPanel = new Panel(new LinearLayout(Direction.VERTICAL));

        leftPanel.addComponent(new Label("Datei im linken Verzeichnis " + (leftFile == null ? "nicht vorhanden" : leftFile.getName()) + " :").addStyle(SGR.BOLD));
        rightPanel.addComponent(new Label("Datei im rechten Verzeichnis " + (rightFile == null ? "nicht vorhanden" : rightFile.getName())   + " :").addStyle(SGR.BOLD));

        interfaceState.setLeftLines(new ArrayList<>());
        interfaceState.setRightLines(new ArrayList<>());
        lineChanges = new ArrayList<>();

        ColoredTextBox leftTextBox = new ColoredTextBox(new TerminalSize(100, 100), selectedSide == Side.LEFT ? Side.LEFT : Side.RIGHT);
        ColoredTextBox rightTextBox = new ColoredTextBox(new TerminalSize(100, 100), selectedSide == Side.LEFT ? Side.RIGHT : Side.LEFT);

        leftTextBox.setOtherBox(rightTextBox);
        rightTextBox.setOtherBox(leftTextBox);

        leftTextBox.setReadOnly(true);
        rightTextBox.setReadOnly(true);

        leftTextBox.setText("Lade Daten...");
        rightTextBox.setText("Lade Daten...");


        //noinspection rawtypes
        SwingWorker worker = new SwingWorker() {
            @Override
            protected Object doInBackground() {
                if (rightFile == null) {
                    interfaceState.setRightLines(List.of("Nur links vorhanden"));
                    interfaceState.setLeftLines(FileUtils.readFile(leftFile));
                    leftTextBox.setRenderer(new TextBox.DefaultTextBoxRenderer());
                    rightTextBox.setRenderer(new TextBox.DefaultTextBoxRenderer());
                } else if(leftFile == null) {
                    interfaceState.setLeftLines(List.of("Nur rechts vorhanden"));
                    interfaceState.setRightLines(FileUtils.readFile(rightFile));
                    leftTextBox.setRenderer(new TextBox.DefaultTextBoxRenderer());
                    rightTextBox.setRenderer(new TextBox.DefaultTextBoxRenderer());
                } else if (leftFile.equals(rightFile) && selectedSide == Side.LEFT) {
                    interfaceState.setLeftLines(FileUtils.readFile(leftFile));
                    interfaceState.setRightLines(interfaceState.getLeftLines());
                    leftTextBox.setRenderer(new TextBox.DefaultTextBoxRenderer());
                    rightTextBox.setRenderer(new TextBox.DefaultTextBoxRenderer());
                } else if (leftFile.equals(rightFile) && selectedSide == Side.RIGHT) {
                    interfaceState.setRightLines(FileUtils.readFile(leftFile));
                    interfaceState.setLeftLines(interfaceState.getRightLines());
                    leftTextBox.setRenderer(new TextBox.DefaultTextBoxRenderer());
                    rightTextBox.setRenderer(new TextBox.DefaultTextBoxRenderer());
                } else if (!leftFile.equals(rightFile)) {
                    interfaceState.setCurrentLineResult(FileUtils.compareFiles(leftFile, rightFile));
                    FileUtils.LineResult result = interfaceState.getCurrentLineResult();

                    if (selectedSide == Side.LEFT) {
                        interfaceState.setLeftLines(result.left());
                        interfaceState.setRightLines(result.right());
                    } else {
                        interfaceState.setLeftLines(result.right());
                        interfaceState.setRightLines(result.left());
                    }

                    lineChanges = result.specificLineChanges();
                }

                leftTextBox.setText("");

                for (String line : interfaceState.getLeftLines()) {
                    leftTextBox.addLine(line);
                }

                rightTextBox.setText("");

                for (String line : interfaceState.getRightLines()) {
                    rightTextBox.addLine(line);
                }

                if (!lineChanges.isEmpty()) {
                    leftTextBox.setSpecificLineChanges(lineChanges);
                    rightTextBox.setSpecificLineChanges(lineChanges);
                }
                return null;
            }

            @Override
            protected void done() {
                super.done();
            }
        };

        worker.execute();

        leftPanel.addComponent(leftTextBox);
        rightPanel.addComponent(rightTextBox);

        outterPanel.addComponent(leftPanel);
        outterPanel.addComponent(new EmptySpace(new TerminalSize(2, 0)));
        outterPanel.addComponent(rightPanel);

        CheckBox linkedCheckBox = new CheckBox("Verlinktes Scrolling");

        linkedCheckBox.addListener((b) -> {
            if (linkedCheckBox.isChecked()) {
                leftTextBox.setScrollSlave(rightTextBox);
                rightTextBox.setEnabled(false);
                return;
            }
            leftTextBox.setScrollSlave(null);
            rightTextBox.setEnabled(true);
        });

        addMenu(menuPanel);
        menuPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));
        menuPanel.addComponent(linkedCheckBox, LinearLayout.createLayoutData(LinearLayout.Alignment.Center));
        menuPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));
        menuPanel.addComponent(outterPanel);

        Button saveButton = new Button("Differenz speichern", () -> {
            Window saveWindow = new BasicWindow("Speichern");
            saveWindow.setHints(Set.of(Window.Hint.CENTERED));
            Panel savePanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
            Button saveLeftButton = new Button("Textdatei");
            Button saveRightButton = new Button("HTML-Datei");
            Button cancelButton = new Button("Abbrechen");

            savePanel.addComponent(saveLeftButton);
            savePanel.addComponent(saveRightButton);
            savePanel.addComponent(cancelButton);

            saveLeftButton.addListener((b) -> {
                File saveFile = getSaveFile();

                if (saveFile == null) {
                    MessageDialog.showMessageDialog(textGUI, "Fehler", "Datei konnte nicht gespeichert werden", MessageDialogButton.OK);
                    saveWindow.close();
                    return;
                }

                boolean success;
                if (interfaceState.getCurrentLineResult() == null) {
                    success = FileUtils.saveDiffAsText(interfaceState.getCurrentLeftFile(), interfaceState.getCurrentRightFile(), saveFile, null);
                } else {
                    success = FileUtils.saveDiffAsText(null, null, saveFile, interfaceState.getCurrentLineResult());
                }

                if (!success) {
                    MessageDialog.showMessageDialog(textGUI, "Fehler", "Datei konnte nicht gespeichert werden", MessageDialogButton.OK);
                    saveWindow.close();
                    return;
                }
                MessageDialog.showMessageDialog(textGUI, "Speichern", "Differenz wurde gespeichert", MessageDialogButton.OK);
                saveWindow.close();
            });

            saveRightButton.addListener((b) -> {
                File saveFile = getSaveFile();

                if (saveFile == null) {
                    MessageDialog.showMessageDialog(textGUI, "Fehler", "Datei konnte nicht gespeichert werden", MessageDialogButton.OK);
                    saveWindow.close();
                    return;
                }

                boolean success;
                if (interfaceState.getCurrentLineResult() == null) {
                    success = FileUtils.saveDiffAsHTML(interfaceState.getCurrentLeftFile(), interfaceState.getCurrentRightFile(), saveFile, null);
                } else {
                    success = FileUtils.saveDiffAsHTML(null, null, saveFile, interfaceState.getCurrentLineResult());
                }

                if (!success) {
                    MessageDialog.showMessageDialog(textGUI, "Fehler", "Datei konnte nicht gespeichert werden", MessageDialogButton.OK);
                    saveWindow.close();
                    return;
                }
                MessageDialog.showMessageDialog(textGUI, "Speichern", "Differenz wurde gespeichert", MessageDialogButton.OK);
                saveWindow.close();
            });

            cancelButton.addListener((b) -> saveWindow.close());

            saveWindow.setComponent(savePanel);
            textGUI.addWindow(saveWindow);
        });

        menuPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));
        menuPanel.addComponent(saveButton, LinearLayout.createLayoutData(LinearLayout.Alignment.Center));

        window.setComponent(menuPanel);
        WindowListenerAdapter listener = new WindowListenerAdapter() {
            @Override
            public void onInput(Window window, KeyStroke keyStroke, AtomicBoolean atomicBoolean) {
                if (keyStroke.getKeyType() == KeyType.Escape || keyStroke.getKeyType() == KeyType.F1) {
                    handleBackwards(this);
                }
            }
        };

        interfaceState.setCurrentListener(listener);
        window.addWindowListener(listener);
    }

    private File getSaveFile() {
        return new FileDialogBuilder()
                .setTitle("Speichern")
                .setDescription("Speichern der Differenz")
                .setActionLabel("Speichern")
                .build()
                .showDialog(textGUI);
    }

    /**
     * Reset the window
     *
     * @param listener Listener to remove
     * @see WindowListenerAdapter
     */
    private void resetWindow(WindowListenerAdapter listener) {
        window.setComponent(null);
        window.removeWindowListener(listener);
    }

    /**
     * Update the screen
     *
     * @noinspection unused
     * @see MultiWindowTextGUI
     */
    public void tryScreenUpdate() {
        try {
            window.invalidate();
            textGUI.updateScreen();
        } catch (IOException e) {
            System.out.println("Error updating screen");
        }
    }

    /**
     * Generate a standard file
     *
     * @return empty file
     * @noinspection unused
     * @see File
     */
    private File generateStandardFile() {
        return new File("placeholderNA");
    }

    /**
     * Adds a menu with help and exit options to the panel
     *
     * @param panel Panel to add the menu to
     * @see MenuBar
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
                        - Um 2 Dateien manuell zu vergleichen, wählen Sie im Menü "Datei" -> "Manueller Vergleich von 2 Dateien".
                        - Um die Differenz von 2 Dateien zu speichern, wählen Sie im Menü "Datei" -> "Differenz von 2 Dateien speichern".
                        - Um eine Datei zu bearbeiten, wählen Sie im Menü "Datei" -> "Datei editieren".
                        - Um ein Blackjack-Minispiel zu starten, wählen Sie im Menü "Zusätzliches" -> "Blackjack".
                        - Um ein TicTacToe-Minispiel zu starten, wählen Sie im Menü "Zusätzliches" -> "TicTacToe".
                        - Um ein TicTacToe-Minispiel in einer GUI-Umgebung zu starten (nicht im Headless-Modus), wählen Sie im Menü "Zusätzliches" -> "GUI-TicTacToe".
                        - Um das Programm zu beenden, wählen Sie im Menü "Beenden" -> "Beende Programm".
                        - Mit der F1-Taste können Sie immer zum vorherigen Menü zurückkehren.
                        - Mit der F2-Taste können Sie immer zum nächsten Menü wechseln, sofern dies möglich ist.
                        """, MessageDialogButton.OK)));

        helpMenu.add(new MenuItem("Über uns", () -> MessageDialog.showMessageDialog(textGUI, "Über uns",
                """
                        Entwickelt im Rahmen der SoftwareProjekt 1 Vorlesung der Hochschule für Technik Stuttgart.
                        Beteiligte: Benedikt Belschner, Colin Traub, Daniel Rodean, Finn Wolf
                        """, MessageDialogButton.OK)));

        helpMenu.add(new MenuItem("In GUI wechseln", () -> {
            if (GraphicsEnvironment.isHeadless()) {
                MessageDialog.showMessageDialog(textGUI, "Fehler", "Das Programm kann nicht in einer GUI-Umgebung ausgeführt werden.\nDas System läuft im Headless Modus", MessageDialogButton.OK);
                return;
            }
            window.close();
            try {
                screen.close();
                SwingInterface swingInterface = new SwingInterface();
                swingInterface.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));

        Menu fileMenu = new Menu("Datei");
        menuBar.add(fileMenu);
        fileMenu.add(new MenuItem("Manueller Vergleich von 2 Dateien", this::diffFilesManually));

        fileMenu.add(new MenuItem("Differenz von 2 Dateien speichern - Rohtext", this::compAndSaveText));

        fileMenu.add(new MenuItem("Differenz von 2 Dateien speichern - HTML", this::compAndSaveHtml));

        fileMenu.add(new MenuItem("Datei editieren", this::editFile));

        fileMenu.add(new MenuItem("Einstellungen", this::showSettings));

        Menu additionalMenu = new Menu("Zusätzliches");
        menuBar.add(additionalMenu);

        additionalMenu.add(new MenuItem("Blackjack", () -> new BlackjackMinigaming(textGUI)));

        additionalMenu.add(new MenuItem("TicTacToe", () -> new LanternaTicTacToeMiniGaming(textGUI)));

        additionalMenu.add(new MenuItem("GUI-TicTacToe", () -> {
            if (GraphicsEnvironment.isHeadless()) {
                MessageDialog.showMessageDialog(textGUI, "Fehler", "Das Spiel kann nicht in einer GUI-Umgebung ausgeführt werden.\nDas System läuft im Headless Modus", MessageDialogButton.OK);
                return;
            }
            SwingTicTacToeMiniGaming swingTicTacToeMinigaming = new SwingTicTacToeMiniGaming();
            JFrame frame = new JFrame("GUI - TicTacToe");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(swingTicTacToeMinigaming);
            frame.setResizable(false);
            frame.pack();
            frame.setVisible(true);
        }));

        Menu exitMenu = new Menu("Beenden");
        menuBar.add(exitMenu);
        exitMenu.add(new MenuItem("Beende Programm", () -> System.exit(3)));
        panel.addComponent(menuBar);
    }

    /**
     * Show the settings window
     *
     * @see BasicWindow
     * @see Panel
     */
    private void showSettings() {
        Window settingsWindow = new BasicWindow("Einstellungen");
        settingsWindow.setHints(Set.of(Window.Hint.CENTERED));
        Panel settingsPanel = new Panel(new LinearLayout(Direction.VERTICAL));

        settingsPanel.addComponent(new Label("Einstellungen:").addStyle(SGR.BOLD));

        settingsPanel.addComponent(new Button("Farbschema ändern", () -> {
            Window colorWindow = new BasicWindow("Farbschema ändern");
            colorWindow.setHints(Set.of(Window.Hint.CENTERED));
            Panel colorPanel = new Panel(new LinearLayout(Direction.VERTICAL));

            Theme oldTheme = textGUI.getTheme();

            ComboBox<String> colorComboBox = new ComboBox<>();
            for (String theme : LanternaThemes.getRegisteredThemes()) {
                colorComboBox.addItem(theme);
            }

            colorPanel.addComponent(new Label("Wähle ein Farbschema:"));
            colorPanel.addComponent(colorComboBox);

            colorComboBox.addListener((i, i2, b) -> {
                String themeString = LanternaThemes.getRegisteredThemes().stream().filter(s -> s.equals(colorComboBox.getText())).findFirst().orElseThrow();
                Theme theme = LanternaThemes.getRegisteredTheme(themeString);
                textGUI.setTheme(theme);
                tryScreenUpdate();
            });

            colorPanel.addComponent(new Button("Abbrechen", () -> {
                textGUI.removeWindow(colorWindow);
                textGUI.setTheme(oldTheme);
            }));

            colorPanel.addComponent(new Button("Speichern", () -> {
                Preferences prefs = Preferences.userNodeForPackage(LanternaInterface.class);
                prefs.put("theme", colorComboBox.getText());
                textGUI.removeWindow(colorWindow);
            }));

            colorWindow.setComponent(colorPanel);
            textGUI.addWindow(colorWindow);
        }));

        settingsPanel.addComponent(new Button("Zurück", () -> textGUI.removeWindow(settingsWindow)));

        textGUI.addWindow(settingsWindow);
        settingsWindow.setComponent(settingsPanel);

        settingsWindow.addWindowListener(new WindowListenerAdapter() {
            @Override
            public void onInput(Window window, KeyStroke keyStroke, AtomicBoolean atomicBoolean) {
                if (keyStroke.getKeyType() == KeyType.Escape) {
                    textGUI.removeWindow(settingsWindow);
                }
            }
        });
    }

    /**
     * Let the user manually compare 2 files
     *
     * @see FileDialogBuilder
     * @see MessageDialog
     * @see MessageDialogButton
     * @see File
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
            interfaceState.setLeftDir(new ArrayList<>());
            interfaceState.setRightDir(new ArrayList<>());
            showFileContents(file1, file2, Side.LEFT);
        }
    }

    /**
     * Let the User compare 2 files and save the differences as a text file
     *
     * @see FileDialogBuilder
     * @see MessageDialog
     * @see MessageDialogButton
     * @see File
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
            File file = new FileDialogBuilder()
                    .setTitle("Speichere die Differenz")
                    .setDescription("Wähle einen Speicherort")
                    .setActionLabel("Speichern")
                    .build()
                    .showDialog(textGUI);

            boolean saveSuccessful = FileUtils.saveDiffAsText(file1, file2, file, null);
            if (!saveSuccessful) {
                MessageDialog.showMessageDialog(textGUI, "Fehler", "Die Datei konnte nicht gespeichert werden", MessageDialogButton.OK);
                return;
            }

            MessageDialog.showMessageDialog(textGUI, "Erfolg", "Die Datei wurde erfolgreich gespeichert", MessageDialogButton.OK);
        }

        MessageDialog.showMessageDialog(textGUI, "Fehler", "Die Datei konnte nicht gespeichert werden", MessageDialogButton.OK);
    }

    /**
     * Let the user edit a file
     *
     * @see FileDialogBuilder
     * @see MessageDialog
     * @see MessageDialogButton
     * @see File
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

                window.addWindowListener(new WindowListenerAdapter() {
                    @Override
                    public void onInput(Window window, KeyStroke keyStroke, AtomicBoolean atomicBoolean) {
                        if (keyStroke.getKeyType() == KeyType.Escape) {
                            resetWindow(this);
                            getInput(List.of("Erstes Verzeichnis:", "Zweites Verzeichnis:"), LanternaInterface.this::compareDirectories);
                        }
                    }
                });
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
            File file = new FileDialogBuilder()
                    .setTitle("Speichere die Differenz")
                    .setDescription("Wähle einen Speicherort")
                    .setActionLabel("Speichern")
                    .build()
                    .showDialog(textGUI);

            boolean saveSuccessful = FileUtils.saveDiffAsHTML(file1, file2, file, null);
            if (!saveSuccessful) {
                MessageDialog.showMessageDialog(textGUI, "Fehler", "Die Datei konnte nicht gespeichert werden", MessageDialogButton.OK);
                return;
            }

            MessageDialog.showMessageDialog(textGUI, "Erfolg", "Die Datei wurde erfolgreich gespeichert", MessageDialogButton.OK);
        }

        MessageDialog.showMessageDialog(textGUI, "Fehler", "Die Datei konnte nicht gespeichert werden", MessageDialogButton.OK);
    }

    /**
     * Handle the escape key and F1 key backward movement
     */
    private void handleBackwards() {
        if (interfaceState.getState() == LanternaState.FILECOMPARE) {
            if (interfaceState.getLeftDir() != null && interfaceState.getRightDir() != null) {
                showFilesAsDirectory(interfaceState.getLeftDir(), interfaceState.getRightDir());
            }
            return;
        }
        if (interfaceState.getState() == LanternaState.FILESELECT) {
            getInput(List.of("Erstes Verzeichnis:", "Zweites Verzeichnis:"), LanternaInterface.this::compareDirectories);
        }
    }

    /**
     * Handle the F2 key forward movement
     */
    private void handleForwards() {
        if (interfaceState.getState() == LanternaState.DIRECTORYSELECT) {
            if (interfaceState.getCurrentDirectorys() != null) {
                compareDirectories(interfaceState.getCurrentDirectorys());
                return;
            }
            getInput(List.of("Erstes Verzeichnis:", "Zweites Verzeichnis:"), LanternaInterface.this::compareDirectories);
            return;
        }

        if (interfaceState.getState() == LanternaState.FILESELECT) {
            if (interfaceState.getCurrentLeftFile() != null && interfaceState.getCurrentRightFile() != null && interfaceState.getCurrentSide() != null) {
                showFileContents(interfaceState.getCurrentLeftFile(), interfaceState.getCurrentRightFile(), interfaceState.getCurrentSide());
            }
        }
    }

    private void handleBackwards(WindowListenerAdapter listener) {
        resetWindow(listener);
        handleBackwards();
    }

    private void handleForwards(WindowListenerAdapter listener) {
        resetWindow(listener);
        handleForwards();
    }
}
