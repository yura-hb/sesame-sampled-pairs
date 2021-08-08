import java.io.File;
import java.io.IOException;

class Files {
    /**
    * Creates any necessary but nonexistent parent directories of the specified file. Note that if
    * this operation fails it may have succeeded in creating some (but not all) of the necessary
    * parent directories.
    *
    * @throws IOException if an I/O error occurs, or if any necessary but nonexistent parent
    *     directories of the specified file could not be created.
    * @since 4.0
    */
    public static void createParentDirs(File file) throws IOException {
	checkNotNull(file);
	File parent = file.getCanonicalFile().getParentFile();
	if (parent == null) {
	    /*
	     * The given directory is a filesystem root. All zero of its ancestors exist. This doesn't
	     * mean that the root itself exists -- consider x:\ on a Windows machine without such a drive
	     * -- or even that the caller can create it, but this method makes no such guarantees even for
	     * non-root files.
	     */
	    return;
	}
	parent.mkdirs();
	if (!parent.isDirectory()) {
	    throw new IOException("Unable to create parent directories of " + file);
	}
    }

}

