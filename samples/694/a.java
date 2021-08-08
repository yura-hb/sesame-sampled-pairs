import java.util.HashSet;
import java.util.Set;

class DBProperties {
    /**
     * Returns the Set of property names stored in this object
     * @return the Set of property names stored in this object
     * @throws IndexException
     */
    public Set&lt;String&gt; getKeySet() throws IndexException {
	return DBProperty.getKeySet(this.db, this.index);
    }

    protected Database db;
    protected BTree index;

    class DBProperty {
	protected Database db;
	protected BTree index;

	public static Set&lt;String&gt; getKeySet(final Database db, final BTree index) throws IndexException {
	    final Set&lt;String&gt; result = new HashSet&lt;String&gt;();
	    index.accept(new IBTreeVisitor() {
		@Override
		public int compare(long record) throws IndexException {
		    return 0;
		}

		@Override
		public boolean visit(long record) throws IndexException {
		    result.add(new DBProperty(db, record).getKey().getString());
		    return true; // There should never be duplicates.
		}
	    });
	    return result;
	}

	/**
		 * Returns an object for accessing an existing DBProperty record at the specified location
		 * in the specified database.
		 * @param db
		 * @param record
		 */
	DBProperty(Database db, long record) {
	    this.record = record;
	    this.db = db;
	}

	public IString getKey() throws IndexException {
	    return this.db.getString(this.db.getRecPtr(this.record + KEY));
	}

    }

}

