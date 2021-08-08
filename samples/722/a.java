import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

class OldConvolution {
    /**
     * Rearrange matrix
     * columns into blocks
    
     * @param col the column
     *            transposed image to convert
     * @param sy stride y
     * @param sx stride x
     * @param ph padding height
     * @param pw padding width
     * @param h height
     * @param w width
     * @return
     */
    public static INDArray col2im(INDArray col, int sy, int sx, int ph, int pw, int h, int w) {
	//number of images
	long n = col.size(0);
	//number of columns
	long c = col.size(1);
	//kernel height
	long kh = col.size(2);
	//kernel width
	long kw = col.size(3);
	//out height
	long outH = col.size(4);
	//out width
	long outW = col.size(5);

	INDArray img = Nd4j.create(n, c, h + 2 * ph + sy - 1, w + 2 * pw + sx - 1);
	for (int i = 0; i &lt; kh; i++) {
	    //iterate over the kernel rows
	    long iLim = i + sy * outH;
	    for (int j = 0; j &lt; kw; j++) {
		//iterate over the kernel columns
		long jLim = j + sx * outW;
		INDArrayIndex[] indices = new INDArrayIndex[] { NDArrayIndex.all(), NDArrayIndex.all(),
			NDArrayIndex.interval(i, sy, iLim), NDArrayIndex.interval(j, sx, jLim) };

		INDArray get = img.get(indices);

		INDArray colAdd = col.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.point(i),
			NDArrayIndex.point(j), NDArrayIndex.all(), NDArrayIndex.all());
		get.addi(colAdd);
		img.put(indices, get);

	    }
	}

	//return the subset of the padded image relative to the height/width of the image and the padding width/height
	return img.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.interval(ph, ph + h),
		NDArrayIndex.interval(pw, pw + w));
    }

}

