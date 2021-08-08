import java.util.NoSuchElementException;

class SpecifiedIndex implements INDArrayIndex {
    class SingleGenerator implements Generator&lt;List&lt;Long&gt;&gt; {
	/**
	 * @return the next item in the sequence.
	 * @throws NoSuchElementException when sequence is exhausted.
	 */
	@Override
	public List&lt;Long&gt; next() throws NoSuchElementException {
	    if (!SpecifiedIndex.this.hasNext())
		throw new NoSuchElementException();

	    return Longs.asList(SpecifiedIndex.this.next());
	}

    }

    private int counter = 0;
    private long[] indexes;

    @Override
    public boolean hasNext() {
	return counter &lt; indexes.length;
    }

    @Override
    public long next() {
	return indexes[counter++];
    }

}

