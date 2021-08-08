import org.datavec.image.data.ImageWritable;

class RandomCropTransform extends BaseImageTransform&lt;Mat&gt; {
    /**
     * Takes an image and returns a randomly cropped image.
     *
     * @param image  to transform, null == end of stream
     * @param random object to use (or null for deterministic)
     * @return transformed image
     */
    @Override
    protected ImageWritable doTransform(ImageWritable image, Random random) {
	if (image == null) {
	    return null;
	}
	// ensure that transform is valid
	if (image.getFrame().imageHeight &lt; outputHeight || image.getFrame().imageWidth &lt; outputWidth)
	    throw new UnsupportedOperationException(
		    "Output height/width cannot be more than the input image. Requested: " + outputHeight + "+x"
			    + outputWidth + ", got " + image.getFrame().imageHeight + "+x"
			    + image.getFrame().imageWidth);

	// determine boundary to place random offset
	int cropTop = image.getFrame().imageHeight - outputHeight;
	int cropLeft = image.getFrame().imageWidth - outputWidth;

	Mat mat = converter.convert(image.getFrame());
	int top = rng.nextInt(cropTop + 1);
	int left = rng.nextInt(cropLeft + 1);

	y = Math.min(top, mat.rows() - 1);
	x = Math.min(left, mat.cols() - 1);
	Mat result = mat.apply(new Rect(x, y, outputWidth, outputHeight));

	return new ImageWritable(converter.convert(result));
    }

    protected int outputHeight;
    protected int outputWidth;
    protected org.nd4j.linalg.api.rng.Random rng;
    private int y;
    private int x;

}

