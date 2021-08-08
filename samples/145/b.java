import java.awt.*;
import javax.swing.*;

class JTreeTable extends JTable {
    class TreeTableCellRenderer extends JTree implements TableCellRenderer {
	/**
	 * This is overridden to set the height to match that of the JTable.
	 */
	public void setBounds(int x, int y, int w, int h) {
	    super.setBounds(x, 0, w, JTreeTable.this.getHeight());
	}

    }

}

