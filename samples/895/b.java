import java.io.*;
import java.nio.channels.*;

class Lock {
    /**
     * Utility method to be run in secondary process which tries to acquire a
     * lock on a FileChannel
     */
    static void attemptLock(String fileName, boolean expectsLock) throws Exception {
	File f = new File(fileName);
	try (RandomAccessFile raf = new RandomAccessFile(f, "rw")) {
	    FileChannel fc = raf.getChannel();
	    if (fc.tryLock(10, 10, false) == null) {
		System.out.println("bad: Failed to grab adjacent lock");
	    }
	    if (fc.tryLock(0, 10, false) == null) {
		if (expectsLock)
		    System.out.println("bad");
		else
		    System.out.println("good");
	    } else {
		if (expectsLock)
		    System.out.println("good");
		else
		    System.out.println("bad");
	    }
	}
    }

}

