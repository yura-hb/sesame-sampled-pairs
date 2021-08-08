class JavaElementDelta extends SimpleDelta implements IJavaElementDelta {
    /**
    * Removes the element from the array.
    * Returns the a new array which has shrunk.
    */
    protected IJavaElementDelta[] removeAndShrinkArray(IJavaElementDelta[] old, int index) {
	IJavaElementDelta[] array = new IJavaElementDelta[old.length - 1];
	if (index &gt; 0)
	    System.arraycopy(old, 0, array, 0, index);
	int rest = old.length - index - 1;
	if (rest &gt; 0)
	    System.arraycopy(old, index + 1, array, index, rest);
	return array;
    }

}

