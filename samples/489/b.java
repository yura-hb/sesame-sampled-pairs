import java.awt.peer.MenuItemPeer;

class MenuItem extends MenuComponent implements Accessible {
    /**
     * Sets the label for this menu item to the specified label.
     * @param     label   the new label, or {@code null} for no label.
     * @see       java.awt.MenuItem#getLabel
     * @since     1.0
     */
    public synchronized void setLabel(String label) {
	this.label = label;
	MenuItemPeer peer = (MenuItemPeer) this.peer;
	if (peer != null) {
	    peer.setLabel(label);
	}
    }

    /**
     * {@code label} is the label of a menu item.
     * It can be any string.
     *
     * @serial
     * @see #getLabel()
     * @see #setLabel(String)
     */
    volatile String label;

}

