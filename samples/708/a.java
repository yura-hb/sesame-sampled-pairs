class ParallelWrapper implements AutoCloseable {
    class Builder&lt;T&gt; {
	/**
	 * Model averaging frequency.
	 *
	 * @param freq number of iterations between averaging
	 * @return
	 */
	public Builder averagingFrequency(int freq) {
	    if (freq &lt; 0)
		freq = 0;

	    this.averagingFrequency = freq;
	    return this;
	}

	protected int averagingFrequency = 1;

    }

}

