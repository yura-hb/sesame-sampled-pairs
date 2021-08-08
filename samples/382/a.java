import org.eclipse.jdt.core.*;

abstract class Openable extends JavaElement implements IOpenable, IBufferChangedListener {
    /**
    * Close the buffer associated with this element, if any.
    */
    protected void closeBuffer() {
	if (!hasBuffer())
	    return; // nothing to do
	IBuffer buffer = getBufferManager().getBuffer(this);
	if (buffer != null) {
	    buffer.close();
	    buffer.removeBufferChangedListener(this);
	}
    }

    /**
    * Returns true if this element may have an associated source buffer,
    * otherwise false. Subclasses must override as required.
    */
    protected boolean hasBuffer() {
	return false;
    }

    /**
    * Returns the buffer manager for this element.
    */
    protected BufferManager getBufferManager() {
	return BufferManager.getDefaultBufferManager();
    }

}

