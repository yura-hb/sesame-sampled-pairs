import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.Vector;
import javax.swing.JPanel;

class StylePatternFrame extends JPanel implements TextTranslator, PropertyChangeListener {
    /**
     * Creates all controls and adds them to the frame.
     */
    public void init() {
	CardLayout cardLayout = new CardLayout();
	JPanel rightStack = new JPanel(cardLayout);
	String form = "right:max(40dlu;p), 4dlu, 20dlu, 7dlu,right:max(40dlu;p), 4dlu, 80dlu, 7dlu";
	FormLayout rightLayout = new FormLayout(form, "");
	DefaultFormBuilder rightBuilder = new DefaultFormBuilder(rightLayout);
	rightBuilder.setDefaultDialogBorder();
	mControls = getControls();
	for (PropertyControl control : mControls) {
	    control.layout(rightBuilder, this);
	}
	// add the last one, too
	rightStack.add(rightBuilder.getPanel(), "testTab");
	add(rightStack, BorderLayout.CENTER);
    }

    private Vector&lt;PropertyControl&gt; mControls;
    private ThreeCheckBoxProperty mClearSetters;
    private static final String CLEAR_ALL_SETTERS = "clear_all_setters";
    private final StylePatternFrameType mType;
    private StringProperty mName;
    private static final String NODE_NAME = "patternname";
    private ThreeCheckBoxProperty mSetChildPattern;
    private static final String SET_CHILD_PATTERN = SET_RESOURCE;
    private ComboProperty mChildPattern;
    private static final String CHILD_PATTERN = "childpattern";
    private ThreeCheckBoxProperty mSetNodeColor;
    private static final String SET_NODE_COLOR = SET_RESOURCE;
    private final MindMapController mMindMapController;
    private ColorProperty mNodeColor;
    private static final String NODE_COLOR = "nodecolor";
    private ThreeCheckBoxProperty mSetNodeBackgroundColor;
    private static final String SET_NODE_BACKGROUND_COLOR = SET_RESOURCE;
    private ColorProperty mNodeBackgroundColor;
    private static final String NODE_BACKGROUND_COLOR = "nodebackgroundcolor";
    private ThreeCheckBoxProperty mSetNodeStyle;
    private static final String SET_NODE_STYLE = SET_RESOURCE;
    private ComboProperty mNodeStyle;
    private static final String NODE_STYLE = "nodestyle";
    private Vector&lt;MindIcon&gt; mIconInformationVector;
    private ThreeCheckBoxProperty mSetIcon;
    private static final String SET_ICON = SET_RESOURCE;
    private IconProperty mIcon;
    private static final String ICON = "icon";
    private ThreeCheckBoxProperty mSetNodeFontName;
    private static final String SET_NODE_FONT_NAME = SET_RESOURCE;
    private FontProperty mNodeFontName;
    private static final String NODE_FONT_NAME = "nodefontname";
    private ThreeCheckBoxProperty mSetNodeFontSize;
    private static final String SET_NODE_FONT_SIZE = SET_RESOURCE;
    private String[] sizes = new String[] { "2", "4", "6", "8", "10", "12", "14", "16", "18", "20", "22", "24", "30",
	    "36", "48", "72" };
    private ComboProperty mNodeFontSize;
    private static final String NODE_FONT_SIZE = "nodefontsize";
    private ThreeCheckBoxProperty mSetNodeFontBold;
    private static final String SET_NODE_FONT_BOLD = SET_RESOURCE;
    private BooleanProperty mNodeFontBold;
    private static final String NODE_FONT_BOLD = "nodefontbold";
    private ThreeCheckBoxProperty mSetNodeFontStrikethrough;
    private static final String SET_NODE_FONT_STRIKETHROUGH = SET_RESOURCE;
    private BooleanProperty mNodeFontStrikethrough;
    private static final String NODE_FONT_STRIKETHROUGH = "nodefontStrikethrough";
    private ThreeCheckBoxProperty mSetNodeFontItalic;
    private static final String SET_NODE_FONT_ITALIC = SET_RESOURCE;
    private BooleanProperty mNodeFontItalic;
    private static final String NODE_FONT_ITALIC = "nodefontitalic";
    private ThreeCheckBoxProperty mSetNodeText;
    private static final String SET_NODE_TEXT = SET_RESOURCE;
    private StringProperty mNodeText;
    private static final String NODE_TEXT = "nodetext";
    private ThreeCheckBoxProperty mSetEdgeWidth;
    private static final String SET_EDGE_WIDTH = SET_RESOURCE;
    private ComboProperty mEdgeWidth;
    private static final String EDGE_WIDTH = "edgewidth";
    private static final String[] EDGE_WIDTHS = new String[] { "EdgeWidth_parent", "EdgeWidth_thin", "EdgeWidth_1",
	    "EdgeWidth_2", "EdgeWidth_4", "EdgeWidth_8" };
    private ThreeCheckBoxProperty mSetEdgeStyle;
    private static final String SET_EDGE_STYLE = SET_RESOURCE;
    private ComboProperty mEdgeStyle;
    private static final String EDGE_STYLE = "edgestyle";
    private static final String[] EDGE_STYLES = new String[] { EdgeAdapter.EDGESTYLE_LINEAR,
	    EdgeAdapter.EDGESTYLE_BEZIER, EdgeAdapter.EDGESTYLE_SHARP_LINEAR, EdgeAdapter.EDGESTYLE_SHARP_BEZIER };
    private ThreeCheckBoxProperty mSetEdgeColor;
    private static final String SET_EDGE_COLOR = SET_RESOURCE;
    private ColorProperty mEdgeColor;
    private static final String EDGE_COLOR = "edgecolor";
    private ThreeCheckBoxProperty mSetScriptPattern;
    private static final String SET_SCRIPT = "setscript";
    private ScriptEditorProperty mScriptPattern;
    private static final String SCRIPT = "script";
    /**
     * Denotes pairs property -&gt; ThreeCheckBoxProperty such that the boolean
     * property can be set, when the format property is changed.
     */
    private HashMap&lt;ThreeCheckBoxProperty, PropertyControl&gt; mPropertyChangePropagation = new HashMap&lt;&gt;();

    private Vector&lt;PropertyControl&gt; getControls() {
	Vector&lt;PropertyControl&gt; controls = new Vector&lt;&gt;();
	controls.add(new SeparatorProperty("General"));
	mClearSetters = new ThreeCheckBoxProperty(CLEAR_ALL_SETTERS + ".tooltip", CLEAR_ALL_SETTERS);
	mClearSetters.setValue(ThreeCheckBoxProperty.TRUE_VALUE);
	controls.add(mClearSetters);
	if (StylePatternFrameType.WITH_NAME_AND_CHILDS.equals(mType)) {
	    mName = new StringProperty(NODE_NAME + ".tooltip", NODE_NAME);
	    controls.add(mName);
	    // child pattern
	    mSetChildPattern = new ThreeCheckBoxProperty(SET_CHILD_PATTERN + ".tooltip", SET_CHILD_PATTERN);
	    controls.add(mSetChildPattern);
	    Vector&lt;String&gt; childNames = new Vector&lt;&gt;();
	    mChildPattern = new ComboProperty(CHILD_PATTERN + ".tooltip", CHILD_PATTERN, childNames, childNames);
	    controls.add(mChildPattern);
	}
	controls.add(new NextLineProperty());
	controls.add(new SeparatorProperty("NodeColors"));
	mSetNodeColor = new ThreeCheckBoxProperty(SET_NODE_COLOR + ".tooltip", SET_NODE_COLOR);
	controls.add(mSetNodeColor);
	FreeMind fmMain = (FreeMind) mMindMapController.getFrame();
	mNodeColor = new ColorProperty(NODE_COLOR + ".tooltip", NODE_COLOR,
		fmMain.getDefaultProperty(FreeMind.RESOURCES_NODE_TEXT_COLOR), this);
	controls.add(mNodeColor);
	mSetNodeBackgroundColor = new ThreeCheckBoxProperty(SET_NODE_BACKGROUND_COLOR + ".tooltip",
		SET_NODE_BACKGROUND_COLOR);
	controls.add(mSetNodeBackgroundColor);
	mNodeBackgroundColor = new ColorProperty(NODE_BACKGROUND_COLOR + ".tooltip", NODE_BACKGROUND_COLOR,
		fmMain.getDefaultProperty(FreeMind.RESOURCES_BACKGROUND_COLOR), this);
	controls.add(mNodeBackgroundColor);
	controls.add(new SeparatorProperty("NodeStyles"));
	mSetNodeStyle = new ThreeCheckBoxProperty(SET_NODE_STYLE + ".tooltip", SET_NODE_STYLE);
	controls.add(mSetNodeStyle);
	mNodeStyle = new ComboProperty(NODE_STYLE + ".tooltip", NODE_STYLE, MindMapNode.NODE_STYLES, this);
	controls.add(mNodeStyle);
	mIconInformationVector = new Vector&lt;&gt;();
	MindMapController controller = mMindMapController;
	Vector&lt;IconAction&gt; iconActions = controller.iconActions;
	for (IconAction action : iconActions) {
	    MindIcon info = action.getMindIcon();
	    mIconInformationVector.add(info);
	}
	mSetIcon = new ThreeCheckBoxProperty(SET_ICON + ".tooltip", SET_ICON);
	controls.add(mSetIcon);
	mIcon = new IconProperty(ICON + ".tooltip", ICON, mMindMapController.getFrame(), mIconInformationVector);
	controls.add(mIcon);
	controls.add(new NextLineProperty());
	controls.add(new SeparatorProperty("NodeFont"));
	mSetNodeFontName = new ThreeCheckBoxProperty(SET_NODE_FONT_NAME + ".tooltip", SET_NODE_FONT_NAME);
	controls.add(mSetNodeFontName);
	mNodeFontName = new FontProperty(NODE_FONT_NAME + ".tooltip", NODE_FONT_NAME, this);
	controls.add(mNodeFontName);
	mSetNodeFontSize = new ThreeCheckBoxProperty(SET_NODE_FONT_SIZE + ".tooltip", SET_NODE_FONT_SIZE);
	controls.add(mSetNodeFontSize);
	Vector&lt;String&gt; sizesVector = new Vector&lt;&gt;();
	for (int i = 0; i &lt; sizes.length; i++) {
	    sizesVector.add(sizes[i]);
	}
	mNodeFontSize = new IntegerComboProperty(NODE_FONT_SIZE + ".tooltip", NODE_FONT_SIZE, sizes, sizesVector);
	controls.add(mNodeFontSize);
	mSetNodeFontBold = new ThreeCheckBoxProperty(SET_NODE_FONT_BOLD + ".tooltip", SET_NODE_FONT_BOLD);
	controls.add(mSetNodeFontBold);
	mNodeFontBold = new BooleanProperty(NODE_FONT_BOLD + ".tooltip", NODE_FONT_BOLD);
	controls.add(mNodeFontBold);
	mSetNodeFontStrikethrough = new ThreeCheckBoxProperty(SET_NODE_FONT_STRIKETHROUGH + ".tooltip",
		SET_NODE_FONT_STRIKETHROUGH);
	controls.add(mSetNodeFontStrikethrough);
	mNodeFontStrikethrough = new BooleanProperty(NODE_FONT_STRIKETHROUGH + ".tooltip", NODE_FONT_STRIKETHROUGH);
	controls.add(mNodeFontStrikethrough);
	mSetNodeFontItalic = new ThreeCheckBoxProperty(SET_NODE_FONT_ITALIC + ".tooltip", SET_NODE_FONT_ITALIC);
	controls.add(mSetNodeFontItalic);
	mNodeFontItalic = new BooleanProperty(NODE_FONT_ITALIC + ".tooltip", NODE_FONT_ITALIC);
	controls.add(mNodeFontItalic);
	/* **** */
	mSetNodeText = new ThreeCheckBoxProperty(SET_NODE_TEXT + ".tooltip", SET_NODE_TEXT);
	controls.add(mSetNodeText);
	mNodeText = new StringProperty(NODE_TEXT + ".tooltip", NODE_TEXT);
	controls.add(mNodeText);
	/* **** */
	controls.add(new SeparatorProperty("EdgeControls"));
	mSetEdgeWidth = new ThreeCheckBoxProperty(SET_EDGE_WIDTH + ".tooltip", SET_EDGE_WIDTH);
	controls.add(mSetEdgeWidth);
	mEdgeWidth = new ComboProperty(EDGE_WIDTH + ".tooltip", EDGE_WIDTH, EDGE_WIDTHS, this);
	controls.add(mEdgeWidth);
	/* **** */
	mSetEdgeStyle = new ThreeCheckBoxProperty(SET_EDGE_STYLE + ".tooltip", SET_EDGE_STYLE);
	controls.add(mSetEdgeStyle);
	mEdgeStyle = new ComboProperty(EDGE_STYLE + ".tooltip", EDGE_STYLE, EDGE_STYLES, this);
	controls.add(mEdgeStyle);
	/* **** */
	mSetEdgeColor = new ThreeCheckBoxProperty(SET_EDGE_COLOR + ".tooltip", SET_EDGE_COLOR);
	controls.add(mSetEdgeColor);
	mEdgeColor = new ColorProperty(EDGE_COLOR + ".tooltip", EDGE_COLOR,
		fmMain.getDefaultProperty(FreeMind.RESOURCES_EDGE_COLOR), this);
	controls.add(mEdgeColor);
	/* **** */
	controls.add(new SeparatorProperty("ScriptingControl"));
	mSetScriptPattern = new ThreeCheckBoxProperty(SET_SCRIPT + ".tooltip", SET_SCRIPT);
	controls.add(mSetScriptPattern);
	mScriptPattern = new ScriptEditorProperty(SCRIPT + ".tooltip", SCRIPT, mMindMapController);
	controls.add(mScriptPattern);
	// fill map;
	mPropertyChangePropagation.put(mSetNodeColor, mNodeColor);
	mPropertyChangePropagation.put(mSetNodeBackgroundColor, mNodeBackgroundColor);
	mPropertyChangePropagation.put(mSetNodeStyle, mNodeStyle);
	mPropertyChangePropagation.put(mSetNodeFontName, mNodeFontName);
	mPropertyChangePropagation.put(mSetNodeFontSize, mNodeFontSize);
	mPropertyChangePropagation.put(mSetNodeFontBold, mNodeFontBold);
	mPropertyChangePropagation.put(mSetNodeFontStrikethrough, mNodeFontStrikethrough);
	mPropertyChangePropagation.put(mSetNodeFontItalic, mNodeFontItalic);
	mPropertyChangePropagation.put(mSetNodeText, mNodeText);
	mPropertyChangePropagation.put(mSetEdgeColor, mEdgeColor);
	mPropertyChangePropagation.put(mSetEdgeStyle, mEdgeStyle);
	mPropertyChangePropagation.put(mSetEdgeWidth, mEdgeWidth);
	mPropertyChangePropagation.put(mSetIcon, mIcon);
	mPropertyChangePropagation.put(mSetScriptPattern, mScriptPattern);
	if (StylePatternFrameType.WITH_NAME_AND_CHILDS.equals(mType)) {
	    // child pattern
	    mPropertyChangePropagation.put(mSetChildPattern, mChildPattern);
	}
	return controls;
    }

}

