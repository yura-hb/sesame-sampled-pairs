import java.awt.*;

class BasicInternalFrameUI extends InternalFrameUI {
    /**
     * Returns the maximum size.
     * @param x the component
     * @return the maximum size
     */
    public Dimension getMaximumSize(JComponent x) {
	return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

}

