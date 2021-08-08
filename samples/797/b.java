import java.security.AccessController;

class DatagramSocket implements Closeable {
    /**
     * Returns the port number on the local host to which this socket
     * is bound.
     *
     * @return  the port number on the local host to which this socket is bound,
                {@code -1} if the socket is closed, or
                {@code 0} if it is not bound yet.
     */
    public int getLocalPort() {
	if (isClosed())
	    return -1;
	try {
	    return getImpl().getLocalPort();
	} catch (Exception e) {
	    return 0;
	}
    }

    private Object closeLock = new Object();
    private boolean closed = false;
    /**
     * Various states of this socket.
     */
    private boolean created = false;
    DatagramSocketImpl impl;
    /**
     * User defined factory for all datagram sockets.
     */
    static DatagramSocketImplFactory factory;
    /**
     * Are we using an older DatagramSocketImpl?
     */
    boolean oldImpl = false;

    /**
     * Returns whether the socket is closed or not.
     *
     * @return true if the socket has been closed
     * @since 1.4
     */
    public boolean isClosed() {
	synchronized (closeLock) {
	    return closed;
	}
    }

    /**
     * Get the {@code DatagramSocketImpl} attached to this socket,
     * creating it if necessary.
     *
     * @return  the {@code DatagramSocketImpl} attached to that
     *          DatagramSocket
     * @throws SocketException if creation fails.
     * @since 1.4
     */
    DatagramSocketImpl getImpl() throws SocketException {
	if (!created)
	    createImpl();
	return impl;
    }

    void createImpl() throws SocketException {
	if (impl == null) {
	    if (factory != null) {
		impl = factory.createDatagramSocketImpl();
		checkOldImpl();
	    } else {
		boolean isMulticast = (this instanceof MulticastSocket) ? true : false;
		impl = DefaultDatagramSocketImplFactory.createDatagramSocketImpl(isMulticast);

		checkOldImpl();
	    }
	}
	// creates a udp socket
	impl.create();
	impl.setDatagramSocket(this);
	created = true;
    }

    private void checkOldImpl() {
	if (impl == null)
	    return;
	// DatagramSocketImpl.peekdata() is a protected method, therefore we need to use
	// getDeclaredMethod, therefore we need permission to access the member
	try {
	    AccessController.doPrivileged(new PrivilegedExceptionAction&lt;&gt;() {
		public Void run() throws NoSuchMethodException {
		    Class&lt;?&gt;[] cl = new Class&lt;?&gt;[1];
		    cl[0] = DatagramPacket.class;
		    impl.getClass().getDeclaredMethod("peekData", cl);
		    return null;
		}
	    });
	} catch (java.security.PrivilegedActionException e) {
	    oldImpl = true;
	}
    }

}

