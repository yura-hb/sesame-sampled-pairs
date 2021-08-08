import com.sun.jmx.defaults.JmxProperties;

class MBeanServerDelegate implements MBeanServerDelegateMBean, NotificationEmitter {
    /**
     * Returns the MBean server agent identity.
     *
     * @return the identity.
     */
    public synchronized String getMBeanServerId() {
	if (mbeanServerId == null) {
	    String localHost;
	    try {
		localHost = java.net.InetAddress.getLocalHost().getHostName();
	    } catch (java.net.UnknownHostException e) {
		JmxProperties.MISC_LOGGER.log(Level.TRACE,
			"Can't get local host name, " + "using \"localhost\" instead. Cause is: " + e);
		localHost = "localhost";
	    }
	    mbeanServerId = localHost + "_" + stamp;
	}
	return mbeanServerId;
    }

    /** The MBean server agent identification.*/
    private String mbeanServerId;
    private final long stamp;

}

