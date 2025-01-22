package swing;

import algorithms.FileUtils;
import lanterna.LanternaInterface;
import utils.Side;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class SwingInterface {

    private static final Dimension defaultTextFieldDimension = new Dimension(400, 25);
    private final JFrame frame = new JFrame("Swing Oberfläche");
    private final JButton backButton = new JButton("⬅");
    private final JButton forwardButton = new JButton("➡");
    private final Menu menu = new Menu(frame);
    private final Level1UI level1UI = new Level1UI();
    private Level2UI level2UI = null;
    private Level3UI level3UI = null;
    private SwingTicTacToeMiniGaming ticTacToeGameUI = null;
    private final GridBagConstraints globalGbc = new GridBagConstraints();

    public void start() {
        SwingUtilities.invokeLater(() -> {
            initializeEscFocus();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int width = (int) (screenSize.width * 0.8);
            int height = (int) (screenSize.height * 0.8);
            frame.setSize(width, height);
            frame.setLayout(new GridBagLayout());
            frame.setJMenuBar(menu);
            globalGbc.anchor = GridBagConstraints.CENTER;
            globalGbc.fill = GridBagConstraints.BOTH;
            globalGbc.weighty = 1;
            globalGbc.weightx = 1;

            frame.add(level1UI, globalGbc);

            frame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    resizeAll();
                }
            });
            frame.addWindowStateListener(e -> resizeAll());

            Preferences preferences = Preferences.userNodeForPackage(SwingInterface.class);
            String themeString = preferences.get("theme", SwingTheme.DARK.toString());
            SwingTheme startTheme = null;
            for (SwingTheme theme : SwingTheme.values()) if (theme.toString().equals(themeString)) startTheme = theme;
            switchThemeTo(startTheme);

            SwingUtilities.updateComponentTreeUI(frame);
            resizeAll();
            frame.setVisible(true);
        });
    }

    private void resizeAll() {
        Dimension frameDimension = new Dimension();
        frameDimension.setSize(frame.getWidth() * 0.95, frame.getHeight() * 0.95);
        level1UI.setSize(frame.getSize());
        if (level2UI != null) {
            level2UI.setSize(frame.getSize());
        }
        if (level3UI != null) {
            level3UI.setSize(frame.getSize());
        }
    }

    private final class Menu extends JMenuBar {
        JButton back = new JButton("⬅");
        JMenuItem ticTacToe = new JMenuItem("TicTacToe Minispiel");

        public Menu(JFrame frame) {
            back.setVisible(false);
            add(back);

            ticTacToe.addActionListener(e -> {
                ticTacToe.setEnabled(false);
                JPanel parent = level1UI.isVisible() ? level1UI : level2UI != null && level2UI.isVisible() ? level2UI : level3UI;
                ticTacToeGameUI = new SwingTicTacToeMiniGaming();
                ticTacToeGameUI.setParent(parent);
                ticTacToeGameUI.setOldSize(frame.getSize());
                GridBagConstraints ticTacToeGbc = new GridBagConstraints();
                ticTacToeGbc.weightx = 1;
                ticTacToeGbc.weighty = 1;
                ticTacToeGbc.fill = GridBagConstraints.BOTH;
                ticTacToeGbc.anchor = GridBagConstraints.CENTER;
                frame.add(ticTacToeGameUI, ticTacToeGbc);
                frame.setResizable(false);
                frame.setSize(615, 640);
                backButton.setVisible(false);
                forwardButton.setVisible(false);
                changeActivePanelFromTo(parent, ticTacToeGameUI);
                back.setVisible(true);
                back.addActionListener(b -> {
                    ticTacToeGameUI.getCustomParent().setVisible(true);
                    SwingUtilities.invokeLater(() -> frame.setSize(ticTacToeGameUI.getOldSize()));
                    frame.getContentPane().remove(ticTacToeGameUI);
                    back.setVisible(false);
                    frame.setResizable(true);
                    backButton.setVisible(true);
                    forwardButton.setVisible(true);
                    ticTacToe.setEnabled(true);

                });
            });

            backButton.addActionListener(e -> {
                if (level2UI != null && level2UI.isVisible()) {
                    changeActivePanelFromTo(level2UI, level1UI);
                } else if (level3UI != null && level3UI.isVisible()) {
                    changeActivePanelFromTo(level3UI, level2UI);
                }
                resizeAll();
            });
            add(backButton);

            forwardButton.addActionListener(e -> {
                if (level2UI != null && level2UI.isVisible() && level3UI != null) {
                    changeActivePanelFromTo(level2UI, level3UI);
                } else if (level1UI.isVisible() && level2UI != null) {
                    changeActivePanelFromTo(level1UI, level2UI);
                }
                resizeAll();
            });
            add(forwardButton);

            JMenu helpMenu = getHelpMenu(frame);
            add(helpMenu);

            JMenu additionalStuff = new JMenu("Zusätzliches");
            additionalStuff.add(ticTacToe);
            add(additionalStuff);

            JMenu settingsMenu = new JMenu("Einstellungen");
            JMenu themeItem = new JMenu("Theme");
            for (SwingTheme theme : SwingTheme.values()) {
                JMenuItem mi = new JMenuItem(theme.toString());
                mi.addActionListener(e -> switchThemeTo(theme));
                themeItem.add(mi);
            }
            settingsMenu.add(themeItem);
            add(settingsMenu);

            JMenu exitMenu = new JMenu("Beenden");
            JMenuItem exitItem = new JMenuItem("Programm beenden");
            exitItem.addActionListener(e -> System.exit(0));
            exitMenu.add(exitItem);
            add(exitMenu);
        }

        private static JMenu getHelpMenu(JFrame frame) {
            JMenu helpMenu = new JMenu("Hilfe");
            MenuItem guideItem = new MenuItem("Guide", """
                    - Wählen Sie die Verzeichnisse aus, die Sie vergleichen möchten.
                    - Die Anwendung zeigt die Unterschiede zwischen 2 Verzeichnissen an.
                      Zusätzlich können Sie die Unterschiede zwischen 2 Dateien anzeigen lassen.
                      Hierfür wählen Sie die Dateien aus, die Sie vergleichen möchten,\s
                      indem Sie den Pfad in der Textbox eingeben oder das Verzeichnis\s
                      mit dem Button \uD83D\uDCC1 auswählen. Danach auf "Bestätigen" klicken.
                    - Jederzeit können Sie mit der Escape-Taste zum vorherigen Menü zurückkehren,
                      alternativ können Sie auch die Pfeilbuttons in der oberen linken Ecke nutzen\s
                      um zwischen den Menüs zu wechseln, sofern dies möglich ist.
                    - Um die Differenz von 2 Dateien zu speichern, wählen Sie den Button\s
                      "Differenz Exportieren", welcher sich unterhalb des Textes mit den\s
                       Differenzen befindet.
                    - Um das Theme der Anwendung zu ändern, wählen Sie im Menü unter "Einstellungen"
                      -> "Theme" das gewünschte Theme aus.
                    - Um diese Anleitung erneut anzuzeigen, wählen Sie im Menü "Hilfe" -> "Guide".
                    - Um Informationen über die Entwickler zu erhalten, wählen Sie im Menü "Hilfe"\s
                      -> "Über uns".
                    - Um ein TicTacToe-Minispiel zu starten, wählen Sie im Menü "Zusätzliches" -> "TicTacToe"
                    - Um das Programm zu beenden, wählen Sie im Menü "Beenden" -> "Beende Programm.""", frame, "Hilfe");
            MenuItem aboutItem = new MenuItem("Über uns", "Entwickelt im Rahmen der SoftwareProjekt 1 Vorlesung der Hochschule für Technik Stuttgart.\n" + "Contributors: Benedikt Belschner, Colin Traub, Daniel Rodean, Finn Wolf", frame, "Über uns");

            MenuItem legendenItem = new MenuItem("Legende", """
                    - Das grüne Plus erscheint bei Zeilen, welche hinzugefügt wurden.
                    - Das rote Minus erscheint bei Zeilen, welche entfernt wurden.
                    - Das orange Ausrufezeichen erscheint bei Zeilen, welche Veränderungen
                      enthalten.
                      -> Die orange markierten Teile innerhalb der Zeilen sind die genauen Änderungen.
                      -> Wenn zu viele Unterschiede innerhalb der Zeilen auftreten, wird die gesamte Zeile
                         orange markiert.
                    
                    """, frame, "Legende");

            JMenuItem switchItem = new JMenuItem("In CUI wechseln");
            switchItem.addActionListener(e -> {
                frame.dispose();
                new Thread(() -> {
                    LanternaInterface lanternaInterface = new LanternaInterface();
                    lanternaInterface.start();
                    lanternaInterface.tryScreenUpdate();
                }).start();
            });

            helpMenu.add(guideItem);
            helpMenu.add(legendenItem);
            helpMenu.add(aboutItem);
            helpMenu.add(switchItem);
            return helpMenu;
        }


        private static class MenuItem extends JMenuItem {
            public MenuItem(String name, String content, JFrame frame, String popUpTitle) {
                super(name);
                addActionListener(e -> JOptionPane.showMessageDialog(frame, content, popUpTitle, JOptionPane.INFORMATION_MESSAGE));
            }
        }

        public void deactivate() {
            backButton.setEnabled(false);
            forwardButton.setEnabled(false);
            ticTacToe.setEnabled(false);
        }

        public void activate() {
            backButton.setEnabled(true);
            forwardButton.setEnabled(true);
            ticTacToe.setEnabled(true);
        }

    }

    private final class Level1UI extends JPanel {
        private final GridBagConstraints gbc = new GridBagConstraints();
        private final DirectorySelectionPanel leftPanel = new DirectorySelectionPanel("Erstes Verzeichnis:");
        private final DirectorySelectionPanel rightPanel = new DirectorySelectionPanel("Zweites Verzeichnis:");
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);

        Level1UI() {
            super(new GridBagLayout());
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(1, 1, 1, 1);
            splitPane.setResizeWeight(0.5);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weighty = 1;
            gbc.weightx = 1;
            add(splitPane, gbc);

            addLowerMenu();
        }

        private void addLowerMenu() {
            JPanel bottomPanel = new JPanel(new FlowLayout());

            JButton okButton = new JButton("Bestätigen");
            okButton.addActionListener(click -> {
                String pathLeft = leftPanel.getDirectory();
                String pathRight = rightPanel.getDirectory();
                if (!new File(pathLeft).exists()) {
                    JOptionPane.showMessageDialog(this, "\"" + pathLeft + "\" (links) ist kein valider Pfad!", "Fehler", JOptionPane.ERROR_MESSAGE);
                } else if (!new File(pathRight).exists()) {
                    JOptionPane.showMessageDialog(this, "\"" + pathRight + "\" (rechts) ist kein valider Pfad!", "Fehler", JOptionPane.ERROR_MESSAGE);
                } else {
                    List<File> leftFiles = FileUtils.getFiles(pathLeft);
                    List<File> rightFiles = FileUtils.getFiles(pathRight);
                    setVisible(false);
                    level2UI = new Level2UI(leftFiles, rightFiles);
                    frame.add(level2UI, globalGbc);
                }
            });
            bottomPanel.add(okButton);

            gbc.gridy = 1;
            gbc.weighty = 0;
            add(bottomPanel, gbc);
        }

        private static class DirectorySelectionPanel extends JPanel {
            JTextField textField = new JTextField();

            public DirectorySelectionPanel(String label1Text) {
                super();
                setLayout(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.insets = new Insets(1, 1, 1, 1);

                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.weighty = 0;
                add(new JLabel(label1Text), gbc);

                gbc.gridx = 0;
                gbc.gridy = 1;
                gbc.weighty = 0;
                add(new JLabel("Bitte geben Sie ein Verzeichnis an:"), gbc);

                textField.setEditable(true);
                textField.setPreferredSize(defaultTextFieldDimension);
                textField.setMinimumSize(defaultTextFieldDimension);

                gbc.gridx = 0;
                gbc.gridy = 2;
                gbc.weighty = 0;
                add(textField, gbc);

                JButton directoryButton = getJButton();
                gbc.gridx = 1;
                gbc.gridy = 2;
                gbc.weighty = 0;
                add(directoryButton, gbc);
            }

            private JButton getJButton() {
                JButton directoryButton = new JButton("\uD83D\uDCC1");
                directoryButton.addActionListener(click -> {
                    JFileChooser jfc = new JFileChooser();
                    jfc.setCurrentDirectory(new File("."));
                    jfc.setDialogTitle("Bitte wählen Sie ein Verzeichnis aus");
                    jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int result = jfc.showOpenDialog(this);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        File resultDir = jfc.getSelectedFile();
                        String resultPath = resultDir.getAbsolutePath();
                        textField.setText(resultPath);
                    }
                });
                return directoryButton;
            }

            public String getDirectory() {
                return textField.getText();
            }
        }
    }

    private final class Level2UI extends JPanel {

        private FileUtils.LineResult lr;
        private JList<String> leftList;
        private JList<String> rightList;
        private List<File> currentLeftFiles;
        private List<File> currentRightFiles;
        private final List<File> allLeftFiles;
        private final List<File> allRightFiles;

        public Level2UI(List<File> leftInput, List<File> rightInput) {
            super(new GridBagLayout());
            this.currentLeftFiles = leftInput;
            this.currentRightFiles = rightInput;
            allLeftFiles = leftInput;
            allRightFiles = rightInput;
            setFocusable(false);
            if (currentLeftFiles == null || currentRightFiles == null) {
                JOptionPane.showMessageDialog(frame, (currentLeftFiles == null ? "Linkes" : "Rechtes") + " Verzeichnis ist leer!", "Fehler", JOptionPane.ERROR_MESSAGE);
                changeActivePanelFromTo(this, level1UI);
                return;
            }
            leftList = new JList<>(getFormatFileNames(currentLeftFiles, currentRightFiles, Side.LEFT));
            rightList = new JList<>(getFormatFileNames(currentRightFiles, currentLeftFiles, Side.RIGHT));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(1, 1, 1, 1);

            Level2UISide left = new Level2UISide(Side.LEFT);
            Level2UISide right = new Level2UISide(Side.RIGHT);

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
            splitPane.setResizeWeight(0.5);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1;
            gbc.weighty = 1;

            add(splitPane, gbc);

        }

        // variable names for left side, but the class can also be used for the right side
        private class Level2UISide extends JPanel {
            private Level2UISide(Side side) {
                super(new BorderLayout());

                // scrollPane
                JList<String> thisSideList = (side == Side.LEFT) ? leftList : rightList;
                applyListSelectionListener(thisSideList, side);
                JScrollPane scrollPane = new JScrollPane(thisSideList);
                add(scrollPane, BorderLayout.CENTER);

                // level2Menu
                JPanel level2Menu = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JComboBox<String> sortingSelection = new JComboBox<>(new String[]{"Unsortiert", "Alphabetisch", "Größe (aufsteigend)", "Datum (aufsteigend)"});
                JCheckBox reverseCheckBox = new JCheckBox("Umgekehrte Sortierung");
                JTextField searchTextField = new JTextField();
                searchTextField.setPreferredSize(new Dimension(300, 25));
                JLabel searchLabel = new JLabel("\uD83D\uDD0E");
                level2Menu.add(reverseCheckBox);
                level2Menu.add(sortingSelection);
                level2Menu.add(searchTextField);
                level2Menu.add(searchLabel);
                add(level2Menu, BorderLayout.NORTH);

                // SEARCH-FIELD: trigger searching and sorting (in actionListener of sortingSelection)
                searchTextField.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        sortingSelection.setSelectedItem(sortingSelection.getSelectedItem());
                    }
                });

                // SORTING-SELECTION: searching and sorting
                sortingSelection.addActionListener(e -> {
                    String search = searchTextField.getText();
                    int selectedSorting = sortingSelection.getSelectedIndex();
                    boolean isReversed = reverseCheckBox.isSelected();

                    // noinspection rawtypes
                    SwingWorker worker = new SwingWorker() {
                        @Override
                        protected Object doInBackground() {
                            List<File> thisSideFiles = (side == Side.LEFT) ? allLeftFiles : allRightFiles;
                            List<File> toBeFilteredAndSorted = new ArrayList<>(thisSideFiles);
                            // search
                            if (!search.isEmpty()) {
                                toBeFilteredAndSorted = filterFilesByName(toBeFilteredAndSorted, search);
                            }
                            // sort
                            Comparator<File> comp = switch (selectedSorting) {
                                case 1 -> Comparator.comparing(file -> file.getName().toLowerCase());
                                case 2 -> Comparator.comparing(File::length);
                                case 3 -> Comparator.comparing(File::lastModified);
                                default -> null;
                            };
                            if (comp != null) {
                                if (isReversed) comp = comp.reversed();
                                toBeFilteredAndSorted.sort(comp);
                            }

                            if (side == Side.LEFT) {
                                currentLeftFiles = toBeFilteredAndSorted;
                            } else {
                                currentRightFiles = toBeFilteredAndSorted;
                            }

                            List<File> otherSideFiles = (side == Side.LEFT) ? allRightFiles : allLeftFiles;
                            thisSideList.setListData(getFormatFileNames(toBeFilteredAndSorted, otherSideFiles, side));
                            return null;
                        }

                        protected void done() {
                            frame.invalidate();
                            frame.revalidate();
                            frame.repaint();
                        }
                    };
                    worker.execute();
                });

                reverseCheckBox.addActionListener((e) -> sortingSelection.setSelectedIndex(sortingSelection.getSelectedIndex()));
            }
        }

        // variable names for left side, method can still be used for right side
        private static String[] getFormatFileNames(List<File> leftFiles, List<File> rightFiles, Side sideInformation) {
            List<String> result = new ArrayList<>();
            for (File leftFile : leftFiles) {
                String leftFileName = leftFile.getName();
                Optional<File> rightFile = rightFiles.stream().filter(f -> f.getName().equals(leftFile.getName())).findFirst();
                if (rightFile.isPresent()) {
                    try {
                        boolean identical = Files.mismatch(leftFile.toPath(), rightFile.get().toPath()) == -1;
                        if (identical) result.add(leftFileName + " (in L&R identisch)");
                        else result.add(leftFileName + " (in L&R verschieden)");
                    } catch (IOException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                } else {
                    result.add(leftFileName + String.format(" (in %s)", sideInformation.toString()));
                }
            }
            return result.toArray(new String[0]);
        }

        // variable names for left side, method can still be used for right side
        private void applyListSelectionListener(JList<String> thisList, Side side) {
            thisList.addListSelectionListener(select -> {
                if (!select.getValueIsAdjusting() && thisList.getSelectedIndex() != -1) {
                    List<File> thisFiles = (side == Side.LEFT) ? currentLeftFiles : currentRightFiles;
                    File thisFile = thisFiles.get(thisList.getSelectedIndex());
                    thisList.clearSelection();

                    //Stream findet in den right Files die left File mit gleichem Namen, falls diese vorhanden ist, falls nicht, wird ein leeres Optional zurückgegeben
                    List<File> otherFiles = (side == Side.LEFT) ? allRightFiles : allLeftFiles;
                    Optional<File> otherFile = otherFiles.stream().filter(f -> f.getName().equals(thisFile.getName())).findFirst();

                    deactivate();
                    menu.deactivate();

                    // noinspection rawtypes
                    SwingWorker worker = new SwingWorker() {
                        @Override
                        protected Object doInBackground() {
                            if (side.equals(Side.LEFT)) {
                                if (otherFile.isEmpty()) {
                                    level3UI = new Level3UI(FileUtils.readFile(thisFile), null, null);
                                } else {
                                    lr = FileUtils.compareFiles(thisFile, otherFile.get());
                                    level3UI = new Level3UI(lr.left(), lr.right(), lr.specificLineChanges());
                                }
                            } else { // called from right side
                                if (otherFile.isEmpty()) {
                                    level3UI = new Level3UI(null, FileUtils.readFile(thisFile), null);
                                } else {
                                    lr = FileUtils.compareFiles(thisFile, otherFile.get());
                                    level3UI = new Level3UI(lr.right(), lr.left(), lr.specificLineChanges());
                                }
                            }
                            frame.add(level3UI, globalGbc);
                            changeActivePanelFromTo(level2UI, level3UI);
                            return null;
                        }

                        @Override
                        protected void done() {
                            super.done();
                            activate();
                            menu.activate();
                        }
                    };
                    worker.execute();
                }
            });
        }

        public static List<File> filterFilesByName(List<File> files, String searchString) {
            return files.stream() //
                    .filter(f -> f.getName().toLowerCase().contains(searchString.toLowerCase())) //
                    .collect(Collectors.toList());
        }

        public void deactivate() {
            leftList.setEnabled(false);
            rightList.setEnabled(false);
        }

        public void activate() {
            leftList.setEnabled(true);
            rightList.setEnabled(true);
        }
    }


    private static final class Level3UI extends JPanel {

        List<String> leftLines;
        List<String> rightLines;
        List<FileUtils.SpecificLineChange> lineChanges;

        public Level3UI(List<String> leftInput, List<String> rightInput, List<FileUtils.SpecificLineChange> lcs) {
            super(new GridBagLayout());
            setFocusable(false);

            leftLines = leftInput;
            rightLines = rightInput;
            lineChanges = lcs;

            Level3UISide leftUISide = new Level3UISide(Side.LEFT);
            Level3UISide rightUISide = new Level3UISide(Side.RIGHT);
            changeColor(leftUISide.textPane, rightUISide.textPane, lineChanges);

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftUISide, rightUISide);
            splitPane.setResizeWeight(0.5);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.weighty = 1;
            gbc.weightx = 1;
            gbc.gridwidth = 3;
            add(splitPane, gbc);

            JCheckBox synchronizedScrolling = new JCheckBox("Synchrones Scrollen");
            gbc.gridy = 0;
            gbc.weighty = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.CENTER;
            add(synchronizedScrolling, gbc);

            synchronizedScrolling.addActionListener(e -> {
                if (synchronizedScrolling.isSelected()) {
                    rightUISide.getHorizontalScrollBar().setEnabled(false);
                    rightUISide.getVerticalScrollBar().setEnabled(false);
                    leftUISide.getVerticalScrollBar().addAdjustmentListener(e1 -> //
                            rightUISide.getVerticalScrollBar().setValue(leftUISide.getVerticalScrollBar().getValue()));
                    leftUISide.getHorizontalScrollBar().addAdjustmentListener(e2 -> //
                            rightUISide.getHorizontalScrollBar().setValue(leftUISide.getHorizontalScrollBar().getValue()));
                } else {
                    rightUISide.getHorizontalScrollBar().setEnabled(true);
                    rightUISide.getVerticalScrollBar().setEnabled(true);
                    Arrays.stream(leftUISide.getVerticalScrollBar().getAdjustmentListeners()).forEach(leftUISide.getVerticalScrollBar()::removeAdjustmentListener);
                    Arrays.stream(leftUISide.getHorizontalScrollBar().getAdjustmentListeners()).forEach(leftUISide.getHorizontalScrollBar()::removeAdjustmentListener);
                }
            });

            JButton saveBtn = new JButton("Differenz exportieren");
            gbc.gridy = 2;
            gbc.weighty = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.CENTER;
            add(saveBtn, gbc);

            saveBtn.addActionListener(click -> {
                Object[] options = {"Textdatei", "HTML", "Abbrechen"};
                int optionPaneResult = JOptionPane.showOptionDialog(this, //
                        "Als HTML oder als Textdatei speichern?", "Differenz Speichern", //
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
                if (optionPaneResult == JOptionPane.YES_OPTION || optionPaneResult == JOptionPane.NO_OPTION) {
                    JFileChooser jfc = new JFileChooser();
                    jfc.setCurrentDirectory(new File("."));
                    jfc.setDialogTitle("Datei Speichern");
                    int result = jfc.showSaveDialog(this);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        File fileToSaveIn = jfc.getSelectedFile();
                        boolean savedSuccessfully;
                        if (optionPaneResult == JOptionPane.YES_OPTION) {
                            savedSuccessfully = FileUtils.saveDiffAsText(null, null, fileToSaveIn, //
                                    new FileUtils.LineResult(leftLines, rightLines, lineChanges));
                        } else {
                            savedSuccessfully = FileUtils.saveDiffAsHTML(null, null, fileToSaveIn, //
                                    new FileUtils.LineResult(leftLines, rightLines, lineChanges));
                        }
                        if (!savedSuccessfully)
                            JOptionPane.showMessageDialog(this, "Konnte nicht gespeichert werden!", "Fehler", JOptionPane.ERROR_MESSAGE);
                    }
                }

            });
        }

        private class Level3UISide extends JScrollPane {

            private final JTextPane textPane = new JTextPane();

            private Level3UISide(Side side) {
                super();

                textPane.setEditable(false);
                textPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

                List<String> thisSideLines = (side == Side.LEFT) ? leftLines : rightLines;

                if (thisSideLines != null) {
                    StringBuilder sb = new StringBuilder();
                    thisSideLines.forEach(s -> sb.append(s).append(System.lineSeparator()));
                    textPane.setText(sb.toString());
                }

                textPane.setCaretPosition(0); // cursor jump to top

                JPanel textArea = new JPanel(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.fill = GridBagConstraints.BOTH; // für linksbündig
                gbc.weighty = 1; // für linksbündig
                gbc.weightx = 1; // für linksbündig
                textArea.add(textPane, gbc);

                this.setViewportView(textArea);
                this.getVerticalScrollBar().setUnitIncrement(20);
            }
        }

        private void changeColor(JTextPane leftTextPane, JTextPane rightTextPane, List<FileUtils.SpecificLineChange> lineChanges) {
            MutableAttributeSet leftAttrs = leftTextPane.getInputAttributes();
            StyledDocument leftDoc = leftTextPane.getStyledDocument();
            MutableAttributeSet rightAttrs = rightTextPane.getInputAttributes();
            StyledDocument rightDoc = rightTextPane.getStyledDocument();

            String[] leftLines = leftTextPane.getText().split(System.lineSeparator());
            int leftOffset = 0;
            String[] rightLines = rightTextPane.getText().split(System.lineSeparator());
            int rightOffset = 0;

            int lineNumber = 1;
            // we know that leftLines.length == rightLines.length
            for (int i = 0; i < leftLines.length; i++) {
                int indexOfSymbol = String.valueOf(lineNumber).length() + 2;

                String leftLine = leftLines[i];
                String rightLine = rightLines[i];


                char leftSymbol = leftLine.charAt(indexOfSymbol);
                if (leftSymbol == '+' || leftSymbol == '-' || leftSymbol == '!') { // make + green, - red, ! orange
                    Color colorOfSymbol = (leftSymbol == '+') ? Color.GREEN : (leftSymbol == '-') ? Color.RED : Color.ORANGE;
                    StyleConstants.setBackground(leftAttrs, colorOfSymbol);
                    StyleConstants.setForeground(leftAttrs, Color.BLACK);
                    leftDoc.setCharacterAttributes(leftOffset + indexOfSymbol, 1, leftAttrs, false);
                }
                leftOffset += leftLine.length() + 1;

                char rightSymbol = rightLine.charAt(indexOfSymbol);
                if (rightSymbol == '+' || rightSymbol == '-' || rightSymbol == '!') { // make + green, - red, ! orange
                    Color colorOfSymbol = (rightSymbol == '+') ? Color.GREEN : (rightSymbol == '-') ? Color.RED : Color.ORANGE;
                    StyleConstants.setBackground(rightAttrs, colorOfSymbol);
                    StyleConstants.setForeground(rightAttrs, Color.BLACK);
                    rightDoc.setCharacterAttributes(rightOffset + indexOfSymbol, 1, rightAttrs, false);
                }
                rightOffset += rightLine.length() + 1;

                if (leftSymbol != '-' && rightSymbol != '-') lineNumber++;
            }

            // specific line changes
            if (lineChanges != null) {
                for (FileUtils.SpecificLineChange change : lineChanges) {
                    String[] changSideLines = (change.displaySide() == Side.LEFT) ? leftLines : rightLines;
                    MutableAttributeSet changSideAttrs = (change.displaySide() == Side.LEFT) ? leftAttrs : rightAttrs;
                    StyledDocument changSideDoc = (change.displaySide() == Side.LEFT) ? leftDoc : rightDoc;

                    int offset = 0;
                    for (int i = 0; i < change.lineNumber() - 1; i++) offset += changSideLines[i].length() + 1;

                    StyleConstants.setBackground(changSideAttrs, Color.ORANGE);
                    StyleConstants.setForeground(changSideAttrs, Color.BLACK);
                    changSideDoc.setCharacterAttributes(offset + change.index(), 1, changSideAttrs, false);
                }
            }
        }
    }


    private void initializeEscFocus() {
        level1UI.setFocusable(false);
        menu.setFocusable(false);
        backButton.setFocusable(false);
        forwardButton.setFocusable(false);
        frame.setFocusable(true);
        frame.requestFocusInWindow();
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (level2UI != null && level2UI.isVisible()) {
                        changeActivePanelFromTo(level2UI, level1UI);
                    } else if (level3UI != null && level3UI.isVisible()) {
                        changeActivePanelFromTo(level3UI, level2UI);
                    }
                }
            }
        });
    }

    private void changeActivePanelFromTo(JPanel oldPanel, JPanel newPanel) {
        oldPanel.setVisible(false);
        newPanel.setVisible(true);
    }

    private void switchThemeTo(SwingTheme theme) {
        try {
            UIManager.setLookAndFeel(theme.laf);
            String[] components = {"Panel", "OptionPane", "Label", "Button", "TextField", "TextPane", "CheckBox", "ComboBox", "List", "MenuBar", "Menu", "MenuItem", "SplitPane", "Frame", "FileChooser", "ScrollBar", "ScrollPane"};
            Arrays.stream(components).forEach(component -> UIManager.put(component + ".foreground", theme.textColor));
            SwingUtilities.updateComponentTreeUI(frame);
            Preferences preferences = Preferences.userNodeForPackage(SwingInterface.class);
            preferences.put("theme", theme.toString());
        } catch (Exception ignored) {
        }
    }
}