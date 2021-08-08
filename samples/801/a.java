import java.io.File;

class CrashReportingUtil {
    /**
     * Method that can be use to customize the output directory for memory crash reporting. By default,
     * the current working directory will be used.
     *
     * @param rootDir Root directory to use for crash reporting. If null is passed, the current working directory
     *                will be used
     */
    public static void crashDumpOutputDirectory(File rootDir) {
	if (rootDir == null) {
	    String userDir = System.getProperty("user.dir");
	    if (userDir == null) {
		userDir = "";
	    }
	    crashDumpRootDirectory = new File(userDir);
	    return;
	}
	crashDumpRootDirectory = rootDir;
    }

    @Getter
    private static File crashDumpRootDirectory;

}

