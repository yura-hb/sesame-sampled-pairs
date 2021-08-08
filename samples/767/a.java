import java.util.ConcurrentModificationException;

class CursorableLinkedList&lt;E&gt; extends AbstractLinkedList&lt;E&gt; implements Serializable {
    class Cursor&lt;E&gt; extends LinkedListIterator&lt;E&gt; {
	/**
	 * Override superclass modCount check, and replace it with our valid flag.
	 */
	@Override
	protected void checkModCount() {
	    if (!valid) {
		throw new ConcurrentModificationException("Cursor closed");
	    }
	}

	/** Is the cursor valid (not closed) */
	boolean valid = true;

    }

}

