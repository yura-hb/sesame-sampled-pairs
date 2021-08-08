import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import com.github.benmanes.caffeine.base.UnsafeAccess;

class SingleConsumerQueue&lt;E&gt; extends HeadAndTailRef&lt;E&gt; implements Queue&lt;E&gt;, Serializable {
    /** Adds the linked list of nodes to the queue. */
    void append(@NonNull Node&lt;E&gt; first, @NonNull Node&lt;E&gt; last) {
	for (;;) {
	    Node&lt;E&gt; t = tail;
	    if (casTail(t, last)) {
		t.lazySetNext(first);
		if (factory == OPTIMISIC) {
		    return;
		}
		for (;;) {
		    first.complete();
		    if (first == last) {
			return;
		    }
		    Node&lt;E&gt; next = first.getNextRelaxed();
		    if (next.value == null) {
			first.next = null; // reduce nepotism
		    }
		    first = next;
		}
	    }
	    Node&lt;E&gt; node = transferOrCombine(first, last);
	    if (node == null) {
		first.await();
		return;
	    } else if (node != first) {
		last = node;
	    }
	}
    }

    final Function&lt;E, Node&lt;E&gt;&gt; factory;
    /** The factory for creating an optimistic node. */
    static final Function&lt;?, ?&gt; OPTIMISIC = Node&lt;Object&gt;::new;
    final AtomicReference&lt;Node&lt;E&gt;&gt;[] arena;
    /**
    * The number of times to spin (doing nothing except polling a memory location) before giving up
    * while waiting to eliminate an operation. Should be zero on uniprocessors. On multiprocessors,
    * this value should be large enough so that two threads exchanging items as fast as possible
    * block only when one of them is stalled (due to GC or preemption), but not much longer, to avoid
    * wasting CPU resources. Seen differently, this value is a little over half the number of cycles
    * of an average context switch time on most systems. The value here is approximately the average
    * of those across a range of tested systems.
    */
    static final int SPINS = (NCPU == 1) ? 0 : 2000;
    /** The number of slots in the elimination array. */
    static final int ARENA_LENGTH = ceilingPowerOfTwo((NCPU + 1) / 2);
    /** The mask value for indexing into the arena. */
    static final int ARENA_MASK = ARENA_LENGTH - 1;
    /** The offset to the thread-specific probe field. */
    static final long PROBE = UnsafeAccess.objectFieldOffset(Thread.class, "threadLocalRandomProbe");

    /**
    * Attempts to receive a linked list from a waiting producer or transfer the specified linked list
    * to an arriving producer.
    *
    * @param first the first node in the linked list to try to transfer
    * @param last the last node in the linked list to try to transfer
    * @return either {@code null} if the element was transferred, the first node if neither a
    *         transfer nor receive were successful, or the received last element from a producer
    */
    @Nullable
    Node&lt;E&gt; transferOrCombine(@NonNull Node&lt;E&gt; first, Node&lt;E&gt; last) {
	int index = index();
	AtomicReference&lt;Node&lt;E&gt;&gt; slot = arena[index];

	for (;;) {
	    Node&lt;E&gt; found = slot.get();
	    if (found == null) {
		if (slot.compareAndSet(null, first)) {
		    for (int spin = 0; spin &lt; SPINS; spin++) {
			if (slot.get() != first) {
			    return null;
			}
		    }
		    return slot.compareAndSet(first, null) ? first : null;
		}
	    } else if (slot.compareAndSet(found, null)) {
		last.lazySetNext(found);
		last = findLast(found);
		for (int i = 1; i &lt; ARENA_LENGTH; i++) {
		    slot = arena[(i + index) & ARENA_MASK];
		    found = slot.get();
		    if ((found != null) && slot.compareAndSet(found, null)) {
			last.lazySetNext(found);
			last = findLast(found);
		    }
		}
		return last;
	    }
	}
    }

    /** Returns the arena index for the current thread. */
    static int index() {
	int probe = UnsafeAccess.UNSAFE.getInt(Thread.currentThread(), PROBE);
	if (probe == 0) {
	    ThreadLocalRandom.current(); // force initialization
	    probe = UnsafeAccess.UNSAFE.getInt(Thread.currentThread(), PROBE);
	}
	return (probe & ARENA_MASK);
    }

    /** Returns the last node in the linked list. */
    @NonNull
    static &lt;E&gt; Node&lt;E&gt; findLast(@NonNull Node&lt;E&gt; node) {
	Node&lt;E&gt; next;
	while ((next = node.getNextRelaxed()) != null) {
	    node = next;
	}
	return node;
    }

    class Node&lt;E&gt; {
	final Function&lt;E, Node&lt;E&gt;&gt; factory;
	/** The factory for creating an optimistic node. */
	static final Function&lt;?, ?&gt; OPTIMISIC = Node&lt;Object&gt;::new;
	final AtomicReference&lt;Node&lt;E&gt;&gt;[] arena;
	/**
	* The number of times to spin (doing nothing except polling a memory location) before giving up
	* while waiting to eliminate an operation. Should be zero on uniprocessors. On multiprocessors,
	* this value should be large enough so that two threads exchanging items as fast as possible
	* block only when one of them is stalled (due to GC or preemption), but not much longer, to avoid
	* wasting CPU resources. Seen differently, this value is a little over half the number of cycles
	* of an average context switch time on most systems. The value here is approximately the average
	* of those across a range of tested systems.
	*/
	static final int SPINS = (NCPU == 1) ? 0 : 2000;
	/** The number of slots in the elimination array. */
	static final int ARENA_LENGTH = ceilingPowerOfTwo((NCPU + 1) / 2);
	/** The mask value for indexing into the arena. */
	static final int ARENA_MASK = ARENA_LENGTH - 1;
	/** The offset to the thread-specific probe field. */
	static final long PROBE = UnsafeAccess.objectFieldOffset(Thread.class, "threadLocalRandomProbe");

	void lazySetNext(@Nullable Node&lt;E&gt; newNext) {
	    UnsafeAccess.UNSAFE.putOrderedObject(this, NEXT_OFFSET, newNext);
	}

	/** A no-op notification that the element was added to the queue. */
	void complete() {
	}

	@SuppressWarnings("unchecked")
	@Nullable
	Node&lt;E&gt; getNextRelaxed() {
	    return (Node&lt;E&gt;) UnsafeAccess.UNSAFE.getObject(this, NEXT_OFFSET);
	}

	/** A no-op wait until the operation has completed. */
	void await() {
	}

    }

}

class SCQHeader {
    abstract class HeadAndTailRef&lt;E&gt; extends PadHeadAndTail&lt;E&gt; {
	boolean casTail(Node&lt;E&gt; expect, Node&lt;E&gt; update) {
	    return UnsafeAccess.UNSAFE.compareAndSwapObject(this, TAIL_OFFSET, expect, update);
	}

	@Nullable
	volatile Node&lt;E&gt; tail;
	static final long TAIL_OFFSET = UnsafeAccess.objectFieldOffset(HeadAndTailRef.class, "tail");

    }

}

