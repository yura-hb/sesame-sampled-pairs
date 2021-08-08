import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

class Platform {
    /** Serializes and deserializes the specified object. */
    @SuppressWarnings("unchecked")
    static &lt;T&gt; T reserialize(T object) {
	checkNotNull(object);
	ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	try {
	    ObjectOutputStream out = new ObjectOutputStream(bytes);
	    out.writeObject(object);
	    ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
	    return (T) in.readObject();
	} catch (IOException | ClassNotFoundException e) {
	    throw new RuntimeException(e);
	}
    }

}

