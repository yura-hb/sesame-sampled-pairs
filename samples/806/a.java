import org.nd4j.linalg.api.ndarray.*;
import org.nd4j.linalg.util.ArrayUtil;

class Nd4j {
    /**
     * An alias for repmat
     *
     * @param tile   the ndarray to tile
     * @param repeat the shape to repeat
     * @return the tiled ndarray
     */
    public static INDArray tile(INDArray tile, int... repeat) {
	int d = repeat.length;
	long[] shape = ArrayUtil.copy(tile.shape());
	long n = Math.max(tile.length(), 1);
	if (d &lt; tile.rank()) {
	    repeat = Ints.concat(ArrayUtil.nTimes(tile.rank() - d, 1), repeat);
	}
	for (int i = 0; i &lt; shape.length; i++) {
	    if (repeat[i] != 1) {
		tile = tile.reshape(-1, n).repeat(0, new int[] { repeat[i] });
	    }

	    long in = shape[i];
	    long nOut = in * repeat[i];
	    shape[i] = nOut;
	    n /= Math.max(in, 1);

	}

	return tile.reshape(shape);
    }

}

