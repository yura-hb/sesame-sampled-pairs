abstract class BaseNDArrayFactory implements NDArrayFactory {
    /**
     * Random normal using the specified seed
     *
     * @param rows    the number of rows in the matrix
     * @param columns the number of columns in the matrix
     * @return
     */
    @Override
    public INDArray randn(long rows, long columns, long seed) {
	Nd4j.getRandom().setSeed(seed);
	return randn(new long[] { rows, columns }, Nd4j.getRandom());
    }

    @Override
    public INDArray randn(long[] shape, org.nd4j.linalg.api.rng.Random r) {
	return r.nextGaussian(shape);
    }

}

