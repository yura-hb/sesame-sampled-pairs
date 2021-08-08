class TransformProcess implements Serializable {
    /**
     *
     * @param input
     * @return
     */
    public List&lt;List&lt;Writable&gt;&gt; executeSequenceToSequence(List&lt;List&lt;Writable&gt;&gt; input) {
	List&lt;List&lt;Writable&gt;&gt; currValues = input;

	for (DataAction d : actionList) {
	    if (d.getTransform() != null) {
		Transform t = d.getTransform();
		currValues = t.mapSequence(currValues);

	    } else if (d.getFilter() != null) {
		if (d.getFilter().removeSequence(currValues)) {
		    return null;
		}
	    } else if (d.getConvertToSequence() != null) {
		throw new RuntimeException(
			"Cannot execute examples individually: TransformProcess contains a ConvertToSequence operation");
	    } else if (d.getConvertFromSequence() != null) {
		throw new RuntimeException(
			"Unexpected operation: TransformProcess contains a ConvertFromSequence operation");
	    } else if (d.getSequenceSplit() != null) {
		throw new RuntimeException(
			"Cannot execute examples individually: TransformProcess contains a SequenceSplit operation");
	    } else {
		throw new RuntimeException("Unknown or not supported action: " + d);
	    }
	}

	return currValues;
    }

    private List&lt;DataAction&gt; actionList;

}

