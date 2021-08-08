import java.util.List;
import javax.management.ObjectName;

abstract class Monitor extends NotificationBroadcasterSupport implements MonitorMBean, MBeanRegistration {
    /**
     * Removes the specified object from the set of observed MBeans.
     *
     * @param object The object to remove.
     *
     */
    public synchronized void removeObservedObject(ObjectName object) {
	// Check for null object.
	//
	if (object == null)
	    return;

	final ObservedObject o = getObservedObject(object);
	if (o != null) {
	    // Remove the specified object from the list.
	    //
	    observedObjects.remove(o);
	    // Update legacy protected stuff.
	    //
	    createAlreadyNotified();
	}
    }

    /**
     * List of ObservedObjects to which the attribute to observe belongs.
     */
    final List&lt;ObservedObject&gt; observedObjects = new CopyOnWriteArrayList&lt;ObservedObject&gt;();
    /**
     * The number of valid components in the vector of observed objects.
     *
     */
    protected int elementCount = 0;
    /**
     * &lt;p&gt;Selected monitor errors that have already been notified.&lt;/p&gt;
     *
     * &lt;p&gt;Each element in this array corresponds to an observed object
     * in the vector.  It contains a bit mask of the flags {@link
     * #OBSERVED_OBJECT_ERROR_NOTIFIED} etc, indicating whether the
     * corresponding notification has already been sent for the MBean
     * being monitored.&lt;/p&gt;
     *
     */
    protected int alreadyNotifieds[] = new int[capacityIncrement];
    /**
     * Monitor errors that have already been notified.
     * @deprecated equivalent to {@link #alreadyNotifieds}[0].
     */
    @Deprecated
    protected int alreadyNotified = 0;

    /**
     * Get the specified {@code ObservedObject} if this object is
     * contained in the set of observed MBeans, or {@code null}
     * otherwise.
     *
     * @param object the name of the {@code ObservedObject} to retrieve.
     *
     * @return The {@code ObservedObject} associated to the supplied
     * {@code ObjectName}.
     *
     * @since 1.6
     */
    synchronized ObservedObject getObservedObject(ObjectName object) {
	for (ObservedObject o : observedObjects)
	    if (o.getObservedObject().equals(object))
		return o;
	return null;
    }

    /**
     * Create the {@link #alreadyNotified} array from
     * the {@code ObservedObject} array list.
     */
    synchronized void createAlreadyNotified() {
	// Update elementCount.
	//
	elementCount = observedObjects.size();

	// Update arrays.
	//
	alreadyNotifieds = new int[elementCount];
	for (int i = 0; i &lt; elementCount; i++) {
	    alreadyNotifieds[i] = observedObjects.get(i).getAlreadyNotified();
	}
	updateDeprecatedAlreadyNotified();
    }

    /**
     * Update the deprecated {@link #alreadyNotified} field.
     */
    synchronized void updateDeprecatedAlreadyNotified() {
	if (elementCount &gt; 0)
	    alreadyNotified = alreadyNotifieds[0];
	else
	    alreadyNotified = 0;
    }

    class ObservedObject {
	/**
	* List of ObservedObjects to which the attribute to observe belongs.
	*/
	final List&lt;ObservedObject&gt; observedObjects = new CopyOnWriteArrayList&lt;ObservedObject&gt;();
	/**
	* The number of valid components in the vector of observed objects.
	*
	*/
	protected int elementCount = 0;
	/**
	* &lt;p&gt;Selected monitor errors that have already been notified.&lt;/p&gt;
	*
	* &lt;p&gt;Each element in this array corresponds to an observed object
	* in the vector.  It contains a bit mask of the flags {@link
	* #OBSERVED_OBJECT_ERROR_NOTIFIED} etc, indicating whether the
	* corresponding notification has already been sent for the MBean
	* being monitored.&lt;/p&gt;
	*
	*/
	protected int alreadyNotifieds[] = new int[capacityIncrement];
	/**
	* Monitor errors that have already been notified.
	* @deprecated equivalent to {@link #alreadyNotifieds}[0].
	*/
	@Deprecated
	protected int alreadyNotified = 0;

	public final ObjectName getObservedObject() {
	    return observedObject;
	}

	public final synchronized int getAlreadyNotified() {
	    return alreadyNotified;
	}

    }

}

