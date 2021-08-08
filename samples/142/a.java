import org.nd4j.linalg.dataset.api.preprocessor.stats.DistributionStats;

class NormalizerStandardize extends AbstractDataSetNormalizer&lt;DistributionStats&gt; {
    /**
     * @param files the files to save to. Needs 4 files if normalizing labels, otherwise 2.
     * @deprecated use {@link NormalizerSerializer} instead
     * &lt;p&gt;
     * Save the current means and standard deviations to the file system
     */
    public void save(File... files) throws IOException {
	getFeatureStats().save(files[0], files[1]);
	if (isFitLabel()) {
	    getLabelStats().save(files[2], files[3]);
	}
    }

}

