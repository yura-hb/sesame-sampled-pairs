import org.nd4j.linalg.dataset.api.DataSet;

class ComputationGraphUtil {
    /** Convert a DataSet to the equivalent MultiDataSet */
    public static MultiDataSet toMultiDataSet(DataSet dataSet) {
	INDArray f = dataSet.getFeatures();
	INDArray l = dataSet.getLabels();
	INDArray fMask = dataSet.getFeaturesMaskArray();
	INDArray lMask = dataSet.getLabelsMaskArray();

	INDArray[] fNew = f == null ? null : new INDArray[] { f };
	INDArray[] lNew = l == null ? null : new INDArray[] { l };
	INDArray[] fMaskNew = (fMask != null ? new INDArray[] { fMask } : null);
	INDArray[] lMaskNew = (lMask != null ? new INDArray[] { lMask } : null);

	return new org.nd4j.linalg.dataset.MultiDataSet(fNew, lNew, fMaskNew, lMaskNew);
    }

}

