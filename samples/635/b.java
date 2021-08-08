import java.nio.file.Files;

class StandardDocFileFactory extends DocFileFactory {
    class StandardDocFile extends DocFile {
	/** Return true if this file is the same as another. */
	@Override
	public boolean isSameFile(DocFile other) {
	    if (!(other instanceof StandardDocFile))
		return false;

	    try {
		return Files.isSameFile(file, ((StandardDocFile) other).file);
	    } catch (IOException e) {
		return false;
	    }
	}

	private final Path file;

    }

}

