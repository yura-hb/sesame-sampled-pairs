import java.awt.Component;
import java.awt.*;
import java.awt.geom.*;
import javax.swing.text.*;

class JLabel extends JComponent implements SwingConstants, Accessible {
    class AccessibleJLabel extends AccessibleJComponent implements AccessibleText, AccessibleExtendedComponent {
	/**
	 * Given a point in local coordinates, return the zero-based index
	 * of the character under that Point.  If the point is invalid,
	 * this method returns -1.
	 *
	 * @param p the Point in local coordinates
	 * @return the zero-based index of the character under Point p; if
	 * Point is invalid returns -1.
	 * @since 1.3
	 */
	public int getIndexAtPoint(Point p) {
	    View view = (View) JLabel.this.getClientProperty("html");
	    if (view != null) {
		Rectangle r = getTextRectangle();
		if (r == null) {
		    return -1;
		}
		Rectangle2D.Float shape = new Rectangle2D.Float(r.x, r.y, r.width, r.height);
		Position.Bias bias[] = new Position.Bias[1];
		return view.viewToModel(p.x, p.y, shape, bias);
	    } else {
		return -1;
	    }
	}

	private Rectangle getTextRectangle() {

	    String text = JLabel.this.getText();
	    Icon icon = (JLabel.this.isEnabled()) ? JLabel.this.getIcon() : JLabel.this.getDisabledIcon();

	    if ((icon == null) && (text == null)) {
		return null;
	    }

	    Rectangle paintIconR = new Rectangle();
	    Rectangle paintTextR = new Rectangle();
	    Rectangle paintViewR = new Rectangle();
	    Insets paintViewInsets = new Insets(0, 0, 0, 0);

	    paintViewInsets = JLabel.this.getInsets(paintViewInsets);
	    paintViewR.x = paintViewInsets.left;
	    paintViewR.y = paintViewInsets.top;
	    paintViewR.width = JLabel.this.getWidth() - (paintViewInsets.left + paintViewInsets.right);
	    paintViewR.height = JLabel.this.getHeight() - (paintViewInsets.top + paintViewInsets.bottom);

	    String clippedText = SwingUtilities.layoutCompoundLabel((JComponent) JLabel.this, getFontMetrics(getFont()),
		    text, icon, JLabel.this.getVerticalAlignment(), JLabel.this.getHorizontalAlignment(),
		    JLabel.this.getVerticalTextPosition(), JLabel.this.getHorizontalTextPosition(), paintViewR,
		    paintIconR, paintTextR, JLabel.this.getIconTextGap());

	    return paintTextR;
	}

    }

    private String text = "";
    private Icon defaultIcon = null;
    private boolean disabledIconSet = false;
    private Icon disabledIcon = null;
    private int verticalAlignment = CENTER;
    private int horizontalAlignment = LEADING;
    private int verticalTextPosition = CENTER;
    private int horizontalTextPosition = TRAILING;
    private int iconTextGap = 4;

    /**
     * Returns the text string that the label displays.
     *
     * @return a String
     * @see #setText
     */
    public String getText() {
	return text;
    }

    /**
     * Returns the graphic image (glyph, icon) that the label displays.
     *
     * @return an Icon
     * @see #setIcon
     */
    public Icon getIcon() {
	return defaultIcon;
    }

    /**
     * Returns the icon used by the label when it's disabled.
     * If no disabled icon has been set this will forward the call to
     * the look and feel to construct an appropriate disabled Icon.
     * &lt;p&gt;
     * Some look and feels might not render the disabled Icon, in which
     * case they will ignore this.
     *
     * @return the &lt;code&gt;disabledIcon&lt;/code&gt; property
     * @see #setDisabledIcon
     * @see javax.swing.LookAndFeel#getDisabledIcon
     * @see ImageIcon
     */
    @Transient
    public Icon getDisabledIcon() {
	if (!disabledIconSet && disabledIcon == null && defaultIcon != null) {
	    disabledIcon = UIManager.getLookAndFeel().getDisabledIcon(this, defaultIcon);
	    if (disabledIcon != null) {
		firePropertyChange("disabledIcon", null, disabledIcon);
	    }
	}
	return disabledIcon;
    }

    /**
     * Returns the alignment of the label's contents along the Y axis.
     *
     * @return   The value of the verticalAlignment property, one of the
     *           following constants defined in &lt;code&gt;SwingConstants&lt;/code&gt;:
     *           &lt;code&gt;TOP&lt;/code&gt;,
     *           &lt;code&gt;CENTER&lt;/code&gt;, or
     *           &lt;code&gt;BOTTOM&lt;/code&gt;.
     *
     * @see SwingConstants
     * @see #setVerticalAlignment
     */
    public int getVerticalAlignment() {
	return verticalAlignment;
    }

    /**
     * Returns the alignment of the label's contents along the X axis.
     *
     * @return   The value of the horizontalAlignment property, one of the
     *           following constants defined in &lt;code&gt;SwingConstants&lt;/code&gt;:
     *           &lt;code&gt;LEFT&lt;/code&gt;,
     *           &lt;code&gt;CENTER&lt;/code&gt;,
     *           &lt;code&gt;RIGHT&lt;/code&gt;,
     *           &lt;code&gt;LEADING&lt;/code&gt; or
     *           &lt;code&gt;TRAILING&lt;/code&gt;.
     *
     * @see #setHorizontalAlignment
     * @see SwingConstants
     */
    public int getHorizontalAlignment() {
	return horizontalAlignment;
    }

    /**
     * Returns the vertical position of the label's text,
     * relative to its image.
     *
     * @return   One of the following constants
     *           defined in &lt;code&gt;SwingConstants&lt;/code&gt;:
     *           &lt;code&gt;TOP&lt;/code&gt;,
     *           &lt;code&gt;CENTER&lt;/code&gt;, or
     *           &lt;code&gt;BOTTOM&lt;/code&gt;.
     *
     * @see #setVerticalTextPosition
     * @see SwingConstants
     */
    public int getVerticalTextPosition() {
	return verticalTextPosition;
    }

    /**
     * Returns the horizontal position of the label's text,
     * relative to its image.
     *
     * @return   One of the following constants
     *           defined in &lt;code&gt;SwingConstants&lt;/code&gt;:
     *           &lt;code&gt;LEFT&lt;/code&gt;,
     *           &lt;code&gt;CENTER&lt;/code&gt;,
     *           &lt;code&gt;RIGHT&lt;/code&gt;,
     *           &lt;code&gt;LEADING&lt;/code&gt; or
     *           &lt;code&gt;TRAILING&lt;/code&gt;.
     *
     * @see SwingConstants
     */
    public int getHorizontalTextPosition() {
	return horizontalTextPosition;
    }

    /**
     * Returns the amount of space between the text and the icon
     * displayed in this label.
     *
     * @return an int equal to the number of pixels between the text
     *         and the icon.
     * @see #setIconTextGap
     */
    public int getIconTextGap() {
	return iconTextGap;
    }

}

