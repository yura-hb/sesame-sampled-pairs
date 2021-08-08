class RegularImmutableMap&lt;K, V&gt; extends ImmutableMap&lt;K, V&gt; {
    /**
    * @return number of entries in this bucket
    * @throws IllegalArgumentException if another entry in the bucket has the same key
    */
    @CanIgnoreReturnValue
    static int checkNoConflictInKeyBucket(Object key, Entry&lt;?, ?&gt; entry,
	    @Nullable ImmutableMapEntry&lt;?, ?&gt; keyBucketHead) {
	int bucketSize = 0;
	for (; keyBucketHead != null; keyBucketHead = keyBucketHead.getNextInKeyBucket()) {
	    checkNoConflict(!key.equals(keyBucketHead.getKey()), "key", entry, keyBucketHead);
	    bucketSize++;
	}
	return bucketSize;
    }

}

