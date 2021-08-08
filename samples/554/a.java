import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;

class TarBuffer {
    /**
     * Skip over a record on the input stream.
     * @throws IOException on error
     */
    public void skipRecord() throws IOException {
	if (debug) {
	    System.err.println("SkipRecord: recIdx = " + currRecIdx + " blkIdx = " + currBlkIdx);
	}

	if (inStream == null) {
	    throw new IOException("reading (via skip) from an output buffer");
	}

	if (currRecIdx &gt;= recsPerBlock) {
	    if (!readBlock()) {
		return; // UNDONE
	    }
	}

	currRecIdx++;
    }

    private boolean debug;
    private int currRecIdx;
    private int currBlkIdx;
    private InputStream inStream;
    private int recsPerBlock;
    private int blockSize;
    private byte[] blockBuffer;

    /**
     * @return false if End-Of-File, else true
     */
    private boolean readBlock() throws IOException {
	if (debug) {
	    System.err.println("ReadBlock: blkIdx = " + currBlkIdx);
	}

	if (inStream == null) {
	    throw new IOException("reading from an output buffer");
	}

	currRecIdx = 0;

	int offset = 0;
	int bytesNeeded = blockSize;

	while (bytesNeeded &gt; 0) {
	    long numBytes = inStream.read(blockBuffer, offset, bytesNeeded);

	    //
	    // NOTE
	    // We have fit EOF, and the block is not full!
	    //
	    // This is a broken archive. It does not follow the standard
	    // blocking algorithm. However, because we are generous, and
	    // it requires little effort, we will simply ignore the error
	    // and continue as if the entire block were read. This does
	    // not appear to break anything upstream. We used to return
	    // false in this case.
	    //
	    // Thanks to 'Yohann.Roussel@alcatel.fr' for this fix.
	    //
	    if (numBytes == -1) {
		if (offset == 0) {
		    // Ensure that we do not read gigabytes of zeros
		    // for a corrupt tar file.
		    // See http://issues.apache.org/bugzilla/show_bug.cgi?id=39924
		    return false;
		}
		// However, just leaving the unread portion of the buffer dirty does
		// cause problems in some cases.  This problem is described in
		// http://issues.apache.org/bugzilla/show_bug.cgi?id=29877
		//
		// The solution is to fill the unused portion of the buffer with zeros.

		Arrays.fill(blockBuffer, offset, offset + bytesNeeded, (byte) 0);

		break;
	    }

	    offset += numBytes;
	    bytesNeeded -= numBytes;

	    if (numBytes != blockSize) {
		if (debug) {
		    System.err.println("ReadBlock: INCOMPLETE READ " + numBytes + " of " + blockSize + " bytes read.");
		}
	    }
	}

	currBlkIdx++;

	return true;
    }

}

