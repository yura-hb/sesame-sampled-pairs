import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

class MultiDataSet implements MultiDataSet {
    /**
     * This method returns memory used by this DataSet
     *
     * @return
     */
    @Override
    public long getMemoryFootprint() {
	long reqMem = 0;

	for (INDArray f : features)
	    reqMem += f == null ? 0 : f.lengthLong() * Nd4j.sizeOfDataType();

	if (featuresMaskArrays != null)
	    for (INDArray f : featuresMaskArrays)
		reqMem += f == null ? 0 : f.lengthLong() * Nd4j.sizeOfDataType();

	if (labelsMaskArrays != null)
	    for (INDArray f : labelsMaskArrays)
		reqMem += f == null ? 0 : f.lengthLong() * Nd4j.sizeOfDataType();

	if (labels != null)
	    for (INDArray f : labels)
		reqMem += f == null ? 0 : f.lengthLong() * Nd4j.sizeOfDataType();

	return reqMem;
    }

    private INDArray[] features;
    private INDArray[] featuresMaskArrays;
    private INDArray[] labelsMaskArrays;
    private INDArray[] labels;

}

