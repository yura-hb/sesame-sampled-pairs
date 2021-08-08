import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ServiceLoader;

class BatchTestUtils {
    /**
     * Load Eclipse compiler and create temporary directories on disk
     */
    public static void init() {
	_tmpFolder = System.getProperty("java.io.tmpdir");
	if (_tmpFolder.endsWith(File.separator)) {
	    _tmpFolder += "eclipse-temp";
	} else {
	    _tmpFolder += (File.separator + "eclipse-temp");
	}
	_tmpBinFolderName = _tmpFolder + File.separator + "bin";
	_tmpBinDir = new File(_tmpBinFolderName);
	BatchTestUtils.deleteTree(_tmpBinDir); // remove existing contents
	_tmpBinDir.mkdirs();
	assert _tmpBinDir.exists() : "couldn't mkdirs " + _tmpBinFolderName;

	_tmpGenFolderName = _tmpFolder + File.separator + "gen-src";
	_tmpGenDir = new File(_tmpGenFolderName);
	BatchTestUtils.deleteTree(_tmpGenDir); // remove existing contents
	_tmpGenDir.mkdirs();
	assert _tmpGenDir.exists() : "couldn't mkdirs " + _tmpGenFolderName;

	_tmpSrcFolderName = _tmpFolder + File.separator + "src";
	_tmpSrcDir = new File(_tmpSrcFolderName);
	BatchTestUtils.deleteTree(_tmpSrcDir); // remove existing contents
	_tmpSrcDir.mkdirs();
	assert _tmpSrcDir.exists() : "couldn't mkdirs " + _tmpSrcFolderName;

	try {
	    _processorJarPath = setupProcessorJar(PROCESSOR_JAR_NAME, _tmpFolder);
	    _jls8ProcessorJarPath = setupProcessorJar(JLS8_PROCESSOR_JAR_NAME, _tmpFolder);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	junit.framework.TestCase.assertNotNull("No processor jar path set", _processorJarPath);
	File processorJar = new File(_processorJarPath);
	junit.framework.TestCase.assertTrue("Couldn't find processor jar at " + processorJar.getAbsolutePath(),
		processorJar.exists());

	ServiceLoader&lt;JavaCompiler&gt; javaCompilerLoader = ServiceLoader.load(JavaCompiler.class,
		BatchTestUtils.class.getClassLoader());
	Class&lt;?&gt; c = null;
	try {
	    c = Class.forName("org.eclipse.jdt.internal.compiler.tool.EclipseCompiler");
	} catch (ClassNotFoundException e) {
	    // ignore
	}
	if (c == null) {
	    junit.framework.TestCase.assertTrue("Eclipse compiler is not available", false);
	}
	for (JavaCompiler javaCompiler : javaCompilerLoader) {
	    if (c.isInstance(javaCompiler)) {
		_eclipseCompiler = javaCompiler;
	    }
	}
	junit.framework.TestCase.assertNotNull("No Eclipse compiler found", _eclipseCompiler);
    }

    private static String _tmpFolder;
    private static String _tmpBinFolderName;
    private static File _tmpBinDir;
    public static String _tmpGenFolderName;
    private static File _tmpGenDir;
    private static String _tmpSrcFolderName;
    private static File _tmpSrcDir;
    public static String _processorJarPath;
    private static final String PROCESSOR_JAR_NAME = "lib/apttestprocessors.jar";
    public static String _jls8ProcessorJarPath;
    private static final String JLS8_PROCESSOR_JAR_NAME = "lib/apttestprocessors8.jar";
    private static JavaCompiler _eclipseCompiler;

    /**
     * Recursively delete the contents of a directory, including any subdirectories.
     * This is not optimized to handle very large or deep directory trees efficiently.
     * @param f is either a normal file (which will be deleted) or a directory
     * (which will be emptied and then deleted).
     */
    public static void deleteTree(File f) {
	if (null == f) {
	    return;
	}
	File[] children = f.listFiles();
	if (null != children) {
	    // if f has any children, (recursively) delete them
	    for (File child : children) {
		deleteTree(child);
	    }
	}
	// At this point f is either a normal file or an empty directory
	f.delete();
    }

    public static String setupProcessorJar(String processorJar, String tmpDir) throws IOException {
	File libDir = new File(getPluginDirectoryPath());
	File libFile = new File(libDir, processorJar);
	File destinationDir = new File(tmpDir);
	File destinationFile = new File(destinationDir, processorJar);
	copyResource(libFile, destinationFile);
	return destinationFile.getCanonicalPath();
    }

    protected static String getPluginDirectoryPath() {
	try {
	    if (Platform.isRunning()) {
		URL platformURL = Platform.getBundle("org.eclipse.jdt.compiler.apt.tests").getEntry("/");
		return new File(FileLocator.toFileURL(platformURL).getFile()).getAbsolutePath();
	    }
	    return new File(System.getProperty("user.dir")).getAbsolutePath();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return null;
    }

    /**
     * Copy a file from one location to another, unless the destination file already exists and has
     * the same timestamp and file size. Create the destination location if necessary. Convert line
     * delimiters according to {@link #shouldConvertToIndependentLineDelimiter(File)}.
     *
     * @param src
     *            the full path to the resource location.
     * @param destFolder
     *            the full path to the destination location.
     * @throws IOException
     */
    public static void copyResource(File src, File dest) throws IOException {
	if (dest.exists() && src.lastModified() &lt; dest.lastModified() && src.length() == dest.length()) {
	    return;
	}

	// read source bytes
	byte[] srcBytes = null;
	srcBytes = read(src);

	if (shouldConvertToIndependentLineDelimiter(src)) {
	    String contents = new String(srcBytes);
	    contents = TestUtils.convertToIndependentLineDelimiter(contents);
	    srcBytes = contents.getBytes();
	}
	writeFile(dest, srcBytes);
    }

    public static byte[] read(java.io.File file) throws java.io.IOException {
	int fileLength;
	byte[] fileBytes = new byte[fileLength = (int) file.length()];
	java.io.FileInputStream stream = null;
	try {
	    stream = new java.io.FileInputStream(file);
	    int bytesRead = 0;
	    int lastReadSize = 0;
	    while ((lastReadSize != -1) && (bytesRead != fileLength)) {
		lastReadSize = stream.read(fileBytes, bytesRead, fileLength - bytesRead);
		bytesRead += lastReadSize;
	    }
	} finally {
	    if (stream != null) {
		stream.close();
	    }
	}
	return fileBytes;
    }

    /**
     * @return true if this file's end-of-line delimiters should be replaced with
     * a platform-independent value, e.g. for compilation.
     */
    public static boolean shouldConvertToIndependentLineDelimiter(File file) {
	return file.getName().endsWith(".java");
    }

    public static void writeFile(File dest, byte[] srcBytes) throws IOException {

	File destFolder = dest.getParentFile();
	if (!destFolder.exists()) {
	    if (!destFolder.mkdirs()) {
		throw new IOException("Unable to create directory " + destFolder);
	    }
	}
	// write bytes to dest
	FileOutputStream out = null;
	try {
	    out = new FileOutputStream(dest);
	    out.write(srcBytes);
	    out.flush();
	} finally {
	    if (out != null) {
		out.close();
	    }
	}
    }

}

