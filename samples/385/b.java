import java.util.*;
import java.awt.*;
import javax.swing.table.*;
import sun.swing.SwingUtilities2;

class JTable extends JComponent implements TableModelListener, Scrollable, TableColumnModelListener,
	ListSelectionListener, CellEditorListener, Accessible, RowSorterListener {
    class AccessibleJTable extends AccessibleJComponent
	    implements AccessibleSelection, ListSelectionListener, TableModelListener, TableColumnModelListener,
	    CellEditorListener, PropertyChangeListener, AccessibleExtendedTable {
	class AccessibleJTableCell extends AccessibleContext implements Accessible, AccessibleComponent {
	    /**
	     * Gets the AccessibleContext for the table cell renderer.
	     *
	     * @return the &lt;code&gt;AccessibleContext&lt;/code&gt; for the table
	     * cell renderer if one exists;
	     * otherwise, returns &lt;code&gt;null&lt;/code&gt;.
	     * @since 1.6
	     */
	    protected AccessibleContext getCurrentAccessibleContext() {
		TableColumn aColumn = getColumnModel().getColumn(column);
		TableCellRenderer renderer = aColumn.getCellRenderer();
		if (renderer == null) {
		    Class&lt;?&gt; columnClass = getColumnClass(column);
		    renderer = getDefaultRenderer(columnClass);
		}
		Component component = renderer.getTableCellRendererComponent(JTable.this, getValueAt(row, column),
			false, false, row, column);
		if (component instanceof Accessible) {
		    return component.getAccessibleContext();
		} else {
		    return null;
		}
	    }

	    private int column;
	    private int row;

	}

    }

    /** The &lt;code&gt;TableColumnModel&lt;/code&gt; of the table. */
    protected TableColumnModel columnModel;
    /**
     * A table of objects that display the contents of a cell,
     * indexed by class as declared in &lt;code&gt;getColumnClass&lt;/code&gt;
     * in the &lt;code&gt;TableModel&lt;/code&gt; interface.
     */
    protected transient Hashtable&lt;Object, Object&gt; defaultRenderersByColumnClass;
    /** The &lt;code&gt;TableModel&lt;/code&gt; of the table. */
    protected TableModel dataModel;
    /**
     * Information used in sorting.
     */
    private transient SortManager sortManager;

    /**
     * Returns the {@code TableColumnModel} that contains all column information
     * of this table.
     *
     * @return the object that provides the column state of the table
     * @see #setColumnModel
     */
    public TableColumnModel getColumnModel() {
	return columnModel;
    }

    /**
     * Returns the type of the column appearing in the view at
     * column position &lt;code&gt;column&lt;/code&gt;.
     *
     * @param   column   the column in the view being queried
     * @return the type of the column at position &lt;code&gt;column&lt;/code&gt;
     *          in the view where the first column is column 0
     */
    public Class&lt;?&gt; getColumnClass(int column) {
	return getModel().getColumnClass(convertColumnIndexToModel(column));
    }

    /**
     * Returns the cell renderer to be used when no renderer has been set in
     * a &lt;code&gt;TableColumn&lt;/code&gt;. During the rendering of cells the renderer is fetched from
     * a &lt;code&gt;Hashtable&lt;/code&gt; of entries according to the class of the cells in the column. If
     * there is no entry for this &lt;code&gt;columnClass&lt;/code&gt; the method returns
     * the entry for the most specific superclass. The &lt;code&gt;JTable&lt;/code&gt; installs entries
     * for &lt;code&gt;Object&lt;/code&gt;, &lt;code&gt;Number&lt;/code&gt;, and &lt;code&gt;Boolean&lt;/code&gt;, all of which can be modified
     * or replaced.
     *
     * @param   columnClass   return the default cell renderer
     *                        for this columnClass
     * @return  the renderer for this columnClass
     * @see     #setDefaultRenderer
     * @see     #getColumnClass
     */
    public TableCellRenderer getDefaultRenderer(Class&lt;?&gt; columnClass) {
	if (columnClass == null) {
	    return null;
	} else {
	    Object renderer = defaultRenderersByColumnClass.get(columnClass);
	    if (renderer != null) {
		return (TableCellRenderer) renderer;
	    } else {
		Class&lt;?&gt; c = columnClass.getSuperclass();
		if (c == null && columnClass != Object.class) {
		    c = Object.class;
		}
		return getDefaultRenderer(c);
	    }
	}
    }

    /**
     * Returns the cell value at &lt;code&gt;row&lt;/code&gt; and &lt;code&gt;column&lt;/code&gt;.
     * &lt;p&gt;
     * &lt;b&gt;Note&lt;/b&gt;: The column is specified in the table view's display
     *              order, and not in the &lt;code&gt;TableModel&lt;/code&gt;'s column
     *              order.  This is an important distinction because as the
     *              user rearranges the columns in the table,
     *              the column at a given index in the view will change.
     *              Meanwhile the user's actions never affect the model's
     *              column ordering.
     *
     * @param   row             the row whose value is to be queried
     * @param   column          the column whose value is to be queried
     * @return  the Object at the specified cell
     */
    public Object getValueAt(int row, int column) {
	return getModel().getValueAt(convertRowIndexToModel(row), convertColumnIndexToModel(column));
    }

    /**
     * Returns the {@code TableModel} that provides the data displayed by this
     * {@code JTable}.
     *
     * @return the {@code TableModel} that provides the data displayed by this
     *         {@code JTable}
     * @see #setModel
     */
    public TableModel getModel() {
	return dataModel;
    }

    /**
     * Maps the index of the column in the view at
     * &lt;code&gt;viewColumnIndex&lt;/code&gt; to the index of the column
     * in the table model.  Returns the index of the corresponding
     * column in the model.  If &lt;code&gt;viewColumnIndex&lt;/code&gt;
     * is less than zero, returns &lt;code&gt;viewColumnIndex&lt;/code&gt;.
     *
     * @param   viewColumnIndex     the index of the column in the view
     * @return  the index of the corresponding column in the model
     *
     * @see #convertColumnIndexToView
     */
    public int convertColumnIndexToModel(int viewColumnIndex) {
	return SwingUtilities2.convertColumnIndexToModel(getColumnModel(), viewColumnIndex);
    }

    /**
     * Maps the index of the row in terms of the view to the
     * underlying &lt;code&gt;TableModel&lt;/code&gt;.  If the contents of the
     * model are not sorted the model and view indices are the same.
     *
     * @param viewRowIndex the index of the row in the view
     * @return the index of the corresponding row in the model
     * @throws IndexOutOfBoundsException if sorting is enabled and passed an
     *         index outside the range of the &lt;code&gt;JTable&lt;/code&gt; as
     *         determined by the method &lt;code&gt;getRowCount&lt;/code&gt;
     * @see javax.swing.table.TableRowSorter
     * @see #getRowCount
     * @since 1.6
     */
    public int convertRowIndexToModel(int viewRowIndex) {
	RowSorter&lt;?&gt; sorter = getRowSorter();
	if (sorter != null) {
	    return sorter.convertRowIndexToModel(viewRowIndex);
	}
	return viewRowIndex;
    }

    /**
     * Returns the object responsible for sorting.
     *
     * @return the object responsible for sorting
     * @since 1.6
     */
    public RowSorter&lt;? extends TableModel&gt; getRowSorter() {
	return (sortManager != null) ? sortManager.sorter : null;
    }

}

