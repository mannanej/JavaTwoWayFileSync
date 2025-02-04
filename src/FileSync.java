// This is where it all begins...
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.StandardCopyOption;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.FileVisitResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/*
 * This is the class for FileSync, which takes two folders and 2-way syncs all files and subfolders
 * 
 * @author: Eddie Mannan
 */
public class FileSync {

    public Path folderAPath;
    public Path folderBPath;

    /*
     * This is the constructor for the File Syncer. Just inits some vars
     */
    public FileSync() {

        // Replace these two file paths to whichever file paths you need. SHOULD be all
        // you need to change
        this.folderAPath = Paths.get("E:\\Folder A");
        this.folderBPath = Paths.get("C:\\Users\\Eddie\\Documents\\Folder B");
    }

    /*
     * This method uses the .walkFileTree to visit all files in the specified tree
     * and mark them as visited
     */
    public Set<Path> getFilesInFolder(Path folder) throws IOException {

        Set<Path> filePaths = new HashSet<>();

        Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                filePaths.add(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        return filePaths;
    }

    /*
     * This method creates any needed parent directories for subfolders and copies
     * all files
     */
    public void copyFile(Path source, Path dest) {

        try {
            Files.createDirectories(dest.getParent());
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Copied: " + source);
        } catch (IOException e) {
            System.out.println("ERROR: Copy file failed | " + e.getMessage());
        }
    }

    /*
     * This method checks that our provided folders exist, creates sets to sort the
     * files in our folders and starts to copy
     */
    public void runProgram() {

        // Check that both folders exist
        if (!Files.exists(this.folderAPath)) {
            System.err.println("ERROR: FolderA with file path '" + this.folderAPath + "' does not exist!");
            return;
        }
        if (!Files.exists(this.folderBPath)) {
            System.err.println("ERROR: FolderB with file path '" + this.folderBPath + "' does not exist!");
            return;
        }

        try {
            // Create sets of all files in root folders
            Set<Path> filesInFolderA = this.getFilesInFolder(this.folderAPath);
            Set<Path> filesInFolderB = this.getFilesInFolder(this.folderBPath);

            // Find files in FolderA that aren't in FolderB
            for (Path fileA : filesInFolderA) {
                Path relativePathA = this.folderAPath.relativize(fileA);
                Path correspondingFileInB = this.folderBPath.resolve(relativePathA);
                if (!filesInFolderB.contains(fileA)) {
                    this.copyFile(fileA, correspondingFileInB);
                }
            }

            // Find files in FolderB that aren't in FolderA
            for (Path fileB : filesInFolderB) {
                Path relativePathB = this.folderBPath.relativize(fileB);
                Path correspondingFileInA = this.folderAPath.resolve(relativePathB);
                if (!filesInFolderA.contains(fileB)) {
                    this.copyFile(fileB, correspondingFileInA);
                }
            }
        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
        }
        return;
    }

    /*
     * This is the main method, which will get the ball rolling
     */
    public static void main(String args[]) {

        System.out.println("SYNC BEGIN");
        long startTime = System.nanoTime();

        FileSync sync = new FileSync();
        sync.runProgram();

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000000; // Convert nanoseconds to seconds
        System.out.println("SYNC COMPLETE! Duration: " + duration + " seconds");
    }
}