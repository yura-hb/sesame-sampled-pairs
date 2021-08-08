import java.util.Collections;
import java.util.concurrent.ConcurrentSkipListMap;

class SocketPermissionCollection extends PermissionCollection implements Serializable {
    /**
     * Returns an enumeration of all the SocketPermission objects in the
     * container.
     *
     * @return an enumeration of all the SocketPermission objects.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Enumeration&lt;Permission&gt; elements() {
	return (Enumeration) Collections.enumeration(perms.values());
    }

    private transient ConcurrentSkipListMap&lt;String, SocketPermission&gt; perms;

}

