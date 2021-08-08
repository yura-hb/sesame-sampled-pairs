import sun.lwawt.macosx.CMenuBar;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import static sun.awt.AWTAccessor.*;

class ScreenMenuBar extends MenuBar implements ContainerListener, ScreenMenuPropertyHandler, ComponentListener {
    /**
     * Invoked when a component has been added to the container.
     */
    public void componentAdded(final ContainerEvent e) {
	final Component child = e.getChild();
	if (!(child instanceof JMenu))
	    return;
	addSubmenu((JMenu) child);
    }

    Hashtable&lt;JMenu, ScreenMenu&gt; fSubmenus;
    JMenuBar fSwingBar;

    ScreenMenu addSubmenu(final JMenu m) {
	ScreenMenu sm = fSubmenus.get(m);

	if (sm == null) {
	    sm = new ScreenMenu(m);
	    m.addComponentListener(this);
	    fSubmenus.put(m, sm);
	}

	sm.setEnabled(m.isEnabled());

	// MenuComponents don't support setVisible, so we just don't add it to the menubar
	if (m.isVisible() && sm.getParent() == null) {
	    int newIndex = 0, currVisibleIndex = 0;
	    JMenu menu = null;
	    final int menuCount = fSwingBar.getMenuCount();
	    for (int i = 0; i &lt; menuCount; i++) {
		menu = fSwingBar.getMenu(i);
		if (menu == m) {
		    newIndex = currVisibleIndex;
		    break;
		}
		if (menu != null && menu.isVisible()) {
		    currVisibleIndex++;
		}
	    }
	    add(sm, newIndex);
	}

	return sm;
    }

    public Menu add(final Menu m, final int index) {
	synchronized (getTreeLock()) {
	    if (m.getParent() != null) {
		m.getParent().remove(m);
	    }

	    final Vector&lt;Menu&gt; menus = getMenuBarAccessor().getMenus(this);
	    menus.insertElementAt(m, index);
	    final MenuComponentAccessor acc = getMenuComponentAccessor();
	    acc.setParent(m, this);

	    final CMenuBar peer = acc.getPeer(this);
	    if (peer == null)
		return m;

	    peer.setNextInsertionIndex(index);
	    final CMenuBar mPeer = acc.getPeer(m);
	    if (mPeer == null) {
		m.addNotify();
	    }

	    peer.setNextInsertionIndex(-1);
	    return m;
	}
    }

}

