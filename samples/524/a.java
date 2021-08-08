import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

class Network implements Iterable&lt;Neuron&gt;, Serializable {
    /**
     * Retrieves the neuron with the given (unique) {@code id}.
     *
     * @param id Identifier.
     * @return the neuron associated with the given {@code id}.
     * @throws NoSuchElementException if the neuron does not exist in the
     * network.
     */
    public Neuron getNeuron(long id) {
	final Neuron n = neuronMap.get(id);
	if (n == null) {
	    throw new NoSuchElementException(Long.toString(id));
	}
	return n;
    }

    /** Neurons. */
    private final ConcurrentHashMap&lt;Long, Neuron&gt; neuronMap = new ConcurrentHashMap&lt;&gt;();

}

