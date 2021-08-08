import org.nd4j.linalg.factory.Nd4j;

class StandardScaler {
    /**
     * Save the current mean and std
     * @param mean the mean
     * @param std the std
     * @throws IOException
     */
    public void save(File mean, File std) throws IOException {
	Nd4j.saveBinary(this.mean, mean);
	Nd4j.saveBinary(this.std, std);
    }

    private INDArray mean, std;
    private INDArray mean, std;

}

