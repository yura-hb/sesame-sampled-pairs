import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.PackageElement;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.StandardJavaFileManager;
import com.sun.source.util.DocTrees;
import com.sun.source.util.JavacTask;
import com.sun.tools.javac.api.JavacTool;

class DocCommentTreeApiTester {
    /**
     * Tests DocTrees.getDocCommentTree(Element e, String relpath) using relative path.
     *
     * @param javaFileName the reference java file
     * @param fileName the relative html file
     * @throws java.lang.Exception ouch
     */
    public void runRelativePathTest(String javaFileName, String fileName, boolean bodyOnly) throws Exception {
	List&lt;File&gt; javaFiles = new ArrayList&lt;&gt;();
	javaFiles.add(new File(testSrc, javaFileName));

	List&lt;File&gt; dirs = new ArrayList&lt;&gt;();
	dirs.add(new File(testSrc));

	try (StandardJavaFileManager fm = javac.getStandardFileManager(null, null, null)) {
	    fm.setLocation(javax.tools.StandardLocation.SOURCE_PATH, dirs);
	    Iterable&lt;? extends JavaFileObject&gt; fos = fm.getJavaFileObjectsFromFiles(javaFiles);

	    final JavacTask t = javac.getTask(null, fm, null, null, null, fos);
	    final DocTrees trees = DocTrees.instance(t);

	    Iterable&lt;? extends Element&gt; elements = t.analyze();

	    Element klass = elements.iterator().next();

	    DocCommentTree dcTree = trees.getDocCommentTree(klass, fileName);

	    if (dcTree == null)
		throw new Error("invalid input: " + fileName);

	    StringWriter sw = new StringWriter();
	    printer.print(dcTree, sw);
	    String found = sw.toString();

	    FileObject htmlFo = fm.getFileForInput(javax.tools.StandardLocation.SOURCE_PATH,
		    t.getElements().getPackageOf(klass).getQualifiedName().toString(), fileName + ".out");

	    String expected = getExpected(htmlFo.openReader(true));
	    astcheck(fileName, expected, found);
	}
    }

    private static final String testSrc = System.getProperty("test.src", ".");
    private static final JavacTool javac = JavacTool.create();
    private static final DocCommentTester.ASTChecker.Printer printer = new DocCommentTester.ASTChecker.Printer();
    private static final String MARKER_START = "EXPECT_START";
    private static final String MARKER_END = "EXPECT_END";
    int pass;
    int fail;

    String getExpected(Reader inrdr) throws IOException {
	BufferedReader rdr = new BufferedReader(inrdr);
	List&lt;String&gt; lines = new ArrayList&lt;&gt;();
	String line = rdr.readLine();
	while (line != null) {
	    lines.add(line);
	    line = rdr.readLine();
	}
	return getExpected(lines);
    }

    void astcheck(String testinfo, String expected, String found) {
	System.err.print("ASTChecker: " + testinfo);
	check0(expected, found);
    }

    String getExpected(List&lt;String&gt; lines) {
	boolean start = false;
	StringWriter sw = new StringWriter();
	PrintWriter out = new PrintWriter(sw);
	for (String line : lines) {
	    if (!start) {
		start = line.startsWith(MARKER_START);
		continue;
	    }
	    if (line.startsWith(MARKER_END)) {
		out.flush();
		return sw.toString();
	    }
	    out.println(line);
	}
	return out.toString() + "Warning: html comment end not found";
    }

    void check0(String expected, String found) {
	if (expected.equals(found)) {
	    pass++;
	    System.err.println(" PASS");
	} else {
	    fail++;
	    System.err.println(" FAILED");
	    System.err.println("Expect:\n" + expected);
	    System.err.println("Found:\n" + found);
	}
    }

}

