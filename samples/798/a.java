import java.util.Deque;

class Closer implements Closeable {
    /**
    * Registers the given {@code closeable} to be closed when this {@code Closer} is {@linkplain
    * #close closed}.
    *
    * @return the given {@code closeable}
    */
    // close. this word no longer has any meaning to me.
    @CanIgnoreReturnValue
    public &lt;C extends Closeable&gt; C register(@Nullable C closeable) {
	if (closeable != null) {
	    stack.addFirst(closeable);
	}

	return closeable;
    }

    private final Deque&lt;Closeable&gt; stack = new ArrayDeque&lt;&gt;(4);

}

