import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.zip.CRC32;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;
import org.eclipse.jdt.internal.core.index.DiskIndex;
import org.eclipse.jdt.internal.core.index.FileIndexLocation;
import org.eclipse.jdt.internal.core.index.Index;
import org.eclipse.jdt.internal.core.index.IndexLocation;
import org.eclipse.jdt.internal.core.nd.indexer.Indexer;
import org.eclipse.jdt.internal.core.search.processing.JobManager;
import org.eclipse.jdt.internal.core.util.Util;

class IndexManager extends JobManager implements IIndexConstants {
    /**
    * Removes the index for a given path.
    * This is a no-op if the index did not exist.
    */
    public synchronized void removeIndex(IPath containerPath) {
	if (VERBOSE || DEBUG)
	    Util.verbose("removing index " + containerPath); //$NON-NLS-1$
	this.indexer.makeWorkspacePathDirty(containerPath);
	IndexLocation indexLocation = computeIndexLocation(containerPath);
	Index index = getIndex(indexLocation);
	File indexFile = null;
	if (index != null) {
	    index.monitor = null;
	    indexFile = index.getIndexFile();
	}
	if (indexFile == null)
	    indexFile = indexLocation.getIndexFile(); // index is not cached yet, but still want to delete the file
	if (this.indexStates.get(indexLocation) == REUSE_STATE) {
	    indexLocation.close();
	    this.indexLocations.put(containerPath, null);
	} else if (indexFile != null && indexFile.exists()) {
	    if (DEBUG)
		Util.verbose("removing index file " + indexFile); //$NON-NLS-1$
	    indexFile.delete();
	}
	this.indexes.removeKey(indexLocation);
	if (IS_MANAGING_PRODUCT_INDEXES_PROPERTY) {
	    this.indexLocations.removeKey(containerPath);
	}
	updateIndexState(indexLocation, null);
    }

    public static boolean DEBUG = false;
    /**
     * The new indexer
     */
    private Indexer indexer = Indexer.getInstance();
    private SimpleLookupTable indexStates = null;
    public static final Integer REUSE_STATE = 4;
    public SimpleLookupTable indexLocations = new SimpleLookupTable();
    private SimpleLookupTable indexes = new SimpleLookupTable();
    private static final boolean IS_MANAGING_PRODUCT_INDEXES_PROPERTY = Boolean
	    .getBoolean(MANAGE_PRODUCT_INDEXES_PROPERTY);
    public static final Integer SAVED_STATE = 0;
    public static final Integer UPDATING_STATE = 1;
    public static final Integer UNKNOWN_STATE = 2;
    public static final Integer REBUILDING_STATE = 3;
    private final IndexNamesRegistry nameRegistry = new IndexNamesRegistry(
	    new File(getSavedIndexesDirectory(), "savedIndexNames.txt"), getJavaPluginWorkingLocation());
    private boolean javaLikeNamesChanged = true;
    private File indexNamesMapFile = new File(getSavedIndexesDirectory(), "indexNamesMap.txt");
    private IPath javaPluginLocation = null;

    public synchronized IndexLocation computeIndexLocation(IPath containerPath) {
	IndexLocation indexLocation = (IndexLocation) this.indexLocations.get(containerPath);
	if (indexLocation == null) {
	    String pathString = containerPath.toOSString();
	    CRC32 checksumCalculator = new CRC32();
	    checksumCalculator.update(pathString.getBytes());
	    String fileName = Long.toString(checksumCalculator.getValue()) + ".index"; //$NON-NLS-1$
	    if (VERBOSE)
		Util.verbose("-&gt; index name for " + pathString + " is " + fileName); //$NON-NLS-1$ //$NON-NLS-2$
	    // to share the indexLocation between the indexLocations and indexStates tables, get the key from the indexStates table
	    indexLocation = (IndexLocation) getIndexStates()
		    .getKey(new FileIndexLocation(new File(getSavedIndexesDirectory(), fileName)));
	    this.indexLocations.put(containerPath, indexLocation);
	}
	return indexLocation;
    }

    /**
    * Returns the index for a given index location
    *
    * @param indexLocation The path of the index file
    * @return The corresponding index or &lt;code&gt;null&lt;/code&gt; if not found
    */
    public synchronized Index getIndex(IndexLocation indexLocation) {
	return (Index) this.indexes.get(indexLocation); // is null if unknown, call if the containerPath must be computed
    }

    private synchronized void updateIndexState(IndexLocation indexLocation, Integer indexState) {
	if (indexLocation == null)
	    throw new IllegalArgumentException();

	getIndexStates(); // ensure the states are initialized
	if (indexState != null) {
	    if (indexState.equals(this.indexStates.get(indexLocation)))
		return; // not changed
	    this.indexStates.put(indexLocation, indexState);
	} else {
	    if (!this.indexStates.containsKey(indexLocation))
		return; // did not exist anyway
	    this.indexStates.removeKey(indexLocation);
	}

	writeSavedIndexNamesFile();

	if (VERBOSE) {
	    if (indexState == null) {
		Util.verbose("-&gt; index state removed for: " + indexLocation); //$NON-NLS-1$
	    } else {
		String state = "?"; //$NON-NLS-1$
		if (indexState == SAVED_STATE)
		    state = "SAVED"; //$NON-NLS-1$
		else if (indexState == UPDATING_STATE)
		    state = "UPDATING"; //$NON-NLS-1$
		else if (indexState == UNKNOWN_STATE)
		    state = "UNKNOWN"; //$NON-NLS-1$
		else if (indexState == REBUILDING_STATE)
		    state = "REBUILDING"; //$NON-NLS-1$
		else if (indexState == REUSE_STATE)
		    state = "REUSE"; //$NON-NLS-1$
		Util.verbose("-&gt; index state updated to: " + state + " for: " + indexLocation); //$NON-NLS-1$ //$NON-NLS-2$
	    }
	}

    }

    private SimpleLookupTable getIndexStates() {
	if (this.indexStates != null)
	    return this.indexStates;

	this.indexStates = new SimpleLookupTable();
	File indexesDirectoryPath = getSavedIndexesDirectory();
	char[][] savedNames = this.nameRegistry.read(null);
	if (savedNames != null) {
	    for (int i = 1, l = savedNames.length; i &lt; l; i++) { // first name is saved signature, see readIndexState()
		char[] savedName = savedNames[i];
		if (savedName.length &gt; 0) {
		    IndexLocation indexLocation = new FileIndexLocation(
			    new File(indexesDirectoryPath, String.valueOf(savedName))); // shares indexesDirectoryPath's segments
		    if (VERBOSE)
			Util.verbose("Reading saved index file " + indexLocation); //$NON-NLS-1$
		    this.indexStates.put(indexLocation, SAVED_STATE);
		}
	    }
	} else {
	    // All the index files are getting deleted and hence there is no need to 
	    // further check for change in javaLikeNames. 
	    writeJavaLikeNamesFile();
	    this.javaLikeNamesChanged = false;
	    deleteIndexFiles();
	}
	readIndexMap();
	return this.indexStates;
    }

    private File getSavedIndexesDirectory() {
	return new File(getJavaPluginWorkingLocation().toOSString());
    }

    private void writeSavedIndexNamesFile() {
	Object[] keys = this.indexStates.keyTable;
	Object[] states = this.indexStates.valueTable;
	int numToSave = 0;
	for (int i = 0, l = states.length; i &lt; l; i++) {
	    IndexLocation key = (IndexLocation) keys[i];
	    if (key != null && states[i] == SAVED_STATE) {
		numToSave++;
	    }
	}
	char[][] arrays = new char[numToSave][];
	int idx = 0;
	for (int i = 0, l = states.length; i &lt; l; i++) {
	    IndexLocation key = (IndexLocation) keys[i];
	    if (key != null && states[i] == SAVED_STATE) {
		arrays[idx++] = key.fileName().toCharArray();
	    }
	}
	this.nameRegistry.write(arrays);
    }

    private void writeJavaLikeNamesFile() {
	BufferedWriter writer = null;
	String pathName = getJavaPluginWorkingLocation().toOSString();
	try {
	    char[][] currentNames = Util.getJavaLikeExtensions();
	    int length = currentNames.length;
	    if (length &gt; 1) {
		// Sort the current java like names. 
		// Copy the array to avoid modifying the Util static variable
		System.arraycopy(currentNames, 0, currentNames = new char[length][], 0, length);
		Util.sort(currentNames);
	    }
	    File javaLikeNamesFile = new File(pathName, "javaLikeNames.txt"); //$NON-NLS-1$
	    writer = new BufferedWriter(new FileWriter(javaLikeNamesFile));
	    for (int i = 0; i &lt; length - 1; i++) {
		writer.write(currentNames[i]);
		writer.write('\n');
	    }
	    if (length &gt; 0)
		writer.write(currentNames[length - 1]);

	} catch (IOException ignored) {
	    if (VERBOSE)
		Util.verbose("Failed to write javaLikeNames file", System.err); //$NON-NLS-1$
	} finally {
	    if (writer != null) {
		try {
		    writer.close();
		} catch (IOException e) {
		    // ignore
		}
	    }
	}
    }

    /**
    * Use {@link #deleteIndexFiles(IProgressMonitor)}
    */
    public final void deleteIndexFiles() {
	deleteIndexFiles(null);
    }

    private void readIndexMap() {
	try {
	    char[] indexMaps = org.eclipse.jdt.internal.compiler.util.Util.getFileCharContent(this.indexNamesMapFile,
		    null);
	    char[][] names = CharOperation.splitOn('\n', indexMaps);
	    if (names.length &gt;= 3) {
		// First line is DiskIndex signature (see writeIndexMapFile())
		String savedSignature = DiskIndex.SIGNATURE;
		if (savedSignature.equals(new String(names[0]))) {
		    for (int i = 1, l = names.length - 1; i &lt; l; i += 2) {
			IndexLocation indexPath = IndexLocation.createIndexLocation(new URL(new String(names[i])));
			if (indexPath == null)
			    continue;
			this.indexLocations.put(new Path(new String(names[i + 1])), indexPath);
			this.indexStates.put(indexPath, REUSE_STATE);
		    }
		}
	    }
	} catch (IOException ignored) {
	    if (VERBOSE)
		Util.verbose("Failed to read saved index file names"); //$NON-NLS-1$
	}
	return;
    }

    private IPath getJavaPluginWorkingLocation() {
	if (this.javaPluginLocation != null)
	    return this.javaPluginLocation;

	IPath stateLocation = JavaCore.getPlugin().getStateLocation();
	return this.javaPluginLocation = stateLocation;
    }

    public void deleteIndexFiles(IProgressMonitor monitor) {
	if (DEBUG)
	    Util.verbose("Deleting index files"); //$NON-NLS-1$
	this.nameRegistry.delete(); // forget saved indexes & delete each index file
	deleteIndexFiles(null, monitor);
    }

    private void deleteIndexFiles(SimpleSet pathsToKeep, IProgressMonitor monitor) {
	File[] indexesFiles = getSavedIndexesDirectory().listFiles();
	if (indexesFiles == null)
	    return;

	SubMonitor subMonitor = SubMonitor.convert(monitor, indexesFiles.length);
	for (int i = 0, l = indexesFiles.length; i &lt; l; i++) {
	    subMonitor.split(1);
	    String fileName = indexesFiles[i].getAbsolutePath();
	    if (pathsToKeep != null && pathsToKeep.includes(new FileIndexLocation(indexesFiles[i])))
		continue;
	    String suffix = ".index"; //$NON-NLS-1$
	    if (fileName.regionMatches(true, fileName.length() - suffix.length(), suffix, 0, suffix.length())) {
		if (VERBOSE || DEBUG)
		    Util.verbose("Deleting index file " + indexesFiles[i]); //$NON-NLS-1$
		indexesFiles[i].delete();
	    }
	}
    }

}

