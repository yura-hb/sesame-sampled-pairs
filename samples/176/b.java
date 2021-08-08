import java.awt.*;
import javax.swing.*;

class BasicSliderUI extends SliderUI {
    /**
     * Returns the x position for a value.
     * @param value the value
     * @return the x position for a value
     */
    protected int xPositionForValue(int value) {
	int min = slider.getMinimum();
	int max = slider.getMaximum();
	int trackLength = trackRect.width;
	double valueRange = (double) max - (double) min;
	double pixelsPerValue = (double) trackLength / valueRange;
	int trackLeft = trackRect.x;
	int trackRight = trackRect.x + (trackRect.width - 1);
	int xPosition;

	if (!drawInverted()) {
	    xPosition = trackLeft;
	    xPosition += Math.round(pixelsPerValue * ((double) value - min));
	} else {
	    xPosition = trackRight;
	    xPosition -= Math.round(pixelsPerValue * ((double) value - min));
	}

	xPosition = Math.max(trackLeft, xPosition);
	xPosition = Math.min(trackRight, xPosition);

	return xPosition;
    }

    /** Slider */
    protected JSlider slider;
    /** Track rectangle */
    protected Rectangle trackRect = null;

    /**
     * Draws inverted.
     * @return the inverted-ness
     */
    protected boolean drawInverted() {
	if (slider.getOrientation() == JSlider.HORIZONTAL) {
	    if (BasicGraphicsUtils.isLeftToRight(slider)) {
		return slider.getInverted();
	    } else {
		return !slider.getInverted();
	    }
	} else {
	    return slider.getInverted();
	}
    }

}

