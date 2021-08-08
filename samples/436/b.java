import org.deeplearning4j.nn.api.layers.RecurrentLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

class ComputationGraph implements Serializable, Model, NeuralNetwork {
    /**
     * Update the internal state of RNN layers after a truncated BPTT fit call
     */
    protected void rnnUpdateStateWithTBPTTState() {
	for (int i = 0; i &lt; layers.length; i++) {
	    if (layers[i] instanceof RecurrentLayer) {
		RecurrentLayer l = ((RecurrentLayer) layers[i]);
		l.rnnSetPreviousState(l.rnnGetTBPTTState());
	    } else if (layers[i] instanceof MultiLayerNetwork) {
		((MultiLayerNetwork) layers[i]).updateRnnStateWithTBPTTState();
	    }
	}
    }

    /**
     * A list of layers. Each of these layers is present in a GraphVertex, but are here for easy reference.
     * This array also defines the order in which the getLayer(int) method returns layers.
     */
    protected Layer[] layers;

}

