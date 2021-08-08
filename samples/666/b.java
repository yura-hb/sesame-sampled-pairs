import java.awt.*;
import javax.swing.*;

class BasicComboBoxUI extends ComboBoxUI {
    /**
     * Returns the area that is reserved for drawing the currently selected item.
     *
     * @return the area that is reserved for drawing the currently selected item
     */
    protected Rectangle rectangleForCurrentValue() {
	int width = comboBox.getWidth();
	int height = comboBox.getHeight();
	Insets insets = getInsets();
	int buttonSize = height - (insets.top + insets.bottom);
	if (arrowButton != null) {
	    buttonSize = arrowButton.getWidth();
	}
	if (BasicGraphicsUtils.isLeftToRight(comboBox)) {
	    return new Rectangle(insets.left, insets.top, width - (insets.left + insets.right + buttonSize),
		    height - (insets.top + insets.bottom));
	} else {
	    return new Rectangle(insets.left + buttonSize, insets.top,
		    width - (insets.left + insets.right + buttonSize), height - (insets.top + insets.bottom));
	}
    }

    /**
     * The instance of {@code JComboBox}.
     */
    protected JComboBox&lt;Object&gt; comboBox;
    /**
     * The arrow button that invokes the popup.
     */
    protected JButton arrowButton;

    /**
     * Gets the insets from the JComboBox.
     *
     * @return the insets
     */
    protected Insets getInsets() {
	return comboBox.getInsets();
    }

}

