import java.io.File;

class XSLTC {
    /**
     * Set the destination directory for the translet.
     * The current working directory will be used by default.
     */
    public boolean setDestDirectory(String dstDirName) {
	final File dir = new File(dstDirName);
	if (SecuritySupport.doesFileExist(dir) || dir.mkdirs()) {
	    _destDir = dir;
	    return true;
	} else {
	    _destDir = null;
	    return false;
	}
    }

    private File _destDir = null;

}

