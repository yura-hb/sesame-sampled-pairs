import javax.swing.*;
import java.util.*;
import javax.swing.tree.*;

class SampleTree {
    class RemoveAction implements ActionListener {
	/**
	 * Removes the selected item as long as it isn't root.
	 */
	public void actionPerformed(ActionEvent e) {
	    TreePath[] selected = getSelectedPaths();

	    if (selected != null && selected.length &gt; 0) {
		TreePath shallowest;

		// The remove process consists of the following steps:
		// 1 - find the shallowest selected TreePath, the shallowest
		//     path is the path with the smallest number of path
		//     components.
		// 2 - Find the siblings of this TreePath
		// 3 - Remove from selected the TreePaths that are descendants
		//     of the paths that are going to be removed. They will
		//     be removed as a result of their ancestors being
		//     removed.
		// 4 - continue until selected contains only null paths.
		while ((shallowest = findShallowestPath(selected)) != null) {
		    removeSiblings(shallowest, selected);
		}
	    }
	}

	/**
	 * Returns the TreePath with the smallest path count in
	 * &lt;code&gt;paths&lt;/code&gt;. Will return null if there is no non-null
	 * TreePath is &lt;code&gt;paths&lt;/code&gt;.
	 */
	private TreePath findShallowestPath(TreePath[] paths) {
	    int shallowest = -1;
	    TreePath shallowestPath = null;

	    for (int counter = paths.length - 1; counter &gt;= 0; counter--) {
		if (paths[counter] != null) {
		    if (shallowest != -1) {
			if (paths[counter].getPathCount() &lt; shallowest) {
			    shallowest = paths[counter].getPathCount();
			    shallowestPath = paths[counter];
			    if (shallowest == 1) {
				return shallowestPath;
			    }
			}
		    } else {
			shallowestPath = paths[counter];
			shallowest = paths[counter].getPathCount();
		    }
		}
	    }
	    return shallowestPath;
	}

	/**
	 * Removes the sibling TreePaths of &lt;code&gt;path&lt;/code&gt;, that are
	 * located in &lt;code&gt;paths&lt;/code&gt;.
	 */
	private void removeSiblings(TreePath path, TreePath[] paths) {
	    // Find the siblings
	    if (path.getPathCount() == 1) {
		// Special case, set the root to null
		for (int counter = paths.length - 1; counter &gt;= 0; counter--) {
		    paths[counter] = null;
		}
		treeModel.setRoot(null);
	    } else {
		// Find the siblings of path.
		TreePath parent = path.getParentPath();
		MutableTreeNode parentNode = (MutableTreeNode) parent.getLastPathComponent();
		ArrayList&lt;TreePath&gt; toRemove = new ArrayList&lt;TreePath&gt;();

		// First pass, find paths with a parent TreePath of parent
		for (int counter = paths.length - 1; counter &gt;= 0; counter--) {
		    if (paths[counter] != null && paths[counter].getParentPath().equals(parent)) {
			toRemove.add(paths[counter]);
			paths[counter] = null;
		    }
		}

		// Second pass, remove any paths that are descendants of the
		// paths that are going to be removed. These paths are
		// implicitly removed as a result of removing the paths in
		// toRemove
		int rCount = toRemove.size();
		for (int counter = paths.length - 1; counter &gt;= 0; counter--) {
		    if (paths[counter] != null) {
			for (int rCounter = rCount - 1; rCounter &gt;= 0; rCounter--) {
			    if ((toRemove.get(rCounter)).isDescendant(paths[counter])) {
				paths[counter] = null;
			    }
			}
		    }
		}

		// Sort the siblings based on position in the model
		if (rCount &gt; 1) {
		    Collections.sort(toRemove, new PositionComparator());
		}
		int[] indices = new int[rCount];
		Object[] removedNodes = new Object[rCount];
		for (int counter = rCount - 1; counter &gt;= 0; counter--) {
		    removedNodes[counter] = (toRemove.get(counter)).getLastPathComponent();
		    indices[counter] = treeModel.getIndexOfChild(parentNode, removedNodes[counter]);
		    parentNode.remove(indices[counter]);
		}
		treeModel.nodesWereRemoved(parentNode, indices, removedNodes);
	    }
	}

    }

    /** Tree used for the example. */
    protected JTree tree;
    /** Tree model. */
    protected DefaultTreeModel treeModel;

    /**
     * Returns the selected TreePaths in the tree, may return null if
     * nothing is selected.
     */
    protected TreePath[] getSelectedPaths() {
	return tree.getSelectionPaths();
    }

}

