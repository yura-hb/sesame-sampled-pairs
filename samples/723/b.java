import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;

class GapContent extends GapVector implements Content, Serializable {
    /**
     * Creates a position within the content that will
     * track change as the content is mutated.
     *
     * @param offset the offset to track &gt;= 0
     * @return the position
     * @exception BadLocationException if the specified position is invalid
     */
    public Position createPosition(int offset) throws BadLocationException {
	while (queue.poll() != null) {
	    unusedMarks++;
	}
	if (unusedMarks &gt; Math.max(5, (marks.size() / 10))) {
	    removeUnusedMarks();
	}
	int g0 = getGapStart();
	int g1 = getGapEnd();
	int index = (offset &lt; g0) ? offset : offset + (g1 - g0);
	search.index = index;
	int sortIndex = findSortIndex(search);
	MarkData m;
	StickyPosition position;
	if (sortIndex &lt; marks.size() && (m = marks.elementAt(sortIndex)).index == index
		&& (position = m.getPosition()) != null) {
	    //position references the correct StickyPostition
	} else {
	    position = new StickyPosition();
	    m = new MarkData(index, position, queue);
	    position.setMark(m);
	    marks.insertElementAt(m, sortIndex);
	}

	return position;
    }

    private transient ReferenceQueue&lt;StickyPosition&gt; queue;
    /**
     * The number of unused mark entries
     */
    private transient int unusedMarks = 0;
    private transient MarkVector marks;
    /**
     * Record used for searching for the place to
     * start updating mark indexs when the gap
     * boundaries are moved.
     */
    private transient MarkData search;

    /**
     * Remove all unused marks out of the sorted collection
     * of marks.
     */
    final void removeUnusedMarks() {
	int n = marks.size();
	MarkVector cleaned = new MarkVector(n);
	for (int i = 0; i &lt; n; i++) {
	    MarkData mark = marks.elementAt(i);
	    if (mark.get() != null) {
		cleaned.addElement(mark);
	    }
	}
	marks = cleaned;
	unusedMarks = 0;
    }

    /**
     * Finds the index of where to insert a new mark.
     *
     * @param o the mark to insert
     * @return the index
     */
    final int findSortIndex(MarkData o) {
	int lower = 0;
	int upper = marks.size() - 1;
	int mid = 0;

	if (upper == -1) {
	    return 0;
	}

	int cmp;
	MarkData last = marks.elementAt(upper);
	cmp = compare(o, last);
	if (cmp &gt; 0)
	    return upper + 1;

	while (lower &lt;= upper) {
	    mid = lower + ((upper - lower) / 2);
	    MarkData entry = marks.elementAt(mid);
	    cmp = compare(o, entry);

	    if (cmp == 0) {
		// found a match
		return mid;
	    } else if (cmp &lt; 0) {
		upper = mid - 1;
	    } else {
		lower = mid + 1;
	    }
	}

	// didn't find it, but we indicate the index of where it would belong.
	return (cmp &lt; 0) ? mid : mid + 1;
    }

    /**
     * Compares two marks.
     *
     * @param o1 the first object
     * @param o2 the second object
     * @return &lt; 0 if o1 &lt; o2, 0 if the same, &gt; 0 if o1 &gt; o2
     */
    final int compare(MarkData o1, MarkData o2) {
	if (o1.index &lt; o2.index) {
	    return -1;
	} else if (o1.index &gt; o2.index) {
	    return 1;
	} else {
	    return 0;
	}
    }

    class MarkVector extends GapVector {
	private transient ReferenceQueue&lt;StickyPosition&gt; queue;
	/**
	* The number of unused mark entries
	*/
	private transient int unusedMarks = 0;
	private transient MarkVector marks;
	/**
	* Record used for searching for the place to
	* start updating mark indexs when the gap
	* boundaries are moved.
	*/
	private transient MarkData search;

	/**
	 * Returns the number of marks currently held
	 */
	public int size() {
	    int len = getArrayLength() - (getGapEnd() - getGapStart());
	    return len;
	}

	/**
	 * Fetches the mark at the given index
	 */
	public MarkData elementAt(int index) {
	    int g0 = getGapStart();
	    int g1 = getGapEnd();
	    MarkData[] array = (MarkData[]) getArray();
	    if (index &lt; g0) {
		// below gap
		return array[index];
	    } else {
		// above gap
		index += g1 - g0;
		return array[index];
	    }
	}

	/**
	 * Inserts a mark into the vector
	 */
	public void insertElementAt(MarkData m, int index) {
	    oneMark[0] = m;
	    replace(index, 0, oneMark, 1);
	}

	/**
	 * Get the length of the allocated array
	 */
	protected int getArrayLength() {
	    MarkData[] marks = (MarkData[]) getArray();
	    return marks.length;
	}

	MarkVector(int size) {
	    super(size);
	}

	/**
	 * Add a mark to the end
	 */
	public void addElement(MarkData m) {
	    insertElementAt(m, size());
	}

    }

    class MarkData extends WeakReference&lt;StickyPosition&gt; {
	private transient ReferenceQueue&lt;StickyPosition&gt; queue;
	/**
	* The number of unused mark entries
	*/
	private transient int unusedMarks = 0;
	private transient MarkVector marks;
	/**
	* Record used for searching for the place to
	* start updating mark indexs when the gap
	* boundaries are moved.
	*/
	private transient MarkData search;

	StickyPosition getPosition() {
	    return get();
	}

	MarkData(int index, StickyPosition position, ReferenceQueue&lt;? super StickyPosition&gt; queue) {
	    super(position, queue);
	    this.index = index;
	}

    }

    class StickyPosition implements Position {
	private transient ReferenceQueue&lt;StickyPosition&gt; queue;
	/**
	* The number of unused mark entries
	*/
	private transient int unusedMarks = 0;
	private transient MarkVector marks;
	/**
	* Record used for searching for the place to
	* start updating mark indexs when the gap
	* boundaries are moved.
	*/
	private transient MarkData search;

	StickyPosition() {
	}

	void setMark(MarkData mark) {
	    this.mark = mark;
	}

    }

}

