import org.deeplearning4j.nn.api.*;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.optimize.Solver;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.util.OneTimeLogger;
import java.util.*;

class MultiLayerNetwork implements Serializable, Classifier, Layer, NeuralNetwork {
    /**
     * This method: initializes the flattened gradients array (used in backprop) and sets the appropriate subset in all layers.
     * As a general rule, this shouldn't ever need to be called manually when doing training via fit(DataSet) or fit(DataSetIterator)
     */
    public void initGradientsView() {
	try (MemoryWorkspace ws = Nd4j.getMemoryManager().scopeOutOfWorkspaces()) {
	    if (layers == null)
		init();

	    int nLayers = layers.length;

	    //First: Work out total length of params
	    int paramLength = 0;
	    val nParamsPerLayer = new long[nLayers];
	    for (int i = 0; i &lt; nLayers; i++) {
		NeuralNetConfiguration conf = layerWiseConfigurations.getConf(i);
		nParamsPerLayer[i] = conf.getLayer().initializer().numParams(conf);
		paramLength += nParamsPerLayer[i];
	    }

	    if (paramLength &gt; 0) {
		flattenedGradients = Nd4j.zeros(new int[] { 1, paramLength }, 'f'); //No need to initialize, as each layer will do it each iteration anyway
	    }

	    int backpropParamsSoFar = 0;
	    for (int i = 0; i &lt; layers.length; i++) {
		if (nParamsPerLayer[i] == 0)
		    continue; //This layer doesn't have any parameters...
		INDArray thisLayerGradView = flattenedGradients.get(NDArrayIndex.point(0),
			NDArrayIndex.interval(backpropParamsSoFar, backpropParamsSoFar + nParamsPerLayer[i]));
		layers[i].setBackpropGradientsViewArray(thisLayerGradView);
		backpropParamsSoFar += nParamsPerLayer[i];
	    }
	}
    }

    protected Layer[] layers;
    protected MultiLayerConfiguration layerWiseConfigurations;
    @Getter
    protected transient INDArray flattenedGradients;
    protected boolean initCalled = false;
    protected LinkedHashMap&lt;String, Layer&gt; layerMap = new LinkedHashMap&lt;&gt;();
    protected INDArray flattenedParams;
    protected Collection&lt;TrainingListener&gt; trainingListeners = new ArrayList&lt;&gt;();
    protected NeuralNetConfiguration defaultConfiguration;
    protected transient Solver solver;

    /**
     * Initialize the MultiLayerNetwork. This should be called once before the network is used.
     * This is functionally equivalent to calling
     * {@code init(null, false)}.
     * @see MultiLayerNetwork#init(INDArray, boolean)
     */
    public void init() {
	init(null, false);
    }

    /**
     * Initialize the MultiLayerNetwork, optionally with an existing parameters array.
     * If an existing parameters array is specified, it will be used (and the values will not be modified) in the network;
     * if no parameters array is specified, parameters will be initialized randomly according to the network configuration.
     *
     * @param parameters              Network parameter. May be null. If null: randomly initialize.
     * @param cloneParametersArray    Whether the parameter array (if any) should be cloned, or used directly
     */
    public void init(INDArray parameters, boolean cloneParametersArray) {
	if (layerWiseConfigurations == null || layers == null)
	    intializeConfigurations();
	if (initCalled)
	    return;

	if (layerMap == null)
	    layerMap = new LinkedHashMap&lt;&gt;();

	if (layerWiseConfigurations.getTrainingWorkspaceMode() == null)
	    layerWiseConfigurations.setTrainingWorkspaceMode(WorkspaceMode.NONE);

	if (layerWiseConfigurations.getInferenceWorkspaceMode() == null)
	    layerWiseConfigurations.setInferenceWorkspaceMode(WorkspaceMode.NONE);

	if (layerWiseConfigurations.getCacheMode() == null)
	    layerWiseConfigurations.setCacheMode(CacheMode.NONE);

	OneTimeLogger.info(log,
		"Starting MultiLayerNetwork with WorkspaceModes set to [training: {}; inference: {}], cacheMode set to [{}]",
		layerWiseConfigurations.getTrainingWorkspaceMode(), layerWiseConfigurations.getInferenceWorkspaceMode(),
		layerWiseConfigurations.getCacheMode());

	//TODO
	//        if (layerWiseConfigurations.getCacheMode() == CacheMode.HOST) {
	//            workspaceConfigurationCache.setPolicyMirroring(MirroringPolicy.HOST_ONLY);
	//        }

	int nLayers = getnLayers();

	if (nLayers &lt; 1)
	    throw new IllegalStateException("Unable to create network: number of layers is less than 1");

	if (this.layers == null || this.layers[0] == null) {
	    if (this.layers == null)
		this.layers = new Layer[nLayers];

	    //First: Work out total length of (backprop) params
	    int paramLength = 0;
	    val nParamsPerLayer = new long[nLayers];
	    for (int i = 0; i &lt; nLayers; i++) {
		NeuralNetConfiguration conf = layerWiseConfigurations.getConf(i);
		nParamsPerLayer[i] = conf.getLayer().initializer().numParams(conf);
		paramLength += nParamsPerLayer[i];
	    }

	    //Create parameters array, if required
	    boolean initializeParams;
	    if (parameters != null) {
		if (!parameters.isRowVectorOrScalar())
		    throw new IllegalArgumentException("Invalid parameters: should be a row vector");
		if (parameters.length() != paramLength)
		    throw new IllegalArgumentException("Invalid parameters: expected length " + paramLength
			    + ", got length " + parameters.length());

		if (cloneParametersArray)
		    flattenedParams = parameters.dup();
		else
		    flattenedParams = parameters;

		initializeParams = false;
	    } else if (paramLength &gt; 0) {
		flattenedParams = Nd4j.create(1, paramLength);
		initializeParams = true;
	    } else {
		//Edge case: 0 params in network
		flattenedParams = null;
		initializeParams = false;
	    }

	    //Set RNG seed, for repeatability between initializations when set
	    if (initializeParams) {
		Nd4j.getRandom().setSeed(getDefaultConfiguration().getSeed());
	    }

	    // construct multi-layer
	    int paramCountSoFar = 0;
	    for (int i = 0; i &lt; nLayers; i++) {
		INDArray paramsView;
		if (nParamsPerLayer[i] &gt; 0) {
		    paramsView = flattenedParams.get(NDArrayIndex.point(0),
			    NDArrayIndex.interval(paramCountSoFar, paramCountSoFar + nParamsPerLayer[i]));
		} else {
		    paramsView = null;
		}
		paramCountSoFar += nParamsPerLayer[i];

		NeuralNetConfiguration conf = layerWiseConfigurations.getConf(i);
		layers[i] = conf.getLayer().instantiate(conf, trainingListeners, i, paramsView, initializeParams);
		layerMap.put(conf.getLayer().getLayerName(), layers[i]);
	    }
	    initCalled = true;
	}

	//Set parameters in MultiLayerNetwork.defaultConfiguration for later use in BaseOptimizer.setupSearchState() etc
	defaultConfiguration.clearVariables();
	List&lt;String&gt; variables = defaultConfiguration.variables(false);
	for (int i = 0; i &lt; layers.length; i++) {
	    if (layers[i] == null) {
		throw new IllegalStateException("Encountered null layer during initialization for layer " + i + ": "
			+ layerWiseConfigurations.getConf(i).getLayer().getClass().getSimpleName() + " initialization "
			+ "returned null layer?");
	    }

	    for (String s : layers[i].conf().variables()) {
		variables.add(i + "_" + s);
	    }
	}

	// now we init solver & optimizer
	if (solver == null) {
	    try (MemoryWorkspace wsO = Nd4j.getMemoryManager().scopeOutOfWorkspaces()) {
		solver = new Solver.Builder().configure(conf()).listeners(getListeners()).model(this).build();
		solver.initOptimizer();
	    }
	}

	//Mark that input modification is allowed.
	//TODO When is it safe to NOT skip the very first layer? It's not always safe...
	// For example dropout + iterating over List&lt;DataSet&gt; that is used for multiple epochs...
	for (int i = 1; i &lt; layers.length; i++) {
	    layers[i].allowInputModification(true);
	}

	synchronizeIterEpochCounts();
    }

    protected void intializeConfigurations() {

	if (layerWiseConfigurations == null)
	    layerWiseConfigurations = new MultiLayerConfiguration.Builder().build();

	if (layers == null)
	    layers = new Layer[getnLayers()];

	if (defaultConfiguration == null)
	    defaultConfiguration = new NeuralNetConfiguration.Builder().build();
    }

    /**
     * Get the number of layers in the network
     *
     * @return the number of layers in the network
     */
    public int getnLayers() {
	return layerWiseConfigurations.getConfs().size();
    }

    public NeuralNetConfiguration getDefaultConfiguration() {
	return defaultConfiguration;
    }

    @Override
    public NeuralNetConfiguration conf() {
	return defaultConfiguration;
    }

    /**
     *
     * @return listeners
     */
    public Collection&lt;TrainingListener&gt; getListeners() {
	return trainingListeners;
    }

    protected void synchronizeIterEpochCounts() {
	//TODO: this is necessary for some schedules - but the redundant values are a little ugly...
	int currIter = getIterationCount();
	int currEpoch = getEpochCount();
	for (Layer l : layers) {
	    l.setIterationCount(currIter);
	    l.setEpochCount(currEpoch);
	}
    }

    @Override
    public int getIterationCount() {
	return getLayerWiseConfigurations().getIterationCount();
    }

    @Override
    public int getEpochCount() {
	return getLayerWiseConfigurations().getEpochCount();
    }

    public MultiLayerConfiguration getLayerWiseConfigurations() {
	return layerWiseConfigurations;
    }

}

