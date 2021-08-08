class File implements Serializable, Comparable&lt;File&gt; {
    /**
     * Creates the directory named by this abstract pathname, including any
     * necessary but nonexistent parent directories.  Note that if this
     * operation fails it may have succeeded in creating some of the necessary
     * parent directories.
     *
     * @return  &lt;code&gt;true&lt;/code&gt; if and only if the directory was created,
     *          along with all necessary parent directories; &lt;code&gt;false&lt;/code&gt;
     *          otherwise
     *
     * @throws  SecurityException
     *          If a security manager exists and its {@link
     *          java.lang.SecurityManager#checkRead(java.lang.String)}
     *          method does not permit verification of the existence of the
     *          named directory and all necessary parent directories; or if
     *          the {@link
     *          java.lang.SecurityManager#checkWrite(java.lang.String)}
     *          method does not permit the named directory and all necessary
     *          parent directories to be created
     */
    public boolean mkdirs() {
	if (exists()) {
	    return false;
	}
	if (mkdir()) {
	    return true;
	}
	File canonFile = null;
	try {
	    canonFile = getCanonicalFile();
	} catch (IOException e) {
	    return false;
	}

	File parent = canonFile.getParentFile();
	return (parent != null && (parent.mkdirs() || parent.exists()) && canonFile.mkdir());
    }

    /**
     * This abstract pathname's normalized pathname string. A normalized
     * pathname string uses the default name-separator character and does not
     * contain any duplicate or redundant separators.
     *
     * @serial
     */
    private final String path;
    /**
     * The FileSystem object representing the platform's local file system.
     */
    private static final FileSystem fs = DefaultFileSystem.getFileSystem();
    /**
     * The length of this abstract pathname's prefix, or zero if it has no
     * prefix.
     */
    private final transient int prefixLength;
    /**
     * The flag indicating whether the file path is invalid.
     */
    private transient PathStatus status = null;
    /**
     * The system-dependent default name-separator character.  This field is
     * initialized to contain the first character of the value of the system
     * property &lt;code&gt;file.separator&lt;/code&gt;.  On UNIX systems the value of this
     * field is &lt;code&gt;'/'&lt;/code&gt;; on Microsoft Windows systems it is &lt;code&gt;'\\'&lt;/code&gt;.
     *
     * @see     java.lang.System#getProperty(java.lang.String)
     */
    public static final char separatorChar = fs.getSeparator();

    /**
     * Tests whether the file or directory denoted by this abstract pathname
     * exists.
     *
     * @return  &lt;code&gt;true&lt;/code&gt; if and only if the file or directory denoted
     *          by this abstract pathname exists; &lt;code&gt;false&lt;/code&gt; otherwise
     *
     * @throws  SecurityException
     *          If a security manager exists and its {@link
     *          java.lang.SecurityManager#checkRead(java.lang.String)}
     *          method denies read access to the file or directory
     */
    public boolean exists() {
	SecurityManager security = System.getSecurityManager();
	if (security != null) {
	    security.checkRead(path);
	}
	if (isInvalid()) {
	    return false;
	}
	return ((fs.getBooleanAttributes(this) & FileSystem.BA_EXISTS) != 0);
    }

    /**
     * Creates the directory named by this abstract pathname.
     *
     * @return  &lt;code&gt;true&lt;/code&gt; if and only if the directory was
     *          created; &lt;code&gt;false&lt;/code&gt; otherwise
     *
     * @throws  SecurityException
     *          If a security manager exists and its {@link
     *          java.lang.SecurityManager#checkWrite(java.lang.String)}
     *          method does not permit the named directory to be created
     */
    public boolean mkdir() {
	SecurityManager security = System.getSecurityManager();
	if (security != null) {
	    security.checkWrite(path);
	}
	if (isInvalid()) {
	    return false;
	}
	return fs.createDirectory(this);
    }

    /**
     * Returns the canonical form of this abstract pathname.  Equivalent to
     * &lt;code&gt;new&nbsp;File(this.{@link #getCanonicalPath})&lt;/code&gt;.
     *
     * @return  The canonical pathname string denoting the same file or
     *          directory as this abstract pathname
     *
     * @throws  IOException
     *          If an I/O error occurs, which is possible because the
     *          construction of the canonical pathname may require
     *          filesystem queries
     *
     * @throws  SecurityException
     *          If a required system property value cannot be accessed, or
     *          if a security manager exists and its {@link
     *          java.lang.SecurityManager#checkRead} method denies
     *          read access to the file
     *
     * @since 1.2
     * @see     Path#toRealPath
     */
    public File getCanonicalFile() throws IOException {
	String canonPath = getCanonicalPath();
	return new File(canonPath, fs.prefixLength(canonPath));
    }

    /**
     * Returns the abstract pathname of this abstract pathname's parent,
     * or &lt;code&gt;null&lt;/code&gt; if this pathname does not name a parent
     * directory.
     *
     * &lt;p&gt; The &lt;em&gt;parent&lt;/em&gt; of an abstract pathname consists of the
     * pathname's prefix, if any, and each name in the pathname's name
     * sequence except for the last.  If the name sequence is empty then
     * the pathname does not name a parent directory.
     *
     * @return  The abstract pathname of the parent directory named by this
     *          abstract pathname, or &lt;code&gt;null&lt;/code&gt; if this pathname
     *          does not name a parent
     *
     * @since 1.2
     */
    public File getParentFile() {
	String p = this.getParent();
	if (p == null)
	    return null;
	return new File(p, this.prefixLength);
    }

    /**
     * Check if the file has an invalid path. Currently, the inspection of
     * a file path is very limited, and it only covers Nul character check.
     * Returning true means the path is definitely invalid/garbage. But
     * returning false does not guarantee that the path is valid.
     *
     * @return true if the file path is invalid.
     */
    final boolean isInvalid() {
	if (status == null) {
	    status = (this.path.indexOf('\u0000') &lt; 0) ? PathStatus.CHECKED : PathStatus.INVALID;
	}
	return status == PathStatus.INVALID;
    }

    /**
     * Returns the canonical pathname string of this abstract pathname.
     *
     * &lt;p&gt; A canonical pathname is both absolute and unique.  The precise
     * definition of canonical form is system-dependent.  This method first
     * converts this pathname to absolute form if necessary, as if by invoking the
     * {@link #getAbsolutePath} method, and then maps it to its unique form in a
     * system-dependent way.  This typically involves removing redundant names
     * such as {@code "."} and {@code ".."} from the pathname, resolving
     * symbolic links (on UNIX platforms), and converting drive letters to a
     * standard case (on Microsoft Windows platforms).
     *
     * &lt;p&gt; Every pathname that denotes an existing file or directory has a
     * unique canonical form.  Every pathname that denotes a nonexistent file
     * or directory also has a unique canonical form.  The canonical form of
     * the pathname of a nonexistent file or directory may be different from
     * the canonical form of the same pathname after the file or directory is
     * created.  Similarly, the canonical form of the pathname of an existing
     * file or directory may be different from the canonical form of the same
     * pathname after the file or directory is deleted.
     *
     * @return  The canonical pathname string denoting the same file or
     *          directory as this abstract pathname
     *
     * @throws  IOException
     *          If an I/O error occurs, which is possible because the
     *          construction of the canonical pathname may require
     *          filesystem queries
     *
     * @throws  SecurityException
     *          If a required system property value cannot be accessed, or
     *          if a security manager exists and its {@link
     *          java.lang.SecurityManager#checkRead} method denies
     *          read access to the file
     *
     * @since   1.1
     * @see     Path#toRealPath
     */
    public String getCanonicalPath() throws IOException {
	if (isInvalid()) {
	    throw new IOException("Invalid file path");
	}
	return fs.canonicalize(fs.resolve(this));
    }

    /**
     * Internal constructor for already-normalized pathname strings.
     */
    private File(String pathname, int prefixLength) {
	this.path = pathname;
	this.prefixLength = prefixLength;
    }

    /**
     * Returns the pathname string of this abstract pathname's parent, or
     * &lt;code&gt;null&lt;/code&gt; if this pathname does not name a parent directory.
     *
     * &lt;p&gt; The &lt;em&gt;parent&lt;/em&gt; of an abstract pathname consists of the
     * pathname's prefix, if any, and each name in the pathname's name
     * sequence except for the last.  If the name sequence is empty then
     * the pathname does not name a parent directory.
     *
     * @return  The pathname string of the parent directory named by this
     *          abstract pathname, or &lt;code&gt;null&lt;/code&gt; if this pathname
     *          does not name a parent
     */
    public String getParent() {
	int index = path.lastIndexOf(separatorChar);
	if (index &lt; prefixLength) {
	    if ((prefixLength &gt; 0) && (path.length() &gt; prefixLength))
		return path.substring(0, prefixLength);
	    return null;
	}
	return path.substring(0, index);
    }

}

