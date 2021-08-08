import java.awt.Component;
import javax.swing.JTable;

class TreeTableCellRenderer extends JTree implements TableCellRenderer {
    /**
     * TreeCellRenderer method. Overridden to update the visible row.
     * @see TableCellRenderer
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
	    int row, int column) {
	if (isSelected) {
	    setBackground(table.getSelectionBackground());
	} else {
	    setBackground(table.getBackground());
	}

	visibleRow = row;
	return this;
    }

    /** Last table/tree row asked to renderer. */
    private int visibleRow;

}

