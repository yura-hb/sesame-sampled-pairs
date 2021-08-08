import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.text.Bidi;
import sun.font.BidiUtils;
import sun.font.CoreMetrics;
import sun.font.GraphicComponent;
import sun.font.LayoutPathImpl;
import sun.font.LayoutPathImpl.EmptyPath;
import sun.font.LayoutPathImpl.SegmentPathBuilder;
import sun.font.TextLabelFactory;
import sun.font.TextLineComponent;

class TextLine {
    /**
     * Create a TextLine from the text.  chars is just the text in the iterator.
     */
    public static TextLine standardCreateTextLine(FontRenderContext frc, AttributedCharacterIterator text, char[] chars,
	    float[] baselineOffsets) {

	StyledParagraph styledParagraph = new StyledParagraph(text, chars);
	Bidi bidi = new Bidi(text);
	if (bidi.isLeftToRight()) {
	    bidi = null;
	}
	int layoutFlags = 0; // no extra info yet, bidi determines run and line direction
	TextLabelFactory factory = new TextLabelFactory(frc, chars, bidi, layoutFlags);

	boolean isDirectionLTR = true;
	if (bidi != null) {
	    isDirectionLTR = bidi.baseIsLeftToRight();
	}
	return createLineFromText(chars, styledParagraph, factory, isDirectionLTR, baselineOffsets);
    }

    private FontRenderContext frc;
    private TextLineComponent[] fComponents;
    private float[] fBaselineOffsets;
    private int[] fComponentVisualOrder;
    private char[] fChars;
    private int fCharsStart;
    private int fCharsLimit;
    private int[] fCharLogicalOrder;
    private byte[] fCharLevels;
    private boolean fIsDirectionLTR;
    private boolean isSimple;
    private float[] locs;
    private TextLineMetrics fMetrics = null;
    private LayoutPathImpl lp;

    /**
     * Create a TextLine from the Font and character data over the
     * range.  The range is relative to both the StyledParagraph and the
     * character array.
     */
    public static TextLine createLineFromText(char[] chars, StyledParagraph styledParagraph, TextLabelFactory factory,
	    boolean isDirectionLTR, float[] baselineOffsets) {

	factory.setLineContext(0, chars.length);

	Bidi lineBidi = factory.getLineBidi();
	int[] charsLtoV = null;
	byte[] levels = null;

	if (lineBidi != null) {
	    levels = BidiUtils.getLevels(lineBidi);
	    int[] charsVtoL = BidiUtils.createVisualToLogicalMap(levels);
	    charsLtoV = BidiUtils.createInverseMap(charsVtoL);
	}

	TextLineComponent[] components = getComponents(styledParagraph, chars, 0, chars.length, charsLtoV, levels,
		factory);

	return new TextLine(factory.getFontRenderContext(), components, baselineOffsets, chars, 0, chars.length,
		charsLtoV, levels, isDirectionLTR);
    }

    /**
     * Returns an array (in logical order) of the TextLineComponents representing
     * the text.  The components are both logically and visually contiguous.
     */
    public static TextLineComponent[] getComponents(StyledParagraph styledParagraph, char[] chars, int textStart,
	    int textLimit, int[] charsLtoV, byte[] levels, TextLabelFactory factory) {

	FontRenderContext frc = factory.getFontRenderContext();

	int numComponents = 0;
	TextLineComponent[] tempComponents = new TextLineComponent[1];

	int pos = textStart;
	do {
	    int runLimit = Math.min(styledParagraph.getRunLimit(pos), textLimit);

	    Decoration decorator = styledParagraph.getDecorationAt(pos);

	    Object graphicOrFont = styledParagraph.getFontOrGraphicAt(pos);

	    if (graphicOrFont instanceof GraphicAttribute) {
		// AffineTransform baseRot = styledParagraph.getBaselineRotationAt(pos);
		// !!! For now, let's assign runs of text with both fonts and graphic attributes
		// a null rotation (e.g. the baseline rotation goes away when a graphic
		// is applied.
		AffineTransform baseRot = null;
		GraphicAttribute graphicAttribute = (GraphicAttribute) graphicOrFont;
		do {
		    int chunkLimit = firstVisualChunk(charsLtoV, levels, pos, runLimit);

		    GraphicComponent nextGraphic = new GraphicComponent(graphicAttribute, decorator, charsLtoV, levels,
			    pos, chunkLimit, baseRot);
		    pos = chunkLimit;

		    ++numComponents;
		    if (numComponents &gt;= tempComponents.length) {
			tempComponents = expandArray(tempComponents);
		    }

		    tempComponents[numComponents - 1] = nextGraphic;

		} while (pos &lt; runLimit);
	    } else {
		Font font = (Font) graphicOrFont;

		tempComponents = createComponentsOnRun(pos, runLimit, chars, charsLtoV, levels, factory, font, null,
			frc, decorator, tempComponents, numComponents);
		pos = runLimit;
		numComponents = tempComponents.length;
		while (tempComponents[numComponents - 1] == null) {
		    numComponents -= 1;
		}
	    }

	} while (pos &lt; textLimit);

	TextLineComponent[] components;
	if (tempComponents.length == numComponents) {
	    components = tempComponents;
	} else {
	    components = new TextLineComponent[numComponents];
	    System.arraycopy(tempComponents, 0, components, 0, numComponents);
	}

	return components;
    }

    public TextLine(FontRenderContext frc, TextLineComponent[] components, float[] baselineOffsets, char[] chars,
	    int charsStart, int charsLimit, int[] charLogicalOrder, byte[] charLevels, boolean isDirectionLTR) {

	int[] componentVisualOrder = computeComponentOrder(components, charLogicalOrder);

	this.frc = frc;
	fComponents = components;
	fBaselineOffsets = baselineOffsets;
	fComponentVisualOrder = componentVisualOrder;
	fChars = chars;
	fCharsStart = charsStart;
	fCharsLimit = charsLimit;
	fCharLogicalOrder = charLogicalOrder;
	fCharLevels = charLevels;
	fIsDirectionLTR = isDirectionLTR;
	checkCtorArgs();

	init();
    }

    private static int firstVisualChunk(int order[], byte direction[], int start, int limit) {
	if (order != null && direction != null) {
	    byte dir = direction[start];
	    while (++start &lt; limit && direction[start] == dir) {
	    }
	    return start;
	}
	return limit;
    }

    private static TextLineComponent[] expandArray(TextLineComponent[] orig) {

	TextLineComponent[] newComponents = new TextLineComponent[orig.length + 8];
	System.arraycopy(orig, 0, newComponents, 0, orig.length);

	return newComponents;
    }

    /**
     * Returns an array in logical order of the TextLineComponents on
     * the text in the given range, with the given attributes.
     */
    public static TextLineComponent[] createComponentsOnRun(int runStart, int runLimit, char[] chars, int[] charsLtoV,
	    byte[] levels, TextLabelFactory factory, Font font, CoreMetrics cm, FontRenderContext frc,
	    Decoration decorator, TextLineComponent[] components, int numComponents) {

	int pos = runStart;
	do {
	    int chunkLimit = firstVisualChunk(charsLtoV, levels, pos, runLimit); // &lt;= displayLimit

	    do {
		int startPos = pos;
		int lmCount;

		if (cm == null) {
		    LineMetrics lineMetrics = font.getLineMetrics(chars, startPos, chunkLimit, frc);
		    cm = CoreMetrics.get(lineMetrics);
		    lmCount = lineMetrics.getNumChars();
		} else {
		    lmCount = (chunkLimit - startPos);
		}

		TextLineComponent nextComponent = factory.createExtended(font, cm, decorator, startPos,
			startPos + lmCount);

		++numComponents;
		if (numComponents &gt;= components.length) {
		    components = expandArray(components);
		}

		components[numComponents - 1] = nextComponent;

		pos += lmCount;
	    } while (pos &lt; chunkLimit);

	} while (pos &lt; runLimit);

	return components;
    }

    /**
     * Compute the components order from the given components array and
     * logical-to-visual character mapping.  May return null if canonical.
     */
    private static int[] computeComponentOrder(TextLineComponent[] components, int[] charsLtoV) {

	/*
	 * Create a visual ordering for the glyph sets.  The important thing
	 * here is that the values have the proper rank with respect to
	 * each other, not the exact values.  For example, the first glyph
	 * set that appears visually should have the lowest value.  The last
	 * should have the highest value.  The values are then normalized
	 * to map 1-1 with positions in glyphs.
	 *
	 */
	int[] componentOrder = null;
	if (charsLtoV != null && components.length &gt; 1) {
	    componentOrder = new int[components.length];
	    int gStart = 0;
	    for (int i = 0; i &lt; components.length; i++) {
		componentOrder[i] = charsLtoV[gStart];
		gStart += components[i].getNumCharacters();
	    }

	    componentOrder = BidiUtils.createContiguousOrder(componentOrder);
	    componentOrder = BidiUtils.createInverseMap(componentOrder);
	}
	return componentOrder;
    }

    private void checkCtorArgs() {

	int checkCharCount = 0;
	for (int i = 0; i &lt; fComponents.length; i++) {
	    checkCharCount += fComponents[i].getNumCharacters();
	}

	if (checkCharCount != this.characterCount()) {
	    throw new IllegalArgumentException(
		    "Invalid TextLine!  " + "char count is different from " + "sum of char counts of components.");
	}
    }

    private void init() {

	// first, we need to check for graphic components on the TOP or BOTTOM baselines.  So
	// we perform the work that used to be in getMetrics here.

	float ascent = 0;
	float descent = 0;
	float leading = 0;
	float advance = 0;

	// ascent + descent must not be less than this value
	float maxGraphicHeight = 0;
	float maxGraphicHeightWithLeading = 0;

	// walk through EGA's
	TextLineComponent tlc;
	boolean fitTopAndBottomGraphics = false;

	isSimple = true;

	for (int i = 0; i &lt; fComponents.length; i++) {
	    tlc = fComponents[i];

	    isSimple &= tlc.isSimple();

	    CoreMetrics cm = tlc.getCoreMetrics();

	    byte baseline = (byte) cm.baselineIndex;

	    if (baseline &gt;= 0) {
		float baselineOffset = fBaselineOffsets[baseline];

		ascent = Math.max(ascent, -baselineOffset + cm.ascent);

		float gd = baselineOffset + cm.descent;
		descent = Math.max(descent, gd);

		leading = Math.max(leading, gd + cm.leading);
	    } else {
		fitTopAndBottomGraphics = true;
		float graphicHeight = cm.ascent + cm.descent;
		float graphicHeightWithLeading = graphicHeight + cm.leading;
		maxGraphicHeight = Math.max(maxGraphicHeight, graphicHeight);
		maxGraphicHeightWithLeading = Math.max(maxGraphicHeightWithLeading, graphicHeightWithLeading);
	    }
	}

	if (fitTopAndBottomGraphics) {
	    if (maxGraphicHeight &gt; ascent + descent) {
		descent = maxGraphicHeight - ascent;
	    }
	    if (maxGraphicHeightWithLeading &gt; ascent + leading) {
		leading = maxGraphicHeightWithLeading - ascent;
	    }
	}

	leading -= descent;

	// we now know enough to compute the locs, but we need the final loc
	// for the advance before we can create the metrics object

	if (fitTopAndBottomGraphics) {
	    // we have top or bottom baselines, so expand the baselines array
	    // full offsets are needed by CoreMetrics.effectiveBaselineOffset
	    fBaselineOffsets = new float[] { fBaselineOffsets[0], fBaselineOffsets[1], fBaselineOffsets[2], descent,
		    -ascent };
	}

	float x = 0;
	float y = 0;
	CoreMetrics pcm = null;

	boolean needPath = false;
	locs = new float[fComponents.length * 2 + 2];

	for (int i = 0, n = 0; i &lt; fComponents.length; ++i, n += 2) {
	    tlc = fComponents[getComponentLogicalIndex(i)];
	    CoreMetrics cm = tlc.getCoreMetrics();

	    if ((pcm != null) && (pcm.italicAngle != 0 || cm.italicAngle != 0) && // adjust because of italics
		    (pcm.italicAngle != cm.italicAngle || pcm.baselineIndex != cm.baselineIndex
			    || pcm.ssOffset != cm.ssOffset)) {

		// 1) compute the area of overlap - min effective ascent and min effective descent
		// 2) compute the x positions along italic angle of ascent and descent for left and right
		// 3) compute maximum left - right, adjust right position by this value
		// this is a crude form of kerning between textcomponents

		// note glyphvectors preposition glyphs based on offset,
		// so tl doesn't need to adjust glyphvector position
		// 1)
		float pb = pcm.effectiveBaselineOffset(fBaselineOffsets);
		float pa = pb - pcm.ascent;
		float pd = pb + pcm.descent;
		// pb += pcm.ssOffset;

		float cb = cm.effectiveBaselineOffset(fBaselineOffsets);
		float ca = cb - cm.ascent;
		float cd = cb + cm.descent;
		// cb += cm.ssOffset;

		float a = Math.max(pa, ca);
		float d = Math.min(pd, cd);

		// 2)
		float pax = pcm.italicAngle * (pb - a);
		float pdx = pcm.italicAngle * (pb - d);

		float cax = cm.italicAngle * (cb - a);
		float cdx = cm.italicAngle * (cb - d);

		// 3)
		float dax = pax - cax;
		float ddx = pdx - cdx;
		float dx = Math.max(dax, ddx);

		x += dx;
		y = cb;
	    } else {
		// no italic adjustment for x, but still need to compute y
		y = cm.effectiveBaselineOffset(fBaselineOffsets); // + cm.ssOffset;
	    }

	    locs[n] = x;
	    locs[n + 1] = y;

	    x += tlc.getAdvance();
	    pcm = cm;

	    needPath |= tlc.getBaselineTransform() != null;
	}

	// do we want italic padding at the right of the line?
	if (pcm.italicAngle != 0) {
	    float pb = pcm.effectiveBaselineOffset(fBaselineOffsets);
	    float pa = pb - pcm.ascent;
	    float pd = pb + pcm.descent;
	    pb += pcm.ssOffset;

	    float d;
	    if (pcm.italicAngle &gt; 0) {
		d = pb + pcm.ascent;
	    } else {
		d = pb - pcm.descent;
	    }
	    d *= pcm.italicAngle;

	    x += d;
	}
	locs[locs.length - 2] = x;
	// locs[locs.length - 1] = 0; // final offset is always back on baseline

	// ok, build fMetrics since we have the final advance
	advance = x;
	fMetrics = new TextLineMetrics(ascent, descent, leading, advance);

	// build path if we need it
	if (needPath) {
	    isSimple = false;

	    Point2D.Double pt = new Point2D.Double();
	    double tx = 0, ty = 0;
	    SegmentPathBuilder builder = new SegmentPathBuilder();
	    builder.moveTo(locs[0], 0);
	    for (int i = 0, n = 0; i &lt; fComponents.length; ++i, n += 2) {
		tlc = fComponents[getComponentLogicalIndex(i)];
		AffineTransform at = tlc.getBaselineTransform();
		if (at != null && ((at.getType() & AffineTransform.TYPE_TRANSLATION) != 0)) {
		    double dx = at.getTranslateX();
		    double dy = at.getTranslateY();
		    builder.moveTo(tx += dx, ty += dy);
		}
		pt.x = locs[n + 2] - locs[n];
		pt.y = 0;
		if (at != null) {
		    at.deltaTransform(pt, pt);
		}
		builder.lineTo(tx += pt.x, ty += pt.y);
	    }
	    lp = builder.complete();

	    if (lp == null) { // empty path
		tlc = fComponents[getComponentLogicalIndex(0)];
		AffineTransform at = tlc.getBaselineTransform();
		if (at != null) {
		    lp = new EmptyPath(at);
		}
	    }
	}
    }

    public int characterCount() {

	return fCharsLimit - fCharsStart;
    }

    /**
     * map a component visual index to the logical index.
     */
    private int getComponentLogicalIndex(int vi) {
	if (fComponentVisualOrder == null) {
	    return vi;
	}
	return fComponentVisualOrder[vi];
    }

    class TextLineMetrics {
	private FontRenderContext frc;
	private TextLineComponent[] fComponents;
	private float[] fBaselineOffsets;
	private int[] fComponentVisualOrder;
	private char[] fChars;
	private int fCharsStart;
	private int fCharsLimit;
	private int[] fCharLogicalOrder;
	private byte[] fCharLevels;
	private boolean fIsDirectionLTR;
	private boolean isSimple;
	private float[] locs;
	private TextLineMetrics fMetrics = null;
	private LayoutPathImpl lp;

	public TextLineMetrics(float ascent, float descent, float leading, float advance) {
	    this.ascent = ascent;
	    this.descent = descent;
	    this.leading = leading;
	    this.advance = advance;
	}

    }

}

