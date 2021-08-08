import java.util.*;

class MembershipRegistry {
    /**
     * Remove a key from the registry
     */
    void remove(MembershipKeyImpl key) {
	InetAddress group = key.group();
	List&lt;MembershipKeyImpl&gt; keys = groups.get(group);
	if (keys != null) {
	    Iterator&lt;MembershipKeyImpl&gt; i = keys.iterator();
	    while (i.hasNext()) {
		if (i.next() == key) {
		    i.remove();
		    break;
		}
	    }
	    if (keys.isEmpty()) {
		groups.remove(group);
	    }
	}
    }

    private Map&lt;InetAddress, List&lt;MembershipKeyImpl&gt;&gt; groups = null;

}

