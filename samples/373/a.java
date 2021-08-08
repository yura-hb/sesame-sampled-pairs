class TreeModel {
    /*********************************************************************
    * Method to set the root node of this tree model and notify the tree
    * to reload the tree.
    * @param root the new root node of this tree model.
    *********************************************************************/
    public void setRoot(Node root) {
	this.root = root;
	reload();
    }

    private Node root;
    private Tree tree;

    /*********************************************************************
    * Method to notify the tree to reload.
    *********************************************************************/
    public void reload() {
	if (tree != null)
	    tree.reload();
    }

}

