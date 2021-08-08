class CheckboxMenuItem extends MenuItem implements ItemSelectable, Accessible {
    /**
     * Creates the peer of the checkbox item.  This peer allows us to
     * change the look of the checkbox item without changing its
     * functionality.
     * Most applications do not call this method directly.
     * @see     java.awt.Component#getToolkit()
     */
    public void addNotify() {
	synchronized (getTreeLock()) {
	    if (peer == null)
		peer = getComponentFactory().createCheckboxMenuItem(this);
	    super.addNotify();
	}
    }

}

