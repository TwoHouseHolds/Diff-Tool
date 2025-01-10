package lanterna;

import com.googlecode.lanterna.gui2.WindowListenerAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import utils.Side;

/**
 * Represents the state of the Lanterna CUI as a Singleton class
 * @see com.googlecode.lanterna.gui2.WindowListenerAdapter
 * @see java.io.File
 * @see java.util.ArrayList
 */
public class InterfaceState {
    private static InterfaceState INSTANCE;
    private LanternaState state;
    private File currentLeftFile;
    private File currentRightFile;
    private Side currentSide;
    private List<File> leftDir;
    private List<File> rightDir;
    private List<String> currentDirectorys;
    private WindowListenerAdapter currentListener;

    private InterfaceState(LanternaState state, File currentLeftFile, File currentRightFile, Side currentSide, List<File> leftDir, List<File> rightDir, List<String> currentDirectorys, WindowListenerAdapter currentListener) {
        this.state = state;
        this.currentLeftFile = currentLeftFile;
        this.currentRightFile = currentRightFile;
        this.currentSide = currentSide;
        this.leftDir = leftDir;
        this.rightDir = rightDir;
        this.currentDirectorys = currentDirectorys;
        this.currentListener = currentListener;
    }

    /**
     * Get the current state of the interface
     * @see LanternaState
     * @return The current state of the interface
     */
    public static InterfaceState getInterfaceState() {
        if(INSTANCE == null) {
            INSTANCE = new InterfaceState(LanternaState.DIRECTORYSELECT, null, null, null, new ArrayList<>(), new ArrayList<>(), null, null);
        }

        return INSTANCE;
    }

    public void setState(LanternaState state) {
        this.state = state;
    }

    public void setCurrentLeftFile(File currentLeftFile) {
        this.currentLeftFile = currentLeftFile;
    }

    public void setCurrentRightFile(File currentRightFile) {
        this.currentRightFile = currentRightFile;
    }

    public void setCurrentSide(Side currentSide) {
        this.currentSide = currentSide;
    }

    public void setLeftDir(List<File> leftDir) {
        this.leftDir = leftDir;
    }

    public void setRightDir(List<File> rightDir) {
        this.rightDir = rightDir;
    }

    public void setCurrentDirectorys(List<String> currentDirectorys) {
        this.currentDirectorys = currentDirectorys;
    }

    public void setCurrentListener(WindowListenerAdapter currentListener) {
        this.currentListener = currentListener;
    }

    public LanternaState getState() {
        return state;
    }

    public File getCurrentLeftFile() {
        return currentLeftFile;
    }

    public File getCurrentRightFile() {
        return currentRightFile;
    }

    public Side getCurrentSide() {
        return currentSide;
    }

    public List<File> getLeftDir() {
        return leftDir;
    }

    public List<File> getRightDir() {
        return rightDir;
    }

    public List<String> getCurrentDirectorys() {
        return currentDirectorys;
    }

    public WindowListenerAdapter getCurrentListener() {
        return currentListener;
    }
}