import com.sun.beans.decoder.DocumentHandler;
import java.security.AccessController;

class XMLDecoder implements AutoCloseable {
    /**
     * Reads the next object from the underlying input stream.
     *
     * @return the next object read
     *
     * @throws ArrayIndexOutOfBoundsException if the stream contains no objects
     *         (or no more objects)
     *
     * @see XMLEncoder#writeObject
     */
    public Object readObject() {
	return (parsingComplete()) ? this.array[this.index++] : null;
    }

    private Object[] array;
    private int index;
    private final InputSource input;
    private final AccessControlContext acc = AccessController.getContext();
    private final DocumentHandler handler = new DocumentHandler();

    private boolean parsingComplete() {
	if (this.input == null) {
	    return false;
	}
	if (this.array == null) {
	    if ((this.acc == null) && (null != System.getSecurityManager())) {
		throw new SecurityException("AccessControlContext is not set");
	    }
	    AccessController.doPrivileged(new PrivilegedAction&lt;Void&gt;() {
		public Void run() {
		    XMLDecoder.this.handler.parse(XMLDecoder.this.input);
		    return null;
		}
	    }, this.acc);
	    this.array = this.handler.getObjects();
	}
	return true;
    }

}

