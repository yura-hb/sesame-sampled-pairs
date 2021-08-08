import com.sun.jmx.remote.security.JMXPluggableAuthenticator;
import com.sun.jmx.remote.util.ClassLogger;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.rmi.server.RemoteServer;
import java.security.Principal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectorServer;
import javax.security.auth.Subject;

abstract class RMIServerImpl implements Closeable, RMIServer {
    /**
     * This method could be overridden by subclasses defined in this package
     * to perform additional operations specific to the underlying transport
     * before creating the new client connection.
     */
    RMIConnection doNewClient(Object credentials) throws IOException {
	final boolean tracing = logger.traceOn();

	if (tracing)
	    logger.trace("newClient", "making new client");

	if (getMBeanServer() == null)
	    throw new IllegalStateException("Not attached to an MBean server");

	Subject subject = null;
	JMXAuthenticator authenticator = (JMXAuthenticator) env.get(JMXConnectorServer.AUTHENTICATOR);
	if (authenticator == null) {
	    /*
	     * Create the JAAS-based authenticator only if authentication
	     * has been enabled
	     */
	    if (env.get("jmx.remote.x.password.file") != null || env.get("jmx.remote.x.login.config") != null) {
		authenticator = new JMXPluggableAuthenticator(env);
	    }
	}
	if (authenticator != null) {
	    if (tracing)
		logger.trace("newClient", "got authenticator: " + authenticator.getClass().getName());
	    try {
		subject = authenticator.authenticate(credentials);
	    } catch (SecurityException e) {
		logger.trace("newClient", "Authentication failed: " + e);
		throw e;
	    }
	}

	if (tracing) {
	    if (subject != null)
		logger.trace("newClient", "subject is not null");
	    else
		logger.trace("newClient", "no subject");
	}

	final String connectionId = makeConnectionId(getProtocol(), subject);

	if (tracing)
	    logger.trace("newClient", "making new connection: " + connectionId);

	RMIConnection client = makeClient(connectionId, subject);

	dropDeadReferences();
	WeakReference&lt;RMIConnection&gt; wr = new WeakReference&lt;RMIConnection&gt;(client);
	synchronized (clientList) {
	    clientList.add(wr);
	}

	connServer.connectionOpened(connectionId, "Connection opened", null);

	synchronized (clientList) {
	    if (!clientList.contains(wr)) {
		// can be removed only by a JMXConnectionNotification listener
		throw new IOException("The connection is refused.");
	    }
	}

	if (tracing)
	    logger.trace("newClient", "new connection done: " + connectionId);

	return client;
    }

    private static final ClassLogger logger = new ClassLogger("javax.management.remote.rmi", "RMIServerImpl");
    private final Map&lt;String, ?&gt; env;
    /** List of WeakReference values.  Each one references an
        RMIConnection created by this object, or null if the
        RMIConnection has been garbage-collected.  */
    private final List&lt;WeakReference&lt;RMIConnection&gt;&gt; clientList = new ArrayList&lt;WeakReference&lt;RMIConnection&gt;&gt;();
    private RMIConnectorServer connServer;
    private MBeanServer mbeanServer;
    private static int connectionIdNumber;

    /**
     * &lt;p&gt;The &lt;code&gt;MBeanServer&lt;/code&gt; to which this connector server
     * is attached.  This is the last value passed to {@link
     * #setMBeanServer} on this object, or null if that method has
     * never been called.&lt;/p&gt;
     *
     * @return the &lt;code&gt;MBeanServer&lt;/code&gt; to which this connector
     * is attached.
     *
     * @see #setMBeanServer
     */
    public synchronized MBeanServer getMBeanServer() {
	return mbeanServer;
    }

    /**
     * &lt;p&gt;Returns the protocol string for this object.  The string is
     * &lt;code&gt;rmi&lt;/code&gt; for RMI/JRMP.
     *
     * @return the protocol string for this object.
     */
    protected abstract String getProtocol();

    private static synchronized String makeConnectionId(String protocol, Subject subject) {
	connectionIdNumber++;

	String clientHost = "";
	try {
	    clientHost = RemoteServer.getClientHost();
	    /*
	     * According to the rules specified in the javax.management.remote
	     * package description, a numeric IPv6 address (detected by the
	     * presence of otherwise forbidden ":" character) forming a part
	     * of the connection id must be enclosed in square brackets.
	     */
	    if (clientHost.contains(":")) {
		clientHost = "[" + clientHost + "]";
	    }
	} catch (ServerNotActiveException e) {
	    logger.trace("makeConnectionId", "getClientHost", e);
	}

	final StringBuilder buf = new StringBuilder();
	buf.append(protocol).append(":");
	if (clientHost.length() &gt; 0)
	    buf.append("//").append(clientHost);
	buf.append(" ");
	if (subject != null) {
	    Set&lt;Principal&gt; principals = subject.getPrincipals();
	    String sep = "";
	    for (Iterator&lt;Principal&gt; it = principals.iterator(); it.hasNext();) {
		Principal p = it.next();
		String name = p.getName().replace(' ', '_').replace(';', ':');
		buf.append(sep).append(name);
		sep = ";";
	    }
	}
	buf.append(" ").append(connectionIdNumber);
	if (logger.traceOn())
	    logger.trace("newConnectionId", "connectionId=" + buf);
	return buf.toString();
    }

    /**
     * &lt;p&gt;Creates a new client connection.  This method is called by
     * the public method {@link #newClient(Object)}.&lt;/p&gt;
     *
     * @param connectionId the ID of the new connection.  Every
     * connection opened by this connector server will have a
     * different ID.  The behavior is unspecified if this parameter is
     * null.
     *
     * @param subject the authenticated subject.  Can be null.
     *
     * @return the newly-created &lt;code&gt;RMIConnection&lt;/code&gt;.
     *
     * @exception IOException if the new client object cannot be
     * created or exported.
     */
    protected abstract RMIConnection makeClient(String connectionId, Subject subject) throws IOException;

    private void dropDeadReferences() {
	synchronized (clientList) {
	    for (Iterator&lt;WeakReference&lt;RMIConnection&gt;&gt; it = clientList.iterator(); it.hasNext();) {
		WeakReference&lt;RMIConnection&gt; wr = it.next();
		if (wr.get() == null)
		    it.remove();
	    }
	}
    }

}

