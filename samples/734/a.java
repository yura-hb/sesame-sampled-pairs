import org.deeplearning4j.nn.updater.graph.ComputationGraphUpdater;
import org.deeplearning4j.optimize.Solver;
import org.deeplearning4j.optimize.api.ConvexOptimizer;

class ComputationGraph implements Serializable, Model, NeuralNetwork {
    /**
     * Get the ComputationGraphUpdater for this network
     * @param initializeIfAbsent If true: create the updater if one is absent. False: return null if absent.
     * @return Updater
     */
    public ComputationGraphUpdater getUpdater(boolean initializeIfAbsent) {
	if (solver == null && initializeIfAbsent) {
	    solver = new Solver.Builder().configure(conf()).listeners(getListeners()).model(this).build();
	    solver.getOptimizer().setUpdaterComputationGraph(new ComputationGraphUpdater(this));
	}
	if (solver != null) {
	    return solver.getOptimizer().getComputationGraphUpdater();
	}
	return null;
    }

    protected transient Solver solver;
    private NeuralNetConfiguration defaultConfiguration;
    private Collection&lt;TrainingListener&gt; trainingListeners = new ArrayList&lt;&gt;();

    @Override
    public NeuralNetConfiguration conf() {
	return defaultConfiguration;
    }

    /**
     * Get the trainingListeners for the ComputationGraph
     */
    public Collection&lt;TrainingListener&gt; getListeners() {
	return trainingListeners;
    }

}

