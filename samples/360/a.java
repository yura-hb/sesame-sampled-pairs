import org.datavec.api.writable.Writable;
import java.io.*;

class WritableUtils {
    /** Convert writables to a byte array */
    public static byte[] toByteArray(Writable... writables) {
	final DataOutputBuffer out = new DataOutputBuffer();
	try {
	    for (Writable w : writables) {
		w.write(out);
	    }
	    out.close();
	} catch (IOException e) {
	    throw new RuntimeException("Fail to convert writables to a byte array", e);
	}
	return out.getData();
    }

}

