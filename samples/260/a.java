abstract class AbstractCircuitBreaker&lt;T&gt; implements CircuitBreaker&lt;T&gt; {
    /**
     * Converts the given state value to a boolean &lt;em&gt;open&lt;/em&gt; property.
     *
     * @param state the state to be converted
     * @return the boolean open flag
     */
    protected static boolean isOpen(final State state) {
	return state == State.OPEN;
    }

}

