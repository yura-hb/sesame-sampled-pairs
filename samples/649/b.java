import java.util.Iterator;
import java.util.List;

class ASTMatcher {
    /**
     * Returns whether the given lists of AST nodes match pair wise according
     * to &lt;code&gt;ASTNode.subtreeMatch&lt;/code&gt;.
     * &lt;p&gt;
     * Note that this is a convenience method, useful for writing recursive
     * subtree matchers.
     * &lt;/p&gt;
     *
     * @param list1 the first list of AST nodes
     *    (element type: {@link ASTNode})
     * @param list2 the second list of AST nodes
     *    (element type: {@link ASTNode})
     * @return &lt;code&gt;true&lt;/code&gt; if the lists have the same number of elements
     *    and match pair-wise according to {@link ASTNode#subtreeMatch(ASTMatcher, Object) ASTNode.subtreeMatch}
     * @see ASTNode#subtreeMatch(ASTMatcher matcher, Object other)
     */
    public final boolean safeSubtreeListMatch(List list1, List list2) {
	int size1 = list1.size();
	int size2 = list2.size();
	if (size1 != size2) {
	    return false;
	}
	for (Iterator it1 = list1.iterator(), it2 = list2.iterator(); it1.hasNext();) {
	    ASTNode n1 = (ASTNode) it1.next();
	    ASTNode n2 = (ASTNode) it2.next();
	    if (!n1.subtreeMatch(this, n2)) {
		return false;
	    }
	}
	return true;
    }

}

