import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class SwingInterface {

    //TODO LVUI doesnt resize correctly after going back

    private static final Dimension defaultTextFieldDimension = new Dimension(300, 25);
    private static final FileUtils fileUtils = new FileUtils();
    private final JFrame frame = new JFrame("Swing Oberfläche");
    private final Menu menu = new Menu(frame);
    private final Level1UI level1UI = new Level1UI();
    private Level2UI level2UI = null;
    private Level3UI level3UI = null;

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
        public Menu(JFrame frame) {
            JButton backButton = new JButton("⬅");
            backButton.addActionListener(e -> {
                //noinspection StatementWithEmptyBody
                if (level1UI.isVisible()) {
                } else if (level2UI.isVisible()) {
                    level2UI.setVisible(false);
                    level1UI.setVisible(true);
                } else if (level3UI.isVisible()) {
                    level3UI.setVisible(false);
                    level2UI.setVisible(true);
                }
            });
            add(backButton);

            JButton forewardButton = new JButton("➡");
            forewardButton.addActionListener(e -> {
                //noinspection StatementWithEmptyBody
                if (level3UI != null && level3UI.isVisible()) {
                } else if (level2UI != null && level2UI.isVisible() && level3UI != null) {
                    level2UI.setVisible(false);
                    level3UI.setVisible(true);
                } else if (level1UI.isVisible() && level2UI != null) {
                    level1UI.setVisible(false);
                    level2UI.setVisible(true);
                }
            });
            add(forewardButton);

            JMenu helpMenu = new JMenu("Help");
            MenuItem guideItem = new MenuItem("Guide", """
                    - Select the directories you want to compare.
                    - The application will show the differences between the directories.
                    - Use the menu to view help or exit the application.""", frame, "Help");
            MenuItem aboutItem = new MenuItem("About Us",
                    "Developed as part of the Software Project 1 course at Hochschule für Technik Stuttgart.\n" +
                            "Contributors: Benedikt Belschner, Colin Traub, Daniel Rodean, Finn Wolf", frame, "About Us");

            JMenuItem switchItem = new JMenuItem("In CUI wechseln");
            switchItem.addActionListener(e ->{
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
        private final GridBagConstraints gbc = new GridBagConstraints();

        public Level2UI(List<File> leftFiles, List<File> rightFiles) {
            super(new GridBagLayout());
            setFocusable(false);
            if (leftFiles == null || rightFiles == null) {
                JOptionPane.showMessageDialog(frame, (leftFiles == null ? "Linkes" : "Rechtes") + " Verzeichnis ist leer!", "Fehler", JOptionPane.ERROR_MESSAGE);
                level1UI.setVisible(true);
                return;
            }

            gbc.fill = GridBagConstraints.BOTH;
            gbc.insets = new Insets(1, 1, 1, 1);

            JList<String> leftList = new JList<>(getFormatFileNames(leftFiles, rightFiles, "L"));
            leftList.addListSelectionListener(select -> {
                if (select.getValueIsAdjusting()) {
                    File leftFile = leftFiles.get(leftList.getSelectedIndex());

                    //TODO: (Fixed) Check if the stream can find a File else then open the File only on the left
                    //Fixed -> Sie Code unten
                    File rightFile = null;
                    try {
                        rightFile = rightFiles.stream().filter(f -> f.getName().equals(leftFile.getName())).findFirst().orElseThrow();
                    }catch (NoSuchElementException e){
                        JOptionPane.showMessageDialog(frame, ("Es existiert keine solche Datei im rechten Verzeichnis"), "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
                    if(rightFile == null){
                        //TODO: RightFile später nicht mehr anzeigen oder das Feld frei lassen -> Idee: Boolean oder Enum an LV3UI
                        rightFile = leftFile;
                    }

                    // End of Fix

                    FileUtils.LineResult lr = fileUtils.compareFiles(leftFile, rightFile);
                    level3UI = new Level3UI(lr.left(), lr.right());
                    frame.add(level3UI);
                    level2UI.setVisible(false);
                    level3UI.setVisible(true);
                }
            });

            JList<String> rightList = new JList<>(getFormatFileNames(rightFiles, leftFiles, "R"));
            rightList.addListSelectionListener(select -> {
                if (select.getValueIsAdjusting()) {
                    File rightFile = rightFiles.get(rightList.getSelectedIndex());

                    //TODO: (Fixed) Check if the stream can find a File else then open the File only on the right
                    //Fixed -> Sie Code unten
                    File leftFile = null;
                    try {
                        leftFile = leftFiles.stream().filter(f -> f.getName().equals(rightFile.getName())).findFirst().orElseThrow();
                    }catch (NoSuchElementException e){
                        JOptionPane.showMessageDialog(frame, ("Es existiert keine solche Datei im linken Verzeichnis"), "Info", JOptionPane.INFORMATION_MESSAGE);
                    }

                    if(leftFile == null){
                        //TODO: LeftFile später nicht mehr anzeigen oder das Feld frei lassen -> Idee: Boolean oder Enum an LV3UI
                        leftFile = rightFile;
                    }

                    // End of Fix

                    FileUtils.LineResult lr = fileUtils.compareFiles(leftFile, rightFile);
                    level3UI = new Level3UI(lr.left(), lr.right());
                    frame.add(level3UI);
                    level2UI.setVisible(false);
                    level3UI.setVisible(true);
                }
            });

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftList, rightList);
            splitPane.setResizeWeight(0.5);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weighty = 1;
            gbc.weightx = 1;
            add(splitPane, gbc);
        }

        // variable names for left side, method can still be used for right side
        private static String[] getFormatFileNames(List<File> leftFiles, List<File> rightFiles, String sideInformation) {
            List<String> result = new ArrayList<>();
            for (File leftFile : leftFiles) {
                String leftFileName = leftFile.getName();
                boolean inBoth = rightFiles.stream().anyMatch(f -> f.getName().equals(leftFile.getName()));
                File otherFile = inBoth ? rightFiles.stream().filter(f -> f.getName().equals(leftFile.getName())).findFirst().orElseThrow() : null;
                if (inBoth) {
                    try {
                        boolean identical = Files.mismatch(leftFile.toPath(), otherFile.toPath()) == -1;
                        if (identical) result.add(leftFileName + " (in L&R identisch)");
                        else result.add(leftFileName + " (in L&R verschieden)");
                    } catch (IOException ignored) {
                    }
                } else {
                    result.add(leftFileName + String.format(" (in %s)", sideInformation));
                }
            }
            return result.toArray(new String[0]);
        }
    }

    private final class Level3UI extends JPanel {
        private final GridBagConstraints gbc = new GridBagConstraints();

        public Level3UI(List<String> leftLines, List<String> rightLines) {
            super(new GridBagLayout());
            setFocusable(false);

            JTextArea leftTextArea = new JTextArea();
            leftTextArea.setEditable(false);
            leftLines.forEach(s -> leftTextArea.append(s + "\n"));
            JScrollPane leftJSP = new JScrollPane(leftTextArea);

            JTextArea rightTextArea = new JTextArea();
            rightTextArea.setEditable(false);
            rightLines.forEach(s -> rightTextArea.append(s + "\n"));
            JScrollPane rightJSP = new JScrollPane(rightTextArea);

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftJSP, rightJSP);
            splitPane.setResizeWeight(0.5);
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
        frame.setFocusable(true);
        frame.requestFocusInWindow();
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    //noinspection StatementWithEmptyBody
                    if (level1UI.isVisible()) {
                    } else if (level2UI.isVisible()) {
                        level2UI.setVisible(false);
                        level1UI.setVisible(true);
                    } else if (level3UI.isVisible()) {
                        level3UI.setVisible(false);
                        level2UI.setVisible(true);
                    }
                }
            }
        });
    }
}