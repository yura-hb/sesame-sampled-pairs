import com.sun.tools.classfile.ClassFile;
import com.sun.tools.classfile.Dependencies.ClassFileError;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

class ClassFileReader implements Closeable {
    /**
     * Returns the ClassFile matching the given binary name
     * or a fully-qualified class name.
     */
    public ClassFile getClassFile(String name) throws IOException {
	if (name.indexOf('.') &gt; 0) {
	    int i = name.lastIndexOf('.');
	    String pathname = name.replace('.', File.separatorChar) + ".class";
	    if (baseFileName.equals(pathname) || baseFileName
		    .equals(pathname.substring(0, i) + "$" + pathname.substring(i + 1, pathname.length()))) {
		return readClassFile(path);
	    }
	} else {
	    if (baseFileName.equals(name.replace('/', File.separatorChar) + ".class")) {
		return readClassFile(path);
	    }
	}
	return null;
    }

    protected final String baseFileName;
    protected final Path path;

    protected ClassFile readClassFile(Path p) throws IOException {
	InputStream is = null;
	try {
	    is = Files.newInputStream(p);
	    return ClassFile.read(is);
	} catch (ConstantPoolException e) {
	    throw new ClassFileError(e);
	} finally {
	    if (is != null) {
		is.close();
	    }
	}
    }

}

