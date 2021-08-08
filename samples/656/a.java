class Node {
    /***************************************************************************
    * Method to builds the parents of node up to and including the root node, 
    * where the original node is the last element in the returned array. 
    * @return the path from this node to the root node, including the root node.
    ***************************************************************************/
    protected Node[] getPathToRoot() {
	Vector v = new Vector();
	pathFromNodeToRoot(v, this);

	Node p[] = new Node[v.size()];
	for (int i = 0; i &lt; p.length; i++)
	    p[i] = (Node) v.items[i];
	return p;
    }

    protected Node parent = null;

    /***************************************************************************
    * Method to get the path from the specified node to the root node.
    * @param v the vector to hold the path.
    * @param node the specified node.
    ***************************************************************************/
    private void pathFromNodeToRoot(Vector v, Node node) {
	if (node == null)
	    return;
	v.add(node);
	pathFromNodeToRoot(v, node.getParent());
    }

    /***************************************************************************
    * Method to return this node's parent or null if this node has no parent. 
    ***************************************************************************/
    public Node getParent() {
	return parent;
    }

}

