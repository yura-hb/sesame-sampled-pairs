import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

class CheckedQueue {
    /**
     * This test tests the CheckedQueue.add method.  It creates a queue of
     * {@code String}s gets the checked queue, and attempt to add an Integer to
     * the checked queue.
     */
    @Test(expectedExceptions = ClassCastException.class)
    public void testAddFail1() {
	int arrayLength = 10;
	ArrayBlockingQueue&lt;String&gt; abq = new ArrayBlockingQueue(arrayLength + 1);

	for (int i = 0; i &lt; arrayLength; i++) {
	    abq.add(Integer.toString(i));
	}

	Queue q = Collections.checkedQueue(abq, String.class);
	q.add(0);
    }

}

