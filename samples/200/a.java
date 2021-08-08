import org.nd4j.linalg.api.ops.*;
import org.nd4j.linalg.api.ops.impl.layers.recurrent.SRU;

class SameDiff {
    /**
     * Simple recurrent unit
     *
     * @param configuration the configuration for the sru
     * @return
     */
    public SDVariable sru(SRUConfiguration configuration) {
	return new SRU(this, configuration).outputVariables()[0];
    }

}

