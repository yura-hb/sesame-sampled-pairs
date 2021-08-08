import org.datavec.api.io.WritableUtils;

class Text extends BinaryComparable implements WritableComparable&lt;BinaryComparable&gt; {
    /** Skips over one Text in the input. */
    public static void skip(DataInput in) throws IOException {
	int length = WritableUtils.readVInt(in);
	WritableUtils.skipFully(in, length);
    }

}

