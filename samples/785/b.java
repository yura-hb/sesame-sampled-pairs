import com.google.common.collect.testing.Helpers;
import java.util.Arrays;

class MultisetForEachEntryTester&lt;E&gt; extends AbstractMultisetTester&lt;E&gt; {
    /**
    * Returns {@link Method} instances for the remove tests that assume multisets support duplicates
    * so that the test of {@code Multisets.forSet()} can suppress them.
    */
    @GwtIncompatible // reflection
    public static List&lt;Method&gt; getForEachEntryDuplicateInitializingMethods() {
	return Arrays.asList(Helpers.getMethod(MultisetForEachEntryTester.class, "testForEachEntryDuplicates"));
    }

}

