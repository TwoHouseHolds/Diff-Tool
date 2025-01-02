import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a directory and its files
 *
 * @see java.io.File
 * @see java.nio.file.Path
 * @see java.util.List
 */
public class Directory {

    private final List<File> files = new ArrayList<>();
    private final Path path;

    /**
     * "Create" a new directory
     * @param path Path of the directory
     * @see java.nio.file.Path
     */
    public Directory(Path path) {
        this.path = path;
        //Get all files in the directory
        File folder = new File(path.toString());
        File[] listOfFiles = folder.listFiles();
        //Add all files to the list
        if(listOfFiles == null) {
            return;
        }
        for (File file : listOfFiles) {
            if (file.isFile()) {
                files.add(file);
            }
        }
    }

    /**
     * Get the files in the directory
     * @return List of files in the directory or null if the directory is empty or does not exist
     * @see java.io.File
     */
    public List<File> getFiles() {
        if(files.isEmpty()) {
            return null;
        }
        return files;
    }

    /**
     * Get the path of the directory
     * @return Path of the directory
     * @see java.nio.file.Path
     * @noinspection unused
     */
    public Path getPath() {
        return path;
    }
}
