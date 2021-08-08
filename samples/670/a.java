import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

class FileBackedOutputStream extends OutputStream {
    /**
    * Calls {@link #close} if not already closed, and then resets this object back to its initial
    * state, for reuse. If data was buffered to a file, it will be deleted.
    *
    * @throws IOException if an I/O error occurred while deleting the file buffer
    */
    public synchronized void reset() throws IOException {
	try {
	    close();
	} finally {
	    if (memory == null) {
		memory = new MemoryOutput();
	    } else {
		memory.reset();
	    }
	    out = memory;
	    if (file != null) {
		File deleteMe = file;
		file = null;
		if (!deleteMe.delete()) {
		    throw new IOException("Could not delete: " + deleteMe);
		}
	    }
	}
    }

    private MemoryOutput memory;
    private OutputStream out;
    private @Nullable File file;

    @Override
    public synchronized void close() throws IOException {
	out.close();
    }

}

