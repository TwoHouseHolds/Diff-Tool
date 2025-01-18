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

    public void start() {
        SwingUtilities.invokeLater(() -> {
            initializeEscFocus();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int width = (int) (screenSize.width * 0.7);
            int height = (int) (screenSize.height * 0.7);
            frame.setSize(width, height);

            frame.setJMenuBar(menu);
            frame.add(level1UI);

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
            frame.setVisible(true);
        });
    }

    private void resizeAll() {
        level1UI.setSize(frame.getSize());
        if (level2UI != null) level2UI.setSize(frame.getSize());
        if (level3UI != null) level3UI.setSize(frame.getSize());
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
                frame.add(ticTacToeGameUI);
                frame.setResizable(false);
                frame.setSize(600, 670);
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
                //noinspection StatementWithEmptyBody
                if (level1UI.isVisible()) {
                } else if (level2UI.isVisible()) {
                    changeActivePanelFromTo(level2UI, level1UI);
                } else if (level3UI.isVisible()) {
                    changeActivePanelFromTo(level3UI, level2UI);
                }
            });
            add(backButton);

            forwardButton.addActionListener(e -> {
                //noinspection StatementWithEmptyBody
                if (level3UI != null && level3UI.isVisible()) {
                } else if (level2UI != null && level2UI.isVisible() && level3UI != null) {
                    changeActivePanelFromTo(level2UI, level3UI);
                } else if (level1UI.isVisible() && level2UI != null) {
                    changeActivePanelFromTo(level1UI, level2UI);
                }
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
                      Hierfür wählen Sie die Dateien aus, die Sie vergleichen möchten, indem
                      Sie den Pfad in der TextBox eingeben oder das Verzeichnis mit dem Button "Select" auswählen.
                      Danach auf "Confirm" klicken.
                    - Jederzeit können Sie mit der Escape-Taste zum vorherigen Menü zurückkehren.
                    - Um diese Anleitung erneut anzuzeigen, wählen Sie im Menü "Hilfe" -> "Guide".
                    - Um Informationen über die Entwickler zu erhalten, wählen Sie im Menü "Hilfe" -> "Über uns".
                    - Um das Programm zu beenden, wählen Sie im Menü "Beenden" -> "Beende Programm".
                    - Use the menu to view help or exit the application.""", frame, "Help");
            MenuItem aboutItem = new MenuItem("Über uns", "Entwickelt im Rahmen der SoftwareProjekt 1 Vorlesung der Hochschule für Technik Stuttgart.\n" + "Contributors: Benedikt Belschner, Colin Traub, Daniel Rodean, Finn Wolf", frame, "About Us");

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

            JButton cancelButton = new JButton("Abbrechen");
            cancelButton.addActionListener(click -> {
                leftPanel.clearInput();
                rightPanel.clearInput();
            });
            bottomPanel.add(cancelButton);

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
                    frame.add(level2UI);
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

            public void clearInput() {
                textField.setText("");
            }
        }
    }

    private final class Level2UI extends JPanel {

        private FileUtils.LineResult lr;
        private JList<String> leftList;
        private JList<String> rightList;
        private JSplitPane splitPane;

        public Level2UI(List<File> leftFiles, List<File> rightFiles) {
            super(new GridBagLayout());
            setFocusable(false);
            if (leftFiles == null || rightFiles == null) {
                JOptionPane.showMessageDialog(frame, (leftFiles == null ? "Linkes" : "Rechtes") + " Verzeichnis ist leer!", "Fehler", JOptionPane.ERROR_MESSAGE);
                level1UI.setVisible(true);
                return;
            }

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(1, 1, 1, 1);

            leftList = new JList<>(getFormatFileNames(leftFiles, rightFiles, Side.LEFT));
            applyListSelectionListener(leftList, leftFiles, rightFiles, Side.LEFT);

            rightList = new JList<>(getFormatFileNames(rightFiles, leftFiles, Side.RIGHT));
            applyListSelectionListener(rightList, rightFiles, leftFiles, Side.RIGHT);

            JScrollPane leftScrollPane = new JScrollPane(leftList);
            JScrollPane rightScrollPane = new JScrollPane(rightList);

            JComboBox<String> leftComboBox = new JComboBox<>(new String[]{"Unsortiert", "Alphabetisch", "Größe", "Datum"});
            JComboBox<String> rightComboBox = new JComboBox<>(new String[]{"Unsortiert", "Alphabetisch", "Größe", "Datum"});

            JCheckBox checkBoxReverseLeft = new JCheckBox("Umgekehrte Sortierung");
            JCheckBox checkBoxReverseRight = new JCheckBox("Umgekehrte Sortierung");

            JPanel left = new JPanel(new BorderLayout());
            JPanel leftNorthPanel = new JPanel();
            leftNorthPanel.setLayout(new BoxLayout(leftNorthPanel, BoxLayout.Y_AXIS));
            leftNorthPanel.add(checkBoxReverseLeft);
            leftNorthPanel.add(leftComboBox);
            left.add(leftScrollPane, BorderLayout.CENTER);
            left.add(leftNorthPanel, BorderLayout.NORTH);

            JPanel right = new JPanel(new BorderLayout());
            JPanel rightNorthPanel = new JPanel();
            rightNorthPanel.setLayout(new BoxLayout(rightNorthPanel, BoxLayout.Y_AXIS));
            rightNorthPanel.add(checkBoxReverseRight);
            rightNorthPanel.add(rightComboBox);
            right.add(rightNorthPanel, BorderLayout.NORTH);
            right.add(rightScrollPane, BorderLayout.CENTER);

            ActionListener leftActionListener = e -> {
                int selected = leftComboBox.getSelectedIndex();
                Boolean isReversed = checkBoxReverseLeft.isSelected();
                manageSortingBox(leftList, leftFiles, rightFiles, Side.LEFT, selected, isReversed);
            };

            leftComboBox.addActionListener(leftActionListener);
            checkBoxReverseLeft.addActionListener((e) -> leftComboBox.setSelectedIndex(leftComboBox.getSelectedIndex()));

            ActionListener rightActionListener = e -> {
                int selected = rightComboBox.getSelectedIndex();
                Boolean isReversed = checkBoxReverseRight.isSelected();
                manageSortingBox(rightList, rightFiles, leftFiles, Side.RIGHT, selected, isReversed);
            };

            rightComboBox.addActionListener(rightActionListener);
            checkBoxReverseRight.addActionListener((e) -> rightComboBox.setSelectedIndex(rightComboBox.getSelectedIndex()));


            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
            splitPane.setResizeWeight(0.5);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1;
            gbc.weighty = 1;

            add(splitPane, gbc);

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
                                if (otherFile.isEmpty())
                                    level3UI = new Level3UI(FileUtils.readFile(thisFile), null, null, false);
                                else {
                                    lr = FileUtils.compareFiles(thisFile, otherFile.get());
                                    level3UI = new Level3UI(lr.left(), lr.right(), lr.specificLineChanges(), false);
                                }
                            } else { // called from right side
                                if (otherFile.isEmpty())
                                    level3UI = new Level3UI(null, FileUtils.readFile(thisFile), null, false);
                                else {
                                    lr = FileUtils.compareFiles(thisFile, otherFile.get());
                                    level3UI = new Level3UI(lr.right(), lr.left(), lr.specificLineChanges(), true);
                                }
                            }
                            frame.add(level3UI);
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

        public void deactivate() {
            leftList.setEnabled(false);
            rightList.setEnabled(false);
        }

        public void activate() {
            leftList.setEnabled(true);
            rightList.setEnabled(true);
        }
    }

    private void manageSortingBox(JList<String> listBox, List<File> firstFiles, List<File> secondFiles, Side side, int selected, Boolean isReversed) {
        //Lade Daten message
        @SuppressWarnings("rawtypes") SwingWorker worker = new SwingWorker() {
            @Override
            protected Object doInBackground() {

                if (selected != -1) {
                    switch (selected) {
                        case 0: {
                            listBox.setListData(Level2UI.getFormatFileNames(firstFiles, secondFiles, side));
                            break;
                        }
                        case 1: {
                            if (isReversed) {
                                firstFiles.sort(Comparator.comparing(File::getName).reversed());
                            } else {
                                firstFiles.sort(Comparator.comparing(File::getName));
                            }
                            listBox.setListData(Level2UI.getFormatFileNames(firstFiles, secondFiles, side));
                            break;
                        }

                        case 2: {
                            if (isReversed) {
                                firstFiles.sort(Comparator.comparingLong(File::length).reversed());
                            } else {
                                firstFiles.sort(Comparator.comparingLong(File::length));
                            }
                            listBox.setListData(Level2UI.getFormatFileNames(firstFiles, secondFiles, side));
                            break;
                        }
                        case 3: {
                            if (isReversed) {
                                firstFiles.sort(Comparator.comparingLong(File::lastModified).reversed());
                            } else {
                                firstFiles.sort(Comparator.comparingLong(File::lastModified));
                            }
                            listBox.setListData(Level2UI.getFormatFileNames(firstFiles, secondFiles, side));
                            break;
                        }
                    }

                }
                return null;
            }

            protected void done() {
                frame.invalidate();
                frame.revalidate();
                frame.repaint();
            }
        };
        worker.execute();
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

            changeColor(leftTextPane, lineChanges, Side.LEFT);
            changeColor(rightTextPane, lineChanges, Side.RIGHT);

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
                    leftJSP.getVerticalScrollBar().addAdjustmentListener(e1 -> {
                        rightJSP.getVerticalScrollBar().setValue(leftJSP.getVerticalScrollBar().getValue());
                    });
                    leftJSP.getHorizontalScrollBar().addAdjustmentListener(e2 -> {
                        rightJSP.getHorizontalScrollBar().setValue(leftJSP.getHorizontalScrollBar().getValue());
                    });
                } else {
                    rightJSP.getHorizontalScrollBar().setEnabled(true);
                    rightJSP.getVerticalScrollBar().setEnabled(true);
                    Arrays.stream(leftJSP.getVerticalScrollBar().getAdjustmentListeners()).forEach(leftJSP.getVerticalScrollBar()::removeAdjustmentListener);
                    Arrays.stream(leftJSP.getHorizontalScrollBar().getAdjustmentListeners()).forEach(leftJSP.getHorizontalScrollBar()::removeAdjustmentListener);
                }
            });
        }

        private void changeColor(JTextPane textPane, List<FileUtils.SpecificLineChange> lineChanges, Side side) {
            MutableAttributeSet attrs = textPane.getInputAttributes();
            StyledDocument doc = textPane.getStyledDocument();

            String[] lines = textPane.getText().split("\n");
            int offset = 0;

            //Change color of every added + to green
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                int space = String.valueOf(i).length() + 4;
                int indexOfPlus = line.indexOf("+");

                if (indexOfPlus >= 0 && indexOfPlus < space) {
                    StyleConstants.setBackground(attrs, Color.GREEN);
                    StyleConstants.setForeground(attrs, Color.BLACK);
                    doc.setCharacterAttributes(offset + indexOfPlus, 1, attrs, false);
                }
                offset += line.length() + 1;
            }

            offset = 0;

            //Change color of every added - to red
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                int space = String.valueOf(i).length() + 4;
                int indexOfMinus = line.indexOf("-");

                if (indexOfMinus >= 0 && indexOfMinus < space) {
                    StyleConstants.setBackground(attrs, Color.RED);
                    StyleConstants.setForeground(attrs, Color.BLACK);
                    doc.setCharacterAttributes(offset + indexOfMinus, 1, attrs, false);
                }
                offset += line.length() + 1;
            }

            offset = 0;

            //Change color of every added ! to orange
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                int space = String.valueOf(i).length() + 4;
                int indexOfExclamationMark = line.indexOf("!");

                if (indexOfExclamationMark >= 0 && indexOfExclamationMark < space) {
                    StyleConstants.setBackground(attrs, Color.ORANGE);
                    StyleConstants.setForeground(attrs, Color.BLACK);
                    doc.setCharacterAttributes(offset + indexOfExclamationMark, 1, attrs, false);
                }
                offset += line.length() + 1;
            }

            if (lineChanges != null) {
                for (FileUtils.SpecificLineChange change : lineChanges) {
                    if (!swapLineChanges) {
                        if (side == change.displaySide()) {

                            offset = 0;
                            int lineNumber = change.lineNumber();
                            int index = change.index();

                            for (int i = 0; i < lineNumber - 1; i++) {
                                offset += lines[i].length() + 1;
                            }

                            int indexOfMarked = offset + index;
                            StyleConstants.setBackground(attrs, Color.ORANGE);
                            StyleConstants.setForeground(attrs, Color.BLACK);
                            doc.setCharacterAttributes(indexOfMarked, 1, attrs, false);
                        }
                    } else {
                        if (side != change.displaySide()) {

                            offset = 0;
                            int lineNumber = change.lineNumber();
                            int index = change.index();

                            for (int i = 0; i < lineNumber - 1; i++) {
                                offset += lines[i].length() + 1;
                            }

                            int indexOfMarked = offset + index;
                            StyleConstants.setBackground(attrs, Color.ORANGE);
                            StyleConstants.setForeground(attrs, Color.BLACK);
                            doc.setCharacterAttributes(indexOfMarked, 1, attrs, false);
                        }
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
                    //noinspection StatementWithEmptyBody
                    if (level1UI.isVisible()) {
                    } else if (level2UI != null && level2UI.isVisible()) {
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
            String[] components = {"Panel", "OptionPane", "Label", "Button", "TextField", "TextPane", "CheckBox",
                    "ComboBox", "List", "MenuBar", "Menu", "MenuItem", "SplitPane", "Frame", "FileChooser", "ScrollBar",
                    "ScrollPane"};
            Arrays.stream(components).forEach(component -> UIManager.put(component + ".foreground", theme.textColor));
            SwingUtilities.updateComponentTreeUI(frame);
            Preferences preferences = Preferences.userNodeForPackage(SwingInterface.class);
            preferences.put("theme", theme.toString());
        } catch (Exception ignored) {
        }
    }
}