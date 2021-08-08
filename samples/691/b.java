import java.util.ArrayList;

abstract class XBaseMenuWindow extends XWindow {
    /**
     * Thread-safely creates a copy of the items vector
     */
    XMenuItemPeer[] copyItems() {
	synchronized (getMenuTreeLock()) {
	    return items.toArray(new XMenuItemPeer[] {});
	}
    }

    /**
     * Array of items.
     */
    private ArrayList&lt;XMenuItemPeer&gt; items;
    /**
     * Static synchronizational object.
     * Following operations should be synchronized
     * using this object:
     * 1. Access to items vector
     * 2. Access to selection
     * 3. Access to showing menu window member
     *
     * This is lowest level lock,
     * no other locks should be taken when
     * thread own this lock.
     */
    private static Object menuTreeLock = new Object();

    /**
     * Returns static lock used for menus
     */
    static Object getMenuTreeLock() {
	return menuTreeLock;
    }

}

