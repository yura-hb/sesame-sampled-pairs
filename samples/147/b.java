import java.util.Collection;
import java.util.Iterator;
import java.util.SortedMap;

class EmptyNavigableMap {
    /**
     * Tests that the iterator is empty.
     */
    @Test(dataProvider = "NavigableMap&lt;?,?&gt;", dataProviderClass = EmptyNavigableMap.class)
    public void testEmptyIterator(String description, NavigableMap&lt;?, ?&gt; navigableMap) {
	assertFalse(navigableMap.keySet().iterator().hasNext(), "The iterator is not empty.");
	assertFalse(navigableMap.values().iterator().hasNext(), "The iterator is not empty.");
	assertFalse(navigableMap.entrySet().iterator().hasNext(), "The iterator is not empty.");
    }

}

