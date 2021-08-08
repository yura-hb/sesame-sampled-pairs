import org.deeplearning4j.ui.api.Utils;

class StyleTable extends Style {
    class Builder extends Builder&lt;Builder&gt; {
	/**
	 * @param color    Background color for the header row
	 */
	public Builder headerColor(Color color) {
	    String hex = Utils.colorToHex(color);
	    return headerColor(hex);
	}

	private String headerColor;

	/**
	 * @param color    Background color for the header row
	 */
	public Builder headerColor(String color) {
	    if (!color.toLowerCase().matches("#[a-f0-9]{6}"))
		throw new IllegalArgumentException("Invalid color: must be hex format. Got: " + color);
	    this.headerColor = color;
	    return this;
	}

    }

}

