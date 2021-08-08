import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

class SerializationUtils {
    /**
     * Performs a serialization roundtrip. Serializes and deserializes the given object, great for testing objects that
     * implement {@link Serializable}.
     *
     * @param &lt;T&gt;
     *           the type of the object involved
     * @param msg
     *            the object to roundtrip
     * @return the serialized and deserialized object
     * @since 3.3
     */
    @SuppressWarnings("unchecked") // OK, because we serialized a type `T`
    public static &lt;T extends Serializable&gt; T roundtrip(final T msg) {
	return (T) deserialize(serialize(msg));
    }

    /**
     * &lt;p&gt;Serializes an {@code Object} to a byte array for
     * storage/serialization.&lt;/p&gt;
     *
     * @param obj  the object to serialize to bytes
     * @return a byte[] with the converted Serializable
     * @throws SerializationException (runtime) if the serialization fails
     */
    public static byte[] serialize(final Serializable obj) {
	final ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
	serialize(obj, baos);
	return baos.toByteArray();
    }

    /**
     * &lt;p&gt;
     * Deserializes a single {@code Object} from an array of bytes.
     * &lt;/p&gt;
     *
     * &lt;p&gt;
     * If the call site incorrectly types the return value, a {@link ClassCastException} is thrown from the call site.
     * Without Generics in this declaration, the call site must type cast and can cause the same ClassCastException.
     * Note that in both cases, the ClassCastException is in the call site, not in this method.
     * &lt;/p&gt;
     *
     * @param &lt;T&gt;  the object type to be deserialized
     * @param objectData
     *            the serialized object, must not be null
     * @return the deserialized object
     * @throws IllegalArgumentException
     *             if {@code objectData} is {@code null}
     * @throws SerializationException
     *             (runtime) if the serialization fails
     */
    public static &lt;T&gt; T deserialize(final byte[] objectData) {
	Validate.isTrue(objectData != null, "The byte[] must not be null");
	return deserialize(new ByteArrayInputStream(objectData));
    }

    /**
     * &lt;p&gt;Serializes an {@code Object} to the specified stream.&lt;/p&gt;
     *
     * &lt;p&gt;The stream will be closed once the object is written.
     * This avoids the need for a finally clause, and maybe also exception
     * handling, in the application code.&lt;/p&gt;
     *
     * &lt;p&gt;The stream passed in is not buffered internally within this method.
     * This is the responsibility of your application if desired.&lt;/p&gt;
     *
     * @param obj  the object to serialize to bytes, may be null
     * @param outputStream  the stream to write to, must not be null
     * @throws IllegalArgumentException if {@code outputStream} is {@code null}
     * @throws SerializationException (runtime) if the serialization fails
     */
    public static void serialize(final Serializable obj, final OutputStream outputStream) {
	Validate.isTrue(outputStream != null, "The OutputStream must not be null");
	try (ObjectOutputStream out = new ObjectOutputStream(outputStream)) {
	    out.writeObject(obj);
	} catch (final IOException ex) {
	    throw new SerializationException(ex);
	}
    }

    /**
     * &lt;p&gt;
     * Deserializes an {@code Object} from the specified stream.
     * &lt;/p&gt;
     *
     * &lt;p&gt;
     * The stream will be closed once the object is written. This avoids the need for a finally clause, and maybe also
     * exception handling, in the application code.
     * &lt;/p&gt;
     *
     * &lt;p&gt;
     * The stream passed in is not buffered internally within this method. This is the responsibility of your
     * application if desired.
     * &lt;/p&gt;
     *
     * &lt;p&gt;
     * If the call site incorrectly types the return value, a {@link ClassCastException} is thrown from the call site.
     * Without Generics in this declaration, the call site must type cast and can cause the same ClassCastException.
     * Note that in both cases, the ClassCastException is in the call site, not in this method.
     * &lt;/p&gt;
     *
     * @param &lt;T&gt;  the object type to be deserialized
     * @param inputStream
     *            the serialized object input stream, must not be null
     * @return the deserialized object
     * @throws IllegalArgumentException
     *             if {@code inputStream} is {@code null}
     * @throws SerializationException
     *             (runtime) if the serialization fails
     */
    public static &lt;T&gt; T deserialize(final InputStream inputStream) {
	Validate.isTrue(inputStream != null, "The InputStream must not be null");
	try (ObjectInputStream in = new ObjectInputStream(inputStream)) {
	    @SuppressWarnings("unchecked")
	    final T obj = (T) in.readObject();
	    return obj;
	} catch (final ClassNotFoundException | IOException ex) {
	    throw new SerializationException(ex);
	}
    }

}

