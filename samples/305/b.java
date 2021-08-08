import java.io.ByteArrayInputStream;

class AttributeClass {
    /**
     * Returns array of int values.
     */
    public int[] getArrayOfIntValues() {

	byte[] bufArray = (byte[]) myValue;
	if (bufArray != null) {

	    //ArrayList valList = new ArrayList();
	    ByteArrayInputStream bufStream = new ByteArrayInputStream(bufArray);
	    int available = bufStream.available();

	    // total number of values is at the end of the stream
	    bufStream.mark(available);
	    bufStream.skip(available - 1);
	    int length = bufStream.read();
	    bufStream.reset();

	    int[] valueArray = new int[length];
	    for (int i = 0; i &lt; length; i++) {
		// read length
		int valLength = bufStream.read();
		if (valLength != 4) {
		    // invalid data
		    return null;
		}

		byte[] bufBytes = new byte[valLength];
		bufStream.read(bufBytes, 0, valLength);
		valueArray[i] = convertToInt(bufBytes);

	    }
	    return valueArray;
	}
	return null;
    }

    private Object myValue;

    private int convertToInt(byte[] buf) {
	int intVal = 0;
	int pos = 0;
	intVal += unsignedByteToInt(buf[pos++]) &lt;&lt; 24;
	intVal += unsignedByteToInt(buf[pos++]) &lt;&lt; 16;
	intVal += unsignedByteToInt(buf[pos++]) &lt;&lt; 8;
	intVal += unsignedByteToInt(buf[pos++]) &lt;&lt; 0;
	return intVal;
    }

    private int unsignedByteToInt(byte b) {
	return (b & 0xff);
    }

}

