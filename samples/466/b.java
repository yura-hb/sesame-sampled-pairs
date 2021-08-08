import java.awt.*;
import java.util.Dictionary;
import java.util.Enumeration;
import javax.swing.*;

class BasicSliderUI extends SliderUI {
    /**
     * Returns the width of the highest value label.
     * @return the width of the highest value label
     */
    protected int getWidthOfHighValueLabel() {
	Component label = getHighestValueLabel();
	int width = 0;

	if (label != null) {
	    width = label.getPreferredSize().width;
	}

	return width;
    }

    /** Slider */
    protected JSlider slider;

    /**
     * Returns the label that corresponds to the lowest slider value in the
     * label table.
     *
     * @return the label that corresponds to the lowest slider value in the
     * label table
     * @see JSlider#setLabelTable
     */
    protected Component getHighestValueLabel() {
	Integer max = getHighestValue();
	if (max != null) {
	    return (Component) slider.getLabelTable().get(max);
	}
	return null;
    }

    /**
     * Returns the biggest value that has an entry in the label table.
     *
     * @return biggest value that has an entry in the label table, or
     *         null.
     * @since 1.6
     */
    protected Integer getHighestValue() {
	@SuppressWarnings("rawtypes")
	Dictionary dictionary = slider.getLabelTable();

	if (dictionary == null) {
	    return null;
	}

	Enumeration&lt;?&gt; keys = dictionary.keys();

	Integer max = null;

	while (keys.hasMoreElements()) {
	    Integer i = (Integer) keys.nextElement();

	    if (max == null || i &gt; max) {
		max = i;
	    }
	}

	return max;
    }

}

