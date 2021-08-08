class JmxMBeanServer implements SunJmxMBeanServer {
    /**
     * Return the MBeanInstantiator associated to this MBeanServer.
     * @exception UnsupportedOperationException if
     *            {@link MBeanServerInterceptor}s
     *            are not enabled on this object.
     * @see #interceptorsEnabled
     **/
    public MBeanInstantiator getMBeanInstantiator() {
	if (interceptorsEnabled)
	    return instantiator;
	else
	    throw new UnsupportedOperationException("MBeanServerInterceptors are disabled.");
    }

    /** true if interceptors are enabled **/
    private final boolean interceptorsEnabled;
    private final MBeanInstantiator instantiator;

}

