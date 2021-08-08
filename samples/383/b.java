class BranchLabel extends Label {
    /**
    * Makes the current label inline all references to the other label
    */
    public void becomeDelegateFor(BranchLabel otherLabel) {
	// other label is delegating to receiver from now on
	otherLabel.delegate = this;

	// all existing forward refs to other label are inlined into current label
	final int otherCount = otherLabel.forwardReferenceCount;
	if (otherCount == 0)
	    return;
	// need to merge the two sorted arrays of forward references
	int[] mergedForwardReferences = new int[this.forwardReferenceCount + otherCount];
	int indexInMerge = 0;
	int j = 0;
	int i = 0;
	int max = this.forwardReferenceCount;
	int max2 = otherLabel.forwardReferenceCount;
	loop1: for (; i &lt; max; i++) {
	    final int value1 = this.forwardReferences[i];
	    for (; j &lt; max2; j++) {
		final int value2 = otherLabel.forwardReferences[j];
		if (value1 &lt; value2) {
		    mergedForwardReferences[indexInMerge++] = value1;
		    continue loop1;
		} else if (value1 == value2) {
		    mergedForwardReferences[indexInMerge++] = value1;
		    j++;
		    continue loop1;
		} else {
		    mergedForwardReferences[indexInMerge++] = value2;
		}
	    }
	    mergedForwardReferences[indexInMerge++] = value1;
	}
	for (; j &lt; max2; j++) {
	    mergedForwardReferences[indexInMerge++] = otherLabel.forwardReferences[j];
	}
	this.forwardReferences = mergedForwardReferences;
	this.forwardReferenceCount = indexInMerge;
    }

    BranchLabel delegate;
    private int forwardReferenceCount = 0;
    private int[] forwardReferences = new int[10];

}

