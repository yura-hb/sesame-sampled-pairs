import java.util.ArrayList;
import java.util.EmptyStackException;

class ArrayStack&lt;E&gt; extends ArrayList&lt;E&gt; {
    /**
     * Returns the top item off of this stack without removing it.
     *
     * @return the top item on the stack
     * @throws EmptyStackException  if the stack is empty
     */
    public E peek() throws EmptyStackException {
	final int n = size();
	if (n &lt;= 0) {
	    throw new EmptyStackException();
	}
	return get(n - 1);
    }

}

