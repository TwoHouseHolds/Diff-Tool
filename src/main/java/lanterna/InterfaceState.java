package lanterna;

import algorithms.FileUtils;
import com.googlecode.lanterna.gui2.WindowListenerAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import utils.Side;
import utils.SortType;

/**
 * Represents the state of the Lanterna CUI as a Singleton class
 * @see com.googlecode.lanterna.gui2.WindowListenerAdapter
 * @see java.io.File
 * @see java.util.ArrayList
 */
public class InterfaceState {
    private static InterfaceState INSTANCE;
    private FileUtils.LineResult currentLineResult;
    private LanternaState state;
    private File currentLeftFile;
    private File currentRightFile;
    private Side currentSide;
    private List<File> leftDir;
    private List<File> rightDir;
    private List<String> currentDirectorys;
    private List<String> leftLines;
    private List<String> rightLines;
    private WindowListenerAdapter currentListener;
    private SortType sortTypeLeft;
    private SortType sortTypeRight;
    private boolean sortLeftReversed = false;
    private boolean sortRightReversed = false;

    private InterfaceState(LanternaState state, File currentLeftFile, File currentRightFile, Side currentSide, List<File> leftDir, List<File> rightDir, List<String> currentDirectorys, WindowListenerAdapter currentListener, SortType sortTypeLeft, SortType sortTypeRight) {
        this.state = state;
        this.currentLeftFile = currentLeftFile;
        this.currentRightFile = currentRightFile;
        this.currentSide = currentSide;
        this.leftDir = leftDir;
        this.rightDir = rightDir;
        this.currentDirectorys = currentDirectorys;
        this.currentListener = currentListener;
        this.sortTypeLeft = sortTypeLeft;
        this.sortTypeRight = sortTypeRight;
    }

    /**
     * Get the current state of the interface
     * @see LanternaState
     * @return The current state of the interface
     */
    public static InterfaceState getInterfaceState() {
        if(INSTANCE == null) {
            INSTANCE = new InterfaceState(LanternaState.DIRECTORYSELECT, null, null, null, new ArrayList<>(), new ArrayList<>(), null, null, SortType.UNSORTED, SortType.UNSORTED);
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

    public int getSortTypeLeft() {
        return switch (sortTypeLeft) {
            case UNSORTED -> 0;
            case ALPHABETICAL -> 1;
            case SIZE -> 2;
            case DATE -> 3;
        };
    }

    public int getSortTypeRight() {
        return switch (sortTypeRight) {
            case UNSORTED -> 0;
            case ALPHABETICAL -> 1;
            case SIZE -> 2;
            case DATE -> 3;
        };
    }

    public void setSortTypeLeft(SortType sortTypeLeft) {
        this.sortTypeLeft = sortTypeLeft;
    }

    public void setSortTypeRight(SortType sortTypeRight) {
        this.sortTypeRight = sortTypeRight;
    }

    public boolean isSortLeftReversed() {
        return sortLeftReversed;
    }

    public boolean isSortRightReversed() {
        return sortRightReversed;
    }

    public void setSortLeftReversed(boolean sortLeftReversed) {
        this.sortLeftReversed = sortLeftReversed;
    }

    public void setSortRightReversed(boolean sortRightReversed) {
        this.sortRightReversed = sortRightReversed;
    }

    public FileUtils.LineResult getCurrentLineResult() {
        return currentLineResult;
    }

    public void setCurrentLineResult(FileUtils.LineResult currentLineResult) {
        this.currentLineResult = currentLineResult;
    }

    public void setLeftLines(List<String> leftLines) {
        this.leftLines = leftLines;
    }

    public void setRightLines(List<String> rightLines) {
        this.rightLines = rightLines;
    }

    public List<String> getLeftLines() {
        return leftLines;
    }

    public List<String> getRightLines() {
        return rightLines;
    }
}