import com.google.common.collect.ObjectArrays;

class MonitorBasedArrayBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt; {
    /**
    * Returns an array containing all of the elements in this queue, in proper sequence; the runtime
    * type of the returned array is that of the specified array. If the queue fits in the specified
    * array, it is returned therein. Otherwise, a new array is allocated with the runtime type of the
    * specified array and the size of this queue.
    *
    * &lt;p&gt;If this queue fits in the specified array with room to spare (i.e., the array has more
    * elements than this queue), the element in the array immediately following the end of the queue
    * is set to &lt;tt&gt;null&lt;/tt&gt;.
    *
    * &lt;p&gt;Like the {@link #toArray()} method, this method acts as bridge between array-based and
    * collection-based APIs. Further, this method allows precise control over the runtime type of the
    * output array, and may, under certain circumstances, be used to save allocation costs.
    *
    * &lt;p&gt;Suppose &lt;tt&gt;x&lt;/tt&gt; is a queue known to contain only strings. The following code can be used
    * to dump the queue into a newly allocated array of &lt;tt&gt;String&lt;/tt&gt;:
    *
    * &lt;pre&gt;
    *     String[] y = x.toArray(new String[0]);&lt;/pre&gt;
    *
    * &lt;p&gt;Note that &lt;tt&gt;toArray(new Object[0])&lt;/tt&gt; is identical in function to &lt;tt&gt;toArray()&lt;/tt&gt;.
    *
    * @param a the array into which the elements of the queue are to be stored, if it is big enough;
    *     otherwise, a new array of the same runtime type is allocated for this purpose
    * @return an array containing all of the elements in this queue
    * @throws ArrayStoreException if the runtime type of the specified array is not a supertype of
    *     the runtime type of every element in this queue
    * @throws NullPointerException if the specified array is null
    */
    @Override
    public &lt;T&gt; T[] toArray(T[] a) {
	final E[] items = this.items;
	final Monitor monitor = this.monitor;
	monitor.enter();
	try {
	    if (a.length &lt; count)
		a = ObjectArrays.newArray(a, count);

	    int k = 0;
	    int i = takeIndex;
	    while (k &lt; count) {
		// This cast is not itself safe, but the following statement
		// will fail if the runtime type of items[i] is not assignable
		// to the runtime type of a[k++], which is all that the method
		// contract requires (see @throws ArrayStoreException above).
		@SuppressWarnings("unchecked")
		T t = (T) items[i];
		a[k++] = t;
		i = inc(i);
	    }
	    if (a.length &gt; count)
		a[count] = null;
	    return a;
	} finally {
	    monitor.leave();
	}
    }

    /** The queued items */
    final E[] items;
    /** Monitor guarding all access */
    final Monitor monitor;
    /** Number of items in the queue */
    private int count;
    /** items index for next take, poll or remove */
    int takeIndex;

    /** Circularly increment i. */
    final int inc(int i) {
	return (++i == items.length) ? 0 : i;
    }

}

