import java.nio.ByteBuffer;
import java.util.*;
import jdk.internal.net.http.hpack.Encoder;

class Http2TestServerConnection {
    /** Encodes an ordered list of headers. */
    List&lt;ByteBuffer&gt; encodeHeadersOrdered(List&lt;Map.Entry&lt;String, String&gt;&gt; headers) {
	List&lt;ByteBuffer&gt; buffers = new LinkedList&lt;&gt;();

	ByteBuffer buf = getBuffer();
	boolean encoded;
	for (Map.Entry&lt;String, String&gt; entry : headers) {
	    String value = entry.getValue();
	    String key = entry.getKey().toLowerCase();
	    do {
		hpackOut.header(key, value);
		encoded = hpackOut.encode(buf);
		if (!encoded) {
		    buf.flip();
		    buffers.add(buf);
		    buf = getBuffer();
		}
	    } while (!encoded);
	}
	buf.flip();
	buffers.add(buf);
	return buffers;
    }

    volatile Encoder hpackOut;

    private ByteBuffer getBuffer() {
	return ByteBuffer.allocate(8 * 1024);
    }

}

