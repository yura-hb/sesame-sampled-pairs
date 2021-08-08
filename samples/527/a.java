class Tree extends Container {
    /*********************************************************************
    * Method to initialize the vertical and horizontal scrollbars maximum.
    *********************************************************************/
    protected void initScrollBars() {
	// initialize the vertical scrollbar
	itemCount = items.size();
	vbar.setEnabled(enabled && visibleItems &lt; itemCount);
	vbar.setMaximum(itemCount); // guich@210_12: forgot this line!

	// initialize the horizontal scrollbar
	int maxWidth = 0;
	for (int i = 0; i &lt; this.items.size(); i++)
	    maxWidth = Math.max(getItemWidth(i), maxWidth);
	maxWidth = maxWidth - (width - vbar.getPreferredWidth());
	hsCount = (maxWidth &gt; 0) ? maxWidth : 0;
	hbar.setEnabled(enabled && hsCount &gt; width - vbar.getPreferredWidth());
	hbar.setMaximum(hsCount);

	switch (hbarPolicy) {
	case 0:
	    hbar.setVisible(true);
	    break;
	case 1:
	    if (hsCount &gt; width - vbar.getPreferredWidth())
		hbar.setVisible(true);
	    else
		hbar.setVisible(false);
	case 2:
	    hbar.setVisible(false);
	    break;
	}
    }

    protected int itemCount;
    protected Vector items = new Vector();
    protected ScrollBar vbar;
    protected int visibleItems;
    protected int hsCount;
    protected ScrollBar hbar;
    private int hbarPolicy = 0;

    /*********************************************************************
    * Method to return the width of the given item index with the current 
    * fontmetrics. 
    * Note: if you overide this class you must implement this method. 
    *********************************************************************/
    protected int getItemWidth(int index) {
	return fm.getTextWidth(items.items[index].toString());
    }

}

