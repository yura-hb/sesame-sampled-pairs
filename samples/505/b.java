interface Archive {
    abstract class Entry {
	/**
	 * Returns the name representing a ResourcePoolEntry in the form of:
	 *    /$MODULE/$ENTRY_NAME
	 */
	public final String getResourcePoolEntryName() {
	    return "/" + archive.moduleName() + "/" + name;
	}

	private final Archive archive;
	private final String name;

    }

    String moduleName();

}

