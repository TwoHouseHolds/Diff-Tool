import javax.swing.*;
import java.awt.*;

public class SwingInterface {

    public void start() {
        SwingUtilities.invokeLater(this::createAndShowGUI);
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Swing Interface");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Create the left and right panels
        DirectorySelectionPanel leftPanel = new DirectorySelectionPanel("First Directory:");
        DirectorySelectionPanel rightPanel = new DirectorySelectionPanel("Second Directory:");

        // Create a JSplitPane to hold the left and right panels
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.5);

        // Add the split pane to the frame
        frame.getContentPane().add(splitPane, BorderLayout.CENTER);

        // Add menu to the frame
        addMenu(frame);

        frame.setVisible(true);
    }

    private static class DirectorySelectionPanel extends JPanel {
        public DirectorySelectionPanel(String label1Text) {
            super();
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(new JLabel(label1Text));
            add(new JLabel("Geben Sie einen Pfad an:"));
            JTextArea textArea = new JTextArea();
            textArea.setEditable(false);
            add(new JScrollPane(textArea));
        }
    }


    private void addMenu(JFrame frame) {
        JMenuBar menuBar = new JMenuBar();

        JMenu helpMenu = new JMenu("Help");

        MenuItem guideItem = new MenuItem("Guide",
                "- Select the directories you want to compare.\n" +
                        "- The application will show the differences between the directories.\n" +
                        "- Use the menu to view help or exit the application.", frame, "Help");
        MenuItem aboutItem = new MenuItem("About Us",
                "Developed as part of the Software Project 1 course at Hochschule für Technik Stuttgart.\n" +
                "Contributors: Benedikt Belschner, Colin Traub, Daniel Rodean, Finn Wolf", frame, "About Us");
        helpMenu.add(guideItem);
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        JMenu exitMenu = new JMenu("Exit");
        JMenuItem exitItem = new JMenuItem("Exit Program");
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