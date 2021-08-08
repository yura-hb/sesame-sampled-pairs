class Tree extends Container {
    /*********************************************************************
    * Method to set the tree model.
    * @param model the tree model.
    *********************************************************************/
    public void setModel(TreeModel model) {
	clear();
	this.model = (model != null) ? model : new TreeModel();
	model.setTree(this);
	initTree(model.getRoot());
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
    private boolean showRoot = false;
    private boolean allowsChildren = true;
    protected int visibleItems;
    private int hbarPolicy = 0;
    private int w1 = 0;
    private int hline = 3;
    private int w2 = 0;
    protected Image imgPlus;
    private int h1 = 0;
    protected Image imgMinus;
    protected Image imgClose;
    protected Image imgOpen;
    private int h2 = 0;
    protected Image imgFile;
    protected Image imgVisit;

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

    /*********************************************************************
    * Method to set the tree root node with the new root node.  If the 
    * new root node is null, the tree is unchanged.
    * @param root the new tree root node.
    *********************************************************************/
    public void initTree(Node root) {
	if (root != null) {
	    if (showRoot) {
		items.add(root);
		levels.add(0);
		expands.add(0);
		expand(root);
	    } else {
		Node childs[] = root.childrenArray();
		for (int i = 0; i &lt; childs.length; i++) {
		    items.add(childs[i]);
		    levels.add(1);
		    expands.add(0);
		}
	    }
	}
	resetScrollBars();
	initImage();
    }

    /*********************************************************************
    * Method to expand a collapsed node.
    * @param node the collapse node to expand.
    *********************************************************************/
    public void expand(Node node) {
	int index = indexOf(node);
	if (index != -1 && expands.items[index] == 0 && !node.isLeaf(allowsChildren)) {
	    expands.items[index] = 1;
	    Node childs[] = node.childrenArray();
	    for (int i = 0; i &lt; childs.length; i++) {
		index++;
		insert(index, childs[i], childs[i].getLevel(), 0);
	    }
	    resetScrollBars();
	    repaint();
	}
    }

    /*********************************************************************
    * Method to rest the vertical and horizontal scrollbars properties.
    * Note: there's still a bug in resetting the horizontal scroll bar.
    *********************************************************************/
    private void resetScrollBars() {
	resetVBar();
	resetHBar();
    }

    /*********************************************************************
    * Method to load icons use for the tree.  You can change the icon by
    * using the setIcon(int iconType, Filename imageFilename).
    *********************************************************************/
    protected void initImage() {
	try {
	    if (Settings.screenWidth &gt; 160) {
		setIcon(0, "cePlus.bmp");
		setIcon(1, "ceMinus.bmp");
		setIcon(2, "ceClose.bmp");
		setIcon(3, "ceOpen.bmp");
		setIcon(4, "ceFile.bmp");
		setIcon(5, "ceFileOpen.bmp");
	    } else {
		setIcon(0, "plus.bmp");
		setIcon(1, "minus.bmp");
		setIcon(2, "close.bmp");
		setIcon(3, "open.bmp");
		setIcon(4, "file.bmp");
		setIcon(5, "fileOpen.bmp");
	    }
	} catch (Exception e) {
	    /* TODO */ }
    }

    /*********************************************************************
    * Method to return the index of the item specified by the name, or -1 
    * if not found. 
    * @param name the object to find.
    * @return the index of the item specified by the name, or -1 if not found. 
    *********************************************************************/
    public int indexOf(Object name) {
	return items.find(name);
    }

    /*********************************************************************
    * Method to insert the items to the tree (For internal uses)
    * Note: this method does not reset the scroll bar..you need to call this
    * resetScrollBars() after you have performed an insert.
    *********************************************************************/
    private void insert(int index, Node node, int level, int expandValue) {
	items.insert(index, node);
	levels.insert(index, level);
	expands.insert(index, expandValue);
    }

    /*********************************************************************
    * Method to reset the horizontal scroll bar properties.
    *********************************************************************/
    private void resetVBar() {
	itemCount = items.size();
	vbar.setMaximum(itemCount);
	vbar.setEnabled(enabled && visibleItems &lt; itemCount);

	if (selectedIndex == itemCount) //last item was removed?
	    select(selectedIndex - 1);

	if (itemCount == 0) // olie@200b4_196: if after removing the list has 0 items, select( -1 ) is called, which does nothing (see there), then selectedIndex keeps being 0 which is wrong, it has to be -1
	    selectedIndex = -1;

	if (itemCount &lt;= visibleItems && offset != 0) // guich@200final_13
	    offset = 0;
    }

    /*********************************************************************
    * Method to reset the horizontal scroll bar properties.
    *********************************************************************/
    private void resetHBar() {
	if (hbarPolicy == 2)
	    return;

	// calculate the horizontalscrollbar maximum
	int max = 0;
	int indent = 3 + (w1 + hline + w2 / 2 - w1 / 2);
	for (int i = 0; i &lt; items.size(); i++) {
	    max = Math.max(max, fm.getTextWidth(((Node) items.items[i]).getNodeName()) + indent * levels.items[i]); // bug: calculation is off
	}
	max += vbar.getPreferredWidth(); // remember to take into account of the pixels used to draw the icons and scrollbar      

	hbar.setMaximum(max);
	if (hbarPolicy == 0 || (width - vbar.getPreferredWidth()) &lt; max) {
	    hbar.setEnabled(enabled && (width - vbar.getPreferredWidth()) &lt; max);
	    hbar.setVisible(true);
	} else
	    hbar.setVisible(false);
    }

    /*********************************************************************
    * Method to set the icon of the tree based on the icon type. 
    * Note: You should not change the plus and minus icons.
    * @param iconType  0 - plus icon  "+"
    *                  1 - minus icon "-" 
    *                  2 - open folder icon
    *                  3 - close folder icon
    *                  4 - file icon
    *                  5 - file opened (visited) icon
    * @param filename the filename of the image to load.
    * @throws Exception
    *********************************************************************/
    public void setIcon(int iconType, String filename) throws Exception {
	Image img = new Image(filename);

	switch (iconType) {
	case 0:
	    imgPlus = img;
	    w1 = img.getWidth();
	    h1 = img.getHeight();
	    break;
	case 1:
	    imgMinus = img;
	    break;
	case 2:
	    imgClose = img;
	    break;
	case 3:
	    imgOpen = img;
	    w2 = img.getWidth();
	    h2 = img.getHeight();
	    break;

	case 4:
	    imgFile = img;
	    break;
	case 5:
	    imgVisit = img;
	    break;
	}
    }

    /*********************************************************************
    * Method to select the given index and scroll to it if necessary. 
    * Note: select must be called only after the control has been added 
    *       to the container and its rect has been set. 
    * @param i the index of the item.
    *********************************************************************/
    public void select(int i) {
	if (0 &lt;= i && i &lt; itemCount && i != selectedIndex && height != 0) {
	    offset = i;
	    int vi = vbar.getVisibleItems();
	    int ma = vbar.getMaximum();
	    if (offset + vi &gt; ma) // astein@200b4_195: fix list items from being lost when the comboBox.select() method is used
		offset = Math.max(ma - vi, 0); // guich@220_4: fixed bug when the listbox is greater than the current item count

	    selectedIndex = i;
	    vbar.setValue(offset); // guich@210_9: fixed scrollbar update when selecting items
	    repaint();
	} else if (i == -1) { // guich@200b4_191: unselect all items
	    offset = 0;
	    vbar.setValue(0);
	    selectedIndex = -1;
	    repaint();
	}
    }

}

