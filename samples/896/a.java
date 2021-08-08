class Tree extends Container {
    /*********************************************************************
    * Method to draw the (line connector) angled line.
    *********************************************************************/
    protected void drawConnector(Graphics g, int index, int dx, int dy, Node node) {
	if (node == null)
	    return;

	int level = node.getLevel() - 1; //levels.items[index] - 1;
	Node prev = node.getPreviousSibling();
	Node next = node.getNextSibling();

	// calculate the x-start position
	int x = dx;
	if (level == 0)
	    x += w1 / 2;
	else if (level == 1)
	    x += w1 + hline + gap + w2 / 2;
	else
	    x += w1 + hline + gap + w2 / 2 + (w1 / 2 + hline + gap + w2 / 2 + 1) * (level - 1);

	// calculate the y-start and y-end position         
	int ystart;
	int yend;

	// handles the last level 1 node
	if (level == 0 && next == null) {
	    if (prev != null && items.items[index] == node) {
		ystart = dy - (fmH - h1) / 2;
		yend = dy + (fmH - h1) / 2;
		g.drawLine(x, ystart, x, yend);
	    }
	}

	// draw vertical connector lines for leaf node
	if (node.isLeaf(allowsChildren) || node.getChildCount() == 0) {
	    ystart = dy - (fmH - h2) / 2;
	    yend = dy + (fmH / 2);
	    g.drawLine(x, ystart, x, yend);

	    if (next != null) {
		ystart = yend;
		yend += (fmH / 2);
		g.drawLine(x, ystart, x, yend);
	    }
	}
	// draw vertical connector lines for folder node
	else {
	    if (next == null && node == items.items[index]) {
		ystart = dy - (fmH - h1) / 2;
		yend = dy + (fmH - h1) / 2;
		g.drawLine(x, ystart, x, yend); // draw from "+" to end of line
	    }

	    if (next != null) {
		ystart = dy - (fmH - h1) / 2;
		yend = dy + fmH;
		g.drawLine(x, ystart, x, yend);
	    }
	}
	drawConnector(g, index, dx, dy, node.getParent());
    }

    private int w1 = 0;
    private int hline = 3;
    private int gap = 2;
    private int w2 = 0;
    protected Vector items = new Vector();
    private int h1 = 0;
    private boolean allowsChildren = true;
    private int h2 = 0;

}

