import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.accum.MatchCondition;
import org.nd4j.linalg.api.shape.Shape;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.conditions.Condition;
import java.util.concurrent.atomic.AtomicBoolean;

class BooleanIndexing {
    /**
     * And over the whole ndarray given some condition
     *
     * @param n    the ndarray to test
     * @param cond the condition to test against
     * @return true if all of the elements meet the specified
     * condition false otherwise
     */
    public static boolean and(final INDArray n, final Condition cond) {
	if (cond instanceof BaseCondition) {
	    long val = (long) Nd4j.getExecutioner().exec(new MatchCondition(n, cond), Integer.MAX_VALUE).getDouble(0);

	    if (val == n.lengthLong())
		return true;
	    else
		return false;

	} else {
	    boolean ret = true;
	    final AtomicBoolean a = new AtomicBoolean(ret);
	    Shape.iterate(n, new CoordinateFunction() {
		@Override
		public void process(long[]... coord) {
		    if (a.get())
			a.compareAndSet(true, a.get() && cond.apply(n.getDouble(coord[0])));
		}
	    });

	    return a.get();
	}
    }

}

