import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

class SerializationUtils {
    /**
     * Deserialize an object from byte array.
     */
    public static Object deserialize(byte[] ba) throws IOException, ClassNotFoundException {
	try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(ba))) {
	    return in.readObject();
	}
    }

}

