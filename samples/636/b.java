import java.util.Collections;
import java.util.SortedSet;

class EmptyNavigableSet {
    /**
     * Tests that the comparator is {@code null}.
     */
    @Test(dataProvider = "NavigableSet&lt;?&gt;", dataProviderClass = EmptyNavigableSet.class)
    public void testComparatorIsNull(String description, NavigableSet&lt;?&gt; navigableSet) {
	Comparator comparator = navigableSet.comparator();

	assertTrue(comparator == null || comparator == Collections.reverseOrder(),
		description + ": Comparator (" + comparator + ") is not null.");
    }

}

