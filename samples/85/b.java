import java.util.Set;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import com.sun.source.util.TaskEvent;
import com.sun.tools.javac.api.MultiTaskListener;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.parser.*;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.*;

class JavaCompiler {
    /** Parse contents of file.
     *  @param filename     The name of the file to be parsed.
     */
    public JCTree.JCCompilationUnit parse(JavaFileObject filename) {
	JavaFileObject prev = log.useSource(filename);
	try {
	    JCTree.JCCompilationUnit t = parse(filename, readSource(filename));
	    if (t.endPositions != null)
		log.setEndPosTable(filename, t.endPositions);
	    return t;
	} finally {
	    log.useSource(prev);
	}
    }

    /** The log to be used for error reporting.
     */
    public Log log;
    /** The set of currently compiled inputfiles, needed to ensure
     *  we don't accidentally overwrite an input file when -s is set.
     *  initialized by `compile'.
     */
    protected Set&lt;JavaFileObject&gt; inputFiles = new HashSet&lt;&gt;();
    /** The tree factory module.
     */
    protected TreeMaker make;
    /** Verbose output.
     */
    public boolean verbose;
    /** Broadcasting listener for progress events
     */
    protected MultiTaskListener taskListener;
    public boolean keepComments = false;
    /** Switch: should we store the ending positions?
     */
    public boolean genEndPos;
    /** Factory for parsers.
     */
    protected ParserFactory parserFactory;
    /** Generate code with the LineNumberTable attribute for debugging
     */
    public boolean lineDebugInfo;
    /** Emit plain Java source files rather than class files.
     */
    public boolean sourceOutput;

    /** Try to open input stream with given name.
     *  Report an error if this fails.
     *  @param filename   The file name of the input stream to be opened.
     */
    public CharSequence readSource(JavaFileObject filename) {
	try {
	    inputFiles.add(filename);
	    return filename.getCharContent(false);
	} catch (IOException e) {
	    log.error(Errors.ErrorReadingFile(filename, JavacFileManager.getMessage(e)));
	    return null;
	}
    }

    /** Parse contents of input stream.
     *  @param filename     The name of the file from which input stream comes.
     *  @param content      The characters to be parsed.
     */
    protected JCCompilationUnit parse(JavaFileObject filename, CharSequence content) {
	long msec = now();
	JCCompilationUnit tree = make.TopLevel(List.nil());
	if (content != null) {
	    if (verbose) {
		log.printVerbose("parsing.started", filename);
	    }
	    if (!taskListener.isEmpty()) {
		TaskEvent e = new TaskEvent(TaskEvent.Kind.PARSE, filename);
		taskListener.started(e);
		keepComments = true;
		genEndPos = true;
	    }
	    Parser parser = parserFactory.newParser(content, keepComments(), genEndPos, lineDebugInfo,
		    filename.isNameCompatible("module-info", Kind.SOURCE));
	    tree = parser.parseCompilationUnit();
	    if (verbose) {
		log.printVerbose("parsing.done", Long.toString(elapsed(msec)));
	    }
	}

	tree.sourcefile = filename;

	if (content != null && !taskListener.isEmpty()) {
	    TaskEvent e = new TaskEvent(TaskEvent.Kind.PARSE, tree);
	    taskListener.finished(e);
	}

	return tree;
    }

    private static long now() {
	return System.currentTimeMillis();
    }

    protected boolean keepComments() {
	return keepComments || sourceOutput;
    }

    private static long elapsed(long then) {
	return now() - then;
    }

}

