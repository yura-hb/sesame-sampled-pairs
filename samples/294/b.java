import java.awt.*;
import javax.swing.*;
import javax.swing.colorchooser.*;

class GTKColorChooserPanel extends AbstractColorChooserPanel implements ChangeListener {
    /**
     * Refreshes the display from the model.
     */
    public void updateChooser() {
	if (!settingColor) {
	    lastLabel.setBackground(getColorFromModel());
	    setColor(getColorFromModel(), true, true, false);
	}
    }

    private boolean settingColor;
    private JLabel lastLabel;
    private float hue;
    private float saturation;
    private float brightness;
    private ColorTriangle triangle;
    private JLabel label;
    private JTextField colorNameTF;
    private JSpinner redSpinner;
    private JSpinner greenSpinner;
    private JSpinner blueSpinner;
    private JSpinner hueSpinner;
    private JSpinner saturationSpinner;
    private JSpinner valueSpinner;
    /**
     * Indicates a color is being set and we should ignore setColor
     */
    private static final int FLAGS_SETTING_COLOR = 1 &lt;&lt; 3;
    /**
     * Flag indicating the angle, or hue, has changed and the triangle
     * needs to be recreated.
     */
    private static final int FLAGS_CHANGED_ANGLE = 1 &lt;&lt; 0;

    /**
     * Rests the color.
     *
     * @param color new Color
     * @param updateSpinners whether or not to update the spinners.
     * @param updateHSB if true, the hsb fields are updated based on the
     *                  new color
     * @param updateModel if true, the model is set.
     */
    private void setColor(Color color, boolean updateSpinners, boolean updateHSB, boolean updateModel) {
	if (color == null) {
	    color = Color.BLACK;
	}

	settingColor = true;

	if (updateHSB) {
	    float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
	    hue = hsb[0];
	    saturation = hsb[1];
	    brightness = hsb[2];
	}

	if (updateModel) {
	    ColorSelectionModel model = getColorSelectionModel();
	    if (model != null) {
		model.setSelectedColor(color);
	    }
	}

	triangle.setColor(hue, saturation, brightness);
	label.setBackground(color);
	// Force Integer to pad the string with 0's by adding 0x1000000 and
	// then removing the first character.
	String hexString = Integer.toHexString((color.getRGB() & 0xFFFFFF) | 0x1000000);
	colorNameTF.setText("#" + hexString.substring(1));

	if (updateSpinners) {
	    redSpinner.setValue(Integer.valueOf(color.getRed()));
	    greenSpinner.setValue(Integer.valueOf(color.getGreen()));
	    blueSpinner.setValue(Integer.valueOf(color.getBlue()));

	    hueSpinner.setValue(Integer.valueOf((int) (hue * 360)));
	    saturationSpinner.setValue(Integer.valueOf((int) (saturation * 255)));
	    valueSpinner.setValue(Integer.valueOf((int) (brightness * 255)));
	}
	settingColor = false;
    }

    class ColorTriangle extends JPanel {
	private boolean settingColor;
	private JLabel lastLabel;
	private float hue;
	private float saturation;
	private float brightness;
	private ColorTriangle triangle;
	private JLabel label;
	private JTextField colorNameTF;
	private JSpinner redSpinner;
	private JSpinner greenSpinner;
	private JSpinner blueSpinner;
	private JSpinner hueSpinner;
	private JSpinner saturationSpinner;
	private JSpinner valueSpinner;
	/**
	* Indicates a color is being set and we should ignore setColor
	*/
	private static final int FLAGS_SETTING_COLOR = 1 &lt;&lt; 3;
	/**
	* Flag indicating the angle, or hue, has changed and the triangle
	* needs to be recreated.
	*/
	private static final int FLAGS_CHANGED_ANGLE = 1 &lt;&lt; 0;

	/**
	 * Resets the selected color.
	 */
	public void setColor(float h, float s, float b) {
	    if (isSet(FLAGS_SETTING_COLOR)) {
		return;
	    }

	    setAngleFromHue(h);
	    setSaturationAndBrightness(s, b);
	}

	/**
	 * Returns true if a particular flag has been set.
	 */
	private boolean isSet(int flag) {
	    return ((flags & flag) == flag);
	}

	/**
	 * Rotates the triangle to accommodate the passed in hue.
	 */
	private void setAngleFromHue(float hue) {
	    setHueAngle((1.0 - hue) * Math.PI * 2);
	}

	/**
	 * Sets the saturation and brightness.
	 */
	private void setSaturationAndBrightness(float s, float b) {
	    int innerR = getTriangleCircumscribedRadius();
	    int triangleSize = innerR * 3 / 2;
	    double x = b * triangleSize;
	    double maxY = x * Math.tan(Math.toRadians(30.0));
	    double y = 2 * maxY * s - maxY;
	    x = x - innerR;
	    double x1 = Math.cos(Math.toRadians(-60.0) - angle) * x - Math.sin(Math.toRadians(-60.0) - angle) * y;
	    double y1 = Math.sin(Math.toRadians(-60.0) - angle) * x + Math.cos(Math.toRadians(-60.0) - angle) * y;
	    int newCircleX = (int) x1 + getWheelXOrigin();
	    int newCircleY = getWheelYOrigin() - (int) y1;

	    setSaturationAndBrightness(s, b, newCircleX, newCircleY);
	}

	/**
	 * Sets the angle representing the hue.
	 */
	private void setHueAngle(double angle) {
	    double oldAngle = this.angle;

	    this.angle = angle;
	    if (angle != oldAngle) {
		setFlag(FLAGS_CHANGED_ANGLE, true);
		repaint();
	    }
	}

	/**
	 * Returns the circumscribed radius of the triangle.
	 */
	private int getTriangleCircumscribedRadius() {
	    return 72;
	}

	/**
	 * Returns the x origin of the wheel and triangle.
	 */
	private int getWheelXOrigin() {
	    return 85;
	}

	/**
	 * Returns y origin of the wheel and triangle.
	 */
	private int getWheelYOrigin() {
	    return 85;
	}

	/**
	 * Sets the saturation and brightness.
	 */
	private void setSaturationAndBrightness(float s, float b, int newCircleX, int newCircleY) {
	    newCircleX -= getIndicatorSize() / 2;
	    newCircleY -= getIndicatorSize() / 2;

	    int minX = Math.min(newCircleX, circleX);
	    int minY = Math.min(newCircleY, circleY);

	    repaint(minX, minY, Math.max(circleX, newCircleX) - minX + getIndicatorSize() + 1,
		    Math.max(circleY, newCircleY) - minY + getIndicatorSize() + 1);
	    circleX = newCircleX;
	    circleY = newCircleY;
	}

	/**
	 * Updates the flags bitmask.
	 */
	private void setFlag(int flag, boolean value) {
	    if (value) {
		flags |= flag;
	    } else {
		flags &= ~flag;
	    }
	}

	/**
	 * Returns the size of the color indicator.
	 */
	private int getIndicatorSize() {
	    return 8;
	}

    }

}

