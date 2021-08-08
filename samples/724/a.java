import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.graph.LayerVertex;
import java.util.*;

class TransferLearning {
    class GraphBuilder {
	/**
	 * Add a vertex of the given configuration to the computation graph
	 * @param vertexName
	 * @param vertex
	 * @param vertexInputs
	 * @return
	 */
	public GraphBuilder addVertex(String vertexName, GraphVertex vertex, String... vertexInputs) {
	    initBuilderIfReq();
	    editedConfigBuilder.addVertex(vertexName, vertex, vertexInputs);
	    editedVertices.add(vertexName);
	    return this;
	}

	private ComputationGraphConfiguration.GraphBuilder editedConfigBuilder;
	private Set&lt;String&gt; editedVertices = new HashSet&lt;&gt;();
	private ComputationGraphConfiguration origConfig;
	private FineTuneConfiguration fineTuneConfiguration;

	private void initBuilderIfReq() {
	    if (editedConfigBuilder == null) {
		//No fine tune config has been set. One isn't required, but we need one to create the editedConfigBuilder
		//So: create an empty finetune config, which won't override anything
		//but keep the seed
		fineTuneConfiguration(new FineTuneConfiguration.Builder()
			.seed(origConfig.getDefaultConfiguration().getSeed()).build());
	    }
	}

	/**
	 * Set parameters to selectively override existing learning parameters
	 * Usage eg. specify a lower learning rate. This will get applied to all layers
	 * @param fineTuneConfiguration
	 * @return GraphBuilder
	 */
	public GraphBuilder fineTuneConfiguration(FineTuneConfiguration fineTuneConfiguration) {
	    this.fineTuneConfiguration = fineTuneConfiguration;
	    this.editedConfigBuilder = new ComputationGraphConfiguration.GraphBuilder(origConfig,
		    fineTuneConfiguration.appliedNeuralNetConfigurationBuilder());

	    Map&lt;String, GraphVertex&gt; vertices = this.editedConfigBuilder.getVertices();
	    for (Map.Entry&lt;String, GraphVertex&gt; gv : vertices.entrySet()) {
		if (gv.getValue() instanceof LayerVertex) {
		    LayerVertex lv = (LayerVertex) gv.getValue();
		    NeuralNetConfiguration nnc = lv.getLayerConf().clone();
		    fineTuneConfiguration.applyToNeuralNetConfiguration(nnc);
		    vertices.put(gv.getKey(), new LayerVertex(nnc, lv.getPreProcessor()));
		    nnc.getLayer().setLayerName(gv.getKey());
		}
	    }

	    return this;
	}

    }

}

