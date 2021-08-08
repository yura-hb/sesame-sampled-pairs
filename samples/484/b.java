class UimaResource {
    /**
     * Use the given analysis engine and process the given text
     * You must release the return cas yourself
     * @param text the text to process
     * @return the processed cas
     */
    public CAS process(String text) {
	CAS cas = retrieve();
	if (cas == null)
	    return null;

	cas.setDocumentText(text);
	try {
	    analysisEngine.process(cas);
	} catch (AnalysisEngineProcessException e) {
	    log.warn("Unable to process text " + text, e);
	}

	return cas;

    }

    private AnalysisEngine analysisEngine;
    private static final Logger log = LoggerFactory.getLogger(UimaResource.class);
    private CasPool casPool;

    public CAS retrieve() {
	CAS ret = casPool.getCas();
	try {
	    return ret == null ? analysisEngine.newCAS() : ret;
	} catch (ResourceInitializationException e) {
	    throw new RuntimeException(e);
	}
    }

}

