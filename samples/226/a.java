import org.datavec.api.io.WritableComparator;

class IOUtils {
    /** Lexicographic order of binary data. */
    public static int compareBytes(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
	return WritableComparator.compareBytes(b1, s1, l1, b2, s2, l2);
    }

}

