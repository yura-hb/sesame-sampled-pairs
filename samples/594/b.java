import java.io.OutputStream;

abstract class ByteSink {
    /**
    * Writes all the given bytes to this sink.
    *
    * @throws IOException if an I/O occurs while writing to this sink
    */
    public void write(byte[] bytes) throws IOException {
	checkNotNull(bytes);

	Closer closer = Closer.create();
	try {
	    OutputStream out = closer.register(openStream());
	    out.write(bytes);
	    out.flush(); // https://code.google.com/p/guava-libraries/issues/detail?id=1330
	} catch (Throwable e) {
	    throw closer.rethrow(e);
	} finally {
	    closer.close();
	}
    }

    /**
    * Opens a new {@link OutputStream} for writing to this sink. This method returns a new,
    * independent stream each time it is called.
    *
    * &lt;p&gt;The caller is responsible for ensuring that the returned stream is closed.
    *
    * @throws IOException if an I/O error occurs while opening the stream
    */
    public abstract OutputStream openStream() throws IOException;

}

