class CloneTransformer&lt;T&gt; implements Transformer&lt;T, T&gt; {
    /**
     * Transforms the input to result by cloning it.
     *
     * @param input  the input object to transform
     * @return the transformed result
     */
    @Override
    public T transform(final T input) {
	if (input == null) {
	    return null;
	}
	return PrototypeFactory.prototypeFactory(input).create();
    }

}

