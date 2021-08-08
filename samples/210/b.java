import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;

class ImageLoader extends BaseImageLoader {
    /**
     * Load a rastered image from file
     * @param file the file to load
     * @return the rastered image
     * @throws IOException
     */
    public int[][] fromFile(File file) throws IOException {
	BufferedImage image = ImageIO.read(file);
	image = scalingIfNeed(image, true);
	return toIntArrayArray(image);
    }

    protected BufferedImage scalingIfNeed(BufferedImage image, boolean needAlpha) {
	return scalingIfNeed(image, height, width, needAlpha);
    }

    protected int[][] toIntArrayArray(BufferedImage image) {
	int w = image.getWidth(), h = image.getHeight();
	int[][] ret = new int[h][w];
	if (image.getRaster().getNumDataElements() == 1) {
	    Raster raster = image.getRaster();
	    for (int i = 0; i &lt; h; i++) {
		for (int j = 0; j &lt; w; j++) {
		    ret[i][j] = raster.getSample(j, i, 0);
		}
	    }
	} else {
	    for (int i = 0; i &lt; h; i++) {
		for (int j = 0; j &lt; w; j++) {
		    ret[i][j] = image.getRGB(j, i);
		}
	    }
	}
	return ret;
    }

    protected BufferedImage scalingIfNeed(BufferedImage image, long dstHeight, long dstWidth, boolean needAlpha) {
	if (dstHeight &gt; 0 && dstWidth &gt; 0 && (image.getHeight() != dstHeight || image.getWidth() != dstWidth)) {
	    Image scaled = image.getScaledInstance((int) dstWidth, (int) dstHeight, Image.SCALE_SMOOTH);

	    if (needAlpha && image.getColorModel().hasAlpha() && channels == BufferedImage.TYPE_4BYTE_ABGR) {
		return toBufferedImage(scaled, BufferedImage.TYPE_4BYTE_ABGR);
	    } else {
		if (channels == BufferedImage.TYPE_BYTE_GRAY)
		    return toBufferedImage(scaled, BufferedImage.TYPE_BYTE_GRAY);
		else
		    return toBufferedImage(scaled, BufferedImage.TYPE_3BYTE_BGR);
	    }
	} else {
	    if (image.getType() == BufferedImage.TYPE_4BYTE_ABGR || image.getType() == BufferedImage.TYPE_3BYTE_BGR) {
		return image;
	    } else if (needAlpha && image.getColorModel().hasAlpha() && channels == BufferedImage.TYPE_4BYTE_ABGR) {
		return toBufferedImage(image, BufferedImage.TYPE_4BYTE_ABGR);
	    } else {
		if (channels == BufferedImage.TYPE_BYTE_GRAY)
		    return toBufferedImage(image, BufferedImage.TYPE_BYTE_GRAY);
		else
		    return toBufferedImage(image, BufferedImage.TYPE_3BYTE_BGR);
	    }
	}
    }

    /**
     * Converts a given Image into a BufferedImage
     *
     * @param img The Image to be converted
     * @param type The color model of BufferedImage
     * @return The converted BufferedImage
     */
    public static BufferedImage toBufferedImage(Image img, int type) {
	if (img instanceof BufferedImage) {
	    return (BufferedImage) img;
	}

	// Create a buffered image with transparency
	BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), type);

	// Draw the image on to the buffered image
	Graphics2D bGr = bimage.createGraphics();
	bGr.drawImage(img, 0, 0, null);
	bGr.dispose();

	// Return the buffered image
	return bimage;
    }

}

