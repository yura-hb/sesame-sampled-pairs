import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

class MapView extends JPanel implements ViewAbstraction, Printable, Autoscroll {
    /**
     * @return an ArrayList of MindMapNode objects. If both ancestor and
     *         descendant node are selected, only the ancestor is returned
     */
    public ArrayList&lt;MindMapNode&gt; getSelectedNodesSortedByY() {
	final HashSet&lt;MindMapNode&gt; selectedNodesSet = new HashSet&lt;&gt;();
	for (int i = 0; i &lt; selected.size(); i++) {
	    selectedNodesSet.add(getSelected(i).getModel());
	}
	LinkedList&lt;Pair&gt; pointNodePairs = new LinkedList&lt;&gt;();

	Point point = new Point();
	iteration: for (int i = 0; i &lt; selected.size(); i++) {
	    final NodeView view = getSelected(i);
	    final MindMapNode node = view.getModel();
	    for (MindMapNode parent = node.getParentNode(); parent != null; parent = parent.getParentNode()) {
		if (selectedNodesSet.contains(parent)) {
		    continue iteration;
		}
	    }
	    view.getContent().getLocation(point);
	    Tools.convertPointToAncestor(view, point, this);
	    pointNodePairs.add(new Pair(new Integer(point.y), node));
	}
	// do the sorting:
	Collections.sort(pointNodePairs, new Comparator&lt;Pair&gt;() {
	    public int compare(Pair pair0, Pair pair1) {
		Integer int0 = (Integer) pair0.getFirst();
		Integer int1 = (Integer) pair1.getFirst();
		return int0.compareTo(int1);

	    }
	});

	ArrayList&lt;MindMapNode&gt; selectedNodes = new ArrayList&lt;&gt;();
	for (Iterator&lt;Pair&gt; it = pointNodePairs.iterator(); it.hasNext();) {
	    selectedNodes.add((MindMapNode) it.next().getSecond());
	}

	// logger.fine("Cutting #" + selectedNodes.size());
	// for (Iterator it = selectedNodes.iterator(); it.hasNext();) {
	// MindMapNode node = (MindMapNode) it.next();
	// logger.fine("Cutting " + node);
	// }
	return selectedNodes;
    }

    private Selected selected = new Selected();

    private NodeView getSelected(int i) {
	return selected.get(i);
    }

    class Selected {
	private Selected selected = new Selected();

	public int size() {
	    return mySelected.size();
	}

	public NodeView get(int i) {
	    return (NodeView) mySelected.get(i);
	}

    }

}

