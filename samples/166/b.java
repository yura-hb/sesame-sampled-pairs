import com.google.common.base.Optional;

class ValueGraphBuilder&lt;N, V&gt; extends AbstractGraphBuilder&lt;N&gt; {
    /**
    * Specifies the expected number of nodes in the graph.
    *
    * @throws IllegalArgumentException if {@code expectedNodeCount} is negative
    */
    public ValueGraphBuilder&lt;N, V&gt; expectedNodeCount(int expectedNodeCount) {
	this.expectedNodeCount = Optional.of(checkNonNegative(expectedNodeCount));
	return this;
    }

}

