class Tree extends Container {
    /*********************************************************************
    * Method to empties this Tree, setting all elements of the array to 
    * null, so they can be garbage collected.
    *********************************************************************/
    public void removeAll() { // guich@210_13
	model = new TreeModel();
	clear();
    }

    protected TreeModel model = null;
    protected Vector items = new Vector();
    protected IntVector levels = new IntVector();
    protected IntVector expands = new IntVector();
    protected ScrollBar vbar;
    protected ScrollBar hbar;
    protected int itemCount;
    protected int hsCount;
    protected int offset;
    protected int hsOffset;
    protected int selectedIndex = -1;

    /*********************************************************************
    * Same as removeAll() method.  Just more clearer method name
    *********************************************************************/
    public void clear() {
	items.clear();
	levels.clear();
	expands.clear();
	vbar.setMaximum(0);
	hbar.setMaximum(0);
	itemCount = 0;
	hsCount = 0;
	offset = 0; // wolfgang@330_23
	hsOffset = 0;
	selectedIndex = -1; // seanwalton@401_26
	repaint();
    }

}

