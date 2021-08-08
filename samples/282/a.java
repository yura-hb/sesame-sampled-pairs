import java.io.FileInputStream;
import java.io.InputStream;
import org.eclipse.jdt.internal.core.nd.StreamHasher;

class FileFingerprint {
    /**
     * Compares the given File with the receiver. If the fingerprint matches (ie: the file
     */
    public FingerprintTestResult test(IPath path, IProgressMonitor monitor) throws CoreException {
	SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
	long currentTime = System.currentTimeMillis();
	IFileStore store = EFS.getLocalFileSystem().getStore(path);
	IFileInfo fileInfo = store.fetchInfo();

	long lastModified = fileInfo.getLastModified();
	if (Math.abs(currentTime - lastModified) &lt; WORST_FILESYSTEM_TIMESTAMP_ACCURACY_MS) {
	    // If the file was modified so recently that it's within our ability to measure it, don't include
	    // the timestamp as part of the fingerprint. If another change were to happen to the file immediately
	    // afterward, we might not be able to detect it using the timestamp.
	    lastModified = UNKNOWN;
	}
	subMonitor.split(5);

	long fileSize = fileInfo.getLength();
	subMonitor.split(5);
	if (lastModified != UNKNOWN && lastModified == this.time && fileSize == this.size) {
	    return new FingerprintTestResult(true, false, this);
	}

	long hashCode;
	try {
	    hashCode = fileSize == 0 ? 0 : computeHashCode(path.toFile(), fileSize, subMonitor.split(90));
	} catch (IOException e) {
	    throw new CoreException(Package.createStatus("An error occurred computing a hash code", e)); //$NON-NLS-1$
	}
	boolean matches = (hashCode == this.hash && fileSize == this.size);

	FileFingerprint newFingerprint = new FileFingerprint(lastModified, fileSize, hashCode);
	return new FingerprintTestResult(matches, !equals(newFingerprint), newFingerprint);
    }

    /**
     * Worst-case accuracy of filesystem timestamps, among all supported platforms (this is currently 1s on linux, 2s on
     * FAT systems).
     */
    private static final long WORST_FILESYSTEM_TIMESTAMP_ACCURACY_MS = 2000;
    /**
     * Sentinel value for {@link #time} indicating that the timestamp was not recorded as part of the fingerprint.
     * This is normally used to indicate that the file's timestamp was so close to the current system time at the time
     * the fingerprint was computed that subsequent changes in the file might not be detected. In such cases, timestamps
     * are an unreliable method for determining if the file has changed and so are not included as part of the fingerprint.
     */
    public static final long UNKNOWN = 1;
    private long time;
    private long size;
    private long hash;

    private long computeHashCode(File toTest, long fileSize, IProgressMonitor monitor) throws IOException {
	final int BUFFER_SIZE = 2048;
	char[] charBuffer = new char[BUFFER_SIZE];
	byte[] byteBuffer = new byte[BUFFER_SIZE * 2];

	SubMonitor subMonitor = SubMonitor.convert(monitor, (int) (fileSize / (BUFFER_SIZE * 2)));
	StreamHasher hasher = new StreamHasher();
	try {
	    InputStream inputStream = new FileInputStream(toTest);
	    try {
		while (true) {
		    subMonitor.split(1);
		    int bytesRead = readUntilBufferFull(inputStream, byteBuffer);

		    if (bytesRead &lt; byteBuffer.length) {
			charBuffer = new char[(bytesRead + 1) / 2];
			copyByteArrayToCharArray(charBuffer, byteBuffer, bytesRead);
			hasher.addChunk(charBuffer);
			break;
		    }

		    copyByteArrayToCharArray(charBuffer, byteBuffer, bytesRead);
		    hasher.addChunk(charBuffer);
		}
	    } finally {
		inputStream.close();
	    }

	} catch (FileNotFoundException e) {
	    return 0;
	}

	return hasher.computeHash();
    }

    public FileFingerprint(long time, long size, long hash) {
	super();
	this.time = time;
	this.size = size;
	this.hash = hash;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	FileFingerprint other = (FileFingerprint) obj;
	if (this.hash != other.hash)
	    return false;
	if (this.size != other.size)
	    return false;
	if (this.time != other.time)
	    return false;
	return true;
    }

    int readUntilBufferFull(InputStream inputStream, byte[] buffer) throws IOException {
	int bytesRead = 0;
	while (bytesRead &lt; buffer.length) {
	    int thisRead = inputStream.read(buffer, bytesRead, buffer.length - bytesRead);

	    if (thisRead == -1) {
		return bytesRead;
	    }

	    bytesRead += thisRead;
	}
	return bytesRead;
    }

    private void copyByteArrayToCharArray(char[] charBuffer, byte[] byteBuffer, int bytesToCopy) {
	for (int ch = 0; ch &lt; bytesToCopy / 2; ch++) {
	    char next = (char) (byteBuffer[ch * 2] + byteBuffer[ch * 2 + 1]);
	    charBuffer[ch] = next;
	}

	if (bytesToCopy % 2 != 0) {
	    charBuffer[bytesToCopy / 2] = (char) byteBuffer[bytesToCopy - 1];
	}
    }

    class FingerprintTestResult {
	/**
	 * Worst-case accuracy of filesystem timestamps, among all supported platforms (this is currently 1s on linux, 2s on
	 * FAT systems).
	 */
	private static final long WORST_FILESYSTEM_TIMESTAMP_ACCURACY_MS = 2000;
	/**
	 * Sentinel value for {@link #time} indicating that the timestamp was not recorded as part of the fingerprint.
	 * This is normally used to indicate that the file's timestamp was so close to the current system time at the time
	 * the fingerprint was computed that subsequent changes in the file might not be detected. In such cases, timestamps
	 * are an unreliable method for determining if the file has changed and so are not included as part of the fingerprint.
	 */
	public static final long UNKNOWN = 1;
	private long time;
	private long size;
	private long hash;

	public FingerprintTestResult(boolean matches, boolean needsNewFingerprint, FileFingerprint newFingerprint) {
	    super();
	    this.matches = matches;
	    this.newFingerprint = newFingerprint;
	    this.needsNewFingerprint = needsNewFingerprint;
	}

    }

}

