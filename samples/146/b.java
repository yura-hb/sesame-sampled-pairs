class TaskQueue {
    /**
     * Removes all elements from the priority queue.
     */
    void clear() {
	// Null out task references to prevent memory leak
	for (int i = 1; i &lt;= size; i++)
	    queue[i] = null;

	size = 0;
    }

    /**
     * The number of tasks in the priority queue.  (The tasks are stored in
     * queue[1] up to queue[size]).
     */
    private int size = 0;
    /**
     * Priority queue represented as a balanced binary heap: the two children
     * of queue[n] are queue[2*n] and queue[2*n+1].  The priority queue is
     * ordered on the nextExecutionTime field: The TimerTask with the lowest
     * nextExecutionTime is in queue[1] (assuming the queue is nonempty).  For
     * each node n in the heap, and each descendant of n, d,
     * n.nextExecutionTime &lt;= d.nextExecutionTime.
     */
    private TimerTask[] queue = new TimerTask[128];

}

