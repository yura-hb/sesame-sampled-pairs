import java.util.LinkedList;

class MemoryHistory implements History {
    /**
     * Return the content of the current buffer.
     */
    public CharSequence current() {
	if (index &gt;= size()) {
	    return "";
	}

	return items.get(index);
    }

    private int index = 0;
    private final LinkedList&lt;CharSequence&gt; items = new LinkedList&lt;CharSequence&gt;();

    public int size() {
	return items.size();
    }

}

