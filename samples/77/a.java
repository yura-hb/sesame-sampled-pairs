import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.lang3.Validate;

class ExtendedMessageFormat extends MessageFormat {
    /**
     * Apply the specified pattern.
     *
     * @param pattern String
     */
    @Override
    public final void applyPattern(final String pattern) {
	if (registry == null) {
	    super.applyPattern(pattern);
	    toPattern = super.toPattern();
	    return;
	}
	final ArrayList&lt;Format&gt; foundFormats = new ArrayList&lt;&gt;();
	final ArrayList&lt;String&gt; foundDescriptions = new ArrayList&lt;&gt;();
	final StringBuilder stripCustom = new StringBuilder(pattern.length());

	final ParsePosition pos = new ParsePosition(0);
	final char[] c = pattern.toCharArray();
	int fmtCount = 0;
	while (pos.getIndex() &lt; pattern.length()) {
	    switch (c[pos.getIndex()]) {
	    case QUOTE:
		appendQuotedString(pattern, pos, stripCustom);
		break;
	    case START_FE:
		fmtCount++;
		seekNonWs(pattern, pos);
		final int start = pos.getIndex();
		final int index = readArgumentIndex(pattern, next(pos));
		stripCustom.append(START_FE).append(index);
		seekNonWs(pattern, pos);
		Format format = null;
		String formatDescription = null;
		if (c[pos.getIndex()] == START_FMT) {
		    formatDescription = parseFormatDescription(pattern, next(pos));
		    format = getFormat(formatDescription);
		    if (format == null) {
			stripCustom.append(START_FMT).append(formatDescription);
		    }
		}
		foundFormats.add(format);
		foundDescriptions.add(format == null ? null : formatDescription);
		Validate.isTrue(foundFormats.size() == fmtCount);
		Validate.isTrue(foundDescriptions.size() == fmtCount);
		if (c[pos.getIndex()] != END_FE) {
		    throw new IllegalArgumentException("Unreadable format element at position " + start);
		}
		//$FALL-THROUGH$
	    default:
		stripCustom.append(c[pos.getIndex()]);
		next(pos);
	    }
	}
	super.applyPattern(stripCustom.toString());
	toPattern = insertFormats(super.toPattern(), foundDescriptions);
	if (containsElements(foundFormats)) {
	    final Format[] origFormats = getFormats();
	    // only loop over what we know we have, as MessageFormat on Java 1.3
	    // seems to provide an extra format element:
	    int i = 0;
	    for (final Iterator&lt;Format&gt; it = foundFormats.iterator(); it.hasNext(); i++) {
		final Format f = it.next();
		if (f != null) {
		    origFormats[i] = f;
		}
	    }
	    super.setFormats(origFormats);
	}
    }

    private final Map&lt;String, ? extends FormatFactory&gt; registry;
    private String toPattern;
    private static final char QUOTE = '\'';
    private static final char START_FE = '{';
    private static final char START_FMT = ',';
    private static final char END_FE = '}';

    /**
     * Consume a quoted string, adding it to &lt;code&gt;appendTo&lt;/code&gt; if
     * specified.
     *
     * @param pattern pattern to parse
     * @param pos current parse position
     * @param appendTo optional StringBuilder to append
     * @return &lt;code&gt;appendTo&lt;/code&gt;
     */
    private StringBuilder appendQuotedString(final String pattern, final ParsePosition pos,
	    final StringBuilder appendTo) {
	assert pattern.toCharArray()[pos.getIndex()] == QUOTE : "Quoted string must start with quote character";

	// handle quote character at the beginning of the string
	if (appendTo != null) {
	    appendTo.append(QUOTE);
	}
	next(pos);

	final int start = pos.getIndex();
	final char[] c = pattern.toCharArray();
	final int lastHold = start;
	for (int i = pos.getIndex(); i &lt; pattern.length(); i++) {
	    switch (c[pos.getIndex()]) {
	    case QUOTE:
		next(pos);
		return appendTo == null ? null : appendTo.append(c, lastHold, pos.getIndex() - lastHold);
	    default:
		next(pos);
	    }
	}
	throw new IllegalArgumentException("Unterminated quoted string at position " + start);
    }

    /**
     * Consume whitespace from the current parse position.
     *
     * @param pattern String to read
     * @param pos current position
     */
    private void seekNonWs(final String pattern, final ParsePosition pos) {
	int len = 0;
	final char[] buffer = pattern.toCharArray();
	do {
	    len = StrMatcher.splitMatcher().isMatch(buffer, pos.getIndex());
	    pos.setIndex(pos.getIndex() + len);
	} while (len &gt; 0 && pos.getIndex() &lt; pattern.length());
    }

    /**
     * Convenience method to advance parse position by 1
     *
     * @param pos ParsePosition
     * @return &lt;code&gt;pos&lt;/code&gt;
     */
    private ParsePosition next(final ParsePosition pos) {
	pos.setIndex(pos.getIndex() + 1);
	return pos;
    }

    /**
     * Read the argument index from the current format element
     *
     * @param pattern pattern to parse
     * @param pos current parse position
     * @return argument index
     */
    private int readArgumentIndex(final String pattern, final ParsePosition pos) {
	final int start = pos.getIndex();
	seekNonWs(pattern, pos);
	final StringBuilder result = new StringBuilder();
	boolean error = false;
	for (; !error && pos.getIndex() &lt; pattern.length(); next(pos)) {
	    char c = pattern.charAt(pos.getIndex());
	    if (Character.isWhitespace(c)) {
		seekNonWs(pattern, pos);
		c = pattern.charAt(pos.getIndex());
		if (c != START_FMT && c != END_FE) {
		    error = true;
		    continue;
		}
	    }
	    if ((c == START_FMT || c == END_FE) && result.length() &gt; 0) {
		try {
		    return Integer.parseInt(result.toString());
		} catch (final NumberFormatException e) { // NOPMD
		    // we've already ensured only digits, so unless something
		    // outlandishly large was specified we should be okay.
		}
	    }
	    error = !Character.isDigit(c);
	    result.append(c);
	}
	if (error) {
	    throw new IllegalArgumentException("Invalid format argument index at position " + start + ": "
		    + pattern.substring(start, pos.getIndex()));
	}
	throw new IllegalArgumentException("Unterminated format element at position " + start);
    }

    /**
     * Parse the format component of a format element.
     *
     * @param pattern string to parse
     * @param pos current parse position
     * @return Format description String
     */
    private String parseFormatDescription(final String pattern, final ParsePosition pos) {
	final int start = pos.getIndex();
	seekNonWs(pattern, pos);
	final int text = pos.getIndex();
	int depth = 1;
	for (; pos.getIndex() &lt; pattern.length(); next(pos)) {
	    switch (pattern.charAt(pos.getIndex())) {
	    case START_FE:
		depth++;
		break;
	    case END_FE:
		depth--;
		if (depth == 0) {
		    return pattern.substring(text, pos.getIndex());
		}
		break;
	    case QUOTE:
		getQuotedString(pattern, pos);
		break;
	    default:
		break;
	    }
	}
	throw new IllegalArgumentException("Unterminated format element at position " + start);
    }

    /**
     * Get a custom format from a format description.
     *
     * @param desc String
     * @return Format
     */
    private Format getFormat(final String desc) {
	if (registry != null) {
	    String name = desc;
	    String args = null;
	    final int i = desc.indexOf(START_FMT);
	    if (i &gt; 0) {
		name = desc.substring(0, i).trim();
		args = desc.substring(i + 1).trim();
	    }
	    final FormatFactory factory = registry.get(name);
	    if (factory != null) {
		return factory.getFormat(name, args, getLocale());
	    }
	}
	return null;
    }

    /**
     * Insert formats back into the pattern for toPattern() support.
     *
     * @param pattern source
     * @param customPatterns The custom patterns to re-insert, if any
     * @return full pattern
     */
    private String insertFormats(final String pattern, final ArrayList&lt;String&gt; customPatterns) {
	if (!containsElements(customPatterns)) {
	    return pattern;
	}
	final StringBuilder sb = new StringBuilder(pattern.length() * 2);
	final ParsePosition pos = new ParsePosition(0);
	int fe = -1;
	int depth = 0;
	while (pos.getIndex() &lt; pattern.length()) {
	    final char c = pattern.charAt(pos.getIndex());
	    switch (c) {
	    case QUOTE:
		appendQuotedString(pattern, pos, sb);
		break;
	    case START_FE:
		depth++;
		sb.append(START_FE).append(readArgumentIndex(pattern, next(pos)));
		// do not look for custom patterns when they are embedded, e.g. in a choice
		if (depth == 1) {
		    fe++;
		    final String customPattern = customPatterns.get(fe);
		    if (customPattern != null) {
			sb.append(START_FMT).append(customPattern);
		    }
		}
		break;
	    case END_FE:
		depth--;
		//$FALL-THROUGH$
	    default:
		sb.append(c);
		next(pos);
	    }
	}
	return sb.toString();
    }

    /**
     * Learn whether the specified Collection contains non-null elements.
     * @param coll to check
     * @return &lt;code&gt;true&lt;/code&gt; if some Object was found, &lt;code&gt;false&lt;/code&gt; otherwise.
     */
    private boolean containsElements(final Collection&lt;?&gt; coll) {
	if (coll == null || coll.isEmpty()) {
	    return false;
	}
	for (final Object name : coll) {
	    if (name != null) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Consume quoted string only
     *
     * @param pattern pattern to parse
     * @param pos current parse position
     */
    private void getQuotedString(final String pattern, final ParsePosition pos) {
	appendQuotedString(pattern, pos, null);
    }

}

