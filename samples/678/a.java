import java.awt.Color;

class NodeView extends JComponent implements TreeModelListener {
    /**
     * Determines to a given color a color, that is the best contrary color. It
     * is different from {@link #getAntiColor1}.
     * 
     * @since PPS 1.1.1
     */
    protected static Color getAntiColor2(Color c) {
	float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
	hsb[0] -= 0.40;
	if (hsb[0] &lt; 0)
	    hsb[0]++;
	hsb[1] = 1;
	hsb[2] = (float) 0.8;
	return Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
    }

}

