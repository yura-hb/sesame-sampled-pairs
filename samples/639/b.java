import org.nd4j.linalg.dataset.api.MultiDataSet;
import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.stats.NormalizerStats;
import java.util.HashMap;
import java.util.Map;

class MultiNormalizerHybrid extends AbstractNormalizer implements MultiDataNormalization, Serializable {
    /**
     * Iterates over a dataset
     * accumulating statistics for normalization
     *
     * @param iterator the iterator to use for collecting statistics
     */
    @Override
    public void fit(@NonNull MultiDataSetIterator iterator) {
	Map&lt;Integer, NormalizerStats.Builder&gt; inputStatsBuilders = new HashMap&lt;&gt;();
	Map&lt;Integer, NormalizerStats.Builder&gt; outputStatsBuilders = new HashMap&lt;&gt;();

	iterator.reset();
	while (iterator.hasNext()) {
	    fitPartial(iterator.next(), inputStatsBuilders, outputStatsBuilders);
	}

	inputStats = buildAllStats(inputStatsBuilders);
	outputStats = buildAllStats(outputStatsBuilders);
    }

    private Map&lt;Integer, NormalizerStats&gt; inputStats;
    private Map&lt;Integer, NormalizerStats&gt; outputStats;
    @Getter
    private NormalizerStrategy globalInputStrategy;
    @Getter
    private Map&lt;Integer, NormalizerStrategy&gt; perInputStrategies = new HashMap&lt;&gt;();
    @Getter
    private NormalizerStrategy globalOutputStrategy;
    @Getter
    private Map&lt;Integer, NormalizerStrategy&gt; perOutputStrategies = new HashMap&lt;&gt;();

    private void fitPartial(MultiDataSet dataSet, Map&lt;Integer, NormalizerStats.Builder&gt; inputStatsBuilders,
	    Map&lt;Integer, NormalizerStats.Builder&gt; outputStatsBuilders) {
	ensureStatsBuilders(inputStatsBuilders, globalInputStrategy, perInputStrategies, dataSet.numFeatureArrays());
	ensureStatsBuilders(outputStatsBuilders, globalOutputStrategy, perOutputStrategies, dataSet.numLabelsArrays());

	for (int index : inputStatsBuilders.keySet()) {
	    inputStatsBuilders.get(index).add(dataSet.getFeatures(index), dataSet.getFeaturesMaskArray(index));
	}
	for (int index : outputStatsBuilders.keySet()) {
	    outputStatsBuilders.get(index).add(dataSet.getLabels(index), dataSet.getLabelsMaskArray(index));
	}
    }

    private Map&lt;Integer, NormalizerStats&gt; buildAllStats(@NonNull Map&lt;Integer, NormalizerStats.Builder&gt; builders) {
	Map&lt;Integer, NormalizerStats&gt; result = new HashMap&lt;&gt;(builders.size());
	for (int index : builders.keySet()) {
	    result.put(index, builders.get(index).build());
	}
	return result;
    }

    private void ensureStatsBuilders(Map&lt;Integer, NormalizerStats.Builder&gt; builders, NormalizerStrategy globalStrategy,
	    Map&lt;Integer, NormalizerStrategy&gt; perArrayStrategies, int numArrays) {
	if (builders.isEmpty()) {
	    for (int i = 0; i &lt; numArrays; i++) {
		NormalizerStrategy strategy = getStrategy(globalStrategy, perArrayStrategies, i);
		if (strategy != null) {
		    builders.put(i, strategy.newStatsBuilder());
		}
	    }
	}
    }

    private NormalizerStrategy getStrategy(NormalizerStrategy globalStrategy,
	    Map&lt;Integer, NormalizerStrategy&gt; perArrayStrategy, int index) {
	NormalizerStrategy strategy = globalStrategy;
	if (perArrayStrategy.containsKey(index)) {
	    strategy = perArrayStrategy.get(index);
	}
	return strategy;
    }

}

