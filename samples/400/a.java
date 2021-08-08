import org.nd4j.linalg.api.ndarray.*;

class Nd4j {
    /**
     * Perform an operation along a diagonal
     *
     * @param x    the ndarray to perform the operation on
     * @param func the operation to perform
     */
    public static void doAlongDiagonal(INDArray x, Function&lt;Number, Number&gt; func) {
	if (x.isMatrix())
	    for (int i = 0; i &lt; x.rows(); i++)
		x.put(i, i, func.apply(x.getDouble(i, i)));
    }

}

