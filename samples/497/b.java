import java.util.Hashtable;
import sun.awt.image.OffScreenImageSource;

class BufferedImage extends Image implements WritableRenderedImage, Transparency {
    /**
     * Returns the object that produces the pixels for the image.
     * @return the {@link ImageProducer} that is used to produce the
     * pixels for this image.
     * @see ImageProducer
     */
    public ImageProducer getSource() {
	if (osis == null) {
	    if (properties == null) {
		properties = new Hashtable&lt;&gt;();
	    }
	    osis = new OffScreenImageSource(this, properties);
	}
	return osis;
    }

    private OffScreenImageSource osis;
    private Hashtable&lt;String, Object&gt; properties;

}

