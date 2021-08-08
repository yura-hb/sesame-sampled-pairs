class HexBin {
    /**
     * Encode a byte array to hex string
     *
     * @param binaryData array of byte to encode
     * @return return encoded string
     */
    static public String encode(byte[] binaryData) {
	if (binaryData == null)
	    return null;
	int lengthData = binaryData.length;
	int lengthEncode = lengthData * 2;
	char[] encodedData = new char[lengthEncode];
	int temp;
	for (int i = 0; i &lt; lengthData; i++) {
	    temp = binaryData[i];
	    if (temp &lt; 0)
		temp += 256;
	    encodedData[i * 2] = lookUpHexAlphabet[temp &gt;&gt; 4];
	    encodedData[i * 2 + 1] = lookUpHexAlphabet[temp & 0xf];
	}
	return new String(encodedData);
    }

    static final private char[] lookUpHexAlphabet = new char[LOOKUPLENGTH];

}

