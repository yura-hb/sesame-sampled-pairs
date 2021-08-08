import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import java.util.concurrent.atomic.AtomicLong;

class MultipleEpochsIterator implements DataSetIterator {
    /**
     * Resets the iterator back to the beginning
     */
    @Override
    public void reset() {
	if (!iter.resetSupported()) {
	    throw new IllegalStateException(
		    "Cannot reset MultipleEpochsIterator with base iter that does not support reset");
	}
	epochs = 0;
	lastBatch = batch;
	batch = 0;
	iterationsCounter.set(0);
	iter.reset();
    }

    protected DataSetIterator iter;
    @VisibleForTesting
    protected int epochs = 0;
    protected int lastBatch = batch;
    protected int batch = 0;
    protected AtomicLong iterationsCounter = new AtomicLong(0);

}

