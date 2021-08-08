class ConvolutionUtils {
    /**
     * Get the height and width
     * for an image
     *
     * @param shape the shape of the image
     * @return the height and width for the image
     */
    public static int[] getHeightAndWidth(int[] shape) {
	if (shape.length &lt; 2)
	    throw new IllegalArgumentException("No width and height able to be found: array must be at least length 2");
	return new int[] { shape[shape.length - 1], shape[shape.length - 2] };
    }

}

