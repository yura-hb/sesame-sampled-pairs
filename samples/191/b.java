import org.nd4j.linalg.api.blas.BlasBufferUtil;
import org.nd4j.linalg.api.blas.Level1;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.executioner.DefaultOpExecutioner;
import org.nd4j.linalg.api.ops.executioner.OpExecutioner;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.profiler.OpProfiler;

abstract class BaseLevel1 extends BaseLevel implements Level1 {
    /**
     * swaps a vector with another vector.
     *
     * @param x
     * @param y
     */
    @Override
    public void copy(INDArray x, INDArray y) {
	if (Nd4j.getExecutioner().getProfilingMode() == OpExecutioner.ProfilingMode.ALL)
	    OpProfiler.getInstance().processBlasCall(false, x, y);

	if (x.isSparse() || y.isSparse()) {
	    Nd4j.getSparseBlasWrapper().level1().copy(x, y);
	    return;
	}
	if (x.data().dataType() == DataBuffer.Type.DOUBLE) {
	    DefaultOpExecutioner.validateDataType(DataBuffer.Type.DOUBLE, x, y);
	    dcopy(x.length(), x, BlasBufferUtil.getBlasStride(x), y, BlasBufferUtil.getBlasStride(y));
	} else {
	    DefaultOpExecutioner.validateDataType(DataBuffer.Type.FLOAT, x, y);
	    scopy(x.length(), x, BlasBufferUtil.getBlasStride(x), y, BlasBufferUtil.getBlasStride(y));
	}
    }

    protected abstract void dcopy(long N, INDArray X, int incX, INDArray Y, int incY);

    protected abstract void scopy(long N, INDArray X, int incX, INDArray Y, int incY);

}

