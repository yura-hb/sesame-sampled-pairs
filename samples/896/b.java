import java.awt.Graphics;

class SwingUtilities2 {
    /**
     * This method should be used for drawing a borders over a filled rectangle.
     * Draws horizontal line, using the current color, between the points {@code
     * (x1, y)} and {@code (x2, y)} in graphics context's coordinate system.
     * Note: it use {@code Graphics.fillRect()} internally.
     *
     * @param g  Graphics to draw the line to.
     * @param x1 the first point's &lt;i&gt;x&lt;/i&gt; coordinate.
     * @param x2 the second point's &lt;i&gt;x&lt;/i&gt; coordinate.
     * @param y  the &lt;i&gt;y&lt;/i&gt; coordinate.
     */
    public static void drawHLine(Graphics g, int x1, int x2, int y) {
	if (x2 &lt; x1) {
	    final int temp = x2;
	    x2 = x1;
	    x1 = temp;
	}
	g.fillRect(x1, y, x2 - x1 + 1, 1);
    }

}

