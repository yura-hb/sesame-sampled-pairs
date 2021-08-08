import javax.swing.*;

class BasicTreeUI extends TreeUI {
    /**
     * Returns an instance of {@code TreeCellEditor}.
     *
     * @return an instance of {@code TreeCellEditor}
     */
    protected TreeCellEditor getCellEditor() {
	return (tree != null) ? tree.getCellEditor() : null;
    }

    /** Component that we're going to be drawing into. */
    protected JTree tree;

}

