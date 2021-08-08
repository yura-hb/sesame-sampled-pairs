import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

class JTreeTable extends JTable {
    class TreeTableCellRenderer extends JTree implements TableCellRenderer {
	/**
	 * TreeCellRenderer method. Overridden to update the visible row.
	 */
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
		int row, int column) {
	    Color background;
	    Color foreground;

	    if (isSelected) {
		background = table.getSelectionBackground();
		foreground = table.getSelectionForeground();
	    } else {
		background = table.getBackground();
		foreground = table.getForeground();
	    }
	    highlightBorder = null;
	    if (realEditingRow() == row && getEditingColumn() == column) {
		background = UIManager.getColor("Table.focusCellBackground");
		foreground = UIManager.getColor("Table.focusCellForeground");
	    } else if (hasFocus) {
		highlightBorder = UIManager.getBorder("Table.focusCellHighlightBorder");
		if (isCellEditable(row, column)) {
		    background = UIManager.getColor("Table.focusCellBackground");
		    foreground = UIManager.getColor("Table.focusCellForeground");
		}
	    }

	    visibleRow = row;
	    setBackground(background);

	    TreeCellRenderer tcr = getCellRenderer();
	    if (tcr instanceof DefaultTreeCellRenderer) {
		DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer) tcr);
		if (isSelected) {
		    dtcr.setTextSelectionColor(foreground);
		    dtcr.setBackgroundSelectionColor(background);
		} else {
		    dtcr.setTextNonSelectionColor(foreground);
		    dtcr.setBackgroundNonSelectionColor(background);
		}
	    }
	    return this;
	}

	/** Border to draw around the tree, if this is non-null, it will
	 * be painted. */
	protected Border highlightBorder;
	/** Last table/tree row asked to renderer. */
	protected int visibleRow;

    }

    /**
     * Returns the actual row that is editing as &lt;code&gt;getEditingRow&lt;/code&gt;
     * will always return -1.
     */
    private int realEditingRow() {
	return editingRow;
    }

}

