import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

class JTreeTable extends JTable {
    class TreeTableCellRenderer extends JTree implements TableCellRenderer {
	/**
	 * Sublcassed to translate the graphics such that the last visible
	 * row will be drawn at 0,0.
	 */
	public void paint(Graphics g) {
	    g.translate(0, -visibleRow * getRowHeight());
	    super.paint(g);
	    // Draw the Table border if we have focus.
	    if (highlightBorder != null) {
		highlightBorder.paintBorder(this, g, 0, visibleRow * getRowHeight(), getWidth(), getRowHeight());
	    }
	}

	/** Last table/tree row asked to renderer. */
	protected int visibleRow;
	/** Border to draw around the tree, if this is non-null, it will
	 * be painted. */
	protected Border highlightBorder;

    }

}

