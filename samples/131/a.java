class RocksDbStorage extends BaseUpdateStorage implements AutoCloseable {
    /**
     * Clear the array storage
     */
    @Override
    public void clear() {
	RocksIterator iterator = db.newIterator();
	while (iterator.isValid())
	    try {
		db.remove(iterator.key());
	    } catch (RocksDBException e) {
		throw new RuntimeException(e);
	    }
	iterator.close();
	size = 0;
    }

    private RocksDB db;
    private int size = 0;

}

