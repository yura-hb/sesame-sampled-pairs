class Tree extends Container {
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

    protected int itemCount;
    protected int selectedIndex = -1;
    protected int offset;
    protected ScrollBar vbar;

}

