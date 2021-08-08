import java.awt.Component;

class JScrollPane extends JComponent implements ScrollPaneConstants, Accessible {
    /**
     * Sets the orientation for the vertical and horizontal
     * scrollbars as determined by the
     * &lt;code&gt;ComponentOrientation&lt;/code&gt; argument.
     *
     * @param  co one of the following values:
     * &lt;ul&gt;
     * &lt;li&gt;java.awt.ComponentOrientation.LEFT_TO_RIGHT
     * &lt;li&gt;java.awt.ComponentOrientation.RIGHT_TO_LEFT
     * &lt;li&gt;java.awt.ComponentOrientation.UNKNOWN
     * &lt;/ul&gt;
     * @see java.awt.ComponentOrientation
     */
    public void setComponentOrientation(ComponentOrientation co) {
	super.setComponentOrientation(co);
	if (verticalScrollBar != null)
	    verticalScrollBar.setComponentOrientation(co);
	if (horizontalScrollBar != null)
	    horizontalScrollBar.setComponentOrientation(co);
    }

    /**
     * The scrollpane's vertical scrollbar child.
     * Default is a &lt;code&gt;JScrollBar&lt;/code&gt;.
     * @see #setVerticalScrollBar
     */
    protected JScrollBar verticalScrollBar;
    /**
     * The scrollpane's horizontal scrollbar child.
     * Default is a &lt;code&gt;JScrollBar&lt;/code&gt;.
     * @see #setHorizontalScrollBar
     */
    protected JScrollBar horizontalScrollBar;

}

