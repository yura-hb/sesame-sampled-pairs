class DataInputStream extends FilterInputStream implements DataInput {
    /**
     * Reads some number of bytes from the contained input stream and
     * stores them into the buffer array &lt;code&gt;b&lt;/code&gt;. The number of
     * bytes actually read is returned as an integer. This method blocks
     * until input data is available, end of file is detected, or an
     * exception is thrown.
     *
     * &lt;p&gt;If &lt;code&gt;b&lt;/code&gt; is null, a &lt;code&gt;NullPointerException&lt;/code&gt; is
     * thrown. If the length of &lt;code&gt;b&lt;/code&gt; is zero, then no bytes are
     * read and &lt;code&gt;0&lt;/code&gt; is returned; otherwise, there is an attempt
     * to read at least one byte. If no byte is available because the
     * stream is at end of file, the value &lt;code&gt;-1&lt;/code&gt; is returned;
     * otherwise, at least one byte is read and stored into &lt;code&gt;b&lt;/code&gt;.
     *
     * &lt;p&gt;The first byte read is stored into element &lt;code&gt;b[0]&lt;/code&gt;, the
     * next one into &lt;code&gt;b[1]&lt;/code&gt;, and so on. The number of bytes read
     * is, at most, equal to the length of &lt;code&gt;b&lt;/code&gt;. Let &lt;code&gt;k&lt;/code&gt;
     * be the number of bytes actually read; these bytes will be stored in
     * elements &lt;code&gt;b[0]&lt;/code&gt; through &lt;code&gt;b[k-1]&lt;/code&gt;, leaving
     * elements &lt;code&gt;b[k]&lt;/code&gt; through &lt;code&gt;b[b.length-1]&lt;/code&gt;
     * unaffected.
     *
     * &lt;p&gt;The &lt;code&gt;read(b)&lt;/code&gt; method has the same effect as:
     * &lt;blockquote&gt;&lt;pre&gt;
     * read(b, 0, b.length)
     * &lt;/pre&gt;&lt;/blockquote&gt;
     *
     * @param      b   the buffer into which the data is read.
     * @return     the total number of bytes read into the buffer, or
     *             &lt;code&gt;-1&lt;/code&gt; if there is no more data because the end
     *             of the stream has been reached.
     * @exception  IOException if the first byte cannot be read for any reason
     * other than end of file, the stream has been closed and the underlying
     * input stream does not support reading after close, or another I/O
     * error occurs.
     * @see        java.io.FilterInputStream#in
     * @see        java.io.InputStream#read(byte[], int, int)
     */
    public final int read(byte b[]) throws IOException {
	return in.read(b, 0, b.length);
    }

}

