import java.awt.Font;

class MetalFontDesktopProperty extends DesktopProperty {
    /**
     * Returns the default font.
     */
    protected Object getDefaultValue() {
	return new Font(DefaultMetalTheme.getDefaultFontName(type), DefaultMetalTheme.getDefaultFontStyle(type),
		DefaultMetalTheme.getDefaultFontSize(type));
    }

    /**
     * Corresponds to a MetalTheme font type.
     */
    private int type;

}

