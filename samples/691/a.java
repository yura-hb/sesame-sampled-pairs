class Tree extends Container {
    /*********************************************************************
    * Debug: convient method to diplay the contents of the items vector.
    *********************************************************************/
    public void display() {
	for (int i = 0; i &lt; items.size(); i++) {
	    Node node = (Node) items.items[i];
	    //Vm.debug(node.toString() + " [" + levels.items[i] + "," + expands.items[i] + "]");
	}
    }

    protected Vector items = new Vector();

}

