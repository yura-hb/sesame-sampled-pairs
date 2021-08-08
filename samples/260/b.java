import static sun.security.pkcs11.wrapper.PKCS11Constants.*;

class Functions {
    /**
     * converts the long value state to a SessionState string
     *
     * @param state the state to be converted
     * @return the SessionState string representation of the state
     */
    public static String sessionStateToString(long state) {
	String name;

	if (state == CKS_RO_PUBLIC_SESSION) {
	    name = "CKS_RO_PUBLIC_SESSION";
	} else if (state == CKS_RO_USER_FUNCTIONS) {
	    name = "CKS_RO_USER_FUNCTIONS";
	} else if (state == CKS_RW_PUBLIC_SESSION) {
	    name = "CKS_RW_PUBLIC_SESSION";
	} else if (state == CKS_RW_USER_FUNCTIONS) {
	    name = "CKS_RW_USER_FUNCTIONS";
	} else if (state == CKS_RW_SO_FUNCTIONS) {
	    name = "CKS_RW_SO_FUNCTIONS";
	} else {
	    name = "ERROR: unknown session state 0x" + toFullHexString(state);
	}

	return name;
    }

    /**
     * For converting numbers to their hex presentation.
     */
    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

    /**
     * Converts a long value to a hexadecimal String of length 16. Includes
     * leading zeros if necessary.
     *
     * @param value The long value to be converted.
     * @return The hexadecimal string representation of the long value.
     */
    public static String toFullHexString(long value) {
	long currentValue = value;
	StringBuilder sb = new StringBuilder(16);
	for (int j = 0; j &lt; 16; j++) {
	    int currentDigit = (int) currentValue & 0xf;
	    sb.append(HEX_DIGITS[currentDigit]);
	    currentValue &gt;&gt;&gt;= 4;
	}

	return sb.reverse().toString();
    }

}

