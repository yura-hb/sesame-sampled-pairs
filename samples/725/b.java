import java.awt.dnd.peer.DropTargetContextPeer;

class DropTargetContext implements Serializable {
    /**
     * This method signals that the drop is completed and
     * if it was successful or not.
     *
     * @param success true for success, false if not
     *
     * @throws InvalidDnDOperationException if a drop is not outstanding/extant
     */

    public void dropComplete(boolean success) throws InvalidDnDOperationException {
	DropTargetContextPeer peer = getDropTargetContextPeer();
	if (peer != null) {
	    peer.dropComplete(success);
	}
    }

    private transient DropTargetContextPeer dropTargetContextPeer;

    /**
     * Get the {@code DropTargetContextPeer}
     *
     * @return the platform peer
     */
    DropTargetContextPeer getDropTargetContextPeer() {
	return dropTargetContextPeer;
    }

}

