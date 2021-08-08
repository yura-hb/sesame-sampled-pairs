import com.sun.org.apache.xerces.internal.xni.XMLString;

class XMLStringBuffer extends XMLString {
    /**
     * append
     *
     * @param ch
     * @param offset
     * @param length
     */
    public void append(char[] ch, int offset, int length) {
	if (this.length + length &gt; this.ch.length) {
	    int newLength = this.ch.length * 2;
	    if (newLength &lt; this.ch.length + length + DEFAULT_SIZE) {
		newLength = this.ch.length + length + DEFAULT_SIZE;
	    }
	    char[] newch = new char[newLength];
	    System.arraycopy(this.ch, 0, newch, 0, this.length);
	    this.ch = newch;
	}
	//making the code more robust as it would handle null or 0 length data,
	//add the data only when it contains some thing
	if (ch != null && length &gt; 0) {
	    System.arraycopy(ch, offset, this.ch, this.length, length);
	    this.length += length;
	}
    }

    /** Default buffer size (32). */
    public static final int DEFAULT_SIZE = 32;

}

