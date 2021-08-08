import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;

abstract class MapAdapter extends DefaultTreeModel implements MindMap {
    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type. The event instance is lazily created using the
     * parameters passed into the fire method.
     * 
     * @param source
     *            the node being changed
     * @param path
     *            the path to the root node
     * @param childIndices
     *            the indices of the changed elements
     * @param children
     *            the changed elements
     * @see EventListenerList
     */
    protected void fireTreeNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	TreeModelEvent e = null;
	// Process the listeners last to first, notifying
	// those that are interested in this event
	e = fireTreeNodesInserted(source, path, childIndices, children, listeners, e);
	MindMapNode node = (MindMapNode) path[path.length - 1];
	fireTreeNodesInserted(source, path, childIndices, children, node.getListeners().getListenerList(), e);
    }

    private TreeModelEvent fireTreeNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children,
	    Object[] listeners, TreeModelEvent e) {
	for (int i = listeners.length - 2; i &gt;= 0; i -= 2) {
	    if (listeners[i] == TreeModelListener.class) {
		// Lazily create the event:
		if (e == null)
		    e = new TreeModelEvent(source, path, childIndices, children);
		((TreeModelListener) listeners[i + 1]).treeNodesInserted(e);
	    }
	}
	return e;
    }

}

