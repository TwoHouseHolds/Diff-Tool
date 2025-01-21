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
    private SwingTicTacToeMinigaming ticTacToeGameUI = null;
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

    //TODO translate to german and correct the information in menu
    private final class Menu extends JMenuBar {
        JButton back = new JButton("⬅");
        JMenuItem ticTacToe = new JMenuItem("TicTacToe Minispiel");

        public Menu(JFrame frame) {
            back.setVisible(false);
            add(back);

            ticTacToe.addActionListener(e -> {
                ticTacToe.setEnabled(false);
                JPanel parent = level1UI.isVisible() ? level1UI : level2UI != null && level2UI.isVisible() ? level2UI : level3UI;
                ticTacToeGameUI = new SwingTicTacToeMinigaming();
                ticTacToeGameUI.setParent(parent);
                ticTacToeGameUI.setOldSize(frame.getSize());
                GridBagConstraints tictacttoeGbc = new GridBagConstraints();
                tictacttoeGbc.weightx = 1;
                tictacttoeGbc.weighty = 1;
                tictacttoeGbc.fill = GridBagConstraints.BOTH;
                tictacttoeGbc.anchor = GridBagConstraints.CENTER;
                frame.add(ticTacToeGameUI, tictacttoeGbc);
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
                      Hierfür wählen Sie die Dateien aus, die Sie vergleichen möchten, 
                      indem Sie den Pfad in der TextBox eingeben oder das Verzeichnis 
                      mit dem Button \uD83D\uDCC1 auswählen. Danach auf "Bestätigen" klicken.
                    - Jederzeit können Sie mit der Escape-Taste zum vorherigen Menü zurückkehren,
                      alternativ können sie auch die Pfeilbuttons in der oberen linken ecke nutzen 
                      um zwischen den Menüs zu wechseln, sofern dies möglich ist.
                    - Um die Differenz von 2 Dateien zu speichern, wählen sie den Button 
                      "Differenz Exportieren", welcher sich unterhalb des Textes mit den 
                       Differenzen befindet.
                    - Um das Theme der Anwendung zu ändern, wählen Sie im Menü unter "Einstellungen"
                      -> "Theme" das gewünschte Theme aus.
                    - Um diese Anleitung erneut anzuzeigen, wählen Sie im Menü "Hilfe" -> "Guide".
                    - Um Informationen über die Entwickler zu erhalten, wählen Sie im Menü "Hilfe" 
                      -> "Über uns".
                    - Um ein TicTacToe-Minispiel zu starten, wählen Sie im Menü "Zusätzliches" -> "TicTacToe"
                    - Um das Programm zu beenden, wählen Sie im Menü "Beenden" -> "Beende Programm.""", frame, "Hilfe");
            MenuItem aboutItem = new MenuItem("Über uns", "Entwickelt im Rahmen der SoftwareProjekt 1 Vorlesung der Hochschule für Technik Stuttgart.\n" + "Contributors: Benedikt Belschner, Colin Traub, Daniel Rodean, Finn Wolf", frame, "Über uns");

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

        public Level2UI(List<File> leftFiles, List<File> rightFiles) {
            super(new GridBagLayout());
            setFocusable(false);
            if (leftFiles == null || rightFiles == null) {
                JOptionPane.showMessageDialog(frame, (leftFiles == null ? "Linkes" : "Rechtes") + " Verzeichnis ist leer!", "Fehler", JOptionPane.ERROR_MESSAGE);
                changeActivePanelFromTo(this, level1UI);
                return;
            }
            leftList = new JList<>(getFormatFileNames(leftFiles, rightFiles, Side.LEFT));
            rightList = new JList<>(getFormatFileNames(rightFiles, leftFiles, Side.RIGHT));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(1, 1, 1, 1);

            Level2UISide left = new Level2UISide(leftFiles, rightFiles, Side.LEFT);
            Level2UISide right = new Level2UISide(rightFiles, leftFiles, Side.RIGHT);

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
            private Level2UISide(List<File> firstFiles, List<File> secondFiles, Side side) {
                super(new BorderLayout());

                // scrollPane
                JList<String> thisSideList = (side == Side.LEFT) ? leftList : rightList;
                applyListSelectionListener(thisSideList, firstFiles, secondFiles, side);
                JScrollPane scrollPane = new JScrollPane(thisSideList);
                add(scrollPane, BorderLayout.CENTER);

                // level2Menu
                JPanel level2Menu = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JComboBox<String> sortingSelection = new JComboBox<>(new String[]{"Unsortiert", "Alphabetisch", "Größe", "Datum"});
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
                            List<File> filteredAndSorted = new ArrayList<>(firstFiles);
                            // search
                            if (!search.isEmpty()) {
                                filteredAndSorted = filterFilesByName(filteredAndSorted, search);
                            }
                            // sort
                            Comparator<File> comp = switch (selectedSorting) {
                                case 1 -> Comparator.comparing(File::getName);
                                case 2 -> Comparator.comparing(File::length);
                                case 3 -> Comparator.comparing(File::lastModified);
                                default -> null;
                            };
                            if (comp != null) {
                                if (isReversed) comp = comp.reversed();
                                filteredAndSorted.sort(comp);
                            }

                            thisSideList.setListData(getFormatFileNames(filteredAndSorted, secondFiles, side));
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
        private void applyListSelectionListener(JList<String> thisList, List<File> thisFiles, List<File> otherFiles, Side sideInformation) {
            thisList.addListSelectionListener(select -> {
                if (!select.getValueIsAdjusting() && thisList.getSelectedIndex() != -1) {
                    File thisFile = thisFiles.get(thisList.getSelectedIndex());
                    thisList.clearSelection();

                    //Stream findet in den right Files die left File mit gleichem Namen, falls diese vorhanden ist, falls nicht, wird ein leeres Optional zurückgegeben
                    Optional<File> otherFile = otherFiles.stream().filter(f -> f.getName().equals(thisFile.getName())).findFirst();

                    deactivate();
                    menu.deactivate();

                    // noinspection rawtypes
                    SwingWorker worker = new SwingWorker() {
                        @Override
                        protected Object doInBackground() {
                            if (sideInformation.equals(Side.LEFT)) {
                                if (otherFile.isEmpty()) {
                                    level3UI = new Level3UI(FileUtils.readFile(thisFile), null, null, false);
                                } else {
                                    lr = FileUtils.compareFiles(thisFile, otherFile.get());
                                    level3UI = new Level3UI(lr.left(), lr.right(), lr.specificLineChanges(), false);
                                }
                            } else { // called from right side
                                if (otherFile.isEmpty()) {
                                    level3UI = new Level3UI(null, FileUtils.readFile(thisFile), null, false);
                                } else {
                                    lr = FileUtils.compareFiles(thisFile, otherFile.get());
                                    level3UI = new Level3UI(lr.right(), lr.left(), lr.specificLineChanges(), true);
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

        private final boolean swapLineChanges;

        public Level3UI(List<String> leftLines, List<String> rightLines, List<FileUtils.SpecificLineChange> lineChanges, boolean swapLineChanges) {
            super(new GridBagLayout());
            setFocusable(false);

            this.swapLineChanges = swapLineChanges;

            JTextPane leftTextPane = new JTextPane();
            leftTextPane.setEditable(false);
            leftTextPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            JTextPane rightTextPane = new JTextPane();
            rightTextPane.setEditable(false);
            rightTextPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

            if (leftLines != null) leftLines.forEach(s -> leftTextPane.setText(leftTextPane.getText() + s + "\n"));
            if (rightLines != null) rightLines.forEach(s -> rightTextPane.setText(rightTextPane.getText() + s + "\n"));

            if (rightLines != null && leftLines != null) {
                changeColor(leftTextPane, lineChanges, Side.LEFT);
                changeColor(rightTextPane, lineChanges, Side.RIGHT);
            }

            leftTextPane.setCaretPosition(0);
            rightTextPane.setCaretPosition(0);

            JPanel leftTextArea = new JPanel();
            leftTextArea.add(leftTextPane);
            JPanel rightTextArea = new JPanel();
            rightTextArea.add(rightTextPane);

            JScrollPane leftJSP = new JScrollPane(leftTextArea);
            leftJSP.getVerticalScrollBar().setUnitIncrement(20);
            JScrollPane rightJSP = new JScrollPane(rightTextArea);
            rightJSP.getVerticalScrollBar().setUnitIncrement(20);


            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftJSP, rightJSP);
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
                    rightJSP.getHorizontalScrollBar().setEnabled(false);
                    rightJSP.getVerticalScrollBar().setEnabled(false);
                    leftJSP.getVerticalScrollBar().addAdjustmentListener(e1 -> //
                            rightJSP.getVerticalScrollBar().setValue(leftJSP.getVerticalScrollBar().getValue()));
                    leftJSP.getHorizontalScrollBar().addAdjustmentListener(e2 -> //
                            rightJSP.getHorizontalScrollBar().setValue(leftJSP.getHorizontalScrollBar().getValue()));
                } else {
                    rightJSP.getHorizontalScrollBar().setEnabled(true);
                    rightJSP.getVerticalScrollBar().setEnabled(true);
                    Arrays.stream(leftJSP.getVerticalScrollBar().getAdjustmentListeners()).forEach(leftJSP.getVerticalScrollBar()::removeAdjustmentListener);
                    Arrays.stream(leftJSP.getHorizontalScrollBar().getAdjustmentListeners()).forEach(leftJSP.getHorizontalScrollBar()::removeAdjustmentListener);
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

        private void changeColor(JTextPane textPane, List<FileUtils.SpecificLineChange> lineChanges, Side side) {
            MutableAttributeSet attrs = textPane.getInputAttributes();
            StyledDocument doc = textPane.getStyledDocument();

            String[] lines = textPane.getText().split("\n");
            int offset = 0;

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                int indexOfSymbol = String.valueOf(i).length() + 2;
                char symbol = line.charAt(indexOfSymbol);
                if (symbol != ' ') { // make + green, - red, ! orange
                    Color colorOfSymbol = (symbol == '+') ? Color.GREEN : (symbol == '-') ? Color.RED : Color.ORANGE;
                    StyleConstants.setBackground(attrs, colorOfSymbol);
                    StyleConstants.setForeground(attrs, Color.BLACK);
                    doc.setCharacterAttributes(offset + indexOfSymbol, 1, attrs, false);
                }
                offset += line.length() + 1;
            }

            if (lineChanges != null) {
                for (FileUtils.SpecificLineChange change : lineChanges) {
                    if (!swapLineChanges && side == change.displaySide() || swapLineChanges && side != change.displaySide()) {
                        offset = 0;

                        for (int i = 0; i < change.lineNumber() - 1; i++) {
                            offset += lines[i].length() + 1;
                        }

                        StyleConstants.setBackground(attrs, Color.ORANGE);
                        StyleConstants.setForeground(attrs, Color.BLACK);
                        doc.setCharacterAttributes(offset + change.index(), 1, attrs, false);
                    }
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