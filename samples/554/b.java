import java.io.*;

class InputLexer {
    /** Parses a long in US-ASCII encoding on the input stream */
    public long parseLong() throws IOException {
	skipWhitespace();
	byte b = readByte();
	if (!Character.isDigit((char) b)) {
	    error();
	}
	long l = 0;
	while (Character.isDigit((char) b)) {
	    l *= 10;
	    l += (b - '0');
	    b = readByte();
	}
	pushBack(b);
	return l;
    }

    private boolean pushedBack;
    private byte backBuf;
    private BufferedInputStream in;

    private void skipWhitespace() throws IOException {
	byte b;
	while (Character.isWhitespace((char) (b = readByte()))) {
	}
	pushBack(b);
    }

    /** Reads binary data; one byte */
    public byte readByte() throws IOException {
	if (pushedBack) {
	    pushedBack = false;
	    return backBuf;
	}
	return readByteInternal();
    }

    private void error() throws IOException {
	throw new IOException("Error parsing output of debug server");
    }

    private void pushBack(byte b) {
	if (pushedBack) {
	    throw new InternalError("Only one character pushback supported");
	}
	backBuf = b;
	pushedBack = true;
    }

    private byte readByteInternal() throws IOException {
	int i = in.read();
	if (i == -1) {
	    throw new IOException("End-of-file reached while reading from server");
	}
	return (byte) i;
    }

}

