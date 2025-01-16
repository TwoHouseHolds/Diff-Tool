package swing;

import algorithms.FileUtils;
import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.ComboBox;
import lanterna.LanternaInterface;
import utils.Side;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

public class SwingInterface {

    //TODO LVUI doesnt resize correctly after going back

    private static final Dimension defaultTextFieldDimension = new Dimension(300, 25);
    private static final FileUtils fileUtils = new FileUtils();
    private final JFrame frame = new JFrame("Swing Oberfläche");
    private final JButton backButton = new JButton("⬅");
    private final JButton forwardButton = new JButton("➡");
    private final Menu menu = new Menu(frame);
    private final Level1UI level1UI = new Level1UI();
    private Level2UI level2UI = null;
    private Level3UI level3UI = null;
    private SwingTicTacToeMinigame ticTacToeGameUI = null;

    public void start() {
        SwingUtilities.invokeLater(() -> {
            initializeEscFocus();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int width = (int) (screenSize.width * 0.5);
            int height = (int) (screenSize.height * 0.5);
            frame.setSize(width, height);

            frame.setJMenuBar(menu);
            frame.add(level1UI);

            frame.setVisible(true);
        });
    }

    //TODO translate to german and correct the information in menu
    private final class Menu extends JMenuBar {
        JButton back = new JButton("⬅");

        public Menu(JFrame frame) {
            this.back.setVisible(false);
            add(back);

            JMenuItem ticTacToe = new JMenuItem("TicTacToe Minispiel");
            ticTacToe.addActionListener(e -> {
                ticTacToe.setEnabled(false);
                JPanel parent = level1UI.isVisible() ? level1UI : level2UI != null && level2UI.isVisible() ? level2UI : level3UI;
                ticTacToeGameUI = new SwingTicTacToeMinigame();
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
                    SwingUtilities.invokeLater(() -> {
                        frame.setSize(ticTacToeGameUI.getOldSize());
                    });
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
            MenuItem aboutItem = new MenuItem("Über uns",
                    "Entwickelt im Rahmen der SoftwareProjekt 1 Vorlesung der Hochschule für Technik Stuttgart.\n" +
                            "Contributors: Benedikt Belschner, Colin Traub, Daniel Rodean, Finn Wolf", frame, "About Us");

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
            add(helpMenu);

            JMenu zusaetzliches = new JMenu("Zusätzliches");
            zusaetzliches.add(ticTacToe);
            add(zusaetzliches);

            JMenu exitMenu = new JMenu("Beenden");
            JMenuItem exitItem = new JMenuItem("Programm beenden");
            exitItem.addActionListener(e -> System.exit(0));
            exitMenu.add(exitItem);
            add(exitMenu);


        }


        private static class MenuItem extends JMenuItem {
            public MenuItem(String name, String content, JFrame frame, String popUpTitle) {
                super(name);
                addActionListener(e -> JOptionPane.showMessageDialog(frame, content, popUpTitle, JOptionPane.INFORMATION_MESSAGE));
            }
        }


    }



    private final class Level1UI extends JPanel {
        private final GridBagConstraints gbc = new GridBagConstraints();
        private final DirectorySelectionPanel leftPanel = new DirectorySelectionPanel("Erstes Verzeichnis:");
        private final DirectorySelectionPanel rightPanel = new DirectorySelectionPanel("Zweites Verzeichnis:");

        Level1UI() {
            super(new GridBagLayout());
            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(1, 1, 1, 1);

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
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
                    JOptionPane.showMessageDialog(this, "'" + pathLeft + "' (links) ist kein valider Pfad!", "Fehler", JOptionPane.ERROR_MESSAGE);
                } else if (!new File(pathRight).exists()) {
                    JOptionPane.showMessageDialog(this, "'" + pathRight + "' (rechts) ist kein valider Pfad!", "Fehler", JOptionPane.ERROR_MESSAGE);
                } else {
                    List<File> leftFiles = fileUtils.getFiles(pathLeft);
                    List<File> rightFiles = fileUtils.getFiles(pathRight);
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

            JList<String> leftList = new JList<>(getFormatFileNames(leftFiles, rightFiles, Side.LEFT));
            applyListSelectionListener(leftList, leftFiles, rightFiles, Side.LEFT);

            JList<String> rightList = new JList<>(getFormatFileNames(rightFiles, leftFiles, Side.RIGHT));
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
            left.add(leftNorthPanel, BorderLayout.NORTH);
            left.add(leftScrollPane, BorderLayout.CENTER);

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
                manageSortingBox(leftComboBox, leftList, leftFiles, rightFiles, Side.LEFT, selected, isReversed);
            };

            leftComboBox.addActionListener(leftActionListener);
            checkBoxReverseLeft.addActionListener((e) -> leftComboBox.setSelectedIndex(leftComboBox.getSelectedIndex()));

            ActionListener rightActionListener = e -> {
                int selected = rightComboBox.getSelectedIndex();
                Boolean isReversed = checkBoxReverseRight.isSelected();
                manageSortingBox(rightComboBox, rightList, rightFiles, leftFiles, Side.RIGHT, selected, isReversed);
            };

            rightComboBox.addActionListener(rightActionListener);
            checkBoxReverseRight.addActionListener((e) -> rightComboBox.setSelectedIndex(rightComboBox.getSelectedIndex()));


            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
            splitPane.setResizeWeight(0.5);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weighty = 1;
            gbc.weightx = 1;

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
                    } catch (IOException ignored) {
                    }
                } else {
                    result.add(leftFileName + String.format(" (in %s)", sideInformation.toString()));
                }
            }
            return result.toArray(new String[0]);
        }

        // variable names for left side, method can still be used for right side
        private void applyListSelectionListener(JList<String> leftList, List<File> leftFiles, List<File> rightFiles, Side sideInformation) {
            leftList.addListSelectionListener(select -> {
                if (!select.getValueIsAdjusting() && leftList.getSelectedIndex() != -1) {
                    File leftFile = leftFiles.get(leftList.getSelectedIndex());
                    leftList.clearSelection();

                    //Stream findet in den right Files die left File mit gleichem Namen, falls diese vorhanden ist, falls nicht, wird ein leeres Optional zurückgegeben
                    Optional<File> rightFile = rightFiles.stream().filter(f -> f.getName().equals(leftFile.getName())).findFirst();

                    if (sideInformation.equals(Side.LEFT)) {
                        if (rightFile.isEmpty()) level3UI = new Level3UI(fileUtils.readFile(leftFile), null, null);
                        else {
                            FileUtils.LineResult lr = fileUtils.compareFiles(leftFile, rightFile.get());
                            level3UI = new Level3UI(lr.left(), lr.right(), lr.specificLineChanges());
                        }
                    } else { // called from right side
                        if (rightFile.isEmpty()) level3UI = new Level3UI(null, fileUtils.readFile(leftFile), null);
                        else {
                            FileUtils.LineResult lr = fileUtils.compareFiles(rightFile.get(), leftFile);
                            level3UI = new Level3UI(lr.left(), lr.right(), lr.specificLineChanges());
                        }
                    }
                    frame.add(level3UI);
                    changeActivePanelFromTo(level2UI, level3UI);
                }
            });
        }
    }

    private void manageSortingBox(JComboBox<String> comboBox, JList<String> listBox, List<File> firstFiles, List<File> secondFiles, Side side, int selected, Boolean isReversed){
            //Lade Daten message
            @SuppressWarnings("rawtypes")
            SwingWorker worker = new SwingWorker() {
                @Override
                protected Object doInBackground() {

                    if(selected != -1) {
                        switch (selected) {
                            case 0: {
                                listBox.setListData(Level2UI.getFormatFileNames(firstFiles, secondFiles, side));
                                break;
                            }
                            case 1: {
                                if(isReversed){
                                    firstFiles.sort(Comparator.comparing(File::getName).reversed());
                                }else{
                                    firstFiles.sort(Comparator.comparing(File::getName));
                                }
                                listBox.setListData(Level2UI.getFormatFileNames(firstFiles, secondFiles, side));
                                break;
                            }

                            case 2: {
                                if(isReversed){
                                    firstFiles.sort(Comparator.comparingLong(File::length).reversed());
                                }else{
                                    firstFiles.sort(Comparator.comparingLong(File::length));
                                }
                                listBox.setListData(Level2UI.getFormatFileNames(firstFiles, secondFiles, side));
                                break;
                            }
                            case 3: {
                                if(isReversed){
                                    firstFiles.sort(Comparator.comparingLong(File::lastModified).reversed());
                                }else{
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

        public Level3UI(List<String> leftLines, List<String> rightLines, List<FileUtils.SpecificLineChange> lineChanges) {
            super(new GridBagLayout());
            setFocusable(false);

            JTextPane leftTextPane = new JTextPane();
            leftTextPane.setEditable(false);
            leftTextPane.setFont(new Font(Font.MONOSPACED,Font.PLAIN,12));
            JTextPane rightTextPane = new JTextPane();
            rightTextPane.setEditable(false);
            rightTextPane.setFont(new Font(Font.MONOSPACED,Font.PLAIN,12));

            if (leftLines != null) leftLines.forEach(s -> leftTextPane.setText(leftTextPane.getText() + s + "\n"));
            if (rightLines != null) rightLines.forEach(s -> rightTextPane.setText(rightTextPane.getText() + s + "\n"));

            changeColor(leftTextPane,lineChanges,Side.LEFT);
            changeColor(rightTextPane,lineChanges,Side.RIGHT);

            JPanel leftTextArea = new JPanel();
            leftTextArea.add(leftTextPane);
            leftTextArea.setBackground(Color.WHITE);
            JPanel rightTextArea = new JPanel();
            rightTextArea.setBackground(Color.WHITE);
            rightTextArea.add(rightTextPane);



            JScrollPane leftJSP = new JScrollPane(leftTextArea);
            JScrollPane rightJSP = new JScrollPane(rightTextArea);

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftJSP, rightJSP);
            splitPane.setResizeWeight(0.5);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weighty = 1;
            gbc.weightx = 1;
            add(splitPane, gbc);
        }
    }

    private static void changeColor(JTextPane textPane,List<FileUtils.SpecificLineChange> lineChanges,Side side){
        MutableAttributeSet attrs = textPane.getInputAttributes();
        StyledDocument doc = textPane.getStyledDocument();

        String[] lines = textPane.getText().split("\n");
        int offset = 0;

        //Change color of every added + to green
        for(int i = 0; i < lines.length; i++){
            String line = lines[i];
            int space = String.valueOf(i).length() + 4;
            int indexOfPlus = line.indexOf("+");

            if(indexOfPlus >= 0 && indexOfPlus <= space){
                StyleConstants.setBackground(attrs, Color.GREEN);
                doc.setCharacterAttributes(offset + indexOfPlus, 1, attrs, false);
            }
            offset += line.length() + 1;
        }

        offset = 0;

        //Change color of every added - to red
        for(int i = 0; i < lines.length; i++){
            String line = lines[i];
            int space = String.valueOf(i).length() + 4;
            int indexOfPlus = line.indexOf("-");

            if(indexOfPlus >= 0 && indexOfPlus <= space){
                StyleConstants.setBackground(attrs, Color.RED);
                doc.setCharacterAttributes(offset + indexOfPlus, 1, attrs, false);
            }
            offset += line.length() + 1;
        }

        offset = 0;

        //Change color of every added ! to orange
        for(int i = 0; i < lines.length; i++){
            String line = lines[i];
            int space = String.valueOf(i).length() + 4;
            int indexOfPlus = line.indexOf("!");

            if(indexOfPlus >= 0 && indexOfPlus <= space){
                StyleConstants.setBackground(attrs, Color.ORANGE);
                doc.setCharacterAttributes(offset + indexOfPlus, 1, attrs, false);
            }
            offset += line.length() + 1;
        }

        if(lineChanges != null){
            for(FileUtils.SpecificLineChange change : lineChanges){
                if(change.displaySide() == side){
                    offset = 0;
                    
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

        /*frame.invalidate();
        frame.revalidate();
        frame.repaint();*/
    }
}