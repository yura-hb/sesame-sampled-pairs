import java.awt.*;
import java.awt.datatransfer.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;
import sun.swing.SwingUtilities2;

class DefaultCaret extends Rectangle implements Caret, FocusListener, MouseListener, MouseMotionListener {
    class Handler implements PropertyChangeListener, DocumentListener, ActionListener, ClipboardOwner {
	/**
	 * This method gets called when a bound property is changed.
	 * We are looking for document changes on the editor.
	 */
	public void propertyChange(PropertyChangeEvent evt) {
	    Object oldValue = evt.getOldValue();
	    Object newValue = evt.getNewValue();
	    if ((oldValue instanceof Document) || (newValue instanceof Document)) {
		setDot(0);
		if (oldValue != null) {
		    ((Document) oldValue).removeDocumentListener(this);
		}
		if (newValue != null) {
		    ((Document) newValue).addDocumentListener(this);
		}
	    } else if ("enabled".equals(evt.getPropertyName())) {
		Boolean enabled = (Boolean) evt.getNewValue();
		if (component.isFocusOwner()) {
		    if (enabled == Boolean.TRUE) {
			if (component.isEditable()) {
			    setVisible(true);
			}
			setSelectionVisible(true);
		    } else {
			setVisible(false);
			setSelectionVisible(false);
		    }
		}
	    } else if ("caretWidth".equals(evt.getPropertyName())) {
		Integer newWidth = (Integer) evt.getNewValue();
		if (newWidth != null) {
		    caretWidth = newWidth.intValue();
		} else {
		    caretWidth = -1;
		}
		repaint();
	    } else if ("caretAspectRatio".equals(evt.getPropertyName())) {
		Number newRatio = (Number) evt.getNewValue();
		if (newRatio != null) {
		    aspectRatio = newRatio.floatValue();
		} else {
		    aspectRatio = -1;
		}
		repaint();
	    }
	}

    }

    JTextComponent component;
    /**
     * The width of the caret in pixels.
     */
    private int caretWidth = -1;
    private float aspectRatio = -1;
    boolean active;
    boolean visible;
    int dot;
    transient Position.Bias dotBias;
    Timer flasher;
    boolean selectionVisible;
    int mark;
    Object selectionTag;
    private transient NavigationFilter.FilterBypass filterBypass;
    /**
     * If this is true, the location of the dot is updated regardless of
     * the current location. This is set in the DocumentListener
     * such that even if the model location of dot hasn't changed (perhaps do
     * to a forward delete) the visual location is updated.
     */
    private boolean forceCaretPositionChange;
    transient Position.Bias markBias;
    boolean markLTR;
    boolean dotLTR;
    /**
     * The event listener list.
     */
    protected EventListenerList listenerList = new EventListenerList();
    /**
     * The change event for the model.
     * Only one ChangeEvent is needed per model instance since the
     * event's only (read-only) state is the source property.  The source
     * of events generated here is always "this".
     */
    protected transient ChangeEvent changeEvent = null;
    /**
     * This is used to indicate if the caret currently owns the selection.
     * This is always false if the system does not support the system
     * clipboard.
     */
    private boolean ownsSelection;
    Point magicCaretPosition;
    transient Handler handler = new Handler();

    /**
     * Sets the caret position and mark to the specified position,
     * with a forward bias. This implicitly sets the
     * selection range to zero.
     *
     * @param dot the position &gt;= 0
     * @see #setDot(int, Position.Bias)
     * @see Caret#setDot
     */
    public void setDot(int dot) {
	setDot(dot, Position.Bias.Forward);
    }

    /**
     * Sets the caret visibility, and repaints the caret.
     * It is important to understand the relationship between this method,
     * &lt;code&gt;isVisible&lt;/code&gt; and &lt;code&gt;isActive&lt;/code&gt;.
     * Calling this method with a value of &lt;code&gt;true&lt;/code&gt; activates the
     * caret blinking. Setting it to &lt;code&gt;false&lt;/code&gt; turns it completely off.
     * To determine whether the blinking is active, you should call
     * &lt;code&gt;isActive&lt;/code&gt;. In effect, &lt;code&gt;isActive&lt;/code&gt; is an
     * appropriate corresponding "getter" method for this one.
     * &lt;code&gt;isVisible&lt;/code&gt; can be used to fetch the current
     * visibility status of the caret, meaning whether or not it is currently
     * painted. This status will change as the caret blinks on and off.
     * &lt;p&gt;
     * Here's a list showing the potential return values of both
     * &lt;code&gt;isActive&lt;/code&gt; and &lt;code&gt;isVisible&lt;/code&gt;
     * after calling this method:
     * &lt;p&gt;
     * &lt;b&gt;&lt;code&gt;setVisible(true)&lt;/code&gt;&lt;/b&gt;:
     * &lt;ul&gt;
     *     &lt;li&gt;isActive(): true&lt;/li&gt;
     *     &lt;li&gt;isVisible(): true or false depending on whether
     *         or not the caret is blinked on or off&lt;/li&gt;
     * &lt;/ul&gt;
     * &lt;p&gt;
     * &lt;b&gt;&lt;code&gt;setVisible(false)&lt;/code&gt;&lt;/b&gt;:
     * &lt;ul&gt;
     *     &lt;li&gt;isActive(): false&lt;/li&gt;
     *     &lt;li&gt;isVisible(): false&lt;/li&gt;
     * &lt;/ul&gt;
     *
     * @param e the visibility specifier
     * @see #isActive
     * @see Caret#setVisible
     */
    @SuppressWarnings("deprecation")
    public void setVisible(boolean e) {
	// focus lost notification can come in later after the
	// caret has been deinstalled, in which case the component
	// will be null.
	active = e;
	if (component != null) {
	    TextUI mapper = component.getUI();
	    if (visible != e) {
		visible = e;
		// repaint the caret
		try {
		    Rectangle loc = mapper.modelToView(component, dot, dotBias);
		    damage(loc);
		} catch (BadLocationException badloc) {
		    // hmm... not legally positioned
		}
	    }
	}
	if (flasher != null) {
	    if (visible) {
		flasher.start();
	    } else {
		flasher.stop();
	    }
	}
    }

    /**
     * Changes the selection visibility.
     *
     * @param vis the new visibility
     */
    public void setSelectionVisible(boolean vis) {
	if (vis != selectionVisible) {
	    selectionVisible = vis;
	    if (selectionVisible) {
		// show
		Highlighter h = component.getHighlighter();
		if ((dot != mark) && (h != null) && (selectionTag == null)) {
		    int p0 = Math.min(dot, mark);
		    int p1 = Math.max(dot, mark);
		    Highlighter.HighlightPainter p = getSelectionPainter();
		    try {
			selectionTag = h.addHighlight(p0, p1, p);
		    } catch (BadLocationException bl) {
			selectionTag = null;
		    }
		}
	    } else {
		// hide
		if (selectionTag != null) {
		    Highlighter h = component.getHighlighter();
		    h.removeHighlight(selectionTag);
		    selectionTag = null;
		}
	    }
	}
    }

    /**
     * Cause the caret to be painted.  The repaint
     * area is the bounding box of the caret (i.e.
     * the caret rectangle or &lt;em&gt;this&lt;/em&gt;).
     * &lt;p&gt;
     * This method is thread safe, although most Swing methods
     * are not. Please see
     * &lt;A HREF="http://docs.oracle.com/javase/tutorial/uiswing/concurrency/index.html"&gt;Concurrency
     * in Swing&lt;/A&gt; for more information.
     */
    protected final synchronized void repaint() {
	if (component != null) {
	    component.repaint(x, y, width, height);
	}
    }

    /**
     * Sets the caret position and mark to the specified position, with the
     * specified bias. This implicitly sets the selection range
     * to zero.
     *
     * @param dot the position &gt;= 0
     * @param dotBias the bias for this position, not &lt;code&gt;null&lt;/code&gt;
     * @throws IllegalArgumentException if the bias is &lt;code&gt;null&lt;/code&gt;
     * @see Caret#setDot
     * @since 1.6
     */
    public void setDot(int dot, Position.Bias dotBias) {
	if (dotBias == null) {
	    throw new IllegalArgumentException("null bias");
	}

	NavigationFilter filter = component.getNavigationFilter();

	if (filter != null) {
	    filter.setDot(getFilterBypass(), dot, dotBias);
	} else {
	    handleSetDot(dot, dotBias);
	}
    }

    /**
     * Damages the area surrounding the caret to cause
     * it to be repainted in a new location.  If paint()
     * is reimplemented, this method should also be
     * reimplemented.  This method should update the
     * caret bounds (x, y, width, and height).
     *
     * @param r  the current location of the caret
     * @see #paint
     */
    protected synchronized void damage(Rectangle r) {
	if (r != null) {
	    int damageWidth = getCaretWidth(r.height);
	    x = r.x - 4 - (damageWidth &gt;&gt; 1);
	    y = r.y;
	    width = 9 + damageWidth;
	    height = r.height;
	    repaint();
	}
    }

    /**
     * Gets the painter for the Highlighter.
     *
     * @return the painter
     */
    protected Highlighter.HighlightPainter getSelectionPainter() {
	return DefaultHighlighter.DefaultPainter;
    }

    private NavigationFilter.FilterBypass getFilterBypass() {
	if (filterBypass == null) {
	    filterBypass = new DefaultFilterBypass();
	}
	return filterBypass;
    }

    void handleSetDot(int dot, Position.Bias dotBias) {
	// move dot, if it changed
	Document doc = component.getDocument();
	if (doc != null) {
	    dot = Math.min(dot, doc.getLength());
	}
	dot = Math.max(dot, 0);

	// The position (0,Backward) is out of range so disallow it.
	if (dot == 0)
	    dotBias = Position.Bias.Forward;

	mark = dot;
	if (this.dot != dot || this.dotBias != dotBias || selectionTag != null || forceCaretPositionChange) {
	    changeCaretPosition(dot, dotBias);
	}
	this.markBias = this.dotBias;
	this.markLTR = dotLTR;
	Highlighter h = component.getHighlighter();
	if ((h != null) && (selectionTag != null)) {
	    h.removeHighlight(selectionTag);
	    selectionTag = null;
	}
    }

    int getCaretWidth(int height) {
	if (aspectRatio &gt; -1) {
	    return (int) (aspectRatio * height) + 1;
	}

	if (caretWidth &gt; -1) {
	    return caretWidth;
	} else {
	    Object property = UIManager.get("Caret.width");
	    if (property instanceof Integer) {
		return ((Integer) property).intValue();
	    } else {
		return 1;
	    }
	}
    }

    /**
     * Sets the caret position (dot) to a new location.  This
     * causes the old and new location to be repainted.  It
     * also makes sure that the caret is within the visible
     * region of the view, if the view is scrollable.
     */
    void changeCaretPosition(int dot, Position.Bias dotBias) {
	// repaint the old position and set the new value of
	// the dot.
	repaint();

	// Make sure the caret is visible if this window has the focus.
	if (flasher != null && flasher.isRunning()) {
	    visible = true;
	    flasher.restart();
	}

	// notify listeners at the caret moved
	this.dot = dot;
	this.dotBias = dotBias;
	dotLTR = isPositionLTR(dot, dotBias);
	fireStateChanged();

	updateSystemSelection();

	setMagicCaretPosition(null);

	// We try to repaint the caret later, since things
	// may be unstable at the time this is called
	// (i.e. we don't want to depend upon notification
	// order or the fact that this might happen on
	// an unsafe thread).
	Runnable callRepaintNewCaret = new Runnable() {
	    public void run() {
		repaintNewCaret();
	    }
	};
	SwingUtilities.invokeLater(callRepaintNewCaret);
    }

    boolean isPositionLTR(int position, Position.Bias bias) {
	Document doc = component.getDocument();
	if (bias == Position.Bias.Backward && --position &lt; 0)
	    position = 0;
	return AbstractDocument.isLeftToRight(doc, position, position);
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.  The listener list is processed last to first.
     *
     * @see EventListenerList
     */
    protected void fireStateChanged() {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length - 2; i &gt;= 0; i -= 2) {
	    if (listeners[i] == ChangeListener.class) {
		// Lazily create the event:
		if (changeEvent == null)
		    changeEvent = new ChangeEvent(this);
		((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
	    }
	}
    }

    private void updateSystemSelection() {
	if (!SwingUtilities2.canCurrentEventAccessSystemClipboard()) {
	    return;
	}
	if (this.dot != this.mark && component != null && component.hasFocus()) {
	    Clipboard clip = getSystemSelection();
	    if (clip != null) {
		String selectedText;
		if (component instanceof JPasswordField
			&& component.getClientProperty("JPasswordField.cutCopyAllowed") != Boolean.TRUE) {
		    //fix for 4793761
		    StringBuilder txt = null;
		    char echoChar = ((JPasswordField) component).getEchoChar();
		    int p0 = Math.min(getDot(), getMark());
		    int p1 = Math.max(getDot(), getMark());
		    for (int i = p0; i &lt; p1; i++) {
			if (txt == null) {
			    txt = new StringBuilder();
			}
			txt.append(echoChar);
		    }
		    selectedText = (txt != null) ? txt.toString() : null;
		} else {
		    selectedText = component.getSelectedText();
		}
		try {
		    clip.setContents(new StringSelection(selectedText), getClipboardOwner());

		    ownsSelection = true;
		} catch (IllegalStateException ise) {
		    // clipboard was unavailable
		    // no need to provide error feedback to user since updating
		    // the system selection is not a user invoked action
		}
	    }
	}
    }

    /**
     * Saves the current caret position.  This is used when
     * caret up/down actions occur, moving between lines
     * that have uneven end positions.
     *
     * @param p the position
     * @see #getMagicCaretPosition
     */
    public void setMagicCaretPosition(Point p) {
	magicCaretPosition = p;
    }

    /**
     * Repaints the new caret position, with the
     * assumption that this is happening on the
     * event thread so that calling &lt;code&gt;modelToView&lt;/code&gt;
     * is safe.
     */
    @SuppressWarnings("deprecation")
    void repaintNewCaret() {
	if (component != null) {
	    TextUI mapper = component.getUI();
	    Document doc = component.getDocument();
	    if ((mapper != null) && (doc != null)) {
		// determine the new location and scroll if
		// not visible.
		Rectangle newLoc;
		try {
		    newLoc = mapper.modelToView(component, this.dot, this.dotBias);
		} catch (BadLocationException e) {
		    newLoc = null;
		}
		if (newLoc != null) {
		    adjustVisibility(newLoc);
		    // If there is no magic caret position, make one
		    if (getMagicCaretPosition() == null) {
			setMagicCaretPosition(new Point(newLoc.x, newLoc.y));
		    }
		}

		// repaint the new position
		damage(newLoc);
	    }
	}
    }

    private Clipboard getSystemSelection() {
	try {
	    return component.getToolkit().getSystemSelection();
	} catch (HeadlessException he) {
	    // do nothing... there is no system clipboard
	} catch (SecurityException se) {
	    // do nothing... there is no allowed system clipboard
	}
	return null;
    }

    /**
     * Fetches the current position of the caret.
     *
     * @return the position &gt;= 0
     * @see Caret#getDot
     */
    public int getDot() {
	return dot;
    }

    /**
     * Fetches the current position of the mark.  If there is a selection,
     * the dot and mark will not be the same.
     *
     * @return the position &gt;= 0
     * @see Caret#getMark
     */
    public int getMark() {
	return mark;
    }

    private ClipboardOwner getClipboardOwner() {
	return handler;
    }

    /**
     * Scrolls the associated view (if necessary) to make
     * the caret visible.  Since how this should be done
     * is somewhat of a policy, this method can be
     * reimplemented to change the behavior.  By default
     * the scrollRectToVisible method is called on the
     * associated component.
     *
     * @param nloc the new position to scroll to
     */
    protected void adjustVisibility(Rectangle nloc) {
	if (component == null) {
	    return;
	}
	if (SwingUtilities.isEventDispatchThread()) {
	    component.scrollRectToVisible(nloc);
	} else {
	    SwingUtilities.invokeLater(new SafeScroller(nloc));
	}
    }

    /**
     * Gets the saved caret position.
     *
     * @return the position
     * see #setMagicCaretPosition
     */
    public Point getMagicCaretPosition() {
	return magicCaretPosition;
    }

    class SafeScroller implements Runnable {
	JTextComponent component;
	/**
	* The width of the caret in pixels.
	*/
	private int caretWidth = -1;
	private float aspectRatio = -1;
	boolean active;
	boolean visible;
	int dot;
	transient Position.Bias dotBias;
	Timer flasher;
	boolean selectionVisible;
	int mark;
	Object selectionTag;
	private transient NavigationFilter.FilterBypass filterBypass;
	/**
	* If this is true, the location of the dot is updated regardless of
	* the current location. This is set in the DocumentListener
	* such that even if the model location of dot hasn't changed (perhaps do
	* to a forward delete) the visual location is updated.
	*/
	private boolean forceCaretPositionChange;
	transient Position.Bias markBias;
	boolean markLTR;
	boolean dotLTR;
	/**
	* The event listener list.
	*/
	protected EventListenerList listenerList = new EventListenerList();
	/**
	* The change event for the model.
	* Only one ChangeEvent is needed per model instance since the
	* event's only (read-only) state is the source property.  The source
	* of events generated here is always "this".
	*/
	protected transient ChangeEvent changeEvent = null;
	/**
	* This is used to indicate if the caret currently owns the selection.
	* This is always false if the system does not support the system
	* clipboard.
	*/
	private boolean ownsSelection;
	Point magicCaretPosition;
	transient Handler handler = new Handler();

	SafeScroller(Rectangle r) {
	    this.r = r;
	}

    }

}

