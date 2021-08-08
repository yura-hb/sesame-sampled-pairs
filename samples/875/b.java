import static com.google.common.io.FileWriteMode.APPEND;
import com.google.common.collect.ImmutableSet;

class Files {
    /**
    * Appends a character sequence (such as a string) to a file using the given character set.
    *
    * @param from the character sequence to append
    * @param to the destination file
    * @param charset the charset used to encode the output stream; see {@link StandardCharsets} for
    *     helpful predefined constants
    * @throws IOException if an I/O error occurs
    * @deprecated Prefer {@code asCharSink(to, charset, FileWriteMode.APPEND).write(from)}. This
    *     method is scheduled to be removed in January 2019.
    */
    @Deprecated
    public static void append(CharSequence from, File to, Charset charset) throws IOException {
	asCharSink(to, charset, FileWriteMode.APPEND).write(from);
    }

    /**
    * Returns a new {@link CharSink} for writing character data to the given file using the given
    * character set. The given {@code modes} control how the file is opened for writing. When no mode
    * is provided, the file will be truncated before writing. When the {@link FileWriteMode#APPEND
    * APPEND} mode is provided, writes will append to the end of the file without truncating it.
    *
    * @since 14.0
    */
    public static CharSink asCharSink(File file, Charset charset, FileWriteMode... modes) {
	return asByteSink(file, modes).asCharSink(charset);
    }

    /**
    * Returns a new {@link ByteSink} for writing bytes to the given file. The given {@code modes}
    * control how the file is opened for writing. When no mode is provided, the file will be
    * truncated before writing. When the {@link FileWriteMode#APPEND APPEND} mode is provided, writes
    * will append to the end of the file without truncating it.
    *
    * @since 14.0
    */
    public static ByteSink asByteSink(File file, FileWriteMode... modes) {
	return new FileByteSink(file, modes);
    }

    class FileByteSink extends ByteSink {
	private FileByteSink(File file, FileWriteMode... modes) {
	    this.file = checkNotNull(file);
	    this.modes = ImmutableSet.copyOf(modes);
	}

    }

}

