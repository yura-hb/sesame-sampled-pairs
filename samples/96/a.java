abstract class DOMNode implements IDOMNode {
    /**
    * Returns a copy of the given range.
    */
    protected int[] rangeCopy(int[] range) {
	int[] copy = new int[range.length];
	for (int i = 0; i &lt; range.length; i++) {
	    copy[i] = range[i];
	}
	return copy;
    }

}

