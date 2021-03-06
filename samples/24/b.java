class CardLayout implements LayoutManager2, Serializable {
    /**
     * Determines the preferred size of the container argument using
     * this card layout.
     * @param   parent the parent container in which to do the layout
     * @return  the preferred dimensions to lay out the subcomponents
     *                of the specified container
     * @see     java.awt.Container#getPreferredSize
     * @see     java.awt.CardLayout#minimumLayoutSize
     */
    public Dimension preferredLayoutSize(Container parent) {
	synchronized (parent.getTreeLock()) {
	    Insets insets = parent.getInsets();
	    int ncomponents = parent.getComponentCount();
	    int w = 0;
	    int h = 0;

	    for (int i = 0; i &lt; ncomponents; i++) {
		Component comp = parent.getComponent(i);
		Dimension d = comp.getPreferredSize();
		if (d.width &gt; w) {
		    w = d.width;
		}
		if (d.height &gt; h) {
		    h = d.height;
		}
	    }
	    return new Dimension(insets.left + insets.right + w + hgap * 2, insets.top + insets.bottom + h + vgap * 2);
	}
    }

    int hgap;
    int vgap;

}

