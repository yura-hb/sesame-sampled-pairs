import java.util.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import com.sun.source.util.DocTrees;
import jdk.javadoc.internal.doclets.toolkit.BaseConfiguration;
import jdk.javadoc.internal.doclets.toolkit.CommentUtils.DocCommentDuo;
import jdk.javadoc.internal.doclets.toolkit.WorkArounds;
import static javax.lang.model.element.ElementKind.*;

class Utils {
    /**
     * Retrieves the doc comments for a given element.
     * @param element
     * @return DocCommentTree for the Element
     */
    public DocCommentTree getDocCommentTree0(Element element) {

	DocCommentDuo duo = null;

	ElementKind kind = element.getKind();
	if (kind == ElementKind.PACKAGE || kind == ElementKind.OTHER) {
	    duo = dcTreeCache.get(element); // local cache
	    if (!isValidDuo(duo) && kind == ElementKind.PACKAGE) {
		// package-info.java
		duo = getDocCommentTuple(element);
	    }
	    if (!isValidDuo(duo)) {
		// package.html or overview.html
		duo = configuration.cmtUtils.getHtmlCommentDuo(element); // html source
	    }
	} else {
	    duo = configuration.cmtUtils.getSyntheticCommentDuo(element);
	    if (!isValidDuo(duo)) {
		duo = dcTreeCache.get(element); // local cache
	    }
	    if (!isValidDuo(duo)) {
		duo = getDocCommentTuple(element); // get the real mccoy
	    }
	}

	DocCommentTree docCommentTree = isValidDuo(duo) ? duo.dcTree : null;
	TreePath path = isValidDuo(duo) ? duo.treePath : null;
	if (!dcTreeCache.containsKey(element)) {
	    if (docCommentTree != null && path != null) {
		if (!configuration.isAllowScriptInComments()) {
		    try {
			javaScriptScanner.scan(docCommentTree, path, p -&gt; {
			    throw new JavaScriptScanner.Fault();
			});
		    } catch (JavaScriptScanner.Fault jsf) {
			String text = configuration.getText("doclet.JavaScript_in_comment");
			throw new UncheckedDocletException(new SimpleDocletException(text, jsf));
		    }
		}
		configuration.workArounds.runDocLint(path);
	    }
	    dcTreeCache.put(element, duo);
	}
	return docCommentTree;
    }

    private final Map&lt;Element, DocCommentDuo&gt; dcTreeCache = new LinkedHashMap&lt;&gt;();
    public final BaseConfiguration configuration;
    public final JavaScriptScanner javaScriptScanner;
    public final DocTrees docTrees;

    boolean isValidDuo(DocCommentDuo duo) {
	return duo != null && duo.dcTree != null;
    }

    private DocCommentDuo getDocCommentTuple(Element element) {
	// prevent nasty things downstream with overview element
	if (element.getKind() != ElementKind.OTHER) {
	    TreePath path = getTreePath(element);
	    if (path != null) {
		DocCommentTree docCommentTree = docTrees.getDocCommentTree(path);
		return new DocCommentDuo(path, docCommentTree);
	    }
	}
	return null;
    }

    /**
     * Gets a TreePath for an Element. Note this method is called very
     * frequently, care must be taken to ensure this method is lithe
     * and efficient.
     * @param e an Element
     * @return TreePath
     */
    public TreePath getTreePath(Element e) {
	DocCommentDuo duo = dcTreeCache.get(e);
	if (isValidDuo(duo) && duo.treePath != null) {
	    return duo.treePath;
	}
	duo = configuration.cmtUtils.getSyntheticCommentDuo(e);
	if (isValidDuo(duo) && duo.treePath != null) {
	    return duo.treePath;
	}
	Map&lt;Element, TreePath&gt; elementToTreePath = configuration.workArounds.getElementToTreePath();
	TreePath path = elementToTreePath.get(e);
	if (path != null || elementToTreePath.containsKey(e)) {
	    // expedite the path and one that is a null
	    return path;
	}
	return elementToTreePath.computeIfAbsent(e, docTrees::getPath);
    }

}

