import org.deeplearning4j.spark.parameterserver.util.BlockingObserver;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class SharedTrainingWrapper {
    /**
     * This method registers given Iterable&lt;MultiDataSet&gt; in VirtualMultiDataSetIterator
     *
     * @param iterator
     */
    public void attachMDS(Iterator&lt;MultiDataSet&gt; iterator) {
	log.debug("Attaching thread...");

	//Count the number of minibatches - used for reporting/debugging purposes
	if (iteratorDataSetCount.get() == null)
	    iteratorDataSetCount.set(new AtomicInteger(0));
	AtomicInteger count = iteratorDataSetCount.get();
	count.set(0);

	// we're creating our Observable wrapper
	VirtualIterator&lt;MultiDataSet&gt; wrapped = new VirtualIterator&lt;&gt;(new CountingIterator&lt;&gt;(iterator, count));

	// and creating Observer which will be used to monitor progress within iterator
	BlockingObserver obs = new BlockingObserver(exceptionEncountered);
	wrapped.addObserver(obs);

	// putting that "somewhere"
	iteratorsMDS.add(wrapped);

	// storing observer into ThreadLocal, since we're going to use that later
	observer.set(obs);
    }

    protected ThreadLocal&lt;AtomicInteger&gt; iteratorDataSetCount = new ThreadLocal&lt;&gt;();
    protected AtomicBoolean exceptionEncountered = new AtomicBoolean(false);
    protected List&lt;Iterator&lt;MultiDataSet&gt;&gt; iteratorsMDS;
    protected ThreadLocal&lt;BlockingObserver&gt; observer = new ThreadLocal&lt;&gt;();

}

