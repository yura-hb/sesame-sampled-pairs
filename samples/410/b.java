import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.plaf.*;
import sun.swing.SwingUtilities2;

class OceanTheme extends DefaultMetalTheme {
    /**
     * Add this theme's custom entries to the defaults table.
     *
     * @param table the defaults table, non-null
     * @throws NullPointerException if {@code table} is {@code null}
     */
    public void addCustomEntriesToTable(UIDefaults table) {
	UIDefaults.LazyValue focusBorder = t -&gt; new BorderUIResource.LineBorderUIResource(getPrimary1());
	// .30 0 DDE8F3 white secondary2
	java.util.List&lt;?&gt; buttonGradient = Arrays.asList(new Object[] { Float.valueOf(.3f), Float.valueOf(0f),
		new ColorUIResource(0xDDE8F3), getWhite(), getSecondary2() });

	// Other possible properties that aren't defined:
	//
	// Used when generating the disabled Icons, provides the region to
	// constrain grays to.
	// Button.disabledGrayRange -&gt; Object[] of Integers giving min/max
	// InternalFrame.inactiveTitleGradient -&gt; Gradient when the
	//   internal frame is inactive.
	Color cccccc = new ColorUIResource(0xCCCCCC);
	Color dadada = new ColorUIResource(0xDADADA);
	Color c8ddf2 = new ColorUIResource(0xC8DDF2);
	Object directoryIcon = getIconResource("icons/ocean/directory.gif");
	Object fileIcon = getIconResource("icons/ocean/file.gif");
	java.util.List&lt;?&gt; sliderGradient = Arrays.asList(new Object[] { Float.valueOf(.3f), Float.valueOf(.2f), c8ddf2,
		getWhite(), new ColorUIResource(SECONDARY2) });

	Object[] defaults = new Object[] { "Button.gradient", buttonGradient, "Button.rollover", Boolean.TRUE,
		"Button.toolBarBorderBackground", INACTIVE_CONTROL_TEXT_COLOR, "Button.disabledToolBarBorderBackground",
		cccccc, "Button.rolloverIconType", "ocean",

		"CheckBox.rollover", Boolean.TRUE, "CheckBox.gradient", buttonGradient,

		"CheckBoxMenuItem.gradient", buttonGradient,

		// home2
		"FileChooser.homeFolderIcon", getIconResource("icons/ocean/homeFolder.gif"),
		// directory2
		"FileChooser.newFolderIcon", getIconResource("icons/ocean/newFolder.gif"),
		// updir2
		"FileChooser.upFolderIcon", getIconResource("icons/ocean/upFolder.gif"),

		// computer2
		"FileView.computerIcon", getIconResource("icons/ocean/computer.gif"), "FileView.directoryIcon",
		directoryIcon,
		// disk2
		"FileView.hardDriveIcon", getIconResource("icons/ocean/hardDrive.gif"), "FileView.fileIcon", fileIcon,
		// floppy2
		"FileView.floppyDriveIcon", getIconResource("icons/ocean/floppy.gif"),

		"Label.disabledForeground", getInactiveControlTextColor(),

		"Menu.opaque", Boolean.FALSE,

		"MenuBar.gradient",
		Arrays.asList(new Object[] { Float.valueOf(1f), Float.valueOf(0f), getWhite(), dadada,
			new ColorUIResource(dadada) }),
		"MenuBar.borderColor", cccccc,

		"InternalFrame.activeTitleGradient", buttonGradient,
		// close2
		"InternalFrame.closeIcon", new UIDefaults.LazyValue() {
		    public Object createValue(UIDefaults table) {
			return new IFIcon(getHastenedIcon("icons/ocean/close.gif", table),
				getHastenedIcon("icons/ocean/close-pressed.gif", table));
		    }
		},
		// minimize
		"InternalFrame.iconifyIcon", new UIDefaults.LazyValue() {
		    public Object createValue(UIDefaults table) {
			return new IFIcon(getHastenedIcon("icons/ocean/iconify.gif", table),
				getHastenedIcon("icons/ocean/iconify-pressed.gif", table));
		    }
		},
		// restore
		"InternalFrame.minimizeIcon", new UIDefaults.LazyValue() {
		    public Object createValue(UIDefaults table) {
			return new IFIcon(getHastenedIcon("icons/ocean/minimize.gif", table),
				getHastenedIcon("icons/ocean/minimize-pressed.gif", table));
		    }
		},
		// menubutton3
		"InternalFrame.icon", getIconResource("icons/ocean/menu.gif"),
		// maximize2
		"InternalFrame.maximizeIcon", new UIDefaults.LazyValue() {
		    public Object createValue(UIDefaults table) {
			return new IFIcon(getHastenedIcon("icons/ocean/maximize.gif", table),
				getHastenedIcon("icons/ocean/maximize-pressed.gif", table));
		    }
		},
		// paletteclose
		"InternalFrame.paletteCloseIcon", new UIDefaults.LazyValue() {
		    public Object createValue(UIDefaults table) {
			return new IFIcon(getHastenedIcon("icons/ocean/paletteClose.gif", table),
				getHastenedIcon("icons/ocean/paletteClose-pressed.gif", table));
		    }
		},

		"List.focusCellHighlightBorder", focusBorder,

		"MenuBarUI", "javax.swing.plaf.metal.MetalMenuBarUI",

		"OptionPane.errorIcon", getIconResource("icons/ocean/error.png"), "OptionPane.informationIcon",
		getIconResource("icons/ocean/info.png"), "OptionPane.questionIcon",
		getIconResource("icons/ocean/question.png"), "OptionPane.warningIcon",
		getIconResource("icons/ocean/warning.png"),

		"RadioButton.gradient", buttonGradient, "RadioButton.rollover", Boolean.TRUE,

		"RadioButtonMenuItem.gradient", buttonGradient,

		"ScrollBar.gradient", buttonGradient,

		"Slider.altTrackColor", new ColorUIResource(0xD2E2EF), "Slider.gradient", sliderGradient,
		"Slider.focusGradient", sliderGradient,

		"SplitPane.oneTouchButtonsOpaque", Boolean.FALSE, "SplitPane.dividerFocusColor", c8ddf2,

		"TabbedPane.borderHightlightColor", getPrimary1(), "TabbedPane.contentAreaColor", c8ddf2,
		"TabbedPane.contentBorderInsets", new Insets(4, 2, 3, 3), "TabbedPane.selected", c8ddf2,
		"TabbedPane.tabAreaBackground", dadada, "TabbedPane.tabAreaInsets", new Insets(2, 2, 0, 6),
		"TabbedPane.unselectedBackground", SECONDARY3,

		"Table.focusCellHighlightBorder", focusBorder, "Table.gridColor", SECONDARY1,
		"TableHeader.focusCellBackground", c8ddf2,

		"ToggleButton.gradient", buttonGradient,

		"ToolBar.borderColor", cccccc, "ToolBar.isRollover", Boolean.TRUE,

		"Tree.closedIcon", directoryIcon,

		"Tree.collapsedIcon", new UIDefaults.LazyValue() {
		    public Object createValue(UIDefaults table) {
			return new COIcon(getHastenedIcon("icons/ocean/collapsed.gif", table),
				getHastenedIcon("icons/ocean/collapsed-rtl.gif", table));
		    }
		},

		"Tree.expandedIcon", getIconResource("icons/ocean/expanded.gif"), "Tree.leafIcon", fileIcon,
		"Tree.openIcon", directoryIcon, "Tree.selectionBorderColor", getPrimary1(), "Tree.dropLineColor",
		getPrimary1(), "Table.dropLineColor", getPrimary1(), "Table.dropLineShortColor", OCEAN_BLACK,

		"Table.dropCellBackground", OCEAN_DROP, "Tree.dropCellBackground", OCEAN_DROP,
		"List.dropCellBackground", OCEAN_DROP, "List.dropLineColor", getPrimary1() };
	table.putDefaults(defaults);
    }

    private static final ColorUIResource SECONDARY2 = new ColorUIResource(0xB8CFE5);
    private static final ColorUIResource INACTIVE_CONTROL_TEXT_COLOR = new ColorUIResource(0x999999);
    private static final ColorUIResource SECONDARY3 = new ColorUIResource(0xEEEEEE);
    private static final ColorUIResource SECONDARY1 = new ColorUIResource(0x7A8A99);
    private static final ColorUIResource OCEAN_BLACK = new PrintColorUIResource(0x333333, Color.BLACK);
    private static final ColorUIResource OCEAN_DROP = new ColorUIResource(0xD2E9FF);
    private static final ColorUIResource PRIMARY1 = new ColorUIResource(0x6382BF);

    /**
     * Returns the primary 1 color. This returns a color with an rgb hex value
     * of {@code 0x6382BF}.
     *
     * @return the primary 1 color
     * @see java.awt.Color#decode
     */
    protected ColorUIResource getPrimary1() {
	return PRIMARY1;
    }

    /**
     * Returns the secondary 2 color. This returns a color with an rgb hex
     * value of {@code 0xB8CFE5}.
     *
     * @return the secondary 2 color
     * @see java.awt.Color#decode
     */
    protected ColorUIResource getSecondary2() {
	return SECONDARY2;
    }

    private Object getIconResource(String iconID) {
	return SwingUtilities2.makeIcon(getClass(), OceanTheme.class, iconID);
    }

    /**
     * Returns the inactive control text color. This returns a color with an
     * rgb hex value of {@code 0x999999}.
     *
     * @return the inactive control text color
     */
    public ColorUIResource getInactiveControlTextColor() {
	return INACTIVE_CONTROL_TEXT_COLOR;
    }

    private Icon getHastenedIcon(String iconID, UIDefaults table) {
	Object res = getIconResource(iconID);
	return (Icon) ((UIDefaults.LazyValue) res).createValue(table);
    }

    class IFIcon extends IconUIResource {
	private static final ColorUIResource SECONDARY2 = new ColorUIResource(0xB8CFE5);
	private static final ColorUIResource INACTIVE_CONTROL_TEXT_COLOR = new ColorUIResource(0x999999);
	private static final ColorUIResource SECONDARY3 = new ColorUIResource(0xEEEEEE);
	private static final ColorUIResource SECONDARY1 = new ColorUIResource(0x7A8A99);
	private static final ColorUIResource OCEAN_BLACK = new PrintColorUIResource(0x333333, Color.BLACK);
	private static final ColorUIResource OCEAN_DROP = new ColorUIResource(0xD2E9FF);
	private static final ColorUIResource PRIMARY1 = new ColorUIResource(0x6382BF);

	public IFIcon(Icon normal, Icon pressed) {
	    super(normal);
	    this.pressed = pressed;
	}

    }

    class COIcon extends IconUIResource {
	private static final ColorUIResource SECONDARY2 = new ColorUIResource(0xB8CFE5);
	private static final ColorUIResource INACTIVE_CONTROL_TEXT_COLOR = new ColorUIResource(0x999999);
	private static final ColorUIResource SECONDARY3 = new ColorUIResource(0xEEEEEE);
	private static final ColorUIResource SECONDARY1 = new ColorUIResource(0x7A8A99);
	private static final ColorUIResource OCEAN_BLACK = new PrintColorUIResource(0x333333, Color.BLACK);
	private static final ColorUIResource OCEAN_DROP = new ColorUIResource(0xD2E9FF);
	private static final ColorUIResource PRIMARY1 = new ColorUIResource(0x6382BF);

	public COIcon(Icon ltr, Icon rtl) {
	    super(ltr);
	    this.rtl = rtl;
	}

    }

}

