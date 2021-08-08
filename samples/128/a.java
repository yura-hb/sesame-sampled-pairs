class Base64Coding {
    /**
     * Helper method for encoding an array of bytes as a Base64 String.
     */
    public static String encode64(byte[] b) {
	StringBuffer sb = new StringBuffer((b.length / 3) * 4);

	int i = 0;
	int remaining = b.length;
	char c[] = new char[4];
	while (remaining &gt; 0) {
	    // Three input bytes are encoded as four chars (6 bits) as
	    // 00000011 11112222 22333333

	    c[0] = (char) ((b[i] & 0xFC) &gt;&gt; 2);
	    c[1] = (char) ((b[i] & 0x03) &lt;&lt; 4);
	    if (remaining &gt;= 2) {
		c[1] += (char) ((b[i + 1] & 0xF0) &gt;&gt; 4);
		c[2] = (char) ((b[i + 1] & 0x0F) &lt;&lt; 2);
		if (remaining &gt;= 3) {
		    c[2] += (char) ((b[i + 2] & 0xC0) &gt;&gt; 6);
		    c[3] = (char) (b[i + 2] & 0x3F);
		} else {
		    c[3] = 64;
		}
	    } else {
		c[2] = 64;
		c[3] = 64;
	    }

	    // Convert to base64 chars
	    for (int j = 0; j &lt; 4; j++) {
		if (c[j] &lt; 26) {
		    c[j] += 'A';
		} else if (c[j] &lt; 52) {
		    c[j] = (char) (c[j] - 26 + 'a');
		} else if (c[j] &lt; 62) {
		    c[j] = (char) (c[j] - 52 + '0');
		} else if (c[j] == 62) {
		    c[j] = '+';
		} else if (c[j] == 63) {
		    c[j] = '/';
		} else {
		    c[j] = '=';
		}
	    }

	    sb.append(c);
	    i += 3;
	    remaining -= 3;
	}

	return sb.toString();
    }

}

