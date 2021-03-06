import sun.net.util.IPAddressUtil;

class InetAddress implements Serializable {
    /**
     * Creates an InetAddress based on the provided host name and IP address.
     * No name service is checked for the validity of the address.
     *
     * &lt;p&gt; The host name can either be a machine name, such as
     * "{@code java.sun.com}", or a textual representation of its IP
     * address.
     * &lt;p&gt; No validity checking is done on the host name either.
     *
     * &lt;p&gt; If addr specifies an IPv4 address an instance of Inet4Address
     * will be returned; otherwise, an instance of Inet6Address
     * will be returned.
     *
     * &lt;p&gt; IPv4 address byte array must be 4 bytes long and IPv6 byte array
     * must be 16 bytes long
     *
     * @param host the specified host
     * @param addr the raw IP address in network byte order
     * @return  an InetAddress object created from the raw IP address.
     * @exception  UnknownHostException  if IP address is of illegal length
     * @since 1.4
     */
    public static InetAddress getByAddress(String host, byte[] addr) throws UnknownHostException {
	if (host != null && host.length() &gt; 0 && host.charAt(0) == '[') {
	    if (host.charAt(host.length() - 1) == ']') {
		host = host.substring(1, host.length() - 1);
	    }
	}
	if (addr != null) {
	    if (addr.length == Inet4Address.INADDRSZ) {
		return new Inet4Address(host, addr);
	    } else if (addr.length == Inet6Address.INADDRSZ) {
		byte[] newAddr = IPAddressUtil.convertFromIPv4MappedAddress(addr);
		if (newAddr != null) {
		    return new Inet4Address(host, newAddr);
		} else {
		    return new Inet6Address(host, addr);
		}
	    }
	}
	throw new UnknownHostException("addr is of illegal length");
    }

}

