import java.util.TreeSet;

abstract class AbstractCheck extends AbstractViolationReporter {
    /**
     * Returns the sorted set of {@link LocalizedMessage}.
     * @return the sorted set of {@link LocalizedMessage}.
     */
    public SortedSet&lt;LocalizedMessage&gt; getMessages() {
	return new TreeSet&lt;&gt;(context.get().messages);
    }

    /**
     * The check context.
     * @noinspection ThreadLocalNotStaticFinal
     */
    private final ThreadLocal&lt;FileContext&gt; context = ThreadLocal.withInitial(FileContext::new);

}

