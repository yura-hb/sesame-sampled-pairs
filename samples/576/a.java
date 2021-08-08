import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.*;

class VersionCheck {
    /**
     * @return A list containing the information for the discovered dependencies
     */
    public static List&lt;VersionInfo&gt; getVersionInfos() {

	boolean dl4jFound = false;
	boolean datavecFound = false;

	List&lt;VersionInfo&gt; repState = new ArrayList&lt;&gt;();
	for (URI s : listGitPropertiesFiles()) {
	    VersionInfo grs;

	    try {
		grs = new VersionInfo(s);
	    } catch (Exception e) {
		log.debug("Error reading property files for {}", s);
		continue;
	    }
	    repState.add(grs);

	    if (!dl4jFound && DL4J_GROUPID.equalsIgnoreCase(grs.getGroupId())
		    && DL4J_ARTIFACT.equalsIgnoreCase(grs.getArtifactId())) {
		dl4jFound = true;
	    }

	    if (!datavecFound && DATAVEC_GROUPID.equalsIgnoreCase(grs.getGroupId())
		    && DATAVEC_ARTIFACT.equalsIgnoreCase(grs.getArtifactId())) {
		datavecFound = true;
	    }
	}

	if (classExists(ND4J_JBLAS_CLASS)) {
	    //nd4j-jblas is ancient and incompatible
	    log.error("Found incompatible/obsolete backend and version (nd4j-jblas) on classpath. ND4J is unlikely to"
		    + " function correctly with nd4j-jblas on the classpath.");
	}

	if (classExists(CANOVA_CLASS)) {
	    //Canova is anchient and likely to pull in incompatible
	    log.error("Found incompatible/obsolete library Canova on classpath. ND4J is unlikely to"
		    + " function correctly with this library on the classpath.");
	}

	return repState;
    }

    private static final String DL4J_GROUPID = "org.deeplearning4j";
    private static final String DL4J_ARTIFACT = "deeplearning4j-nn";
    private static final String DATAVEC_GROUPID = "org.datavec";
    private static final String DATAVEC_ARTIFACT = "datavec-api";
    private static final String ND4J_JBLAS_CLASS = "org.nd4j.linalg.jblas.JblasBackend";
    private static final String CANOVA_CLASS = "org.canova.api.io.data.DoubleWritable";
    public static final String GIT_PROPERTY_FILE_SUFFIX = "-git.properties";

    /**
     * @return A list of the property files containing the build/version info
     */
    public static List&lt;URI&gt; listGitPropertiesFiles() {
	Enumeration&lt;URL&gt; roots;
	try {
	    roots = VersionCheck.class.getClassLoader().getResources("ai/skymind/");
	} catch (IOException e) {
	    //Should never happen?
	    log.debug("Error listing resources for version check", e);
	    return Collections.emptyList();
	}

	final List&lt;URI&gt; out = new ArrayList&lt;&gt;();
	while (roots.hasMoreElements()) {
	    URL u = roots.nextElement();

	    try {
		URI uri = u.toURI();
		try (FileSystem fileSystem = (uri.getScheme().equals("jar")
			? FileSystems.newFileSystem(uri, Collections.&lt;String, Object&gt;emptyMap())
			: null)) {
		    Path myPath = Paths.get(uri);
		    Files.walkFileTree(myPath, new SimpleFileVisitor&lt;Path&gt;() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
			    URI fileUri = file.toUri();
			    String s = fileUri.toString();
			    if (s.endsWith(GIT_PROPERTY_FILE_SUFFIX)) {
				out.add(fileUri);
			    }
			    return FileVisitResult.CONTINUE;
			}
		    });
		}
	    } catch (Exception e) {
		//log and skip
		log.debug("Error finding/loading version check resources", e);
	    }
	}

	Collections.sort(out); //Equivalent to sorting by groupID and artifactID
	return out;
    }

    private static boolean classExists(String className) {
	try {
	    Class.forName(className);
	    return true;
	} catch (ClassNotFoundException e) {
	    //OK - not found
	}
	return false;
    }

}

