package gitlet;

import java.io.File;
import java.util.TreeMap;

import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  TODO: does at a high level.
 *
 *  @author Xiaoli Li
 */
public class Repository {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** Store versioned files of my working directory. File name is its SHA1 ID. */
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    /** Store pointer (the latest commit) of each branch. File name is branch name. */
    public static final File POINTER_OF_BRANCH_DIR = join(GITLET_DIR, "refs", "heads");
    /**
     * Store history commits of each branch.
     * Subfolder name is branch name.
     * Inside the subfolder, each commit is saved as a single object with SHA1 ID as its name.
     * */
    public static final File COMMITS_OF_BRANCH_DIR = join(GITLET_DIR, "logs", "refs", "heads");

    public static void initCommand() {
        if (GITLET_DIR.exists()) {
            message("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();
        OBJECTS_DIR.mkdir();

        /** Initialize the branch as master branch. */
        String branch = "master";

        File commitsDir = join(COMMITS_OF_BRANCH_DIR, branch);
        commitsDir.mkdirs();
        /** Make the initial commit and generate SHA1 of this commit. */
        Commit initialCommit = new Commit();
        String initialCommitID = sha1(serialize(initialCommit));
        /** Save the commit to corresponding branch together with its ID. */
        initialCommit.saveCommit(initialCommitID, branch);

        /** The master branch now points to initial commit. */
        POINTER_OF_BRANCH_DIR.mkdirs();
        File masterBranchPointer = join(POINTER_OF_BRANCH_DIR, branch);
        writeContents(masterBranchPointer, initialCommitID);

        /** headPointer indicates the current branch. */
        // TODO: need to figure out how to store it
        File headPointer = join(GITLET_DIR, "HEAD");

        /** Create a file used as staging area for later. */
        File index = join(GITLET_DIR, "index");
        TreeMap<String, String> stagingArea = new TreeMap<>();
        writeObject(index, stagingArea);
    }

    public static void addCommand(String fileName) {
        File f = join(CWD, fileName);
        if (!f.exists()) {
            message("File does not exist.");
            System.exit(0);
        }
        byte[] fContents = readContents(f);
        String fSHA1 = sha1(fContents);
        System.out.println("Should print SHA1 of the file content in below. ");

        /** Get info from the staging area. */
        File index = join(GITLET_DIR, "index");
        TreeMap stagingArea = readObject(index, TreeMap.class);
        /** Get info from the parent commit. */
        TreeMap<String, String> parentFilesMapping = Commit.getParentCommit("master").getFilesMapping();

        /**
         * If current working version of the file is the same as the version in parent commit,
         * the remove method will remove the file from staging area if it exists.
         * */
        if (fSHA1.equals(parentFilesMapping.get(fileName))){
            if (stagingArea.remove(fileName) != null) {
                writeObject(index, stagingArea);
            }
        }
        /** Following codes run if current working version of the file is different thant parent commit. */
        if (!stagingArea.containsKey(fileName) || fSHA1.equals(stagingArea.get(fileName))) {
            stagingArea.put(fileName, fSHA1);
            writeObject(index, stagingArea);
            File object = join(OBJECTS_DIR, fSHA1);
            writeContents(object, fContents);
        }
    }


    //public static void commitCommand(String message, Commit parent) {
        // Read from my computer the head commit object and the staging area
        // Clone the HEAD commit
        // Modify its message and timestamp according to user input
        // Use the staging area in order to modify the files tracked by the new commit
        // Write back any new object made or any modified objects read earlier

    //}

    public static void logCommand() {
        // TODO: print logs of all the commits
    }

}


