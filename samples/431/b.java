import java.io.*;
import java.nio.file.*;
import static java.nio.file.Files.*;
import static java.nio.file.StandardCopyOption.*;
import java.util.*;

class CopyAndMove {
    /**
     * Test copy from an input stream to a file
     */
    static void testCopyInputStreamToFile() throws IOException {
	testCopyInputStreamToFile(0);
	for (int i = 0; i &lt; 100; i++) {
	    testCopyInputStreamToFile(rand.nextInt(32000));
	}

	// FileAlreadyExistsException
	Path target = createTempFile("blah", null);
	try {
	    InputStream in = new ByteArrayInputStream(new byte[0]);
	    try {
		copy(in, target);
		throw new RuntimeException("FileAlreadyExistsException expected");
	    } catch (FileAlreadyExistsException ignore) {
	    }
	} finally {
	    delete(target);
	}
	Path tmpdir = createTempDirectory("blah");
	try {
	    if (TestUtil.supportsLinks(tmpdir)) {
		Path link = createSymbolicLink(tmpdir.resolve("link"), tmpdir.resolve("target"));
		try {
		    InputStream in = new ByteArrayInputStream(new byte[0]);
		    try {
			copy(in, link);
			throw new RuntimeException("FileAlreadyExistsException expected");
		    } catch (FileAlreadyExistsException ignore) {
		    }
		} finally {
		    delete(link);
		}
	    }
	} finally {
	    delete(tmpdir);
	}

	// nulls
	try {
	    copy((InputStream) null, target);
	    throw new RuntimeException("NullPointerException expected");
	} catch (NullPointerException ignore) {
	}
	try {
	    copy(new ByteArrayInputStream(new byte[0]), (Path) null);
	    throw new RuntimeException("NullPointerException expected");
	} catch (NullPointerException ignore) {
	}
    }

    static final Random rand = RandomFactory.getRandom();

    static void testCopyInputStreamToFile(int size) throws IOException {
	Path tmpdir = createTempDirectory("blah");
	Path source = tmpdir.resolve("source");
	Path target = tmpdir.resolve("target");
	try {
	    boolean testReplaceExisting = rand.nextBoolean();

	    // create source file
	    byte[] b = new byte[size];
	    rand.nextBytes(b);
	    write(source, b);

	    // target file might already exist
	    if (testReplaceExisting && rand.nextBoolean()) {
		write(target, new byte[rand.nextInt(512)]);
	    }

	    // copy from stream to file
	    InputStream in = new FileInputStream(source.toFile());
	    try {
		long n;
		if (testReplaceExisting) {
		    n = copy(in, target, StandardCopyOption.REPLACE_EXISTING);
		} else {
		    n = copy(in, target);
		}
		assertTrue(in.read() == -1); // EOF
		assertTrue(n == size);
		assertTrue(size(target) == size);
	    } finally {
		in.close();
	    }

	    // check file
	    byte[] read = readAllBytes(target);
	    assertTrue(Arrays.equals(read, b));

	} finally {
	    deleteIfExists(source);
	    deleteIfExists(target);
	    delete(tmpdir);
	}
    }

    static void assertTrue(boolean value) {
	if (!value)
	    throw new RuntimeException("Assertion failed");
    }

}

