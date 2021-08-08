import java.io.File;
import java.util.Collections;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

class SJFM_GetFileObjects extends SJFM_TestBase {
    /**
     * Compiles a set of files.
     *
     * @param files the files to be compiled.
     * @throws IOException
     */
    void compile(Iterable&lt;? extends JavaFileObject&gt; files) throws IOException {
	String name = "compile" + (compileCount++);
	try (StandardJavaFileManager fm = comp.getStandardFileManager(null, null, null)) {
	    File f = new File(name);
	    f.mkdirs();
	    // use setLocation(Iterable&lt;File&gt;) to avoid relying on setLocationFromPaths
	    fm.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(f));
	    boolean ok = comp.getTask(null, fm, null, null, null, files).call();
	    if (!ok)
		error(name + ": compilation failed");
	}
    }

    int compileCount;

}

