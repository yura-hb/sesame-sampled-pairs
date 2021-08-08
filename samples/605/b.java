class HashTestKit {
    /**
     * Confirm that the internal FREE counter matches the values in the slots.
     */
    public static void checkFreeSlotCount(THash hash, Object[] slot_keys, Object free_marker) {

	int free_counter = hash._free;

	int count = 0;
	for (Object slot_key : slot_keys) {
	    if (slot_key == free_marker)
		count++;
	}

	TestCase.assertEquals(free_counter, count);
    }

}

