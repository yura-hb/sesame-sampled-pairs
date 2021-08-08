import java.awt.datatransfer.*;
import java.awt.dnd.*;

class TransferHandler implements Serializable {
    class TransferSupport {
	/**
	 * Returns whether or not the given data flavor is supported.
	 *
	 * @param df the &lt;code&gt;DataFlavor&lt;/code&gt; to test
	 * @return whether or not the given flavor is supported.
	 */
	public boolean isDataFlavorSupported(DataFlavor df) {
	    if (isDrop) {
		if (source instanceof DropTargetDragEvent) {
		    return ((DropTargetDragEvent) source).isDataFlavorSupported(df);
		} else {
		    return ((DropTargetDropEvent) source).isDataFlavorSupported(df);
		}
	    }

	    return ((Transferable) source).isDataFlavorSupported(df);
	}

	private boolean isDrop;
	/**
	 * The source is a {@code DropTargetDragEvent} or
	 * {@code DropTargetDropEvent} for drops,
	 * and a {@code Transferable} otherwise
	 */
	private Object source;

    }

}

