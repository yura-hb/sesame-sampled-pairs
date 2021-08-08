import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

abstract class AbstractMultiValuedMap&lt;K, V&gt; implements MultiValuedMap&lt;K, V&gt; {
    /**
     * Write the map out using a custom routine.
     * @param out the output stream
     * @throws IOException any of the usual I/O related exceptions
     */
    protected void doWriteObject(final ObjectOutputStream out) throws IOException {
	out.writeInt(map.size());
	for (final Map.Entry&lt;K, Collection&lt;V&gt;&gt; entry : map.entrySet()) {
	    out.writeObject(entry.getKey());
	    out.writeInt(entry.getValue().size());
	    for (final V value : entry.getValue()) {
		out.writeObject(value);
	    }
	}
    }

    /** The map used to store the data */
    private transient Map&lt;K, Collection&lt;V&gt;&gt; map;

}

