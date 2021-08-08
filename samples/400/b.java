import java.awt.*;
import javax.swing.SwingConstants;

class DefaultEditorKit extends EditorKit {
    class VerticalPageAction extends TextAction {
	/** The operation to perform when this action is triggered. */
	@SuppressWarnings("deprecation")
	public void actionPerformed(ActionEvent e) {
	    JTextComponent target = getTextComponent(e);
	    if (target != null) {
		Rectangle visible = target.getVisibleRect();
		Rectangle newVis = new Rectangle(visible);
		int selectedIndex = target.getCaretPosition();
		int scrollAmount = direction
			* target.getScrollableBlockIncrement(visible, SwingConstants.VERTICAL, direction);
		int initialY = visible.y;
		Caret caret = target.getCaret();
		Point magicPosition = caret.getMagicCaretPosition();

		if (selectedIndex != -1) {
		    try {
			Rectangle dotBounds = target.modelToView(selectedIndex);
			int x = (magicPosition != null) ? magicPosition.x : dotBounds.x;
			int h = dotBounds.height;
			if (h &gt; 0) {
			    // We want to scroll by a multiple of caret height,
			    // rounding towards lower integer
			    scrollAmount = scrollAmount / h * h;
			}
			newVis.y = constrainY(target, initialY + scrollAmount, visible.height);

			int newIndex;

			if (visible.contains(dotBounds.x, dotBounds.y)) {
			    // Dot is currently visible, base the new
			    // location off the old, or
			    newIndex = target
				    .viewToModel(new Point(x, constrainY(target, dotBounds.y + scrollAmount, 0)));
			} else {
			    // Dot isn't visible, choose the top or the bottom
			    // for the new location.
			    if (direction == -1) {
				newIndex = target.viewToModel(new Point(x, newVis.y));
			    } else {
				newIndex = target.viewToModel(new Point(x, newVis.y + visible.height));
			    }
			}
			newIndex = constrainOffset(target, newIndex);
			if (newIndex != selectedIndex) {
			    // Make sure the new visible location contains
			    // the location of dot, otherwise Caret will
			    // cause an additional scroll.
			    int newY = getAdjustedY(target, newVis, newIndex);

			    if (direction == -1 && newY &lt;= initialY || direction == 1 && newY &gt;= initialY) {
				// Change index and correct newVis.y only if won't cause scrolling upward
				newVis.y = newY;

				if (select) {
				    target.moveCaretPosition(newIndex);
				} else {
				    target.setCaretPosition(newIndex);
				}
			    }
			} else {
			    // If the caret index is same as the visible offset
			    // then correct newVis.y so that it won't cause
			    // unnecessary scrolling upward/downward when
			    // page-down/page-up is received after ctrl-END/ctrl-HOME
			    if (direction == -1 && newVis.y &lt;= initialY || direction == 1 && newVis.y &gt;= initialY) {
				newVis.y = initialY;
			    }
			}
		    } catch (BadLocationException ble) {
		    }
		} else {
		    newVis.y = constrainY(target, initialY + scrollAmount, visible.height);
		}
		if (magicPosition != null) {
		    caret.setMagicCaretPosition(magicPosition);
		}
		target.scrollRectToVisible(newVis);
	    }
	}

	/**
	 * Direction to scroll, 1 is down, -1 is up.
	 */
	private int direction;
	/**
	 * Adjusts the Rectangle to contain the bounds of the character at
	 * &lt;code&gt;index&lt;/code&gt; in response to a page up.
	 */
	private boolean select;

	/**
	 * Makes sure &lt;code&gt;y&lt;/code&gt; is a valid location in
	 * &lt;code&gt;target&lt;/code&gt;.
	 */
	private int constrainY(JTextComponent target, int y, int vis) {
	    if (y &lt; 0) {
		y = 0;
	    } else if (y + vis &gt; target.getHeight()) {
		y = Math.max(0, target.getHeight() - vis);
	    }
	    return y;
	}

	/**
	 * Ensures that &lt;code&gt;offset&lt;/code&gt; is a valid offset into the
	 * model for &lt;code&gt;text&lt;/code&gt;.
	 */
	private int constrainOffset(JTextComponent text, int offset) {
	    Document doc = text.getDocument();

	    if ((offset != 0) && (offset &gt; doc.getLength())) {
		offset = doc.getLength();
	    }
	    if (offset &lt; 0) {
		offset = 0;
	    }
	    return offset;
	}

	/**
	 * Returns adjustsed {@code y} position that indicates the location to scroll to
	 * after selecting &lt;code&gt;index&lt;/code&gt;.
	 */
	@SuppressWarnings("deprecation")
	private int getAdjustedY(JTextComponent text, Rectangle visible, int index) {
	    int result = visible.y;

	    try {
		Rectangle dotBounds = text.modelToView(index);

		if (dotBounds.y &lt; visible.y) {
		    result = dotBounds.y;
		} else {
		    if ((dotBounds.y &gt; visible.y + visible.height)
			    || (dotBounds.y + dotBounds.height &gt; visible.y + visible.height)) {
			result = dotBounds.y + dotBounds.height - visible.height;
		    }
		}
	    } catch (BadLocationException ble) {
	    }

	    return result;
	}

    }

}

