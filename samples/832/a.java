class SequenceVectors&lt;T&gt; extends WordVectorsImpl&lt;T&gt; implements WordVectors {
    class Builder&lt;T&gt; {
	/**
	 * Sets specific LearningAlgorithm as Sequence Learning Algorithm
	 *
	 * @param algoName fully qualified class name
	 * @return
	 */
	public Builder&lt;T&gt; sequenceLearningAlgorithm(@NonNull String algoName) {
	    try {
		Class clazz = Class.forName(algoName);
		sequenceLearningAlgorithm = (SequenceLearningAlgorithm&lt;T&gt;) clazz.newInstance();
	    } catch (Exception e) {
		throw new RuntimeException(e);
	    }
	    return this;
	}

	protected SequenceLearningAlgorithm&lt;T&gt; sequenceLearningAlgorithm = new DBOW&lt;&gt;();

    }

}

