import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

class AffineTransformOp implements BufferedImageOp, RasterOp {
    /**
     * Creates a zeroed destination {@code Raster} with the correct size
     * and number of bands.  A {@code RasterFormatException} may be thrown
     * if the transformed width or height is equal to 0.
     *
     * @param src The {@code Raster} to be transformed.
     *
     * @return The zeroed destination {@code Raster}.
     */
    public WritableRaster createCompatibleDestRaster(Raster src) {
	Rectangle2D r = getBounds2D(src);

	return src.createCompatibleWritableRaster((int) r.getX(), (int) r.getY(), (int) r.getWidth(),
		(int) r.getHeight());
    }

    private AffineTransform xform;

    /**
     * Returns the bounding box of the transformed destination.  The
     * rectangle returned will be the actual bounding box of the
     * transformed points.  The coordinates of the upper-left corner
     * of the returned rectangle might not be (0,&nbsp;0).
     *
     * @param src The {@code Raster} to be transformed.
     *
     * @return The {@code Rectangle2D} representing the destination's
     * bounding box.
     */
    public final Rectangle2D getBounds2D(Raster src) {
	int w = src.getWidth();
	int h = src.getHeight();

	// Get the bounding box of the src and transform the corners
	float[] pts = { 0, 0, w, 0, w, h, 0, h };
	xform.transform(pts, 0, pts, 0, 4);

	// Get the min, max of the dst
	float fmaxX = pts[0];
	float fmaxY = pts[1];
	float fminX = pts[0];
	float fminY = pts[1];
	for (int i = 2; i &lt; 8; i += 2) {
	    if (pts[i] &gt; fmaxX) {
		fmaxX = pts[i];
	    } else if (pts[i] &lt; fminX) {
		fminX = pts[i];
	    }
	    if (pts[i + 1] &gt; fmaxY) {
		fmaxY = pts[i + 1];
	    } else if (pts[i + 1] &lt; fminY) {
		fminY = pts[i + 1];
	    }
	}

	return new Rectangle2D.Float(fminX, fminY, fmaxX - fminX, fmaxY - fminY);
    }

}

