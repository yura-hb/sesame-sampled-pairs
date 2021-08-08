import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;

class XTree extends JTree {
    /**
     * This method removes all the displayed nodes from the tree,
     * but does not affect actual MBeanServer contents.
     */
    // Call on EDT
    @Override
    public synchronized void removeAll() {
	DefaultTreeModel model = (DefaultTreeModel) getModel();
	DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
	root.removeAllChildren();
	model.nodeStructureChanged(root);
	nodes.clear();
    }

    private Map&lt;String, DefaultMutableTreeNode&gt; nodes = new HashMap&lt;String, DefaultMutableTreeNode&gt;();

}

