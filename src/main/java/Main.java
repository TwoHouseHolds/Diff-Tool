import gnu.getopt.Getopt;

import java.awt.*;

public class Main {
    public static void main(String[] args) {
        Getopt options = new Getopt("JayWinDiff", args, "wW");
        boolean withGui = false;
        boolean withoutGui = false;
        switch(options.getopt()) {
            case 'w':
                if ( withoutGui ) {
                    System.err.println("JayWinDiff: Options -w and -W are mutually exclusive.");
                }
                else if ( GraphicsEnvironment.isHeadless() ) {
                    System.err.println("JayWinDiff: Environment is headless.");
                }
                withGui = true;
                break;
            case 'W':
                if ( withGui ) {
                    System.err.println("JayWinDiff: Options -w and -W are mutually exclusive.");
                }
                withoutGui = true;
                break;
            default:
                break;
        }
        if (!(withGui || withoutGui)) {
            withoutGui = GraphicsEnvironment.isHeadless();
            withGui = !withoutGui;
        }

        if (withGui) {
            /*
            Swing gibts später meine Freunde
            EventQueue.invokeLater(() -> {
                JayWinDiff jwd = new JayWinDiff();
                jwd.setVisible(true);
            });
            */
        } else {
            LanternaInterface lanternaInterface = new LanternaInterface();
            lanternaInterface.start();
        }

    }
}

