class TIFFLZWUtil {
    /**
     * Add a new string to the string table.
     */
    public void addStringToTable(byte oldString[], byte newString) {
	int length = oldString.length;
	byte string[] = new byte[length + 1];
	System.arraycopy(oldString, 0, string, 0, length);
	string[length] = newString;

	// Add this new String to the table
	stringTable[tableIndex++] = string;

	if (tableIndex == 511) {
	    bitsToGet = 10;
	} else if (tableIndex == 1023) {
	    bitsToGet = 11;
	} else if (tableIndex == 2047) {
	    bitsToGet = 12;
	}
    }

    byte stringTable[][];
    int tableIndex, bitsToGet = 9;
    int tableIndex, bitsToGet = 9;

}

