import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class JmodFile implements AutoCloseable {
    /**
     * Returns a stream of entries in this JMOD file.
     */
    public Stream&lt;Entry&gt; stream() {
	return zipfile.stream().map(Entry::new);
    }

    private final ZipFile zipfile;

    class Entry {
	private final ZipFile zipfile;

	private Entry(ZipEntry e) {
	    String name = e.getName();
	    int i = name.indexOf('/');
	    if (i &lt;= 1) {
		throw new RuntimeException("invalid jmod entry: " + name);
	    }

	    this.zipEntry = e;
	    this.section = section(name.substring(0, i));
	    this.name = name.substring(i + 1);
	}

	static Section section(String name) {
	    if (!NAME_TO_SECTION.containsKey(name)) {
		throw new IllegalArgumentException("invalid section: " + name);

	    }
	    return NAME_TO_SECTION.get(name);
	}

    }

}

