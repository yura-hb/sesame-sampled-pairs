import java.io.File;
import java.util.Set;

class FileHandler extends StreamHandler {
    /**
     * Close all the files.
     *
     * @exception  SecurityException  if a security manager exists and if
     *             the caller does not have {@code LoggingPermission("control")}.
     */
    @Override
    public synchronized void close() throws SecurityException {
	super.close();
	// Unlock any lock file.
	if (lockFileName == null) {
	    return;
	}
	try {
	    // Close the lock file channel (which also will free any locks)
	    lockFileChannel.close();
	} catch (Exception ex) {
	    // Problems closing the stream.  Punt.
	}
	synchronized (locks) {
	    locks.remove(lockFileName);
	}
	new File(lockFileName).delete();
	lockFileName = null;
	lockFileChannel = null;
    }

    private String lockFileName;
    private FileChannel lockFileChannel;
    private static final Set&lt;String&gt; locks = new HashSet&lt;&gt;();

}

