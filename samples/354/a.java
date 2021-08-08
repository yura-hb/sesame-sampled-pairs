import java.io.*;

class DataOutputBuffer extends DataOutputStream {
    /** Resets the buffer to empty. */
    public DataOutputBuffer reset() {
	this.written = 0;
	buffer.reset();
	return this;
    }

    private Buffer buffer;

}

