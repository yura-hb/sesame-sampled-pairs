import java.net.InetAddress;

class InetAddresses {
    /**
    * Returns an address from a &lt;b&gt;little-endian ordered&lt;/b&gt; byte array (the opposite of what {@link
    * InetAddress#getByAddress} expects).
    *
    * &lt;p&gt;IPv4 address byte array must be 4 bytes long and IPv6 byte array must be 16 bytes long.
    *
    * @param addr the raw IP address in little-endian byte order
    * @return an InetAddress object created from the raw IP address
    * @throws UnknownHostException if IP address is of illegal length
    */
    public static InetAddress fromLittleEndianByteArray(byte[] addr) throws UnknownHostException {
	byte[] reversed = new byte[addr.length];
	for (int i = 0; i &lt; addr.length; i++) {
	    reversed[i] = addr[addr.length - i - 1];
	}
	return InetAddress.getByAddress(reversed);
    }

}

