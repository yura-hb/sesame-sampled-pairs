import java.awt.Graphics;
import javax.swing.JTree;

class TreeTableCellRenderer extends JTree implements TableCellRenderer {
    /**
     * Subclassed to translate the graphics such that the last visible
     * row will be drawn at 0,0.
     */
    @Override
    public void paint(Graphics graph) {
	graph.translate(0, -visibleRow * getRowHeight());
	super.paint(graph);
    }

    /** Last table/tree row asked to renderer. */
    private int visibleRow;

}

