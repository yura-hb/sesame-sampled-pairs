import org.deeplearning4j.eval.IEvaluation;
import org.deeplearning4j.exception.DL4JInvalidInputException;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.api.InvocationType;
import org.deeplearning4j.optimize.listeners.callbacks.EvaluationCallback;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.MultiDataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;
import java.util.concurrent.atomic.AtomicLong;

class EvaluativeListener extends BaseTrainingListener {
    /**
     * Event listener for each iteration
     *
     * @param model     the model iterating
     * @param iteration the iteration
     */
    @Override
    public void iterationDone(Model model, int iteration, int epoch) {
	if (invocationType == InvocationType.ITERATION_END)
	    invokeListener(model);
    }

    @Getter
    protected InvocationType invocationType;
    protected transient ThreadLocal&lt;AtomicLong&gt; iterationCount = new ThreadLocal&lt;&gt;();
    protected int frequency;
    @Getter
    protected IEvaluation[] evaluations;
    protected transient DataSetIterator dsIterator;
    protected transient MultiDataSetIterator mdsIterator;
    protected AtomicLong invocationCount = new AtomicLong(0);
    protected DataSet ds;
    protected MultiDataSet mds;
    /**
     * This callback will be invoked after evaluation finished
     */
    @Getter
    @Setter
    protected transient EvaluationCallback callback;

    protected void invokeListener(Model model) {
	if (iterationCount.get() == null)
	    iterationCount.set(new AtomicLong(0));

	if (iterationCount.get().getAndIncrement() % frequency != 0)
	    return;

	for (IEvaluation evaluation : evaluations)
	    evaluation.reset();

	if (dsIterator != null && dsIterator.resetSupported())
	    dsIterator.reset();
	else if (mdsIterator != null && mdsIterator.resetSupported())
	    mdsIterator.reset();

	// FIXME: we need to save/restore inputs, if we're being invoked with iterations &gt; 1

	log.info("Starting evaluation nr. {}", invocationCount.incrementAndGet());
	if (model instanceof MultiLayerNetwork) {
	    if (dsIterator != null) {
		((MultiLayerNetwork) model).doEvaluation(dsIterator, evaluations);
	    } else if (ds != null) {
		for (IEvaluation evaluation : evaluations)
		    evaluation.eval(ds.getLabels(), ((MultiLayerNetwork) model).output(ds.getFeatures()));
	    }
	} else if (model instanceof ComputationGraph) {
	    if (dsIterator != null) {
		((ComputationGraph) model).doEvaluation(dsIterator, evaluations);
	    } else if (mdsIterator != null) {
		((ComputationGraph) model).doEvaluation(mdsIterator, evaluations);
	    } else if (ds != null) {
		for (IEvaluation evaluation : evaluations)
		    evalAtIndex(evaluation, new INDArray[] { ds.getLabels() },
			    ((ComputationGraph) model).output(ds.getFeatures()), 0);
	    } else if (mds != null) {
		for (IEvaluation evaluation : evaluations)
		    evalAtIndex(evaluation, mds.getLabels(), ((ComputationGraph) model).output(mds.getFeatures()), 0);
	    }
	} else
	    throw new DL4JInvalidInputException("Model is unknown: " + model.getClass().getCanonicalName());

	// TODO: maybe something better should be used here?
	log.info("Reporting evaluation results:");
	for (IEvaluation evaluation : evaluations)
	    log.info("{}:\n{}", evaluation.getClass().getSimpleName(), evaluation.stats());

	if (callback != null)
	    callback.call(this, model, invocationCount.get(), evaluations);
    }

    protected void evalAtIndex(IEvaluation evaluation, INDArray[] labels, INDArray[] predictions, int index) {
	evaluation.eval(labels[index], predictions[index]);
    }

}

