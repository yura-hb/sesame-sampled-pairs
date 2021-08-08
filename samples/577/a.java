abstract class BaseOutputLayer extends FeedForwardLayer {
    abstract class Builder&lt;T&gt; extends Builder&lt;T&gt; {
	/**
	 * @param lossFunction Loss function for the output layer
	 */
	public T lossFunction(ILossFunction lossFunction) {
	    this.lossFn = lossFunction;
	    return (T) this;
	}

	protected ILossFunction lossFn = new LossMCXENT();

    }

}

