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
     * performs rotation of points in the plane.
     *
     * @param N
     * @param X
     * @param Y
     * @param c
     * @param s
     */
    @Override
    public void rot(long N, INDArray X, INDArray Y, double c, double s) {

	if (Nd4j.getExecutioner().getProfilingMode() == OpExecutioner.ProfilingMode.ALL)
	    OpProfiler.getInstance().processBlasCall(false, X, Y);

	if (X.isSparse() && !Y.isSparse()) {
	    Nd4j.getSparseBlasWrapper().level1().rot(N, X, Y, c, s);
	} else if (X.data().dataType() == DataBuffer.Type.DOUBLE) {
	    DefaultOpExecutioner.validateDataType(DataBuffer.Type.DOUBLE, X, Y);
	    drot(N, X, BlasBufferUtil.getBlasStride(X), Y, BlasBufferUtil.getBlasStride(X), c, s);
	} else {
	    DefaultOpExecutioner.validateDataType(DataBuffer.Type.FLOAT, X, Y);
	    srot(N, X, BlasBufferUtil.getBlasStride(X), Y, BlasBufferUtil.getBlasStride(X), (float) c, (float) s);
	}
    }

    protected abstract void drot(long N, INDArray X, int incX, INDArray Y, int incY, double c, double s);

    protected abstract void srot(long N, INDArray X, int incX, INDArray Y, int incY, float c, float s);

}

