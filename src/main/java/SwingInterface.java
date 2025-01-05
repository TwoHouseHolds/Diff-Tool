import javax.swing.*;
import java.awt.*;
import java.io.File;

public class SwingInterface {

    public void start() {
        SwingUtilities.invokeLater(this::createAndShowGUI);
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Swing Oberfläche");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) (screenSize.width * 0.5);
        int height = (int) (screenSize.height * 0.5);
        frame.setSize(width, height);

        DirectorySelectionPanel leftPanel = new DirectorySelectionPanel("Erstes Verzeichnis:");
        DirectorySelectionPanel rightPanel = new DirectorySelectionPanel("Zweites Verzeichnis:");

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.5);
        frame.getContentPane().add(splitPane, BorderLayout.CENTER);
        addMenu(frame);

        frame.setVisible(true);
    }

    private static class DirectorySelectionPanel extends JPanel {
        public DirectorySelectionPanel(String label1Text) {
            super();
            // TODO GridBagLayout
            setLayout(new GridLayout(0, 1));
            add(new JLabel(label1Text));
            add(new JLabel("Bitte geben Sie ein Verzeichnis an:"));

            JTextField textfield = new JTextField();
            textfield.setEditable(true);
            textfield.setPreferredSize(new Dimension(300, getPreferredSize().height));
            add(textfield);


            /*JFileChooser jfc = new JFileChooser();
            jfc.setCurrentDirectory(new File("."));
            jfc.setDialogTitle("Bitte wählen Sie ein Verzeichnis aus");
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            // TODO Button to open dialog
            int result = jfc.showOpenDialog(this);
            if(result == JFileChooser.APPROVE_OPTION){
                File resultDir = jfc.getCurrentDirectory();
                String resultPath = resultDir.getAbsolutePath();
                --resultPath in FileUtils

            }*/
        }
      }

    //TODO translate to german and correct the information in menu
    private void addMenu(JFrame frame) {
        JMenuBar menuBar = new JMenuBar();
        JMenu helpMenu = new JMenu("Help");

        MenuItem guideItem = new MenuItem("Guide", "- Select the directories you want to compare.\n" +
                        "- The application will show the differences between the directories.\n" +
                        "- Use the menu to view help or exit the application.", frame, "Help");
        MenuItem aboutItem = new MenuItem("About Us",
                "Developed as part of the Software Project 1 course at Hochschule für Technik Stuttgart.\n" +
                "Contributors: Benedikt Belschner, Colin Traub, Daniel Rodean, Finn Wolf", frame, "About Us");
        helpMenu.add(guideItem);
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        JMenu exitMenu = new JMenu("Beenden");
        JMenuItem exitItem = new JMenuItem("Programm beenden");
        exitItem.addActionListener(e -> System.exit(0));
        exitMenu.add(exitItem);

        menuBar.add(exitMenu);
        frame.setJMenuBar(menuBar);
    }

    private static class MenuItem extends JMenuItem {
        public MenuItem(String name, String content, JFrame frame, String popUpTitle) {
            super(name);
            addActionListener(e -> JOptionPane.showMessageDialog(frame, content, popUpTitle, JOptionPane.INFORMATION_MESSAGE));
        }
    }
}