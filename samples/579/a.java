import java.util.Map;

class NdStringSet {
    /**
     * Return a pointer to the record of the String that was removed.
     */
    public long remove(String str) throws CoreException {
	if (this.lazyCache != null)
	    this.lazyCache.remove(str);

	long prev = 0;
	long curr = getHead();
	while (curr != 0) {
	    long next = NodeType.Next.get(this.db, curr);
	    long item = NodeType.Item.get(this.db, curr);

	    IString string = this.db.getString(item);

	    if (string.compare(str, true) == 0) {
		if (this.head != curr)
		    NodeType.Next.put(this.db, prev, next);
		else {
		    this.db.putRecPtr(this.ptr, next);
		    this.head = next;
		}

		this.db.free(curr, Database.POOL_STRING_SET);
		return item;
	    }

	    prev = curr;
	    curr = next;
	}

	return 0;
    }

    private Map&lt;String, Long&gt; lazyCache;
    private final Database db;
    private long head;
    private long ptr;

    private long getHead() throws CoreException {
	if (this.head == 0)
	    this.head = this.db.getRecPtr(this.ptr);
	return this.head;
    }

    class NodeType extends Enum&lt;NodeType&gt; {
	private Map&lt;String, Long&gt; lazyCache;
	private final Database db;
	private long head;
	private long ptr;

	/** Return the value of the pointer stored in this field in the given instance. */
	public long get(Database db, long instance) throws CoreException {
	    return db.getRecPtr(instance + this.offset);
	}

	/** Store the given pointer into this field in the given instance. */
	public void put(Database db, long instance, long value) throws CoreException {
	    db.putRecPtr(instance + this.offset, value);
	}

    }

}

