import javax.swing.*;
import javax.swing.tree.*;

class BasicTreeUI extends TreeUI {
    /**
     * Sets the right child indent.
     *
     * @param newAmount the right child indent
     */
    public void setRightChildIndent(int newAmount) {
	rightChildIndent = newAmount;
	totalChildIndent = leftChildIndent + rightChildIndent;
	if (treeState != null)
	    treeState.invalidateSizes();
	updateSize();
    }

    /** Distance to add to leftChildIndent to determine where cell
      * contents will be drawn. */
    protected int rightChildIndent;
    /** Total distance that will be indented.  The sum of leftChildIndent
      * and rightChildIndent. */
    protected int totalChildIndent;
    /** Distance between left margin and where vertical dashes will be
      * drawn. */
    protected int leftChildIndent;
    /** Object responsible for handling sizing and expanded issues. */
    // WARNING: Be careful with the bounds held by treeState. They are
    // always in terms of left-to-right. They get mapped to right-to-left
    // by the various methods of this class.
    protected AbstractLayoutCache treeState;
    /** Is the preferredSize valid? */
    protected boolean validCachedPreferredSize;
    /** Component that we're going to be drawing into. */
    protected JTree tree;

    /**
     * Marks the cached size as being invalid, and messages the
     * tree with &lt;code&gt;treeDidChange&lt;/code&gt;.
     */
    protected void updateSize() {
	validCachedPreferredSize = false;
	tree.treeDidChange();
    }

}

