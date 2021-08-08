import java.io.*;
import java.net.*;
import java.util.concurrent.*;

class SimpleOCSPServer {
    /**
     * Start the server.  The server will bind to the specified network
     * address and begin listening for incoming connections.
     *
     * @throws IOException if any number of things go wonky.
     */
    public synchronized void start() throws IOException {
	// You cannot start the server twice.
	if (started) {
	    log("Server has already been started");
	    return;
	} else {
	    started = true;
	}

	// Create and start the thread pool
	threadPool = Executors.newFixedThreadPool(32, new ThreadFactory() {
	    @Override
	    public Thread newThread(Runnable r) {
		Thread t = Executors.defaultThreadFactory().newThread(r);
		t.setDaemon(true);
		return t;
	    }
	});

	threadPool.submit(new Runnable() {
	    @Override
	    public void run() {
		try (ServerSocket sSock = new ServerSocket()) {
		    servSocket = sSock;
		    servSocket.setReuseAddress(true);
		    servSocket.setSoTimeout(500);
		    servSocket.bind(new InetSocketAddress(listenAddress, listenPort), 128);
		    log("Listening on " + servSocket.getLocalSocketAddress());

		    // Singal ready
		    serverReady = true;

		    // Update the listenPort with the new port number.  If
		    // the server is restarted, it will bind to the same
		    // port rather than picking a new one.
		    listenPort = servSocket.getLocalPort();

		    // Main dispatch loop
		    while (!receivedShutdown) {
			try {
			    Socket newConnection = servSocket.accept();
			    if (!acceptConnections) {
				try {
				    log("Reject connection");
				    newConnection.close();
				} catch (IOException e) {
				    // ignore
				}
				continue;
			    }
			    threadPool.submit(new OcspHandler(newConnection));
			} catch (SocketTimeoutException timeout) {
			    // Nothing to do here.  If receivedShutdown
			    // has changed to true then the loop will
			    // exit on its own.
			} catch (IOException ioe) {
			    // Something bad happened, log and force a shutdown
			    log("Unexpected Exception: " + ioe);
			    stop();
			}
		    }

		    log("Shutting down...");
		    threadPool.shutdown();
		} catch (IOException ioe) {
		    err(ioe);
		} finally {
		    // Reset state variables so the server can be restarted
		    receivedShutdown = false;
		    started = false;
		    serverReady = false;
		}
	    }
	});
    }

    private volatile boolean started = false;
    private ExecutorService threadPool;
    private ServerSocket servSocket;
    private InetAddress listenAddress;
    private int listenPort;
    private volatile boolean serverReady = false;
    private volatile boolean receivedShutdown = false;
    private volatile boolean acceptConnections = true;
    private boolean logEnabled = false;
    private final Debug debug = Debug.getInstance("oserv");

    /**
     * Log a message to stdout.
     *
     * @param message the message to log
     */
    private synchronized void log(String message) {
	if (logEnabled || debug != null) {
	    System.out.println("[" + Thread.currentThread().getName() + "]: " + message);
	}
    }

    /**
     * Stop the OCSP server.
     */
    public synchronized void stop() {
	if (started) {
	    receivedShutdown = true;
	    log("Received shutdown notification");
	}
    }

    /**
     * Log exception information on the stderr stream.
     *
     * @param exc the exception to dump information about
     */
    private static synchronized void err(Throwable exc) {
	System.out.print("[" + Thread.currentThread().getName() + "]: Exception: ");
	exc.printStackTrace(System.out);
    }

    class OcspHandler implements Runnable {
	private volatile boolean started = false;
	private ExecutorService threadPool;
	private ServerSocket servSocket;
	private InetAddress listenAddress;
	private int listenPort;
	private volatile boolean serverReady = false;
	private volatile boolean receivedShutdown = false;
	private volatile boolean acceptConnections = true;
	private boolean logEnabled = false;
	private final Debug debug = Debug.getInstance("oserv");

	/**
	 * Construct an {@code OcspHandler}.
	 *
	 * @param incomingSocket the socket the server created on accept()
	 */
	private OcspHandler(Socket incomingSocket) {
	    sock = incomingSocket;
	}

    }

}

