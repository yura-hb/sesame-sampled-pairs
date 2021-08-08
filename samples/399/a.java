import java.awt.Color;
import java.io.File;

class FileNodeModel extends NodeAdapter {
    /**
     * This could be a nice feature. Improve it!
     */
    public Color getColor() {
	if (color == null) {

	    // float hue = (float)getFile().length() / 100000;
	    // float hue = 6.3F;
	    // if (hue &gt; 1) {
	    // hue = 1;
	    // }
	    // color = Color.getHSBColor(hue,0.5F, 0.5F);
	    // int red = (int)(1 / (getFile().length()+1) * 255);
	    // color = new Color(red,0,0);
	    color = isLeaf() ? Color.BLACK : Color.GRAY;
	}
	return color;
    }

    private Color color;
    private File file;

    public boolean isLeaf() {
	return file.isFile();
    }

}

