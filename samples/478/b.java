import java.awt.Point;
import javax.swing.JTable;
import javax.swing.table.TableModel;

class JTableOperator extends JComponentOperator implements Outputable, Timeoutable {
    /**
     * Searches cell row index. Searching is performed between cells in one
     * column.
     *
     * @param chooser Component verifying object.
     * @param column a column index to search in
     * @param index Ordinal index in suitable cells.
     * @return a row index.
     */
    public int findCellRow(ComponentChooser chooser, int column, int index) {
	return findCell(chooser, null, new int[] { column }, index).y;
    }

    /**
     * Searches cell coordinates.
     *
     * @param chooser Component verifying object.
     * @param rows rows to search in
     * @param columns columns to search in
     * @param index an ordinal cell index
     * @return Point indicating coordinates (x - column, y - row)
     */
    public Point findCell(ComponentChooser chooser, int[] rows, int[] columns, int index) {
	return findCell(new ByRenderedComponentTableCellChooser(chooser), rows, columns, index);
    }

    /**
     * Searches cell coordinates in the specified rows and columns.
     *
     * @param chooser cell verifying object.
     * @param rows rows to search in
     * @param columns columns to search in
     * @param index an ordinal cell index
     * @return Point indicating coordinates (x - column, y - row)
     */
    public Point findCell(TableCellChooser chooser, int[] rows, int[] columns, int index) {
	TableModel model = getModel();
	int[] realRows;
	if (rows != null) {
	    realRows = rows;
	} else {
	    realRows = new int[model.getRowCount()];
	    for (int i = 0; i &lt; model.getRowCount(); i++) {
		realRows[i] = i;
	    }
	}
	int[] realColumns;
	if (columns != null) {
	    realColumns = columns;
	} else {
	    realColumns = new int[model.getColumnCount()];
	    for (int i = 0; i &lt; model.getColumnCount(); i++) {
		realColumns[i] = i;
	    }
	}
	int count = 0;
	for (int realRow : realRows) {
	    for (int realColumn : realColumns) {
		if (chooser.checkCell(this, realRow, realColumn)) {
		    if (count == index) {
			return new Point(realColumn, realRow);
		    } else {
			count++;
		    }
		}
	    }
	}
	return new Point(-1, -1);
    }

    /**
     * Maps {@code JTable.getModel()} through queue
     */
    public TableModel getModel() {
	return (runMapping(new MapAction&lt;TableModel&gt;("getModel") {
	    @Override
	    public TableModel map() {
		return ((JTable) getSource()).getModel();
	    }
	}));
    }

    class ByRenderedComponentTableCellChooser implements TableCellChooser {
	public ByRenderedComponentTableCellChooser(ComponentChooser chooser) {
	    this.chooser = chooser;
	}

    }

    interface TableCellChooser {
	/**
	 * Should be true if item is good.
	 *
	 * @param oper Operator used to search item.
	 * @param row Row be checked.
	 * @param column Column be checked.
	 * @return true if cell fits the criteria
	 */
	public boolean checkCell(JTableOperator oper, int row, int column);

    }

}

