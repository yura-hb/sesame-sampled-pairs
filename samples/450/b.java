import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.awt.image.*;
import sun.util.logging.PlatformLogger;

class XListPeer extends XComponentPeer implements ListPeer, XScrollbarClient {
    /**
     * return value from the scrollbar
     */
    public void notifyValue(XScrollbar obj, int type, int v, boolean isAdjusting) {

	if (log.isLoggable(PlatformLogger.Level.FINE)) {
	    log.fine("Notify value changed on " + obj + " to " + v);
	}
	int value = obj.getValue();
	if (obj == vsb) {
	    scrollVertical(v - value);

	    // See 6243382 for more information
	    int oldSel = eventIndex;
	    int newSel = eventIndex + v - value;
	    if (mouseDraggedOutVertically && !isSelected(newSel)) {
		selectItem(newSel);
		eventIndex = newSel;
		repaint(oldSel, eventIndex, PAINT_ITEMS);
		// Scrolling select() should also set the focus index
		// Otherwise, the updating of the 'focusIndex' variable will be incorrect
		// if user drag mouse out of the area of the list
		setFocusIndex(newSel);
		repaint(PAINT_FOCUS);
	    }

	} else if ((XHorizontalScrollbar) obj == hsb) {
	    scrollHorizontal(v - value);
	}

    }

    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XListPeer");
    XVerticalScrollbar vsb;
    int eventIndex = -1;
    boolean mouseDraggedOutVertically = false;
    private static final int PAINT_ITEMS = 8;
    private static final int PAINT_FOCUS = 16;
    XHorizontalScrollbar hsb;
    private static final int PAINT_HIDEFOCUS = 64;
    private static final int PAINT_VSCROLL = 2;
    public static final int MARGIN = 2;
    public static final int SCROLLBAR_AREA = 17;
    private static final int COPY_AREA = 128;
    int eventType = NONE;
    int selected[];
    int currentIndex = -1;
    boolean multipleSelections;
    int focusIndex;
    public static final int SPACE = 1;
    private static final int PAINT_HSCROLL = 4;
    boolean hsbVis;
    int fontHeight;
    int fontLeading;
    boolean vsbVis;
    Vector&lt;String&gt; items;
    ListPainter painter;
    private static final int PAINT_ALL = PAINT_VSCROLL | PAINT_HSCROLL | PAINT_ITEMS | PAINT_FOCUS | PAINT_BACKGROUND;
    private static final int PAINT_BACKGROUND = 32;
    int listWidth;
    int listHeight;
    public static final int SCROLLBAR_WIDTH = 13;
    boolean bgColorSet;
    boolean fgColorSet;
    int fontAscent;

    /**
     * scrollVertical
     * y is the number of items to scroll
     */
    void scrollVertical(int y) {
	if (log.isLoggable(PlatformLogger.Level.FINE)) {
	    log.fine("Scrolling vertically by " + y);
	}
	int itemsInWin = itemsInWindow();
	int h = getItemHeight();
	int pixelsToScroll = y * h;

	if (vsb.getValue() &lt; -y) {
	    y = -vsb.getValue();
	}
	vsb.setValue(vsb.getValue() + y);

	Rectangle source = null;
	Point distance = null;
	int firstItem = 0, lastItem = 0;
	int options = PAINT_HIDEFOCUS | PAINT_ITEMS | PAINT_VSCROLL | PAINT_FOCUS;
	if (y &gt; 0) {
	    if (y &lt; itemsInWin) {
		source = new Rectangle(MARGIN, MARGIN + pixelsToScroll, width - SCROLLBAR_AREA,
			h * (itemsInWin - y - 1) - 1);
		distance = new Point(0, -pixelsToScroll);
		options |= COPY_AREA;
	    }
	    firstItem = vsb.getValue() + itemsInWin - y - 1;
	    lastItem = vsb.getValue() + itemsInWin - 1;

	} else if (y &lt; 0) {
	    if (y + itemsInWindow() &gt; 0) {
		source = new Rectangle(MARGIN, MARGIN, width - SCROLLBAR_AREA, h * (itemsInWin + y));
		distance = new Point(0, -pixelsToScroll);
		options |= COPY_AREA;
	    }
	    firstItem = vsb.getValue();
	    lastItem = Math.min(getLastVisibleItem(), vsb.getValue() + -y);
	}
	repaint(firstItem, lastItem, options, source, distance);
    }

    /**
     * is the index "index" selected
     */
    boolean isSelected(int index) {
	if (eventType == ItemEvent.SELECTED && index == eventIndex) {
	    return true;
	}
	for (int i = 0; i &lt; selected.length; i++) {
	    if (selected[i] == index) {
		return true;
	    }
	}
	return false;
    }

    /**
     * select the index
     * redraw the list to the screen
     */
    void selectItem(int index) {
	// NOTE: instead of recalculating and the calling repaint(), painting
	// is done immediately

	// 6190746 List does not trigger ActionEvent when double clicking a programmatically selected item, XToolkit
	// If we invoke select(int) before setVisible(boolean), then variable currentIndex will equals -1. At the same time isSelected may be true.
	// Restoring Motif behavior
	currentIndex = index;

	if (isSelected(index)) {
	    return;
	}
	if (!multipleSelections) {
	    if (selected.length == 0) { // No current selection
		selected = new int[1];
		selected[0] = index;
	    } else {
		int oldSel = selected[0];
		selected[0] = index;
		if (!isItemHidden(oldSel)) {
		    // Only bother painting if item is visible (4895367)
		    repaint(oldSel, oldSel, PAINT_ITEMS);
		}
	    }
	} else {
	    // insert "index" into the selection array
	    int newsel[] = new int[selected.length + 1];
	    int i = 0;
	    while (i &lt; selected.length && index &gt; selected[i]) {
		newsel[i] = selected[i];
		i++;
	    }
	    newsel[i] = index;
	    System.arraycopy(selected, i, newsel, i + 1, selected.length - i);
	    selected = newsel;
	}
	if (!isItemHidden(index)) {
	    // Only bother painting if item is visible (4895367)
	    repaint(index, index, PAINT_ITEMS);
	}
    }

    private void repaint(int firstItem, int lastItem, int options) {
	repaint(firstItem, lastItem, options, null, null);
    }

    void setFocusIndex(int value) {
	focusIndex = value;
    }

    private void repaint(int options) {
	repaint(getFirstVisibleItem(), getLastVisibleItem(), options);
    }

    /**
     * scrollHorizontal
     * x is the number of pixels to scroll
     */
    void scrollHorizontal(int x) {
	if (log.isLoggable(PlatformLogger.Level.FINE)) {
	    log.fine("Scrolling horizontally by " + y);
	}
	int w = getListWidth();
	w -= ((2 * SPACE) + (2 * MARGIN));
	int h = height - (SCROLLBAR_AREA + (2 * MARGIN));
	hsb.setValue(hsb.getValue() + x);

	int options = PAINT_ITEMS | PAINT_HSCROLL;

	Rectangle source = null;
	Point distance = null;
	if (x &lt; 0) {
	    source = new Rectangle(MARGIN + SPACE, MARGIN, w + x, h);
	    distance = new Point(-x, 0);
	    options |= COPY_AREA;
	} else if (x &gt; 0) {
	    source = new Rectangle(MARGIN + SPACE + x, MARGIN, w - x, h);
	    distance = new Point(-x, 0);
	    options |= COPY_AREA;
	}
	repaint(vsb.getValue(), lastItemDisplayed(), options, source, distance);
    }

    int itemsInWindow() {
	return itemsInWindow(hsbVis);
    }

    int getItemHeight() {
	return (fontHeight - fontLeading) + (2 * SPACE);
    }

    int getLastVisibleItem() {
	if (vsbVis) {
	    return Math.min(items.size() - 1, vsb.getValue() + itemsInWindow() - 1);
	} else {
	    return Math.min(items.size() - 1, itemsInWindow() - 1);
	}
    }

    /**
     * In most cases the entire area of the component doesn't have
     * to be repainted. The method repaints the particular areas of
     * the component. The areas to repaint is specified by the option
     * parameter. The possible values of the option parameter are:
     * PAINT_VSCROLL, PAINT_HSCROLL, PAINT_ITEMS, PAINT_FOCUS,
     * PAINT_HIDEFOCUS, PAINT_BACKGROUND, PAINT_ALL, COPY_AREA.
     *
     * Note that the COPY_AREA value initiates copy of a source area
     * of the component by a distance by means of the copyArea method
     * of the Graphics class.
     *
     * @param firstItem the position of the first item of the range to repaint
     * @param lastItem the position of the last item of the range to repaint
     * @param options specifies the particular area of the component to repaint
     * @param source the area of the component to copy
     * @param distance the distance to copy the source area
     */
    private void repaint(int firstItem, int lastItem, int options, Rectangle source, Point distance) {
	final Graphics g = getGraphics();
	if (g != null) {
	    try {
		painter.paint(g, firstItem, lastItem, options, source, distance);
		postPaintEvent(target, 0, 0, getWidth(), getHeight());
	    } finally {
		g.dispose();
	    }
	}
    }

    /**
     * returns whether the given index is currently scrolled off the top or
     * bottom of the List.
     */
    boolean isItemHidden(int index) {
	return index &lt; vsb.getValue() || index &gt;= vsb.getValue() + itemsInWindow();
    }

    int getFirstVisibleItem() {
	if (vsbVis) {
	    return vsb.getValue();
	} else {
	    return 0;
	}
    }

    /**
     * returns the width of the list portion of the component (accounts for
     * presence of vertical scrollbar)
     */
    int getListWidth() {
	return vsbVis ? width - SCROLLBAR_AREA : width;
    }

    /**
     * returns index of last item displayed in the List
     */
    int lastItemDisplayed() {
	int n = itemsInWindow();
	return (Math.min(items.size() - 1, (vsb.getValue() + n) - 1));
    }

    /**
     * return the number of items that can fit
     * in the current window
     */
    int itemsInWindow(boolean scrollbarVisible) {
	int h;
	if (scrollbarVisible) {
	    h = height - ((2 * MARGIN) + SCROLLBAR_AREA);
	} else {
	    h = height - 2 * MARGIN;
	}
	return (h / getItemHeight());
    }

    Rectangle getVScrollBarRec() {
	return new Rectangle(width - (SCROLLBAR_WIDTH), 0, SCROLLBAR_WIDTH + 1, height);
    }

    Rectangle getHScrollBarRec() {
	return new Rectangle(0, height - SCROLLBAR_WIDTH, width, SCROLLBAR_WIDTH);
    }

    int getFocusIndex() {
	return focusIndex;
    }

    /**
     * Update and return the focus rectangle.
     * Focus is around the focused item, if it is visible, or
     * around the border of the list if the focused item is scrolled off the top
     * or bottom of the list.
     */
    Rectangle getFocusRect() {
	Rectangle focusRect = new Rectangle();
	// width is always only based on presence of vert sb
	focusRect.x = 1;
	focusRect.width = getListWidth() - 3;
	// if focused item is not currently displayed in the list,  paint
	// focus around entire list (not including scrollbars)
	if (isIndexDisplayed(getFocusIndex())) {
	    // focus rect is around the item
	    focusRect.y = index2y(getFocusIndex()) - 2;
	    focusRect.height = getItemHeight() + 1;
	} else {
	    // focus rect is around the list
	    focusRect.y = 1;
	    focusRect.height = hsbVis ? height - SCROLLBAR_AREA : height;
	    focusRect.height -= 3;
	}
	return focusRect;
    }

    boolean isIndexDisplayed(int idx) {
	int lastDisplayed = lastItemDisplayed();

	return idx &lt;= lastDisplayed && idx &gt;= Math.max(0, lastDisplayed - itemsInWindow() + 1);
    }

    /**
     * return the y value of the given index "i".
     * the y value represents the top of the text
     * NOTE: index can be larger than items.size as long
     * as it can fit the window
     */
    int index2y(int index) {
	int h = getItemHeight();

	//if (index &lt; vsb.getValue() || index &gt; vsb.getValue() + itemsInWindow()) {
	return MARGIN + ((index - vsb.getValue()) * h) + SPACE;
    }

    int getItemWidth() {
	return width - ((2 * MARGIN) + (vsbVis ? SCROLLBAR_AREA : 0));
    }

    int getItemY(int item) {
	return index2y(item);
    }

    int getItemX() {
	return MARGIN + SPACE;
    }

    class ListPainter {
	private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XListPeer");
	XVerticalScrollbar vsb;
	int eventIndex = -1;
	boolean mouseDraggedOutVertically = false;
	private static final int PAINT_ITEMS = 8;
	private static final int PAINT_FOCUS = 16;
	XHorizontalScrollbar hsb;
	private static final int PAINT_HIDEFOCUS = 64;
	private static final int PAINT_VSCROLL = 2;
	public static final int MARGIN = 2;
	public static final int SCROLLBAR_AREA = 17;
	private static final int COPY_AREA = 128;
	int eventType = NONE;
	int selected[];
	int currentIndex = -1;
	boolean multipleSelections;
	int focusIndex;
	public static final int SPACE = 1;
	private static final int PAINT_HSCROLL = 4;
	boolean hsbVis;
	int fontHeight;
	int fontLeading;
	boolean vsbVis;
	Vector&lt;String&gt; items;
	ListPainter painter;
	private static final int PAINT_ALL = PAINT_VSCROLL | PAINT_HSCROLL | PAINT_ITEMS | PAINT_FOCUS
		| PAINT_BACKGROUND;
	private static final int PAINT_BACKGROUND = 32;
	int listWidth;
	int listHeight;
	public static final int SCROLLBAR_WIDTH = 13;
	boolean bgColorSet;
	boolean fgColorSet;
	int fontAscent;

	private void paint(Graphics listG, int firstItem, int lastItem, int options, Rectangle source, Point distance) {
	    if (log.isLoggable(PlatformLogger.Level.FINER)) {
		log.finer("Repaint from " + firstItem + " to " + lastItem + " options " + options);
	    }
	    if (firstItem &gt; lastItem) {
		int t = lastItem;
		lastItem = firstItem;
		firstItem = t;
	    }
	    if (firstItem &lt; 0) {
		firstItem = 0;
	    }
	    colors = getGUIcolors();
	    VolatileImage localBuffer = null;
	    do {
		XToolkit.awtLock();
		try {
		    if (createBuffer()) {
			// First time created buffer should be painted over at full.
			options = PAINT_ALL;
		    }
		    localBuffer = buffer;
		} finally {
		    XToolkit.awtUnlock();
		}
		switch (localBuffer.validate(getGraphicsConfiguration())) {
		case VolatileImage.IMAGE_INCOMPATIBLE:
		    invalidate();
		    options = PAINT_ALL;
		    continue;
		case VolatileImage.IMAGE_RESTORED:
		    options = PAINT_ALL;
		}
		Graphics g = localBuffer.createGraphics();

		// Note that the order of the following painting operations
		// should not be modified
		try {
		    g.setFont(getFont());

		    // hiding the focus rectangle must be done prior to copying
		    // area and so this is the first action to be performed
		    if ((options & (PAINT_HIDEFOCUS)) != 0) {
			paintFocus(g, PAINT_HIDEFOCUS);
		    }
		    /*
		     * The shift of the component contents occurs while someone
		     * scrolls the component, the only purpose of the shift is to
		     * increase the painting performance. The shift should be done
		     * prior to painting any area (except hiding focus) and actually
		     * it should never be done jointly with erase background.
		     */
		    if ((options & COPY_AREA) != 0) {
			g.copyArea(source.x, source.y, source.width, source.height, distance.x, distance.y);
		    }
		    if ((options & PAINT_BACKGROUND) != 0) {
			paintBackground(g);
			// Since we made full erase update items
			firstItem = getFirstVisibleItem();
			lastItem = getLastVisibleItem();
		    }
		    if ((options & PAINT_ITEMS) != 0) {
			paintItems(g, firstItem, lastItem, options);
		    }
		    if ((options & PAINT_VSCROLL) != 0 && vsbVis) {
			g.setClip(getVScrollBarRec());
			paintVerScrollbar(g, true);
		    }
		    if ((options & PAINT_HSCROLL) != 0 && hsbVis) {
			g.setClip(getHScrollBarRec());
			paintHorScrollbar(g, true);
		    }
		    if ((options & (PAINT_FOCUS)) != 0) {
			paintFocus(g, PAINT_FOCUS);
		    }
		} finally {
		    g.dispose();
		}
	    } while (localBuffer.contentsLost());
	    listG.drawImage(localBuffer, 0, 0, null);
	}

	private boolean createBuffer() {
	    VolatileImage localBuffer = null;
	    XToolkit.awtLock();
	    try {
		localBuffer = buffer;
	    } finally {
		XToolkit.awtUnlock();
	    }

	    if (localBuffer == null) {
		if (log.isLoggable(PlatformLogger.Level.FINE)) {
		    log.fine("Creating buffer " + width + "x" + height);
		}
		// use GraphicsConfig.cCVI() instead of Component.cVI(),
		// because the latter may cause a deadlock with the tree lock
		localBuffer = graphicsConfig.createCompatibleVolatileImage(width + 1, height + 1);
	    }
	    XToolkit.awtLock();
	    try {
		if (buffer == null) {
		    buffer = localBuffer;
		    return true;
		}
	    } finally {
		XToolkit.awtUnlock();
	    }
	    return false;
	}

	public void invalidate() {
	    XToolkit.awtLock();
	    try {
		if (buffer != null) {
		    buffer.flush();
		}
		buffer = null;
	    } finally {
		XToolkit.awtUnlock();
	    }
	}

	private void paintFocus(Graphics g, int options) {
	    boolean paintFocus = (options & PAINT_FOCUS) != 0;
	    if (paintFocus && !hasFocus()) {
		paintFocus = false;
	    }
	    if (log.isLoggable(PlatformLogger.Level.FINE)) {
		log.fine("Painting focus, focus index " + getFocusIndex() + ", focus is "
			+ (isItemHidden(getFocusIndex()) ? ("invisible") : ("visible")) + ", paint focus is "
			+ paintFocus);
	    }
	    Shape clip = g.getClip();
	    g.setClip(0, 0, listWidth, listHeight);
	    if (log.isLoggable(PlatformLogger.Level.FINEST)) {
		log.finest("Setting focus clip " + new Rectangle(0, 0, listWidth, listHeight));
	    }
	    Rectangle rect = getFocusRect();
	    if (prevFocusRect != null) {
		// Erase focus rect
		if (log.isLoggable(PlatformLogger.Level.FINEST)) {
		    log.finest("Erasing previous focus rect " + prevFocusRect);
		}
		g.setColor(getListBackground());
		g.drawRect(prevFocusRect.x, prevFocusRect.y, prevFocusRect.width, prevFocusRect.height);
		prevFocusRect = null;
	    }
	    if (paintFocus) {
		// Paint new
		if (log.isLoggable(PlatformLogger.Level.FINEST)) {
		    log.finest("Painting focus rect " + rect);
		}
		g.setColor(getListForeground()); // Focus color is always black on Linux
		g.drawRect(rect.x, rect.y, rect.width, rect.height);
		prevFocusRect = rect;
	    }
	    g.setClip(clip);
	}

	private void paintBackground(Graphics g) {
	    g.setColor(SystemColor.window);
	    g.fillRect(0, 0, width, height);
	    g.setColor(getListBackground());
	    g.fillRect(0, 0, listWidth, listHeight);
	    draw3DRect(g, getSystemColors(), 0, 0, listWidth - 1, listHeight - 1, false);
	}

	private void paintItems(Graphics g, int firstItem, int lastItem, int options) {
	    if (log.isLoggable(PlatformLogger.Level.FINER)) {
		log.finer("Painting items from " + firstItem + " to " + lastItem + ", focused " + focusIndex
			+ ", first " + getFirstVisibleItem() + ", last " + getLastVisibleItem());
	    }

	    firstItem = Math.max(getFirstVisibleItem(), firstItem);
	    if (firstItem &gt; lastItem) {
		int t = lastItem;
		lastItem = firstItem;
		firstItem = t;
	    }
	    firstItem = Math.max(getFirstVisibleItem(), firstItem);
	    lastItem = Math.min(lastItem, items.size() - 1);

	    if (log.isLoggable(PlatformLogger.Level.FINER)) {
		log.finer("Actually painting items from " + firstItem + " to " + lastItem + ", items in window "
			+ itemsInWindow());
	    }
	    for (int i = firstItem; i &lt;= lastItem; i++) {
		paintItem(g, i);
	    }
	}

	/**
	 * Paint the vertical scrollbar to the screen
	 *
	 * @param g the graphics context to draw into
	 * @param paintAll paint the whole scrollbar if true, just the thumb if false
	 */
	void paintVerScrollbar(Graphics g, boolean paintAll) {
	    int h = height - (hsbVis ? (SCROLLBAR_AREA - 2) : 0);
	    paintScrollBar(vsb, g, width - SCROLLBAR_WIDTH, 0, SCROLLBAR_WIDTH - 2, h, paintAll);
	}

	/**
	 * Paint the horizontal scrollbar to the screen
	 *
	 * @param g the graphics context to draw into
	 * @param paintAll paint the whole scrollbar if true, just the thumb if false
	 */
	void paintHorScrollbar(Graphics g, boolean paintAll) {
	    int w = getListWidth();
	    paintScrollBar(hsb, g, 0, height - (SCROLLBAR_WIDTH), w, SCROLLBAR_WIDTH, paintAll);
	}

	private Color getListBackground() {
	    if (bgColorSet) {
		return colors[BACKGROUND_COLOR];
	    } else {
		return SystemColor.text;
	    }
	}

	private Color getListForeground() {
	    if (fgColorSet) {
		return colors[FOREGROUND_COLOR];
	    } else {
		return SystemColor.textText;
	    }
	}

	private void paintItem(Graphics g, int index) {
	    if (log.isLoggable(PlatformLogger.Level.FINEST)) {
		log.finest("Painting item " + index);
	    }
	    // 4895367 - only paint items which are visible
	    if (!isItemHidden(index)) {
		Shape clip = g.getClip();
		int w = getItemWidth();
		int h = getItemHeight();
		int y = getItemY(index);
		int x = getItemX();
		if (log.isLoggable(PlatformLogger.Level.FINEST)) {
		    log.finest("Setting clip " + new Rectangle(x, y, w - (SPACE * 2), h - (SPACE * 2)));
		}
		g.setClip(x, y, w - (SPACE * 2), h - (SPACE * 2));

		// Always paint the background so that focus is unpainted in
		// multiselect mode
		if (isSelected(index)) {
		    if (log.isLoggable(PlatformLogger.Level.FINEST)) {
			log.finest("Painted item is selected");
		    }
		    g.setColor(getListForeground());
		} else {
		    g.setColor(getListBackground());
		}
		if (log.isLoggable(PlatformLogger.Level.FINEST)) {
		    log.finest("Filling " + new Rectangle(x, y, w, h));
		}
		g.fillRect(x, y, w, h);

		if (index &lt;= getLastVisibleItem() && index &lt; items.size()) {
		    if (!isEnabled()) {
			g.setColor(getDisabledColor());
		    } else if (isSelected(index)) {
			g.setColor(getListBackground());
		    } else {
			g.setColor(getListForeground());
		    }
		    String str = items.elementAt(index);
		    g.drawString(str, x - hsb.getValue(), y + fontAscent);
		} else {
		    // Clear the remaining area around the item - focus area and the rest of border
		    g.setClip(x, y, listWidth, h);
		    g.setColor(getListBackground());
		    g.fillRect(x, y, listWidth, h);
		}
		g.setClip(clip);
	    }
	}

	void paintScrollBar(XScrollbar scr, Graphics g, int x, int y, int width, int height, boolean paintAll) {
	    if (log.isLoggable(PlatformLogger.Level.FINEST)) {
		log.finest("Painting scrollbar " + scr + " width " + width + " height " + height + ", paintAll "
			+ paintAll);
	    }
	    g.translate(x, y);
	    scr.paint(g, getSystemColors(), paintAll);
	    g.translate(-x, -y);
	}

	private Color getDisabledColor() {
	    Color backgroundColor = getListBackground();
	    Color foregroundColor = getListForeground();
	    return (backgroundColor.equals(Color.BLACK)) ? foregroundColor.darker() : backgroundColor.darker();
	}

    }

}

