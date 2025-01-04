import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class SwingInterface {

    public static void start() {
        SwingUtilities.invokeLater(SwingInterface::createAndShowGUI);
    }

    static void createAndShowGUI() {
        JFrame frame = new JFrame("Swing Interface");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Create the left and right panels
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(new JLabel("First Directory:"));
        leftPanel.add(new JLabel("Geben Sie einen Pfad an:"));
        JTextArea leftTextArea = new JTextArea();
        leftTextArea.setEditable(false);
        leftPanel.add(new JScrollPane(leftTextArea));

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(new JLabel("Second Directory:"));
        rightPanel.add(new JLabel("Geben Sie einen Pfad an:"));
        JTextArea rightTextArea =  new JTextArea();
        rightTextArea.setEditable(false);
        rightPanel.add(new JScrollPane(rightTextArea));

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


    private static void addMenu(JFrame frame) {
        JMenuBar menuBar = new JMenuBar();

        JMenu helpMenu = new JMenu("Help");
        JMenuItem guideItem = new JMenuItem("Guide");
        guideItem.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "- Select the directories you want to compare.\n" +
                        "- The application will show the differences between the directories.\n" +
                        "- Use the menu to view help or exit the application.",
                "Help", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(guideItem);

        JMenuItem aboutItem = new JMenuItem("About Us");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                "Developed as part of the Software Project 1 course at Hochschule für Technik Stuttgart.\n" +
                        "Contributors: Benedikt Belschner, Colin Traub, Daniel Rodean, Finn Wolf",
                "About Us", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(helpMenu);

        JMenu exitMenu = new JMenu("Exit");
        JMenuItem exitItem = new JMenuItem("Exit Program");
        exitItem.addActionListener(e -> System.exit(0));
        exitMenu.add(exitItem);

        menuBar.add(exitMenu);

        frame.setJMenuBar(menuBar);
    }
}