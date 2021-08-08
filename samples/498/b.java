import java.net.InetAddress;
import java.security.Permission;

class SocketPermission extends Permission implements Serializable {
    /**
     * attempt to get the fully qualified domain name
     *
     */
    void getCanonName() throws UnknownHostException {
	if (cname != null || invalid || untrusted)
	    return;

	// attempt to get the canonical name

	try {
	    // first get the IP addresses if we don't have them yet
	    // this is because we need the IP address to then get
	    // FQDN.
	    if (addresses == null) {
		getIP();
	    }

	    // we have to do this check, otherwise we might not
	    // get the fully qualified domain name
	    if (init_with_ip) {
		cname = addresses[0].getHostName(false).toLowerCase();
	    } else {
		cname = InetAddress.getByName(addresses[0].getHostAddress()).getHostName(false).toLowerCase();
	    }
	} catch (UnknownHostException uhe) {
	    invalid = true;
	    throw uhe;
	}
    }

    private transient String cname;
    private transient boolean invalid;
    private transient boolean untrusted;
    private transient InetAddress[] addresses;
    private transient boolean init_with_ip;
    private transient boolean wildcard;

    /**
     * get IP addresses. Sets invalid to true if we can't get them.
     *
     */
    void getIP() throws UnknownHostException {
	if (addresses != null || wildcard || invalid)
	    return;

	try {
	    // now get all the IP addresses
	    String host;
	    if (getName().charAt(0) == '[') {
		// Literal IPv6 address
		host = getName().substring(1, getName().indexOf(']'));
	    } else {
		int i = getName().indexOf(':');
		if (i == -1)
		    host = getName();
		else {
		    host = getName().substring(0, i);
		}
	    }

	    addresses = new InetAddress[] { InetAddress.getAllByName0(host, false)[0] };

	} catch (UnknownHostException uhe) {
	    invalid = true;
	    throw uhe;
	} catch (IndexOutOfBoundsException iobe) {
	    invalid = true;
	    throw new UnknownHostException(getName());
	}
    }

}

