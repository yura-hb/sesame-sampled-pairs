import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.QueueTool;

abstract class Operator implements Timeoutable, Outputable {
    /**
     * Unlocks Queue and then throw exception.
     *
     * @param e an exception to be thrown.
     */
    protected void unlockAndThrow(Exception e) {
	unlockQueue();
	throw (new JemmyException("Exception during queue locking", e));
    }

    private QueueTool queueTool;

    /**
     * Equivalent to {@code getQueue().unlock();}.
     */
    protected void unlockQueue() {
	queueTool.unlock();
    }

}

