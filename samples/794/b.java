import java.util.Map;

class UnicastServerRef extends UnicastRef implements ServerRef, Dispatcher {
    /**
     * Discovers and sets the appropriate skeleton for the impl.
     */
    public void setSkeleton(Remote impl) throws RemoteException {
	if (!withoutSkeletons.containsKey(impl.getClass())) {
	    try {
		skel = Util.createSkeleton(impl);
	    } catch (SkeletonNotFoundException e) {
		/*
		 * Ignore exception for skeleton class not found, because a
		 * skeleton class is not necessary with the 1.2 stub protocol.
		 * Remember that this impl's class does not have a skeleton
		 * class so we don't waste time searching for it again.
		 */
		withoutSkeletons.put(impl.getClass(), null);
	    }
	}
    }

    /** cache of impl classes that have no corresponding skeleton class */
    private static final Map&lt;Class&lt;?&gt;, ?&gt; withoutSkeletons = Collections
	    .synchronizedMap(new WeakHashMap&lt;Class&lt;?&gt;, Void&gt;());
    /**
     * skeleton to dispatch remote calls through, for 1.1 stub protocol
     * (may be null if stub class only uses 1.2 stub protocol)
     */
    private transient Skeleton skel;

}

