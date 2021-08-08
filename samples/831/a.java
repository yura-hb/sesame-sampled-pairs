import java.io.*;
import org.eclipse.jdt.core.tests.util.Util;

class LocalVirtualMachine {
    /**
    * Shuts down this target VM.
    * This causes the VM to exit. This operation is ignored
    * if the VM has already shut down.
    *
    * @throws TargetException if the target path could not be cleaned up
    */
    public synchronized void shutDown() throws TargetException {
	if (this.process != null) {
	    this.process.destroy();
	    this.process = null;
	    cleanupTargetPath();
	}
    }

    protected Process process;
    protected String evalTargetPath;

    /**
    * Cleans up this context's target path by removing all the files it contains
    * but leaving the directory.
    * @throws TargetException if the target path could not be cleaned up
    */
    protected void cleanupTargetPath() throws TargetException {
	if (this.evalTargetPath == null)
	    return;
	String targetPath = this.evalTargetPath;
	if (LocalVMLauncher.TARGET_HAS_FILE_SYSTEM) {
	    Util.delete(new File(targetPath, LocalVMLauncher.REGULAR_CLASSPATH_DIRECTORY));
	    Util.delete(new File(targetPath, LocalVMLauncher.BOOT_CLASSPATH_DIRECTORY));
	    File file = new File(targetPath, RuntimeConstants.SUPPORT_ZIP_FILE_NAME);

	    /* workaround pb with Process.exitValue() that returns the process has exited, but it has not free the file yet
	    int count = 10;
	    for (int i = 0; i &lt; count; i++) {
	    	if (file.delete()) {
	    		break;
	    	}
	    	try {
	    		Thread.sleep(count * 100);
	    	} catch (InterruptedException e) {
	    	}
	    }
	    */
	    if (!Util.delete(file)) {
		throw new TargetException("Could not delete " + file.getPath());
	    }
	} else {
	    Util.delete(targetPath);
	}
    }

}

