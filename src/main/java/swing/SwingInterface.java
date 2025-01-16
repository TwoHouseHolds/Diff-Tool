package swing;

import algorithms.FileUtils;
import lanterna.LanternaInterface;
import utils.Side;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        private static final JMenu sortMenu = new JMenu("Sortier Optionen");

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
                    if(level2UI.isVisible()){
                        sortMenu.setVisible(true);
                    }
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

            /// /////////////////////////// SORT MENU /////////////////////////////
            //TODO implement sorting
            JMenuItem sortByName = new JMenuItem("Alphabetisch");
            JMenuItem sortBySize = new JMenuItem("Größe");
            JMenuItem sortByDate = new JMenuItem("Datum");
            sortByName.addActionListener(e -> {

            });
            sortBySize.addActionListener(e -> {

            });
            sortByDate.addActionListener(e -> {

            });
            sortMenu.add(sortByName);
            sortMenu.add(sortBySize);
            sortMenu.add(sortByDate);
            add(sortMenu);
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
            Menu.sortMenu.setVisible(false);
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
            Menu.sortMenu.setVisible(true);
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

            JComboBox<String> leftComboBox = new JComboBox<>(new String[]{"Unsortiert", "Alphabetisch"});
            JComboBox<String> rightComboBox = new JComboBox<>(new String[]{"Unsortiert", "Alphabetisch"});

            JPanel left = new JPanel(new BorderLayout());
            left.add(rightComboBox, BorderLayout.NORTH);
            left.add(leftScrollPane, BorderLayout.CENTER);

            JPanel right = new JPanel(new BorderLayout());
            right.add(leftComboBox, BorderLayout.NORTH);
            right.add(rightScrollPane, BorderLayout.CENTER);

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
                        if (rightFile.isEmpty()) level3UI = new Level3UI(fileUtils.readFile(leftFile), null);
                        else {
                            FileUtils.LineResult lr = fileUtils.compareFiles(leftFile, rightFile.get());
                            level3UI = new Level3UI(lr.left(), lr.right());
                        }
                    } else { // called from right side
                        if (rightFile.isEmpty()) level3UI = new Level3UI(null, fileUtils.readFile(leftFile));
                        else {
                            FileUtils.LineResult lr = fileUtils.compareFiles(rightFile.get(), leftFile);
                            level3UI = new Level3UI(lr.left(), lr.right());
                        }
                    }
                    frame.add(level3UI);
                    changeActivePanelFromTo(level2UI, level3UI);
                }
            });
        }

    }

    private static final class Level3UI extends JPanel {

        public Level3UI(List<String> leftLines, List<String> rightLines) {
            super(new GridBagLayout());
            setFocusable(false);

            JTextArea leftTextArea = new JTextArea();
            leftTextArea.setEditable(false);
            JTextArea rightTextArea = new JTextArea();
            rightTextArea.setEditable(false);

            if (leftLines != null) leftLines.forEach(s -> leftTextArea.append(s + "\n"));
            if (rightLines != null) rightLines.forEach(s -> rightTextArea.append(s + "\n"));

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

        if(newPanel == level2UI){
            Menu.sortMenu.setVisible(true);
        }else{
            Menu.sortMenu.setVisible(false);
        }
        /*frame.invalidate();
        frame.revalidate();
        frame.repaint();*/
    }
}