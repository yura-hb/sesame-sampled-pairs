import java.io.*;

class InputLexer {
    /** Reads a block of binary data in BLOCKING fashion */
    public void readBytes(byte[] buf, int off, int len) throws IOException {
	int startIdx = off;
	int numRead = 0;
	if (pushedBack) {
	    buf[startIdx] = backBuf;
	    pushedBack = false;
	    ++startIdx;
	    ++numRead;
	}
	while (numRead &lt; len) {
	    numRead += in.read(buf, startIdx + numRead, len - numRead);
	}
	//    if (numRead != len) {
	//      throw new IOException("Only read " + numRead + " out of " +
	//                            len + " bytes requested");
	//    }
    }

    private boolean pushedBack;
    private byte backBuf;
    private BufferedInputStream in;

}

