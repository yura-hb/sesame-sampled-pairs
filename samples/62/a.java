import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;

abstract class MapAdapter extends DefaultTreeModel implements MindMap {
    /**
     * Invoke this method if you've totally changed the children of node and its
     * childrens children... This will post a treeStructureChanged event.
     */
    void nodeChangedInternal(TreeNode node) {
	if (node != null) {
	    fireTreeNodesChanged(this, getPathToRoot(node), null, null);
	}
    }

    protected void fireTreeNodesChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	TreeModelEvent e = null;
	// Process the listeners last to first, notifying
	// those that are interested in this event
	e = fireTreeNodesChanged(source, path, childIndices, children, listeners, e);
	MindMapNode node = (MindMapNode) path[path.length - 1];
	fireTreeNodesChanged(source, path, childIndices, children, node.getListeners().getListenerList(), e);
    }

    private TreeModelEvent fireTreeNodesChanged(Object source, Object[] path, int[] childIndices, Object[] children,
	    Object[] listeners, TreeModelEvent e) {
	for (int i = listeners.length - 2; i &gt;= 0; i -= 2) {
	    if (listeners[i] == TreeModelListener.class) {
		// Lazily create the event:
		if (e == null)
		    e = new TreeModelEvent(source, path, childIndices, children);
		((TreeModelListener) listeners[i + 1]).treeNodesChanged(e);
	    }
	}
	return e;
    }

}

