import java.util.Set;
import com.sun.source.util.TaskEvent;
import com.sun.tools.javac.api.MultiTaskListener;
import com.sun.tools.javac.comp.*;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.*;

class JavaCompiler {
    /**
     * Enter the symbols found in a list of parse trees.
     * As a side-effect, this puts elements on the "todo" list.
     * Also stores a list of all top level classes in rootClasses.
     */
    public List&lt;JCCompilationUnit&gt; enterTrees(List&lt;JCCompilationUnit&gt; roots) {
	//enter symbols for all files
	if (!taskListener.isEmpty()) {
	    for (JCCompilationUnit unit : roots) {
		TaskEvent e = new TaskEvent(TaskEvent.Kind.ENTER, unit);
		taskListener.started(e);
	    }
	}

	enter.main(roots);

	enterDone();

	if (!taskListener.isEmpty()) {
	    for (JCCompilationUnit unit : roots) {
		TaskEvent e = new TaskEvent(TaskEvent.Kind.ENTER, unit);
		taskListener.finished(e);
	    }
	}

	// If generating source, or if tracking public apis,
	// then remember the classes declared in
	// the original compilation units listed on the command line.
	if (sourceOutput) {
	    ListBuffer&lt;JCClassDecl&gt; cdefs = new ListBuffer&lt;&gt;();
	    for (JCCompilationUnit unit : roots) {
		for (List&lt;JCTree&gt; defs = unit.defs; defs.nonEmpty(); defs = defs.tail) {
		    if (defs.head instanceof JCClassDecl)
			cdefs.append((JCClassDecl) defs.head);
		}
	    }
	    rootClasses = cdefs.toList();
	}

	// Ensure the input files have been recorded. Although this is normally
	// done by readSource, it may not have been done if the trees were read
	// in a prior round of annotation processing, and the trees have been
	// cleaned and are being reused.
	for (JCCompilationUnit unit : roots) {
	    inputFiles.add(unit.sourcefile);
	}

	return roots;
    }

    /** Broadcasting listener for progress events
     */
    protected MultiTaskListener taskListener;
    /** The module for the symbol table entry phases.
     */
    protected Enter enter;
    /** Emit plain Java source files rather than class files.
     */
    public boolean sourceOutput;
    /**
     * The list of classes explicitly supplied on the command line for compilation.
     * Not always populated.
     */
    private List&lt;JCClassDecl&gt; rootClasses;
    /** The set of currently compiled inputfiles, needed to ensure
     *  we don't accidentally overwrite an input file when -s is set.
     *  initialized by `compile'.
     */
    protected Set&lt;JavaFileObject&gt; inputFiles = new HashSet&lt;&gt;();
    private boolean enterDone;
    /** The annotation annotator.
     */
    protected Annotate annotate;

    public void enterDone() {
	enterDone = true;
	annotate.enterDone();
    }

}

