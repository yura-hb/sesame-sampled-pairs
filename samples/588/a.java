class AeronUtil {
    /**
     * Return a reusable, parameterized {@link FragmentHandler} that prints to stdout
     *
     * @param streamId to show when printing
     * @return subscription data handler function that prints the message contents
     */
    public static FragmentHandler printStringMessage(final int streamId) {
	return (buffer, offset, length, header) -&gt; {
	    final byte[] data = new byte[length];
	    buffer.getBytes(offset, data);

	    System.out.println(String.format("Message to stream %d from session %d (%d@%d) &lt;&lt;%s&gt;&gt;", streamId,
		    header.sessionId(), length, offset, new String(data)));
	};
    }

}

