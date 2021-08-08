import java.io.*;

abstract class JavapTester {
    /**
     * Compile the Java source code.
     */
    protected void compileTestFile() {
	String path = javaFile.getPath();
	String params[] = { "-g", path };
	int rc = com.sun.tools.javac.Main.compile(params);
	if (rc != 0)
	    throw new Error("compilation failed. rc=" + rc);
	classFile = new File(path.substring(0, path.length() - 5) + ".class");
    }

    private File javaFile = null;
    private File classFile = null;

}

