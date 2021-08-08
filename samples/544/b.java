import java.nio.file.Path;
import java.nio.file.Paths;
import org.graalvm.compiler.serviceprovider.GraalServices;

class DiagnosticsOutputDirectory {
    /**
     * Gets the path of the directory to be created.
     *
     * Subclasses can override this to determine how the path name is created.
     *
     * @return the path to be created
     */
    protected String createPath() {
	Path baseDir;
	try {
	    baseDir = DebugOptions.getDumpDirectory(options);
	} catch (IOException e) {
	    // Default to current directory if there was a problem creating the
	    // directory specified by the DumpPath option.
	    baseDir = Paths.get(".");
	}
	return baseDir.resolve("graal_diagnostics_" + GraalServices.getExecutionID()).toAbsolutePath().toString();
    }

    private final OptionValues options;

}

