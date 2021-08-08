import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import org.apache.commons.lang3.StringUtils;

class StrTokenizer implements ListIterator&lt;String&gt;, Cloneable {
    /**
     * Gets the next token.
     *
     * @return the next String token
     * @throws NoSuchElementException if there are no more elements
     */
    @Override
    public String next() {
	if (hasNext()) {
	    return tokens[tokenPos++];
	}
	throw new NoSuchElementException();
    }

    /** The parsed tokens */
    private String tokens[];
    /** The current iteration position */
    private int tokenPos;
    /** The text to work on. */
    private char chars[];
    /** The ignored matcher */
    private StrMatcher ignoredMatcher = StrMatcher.noneMatcher();
    /** The trimmer matcher */
    private StrMatcher trimmerMatcher = StrMatcher.noneMatcher();
    /** The delimiter matcher */
    private StrMatcher delimMatcher = StrMatcher.splitMatcher();
    /** The quote matcher */
    private StrMatcher quoteMatcher = StrMatcher.noneMatcher();
    /** Whether to ignore empty tokens */
    private boolean ignoreEmptyTokens = true;
    /** Whether to return empty tokens as null */
    private boolean emptyAsNull = false;

    /**
     * Checks whether there are any more tokens.
     *
     * @return true if there are more tokens
     */
    @Override
    public boolean hasNext() {
	checkTokenized();
	return tokenPos &lt; tokens.length;
    }

    /**
     * Checks if tokenization has been done, and if not then do it.
     */
    private void checkTokenized() {
	if (tokens == null) {
	    if (chars == null) {
		// still call tokenize as subclass may do some work
		final List&lt;String&gt; split = tokenize(null, 0, 0);
		tokens = split.toArray(new String[split.size()]);
	    } else {
		final List&lt;String&gt; split = tokenize(chars, 0, chars.length);
		tokens = split.toArray(new String[split.size()]);
	    }
	}
    }

    /**
     * Internal method to performs the tokenization.
     * &lt;p&gt;
     * Most users of this class do not need to call this method. This method
     * will be called automatically by other (public) methods when required.
     * &lt;p&gt;
     * This method exists to allow subclasses to add code before or after the
     * tokenization. For example, a subclass could alter the character array,
     * offset or count to be parsed, or call the tokenizer multiple times on
     * multiple strings. It is also be possible to filter the results.
     * &lt;p&gt;
     * &lt;code&gt;StrTokenizer&lt;/code&gt; will always pass a zero offset and a count
     * equal to the length of the array to this method, however a subclass
     * may pass other values, or even an entirely different array.
     *
     * @param srcChars  the character array being tokenized, may be null
     * @param offset  the start position within the character array, must be valid
     * @param count  the number of characters to tokenize, must be valid
     * @return the modifiable list of String tokens, unmodifiable if null array or zero count
     */
    protected List&lt;String&gt; tokenize(final char[] srcChars, final int offset, final int count) {
	if (srcChars == null || count == 0) {
	    return Collections.emptyList();
	}
	final StrBuilder buf = new StrBuilder();
	final List&lt;String&gt; tokenList = new ArrayList&lt;&gt;();
	int pos = offset;

	// loop around the entire buffer
	while (pos &gt;= 0 && pos &lt; count) {
	    // find next token
	    pos = readNextToken(srcChars, pos, count, buf, tokenList);

	    // handle case where end of string is a delimiter
	    if (pos &gt;= count) {
		addToken(tokenList, StringUtils.EMPTY);
	    }
	}
	return tokenList;
    }

    /**
     * Reads character by character through the String to get the next token.
     *
     * @param srcChars  the character array being tokenized
     * @param start  the first character of field
     * @param len  the length of the character array being tokenized
     * @param workArea  a temporary work area
     * @param tokenList  the list of parsed tokens
     * @return the starting position of the next field (the character
     *  immediately after the delimiter), or -1 if end of string found
     */
    private int readNextToken(final char[] srcChars, int start, final int len, final StrBuilder workArea,
	    final List&lt;String&gt; tokenList) {
	// skip all leading whitespace, unless it is the
	// field delimiter or the quote character
	while (start &lt; len) {
	    final int removeLen = Math.max(getIgnoredMatcher().isMatch(srcChars, start, start, len),
		    getTrimmerMatcher().isMatch(srcChars, start, start, len));
	    if (removeLen == 0 || getDelimiterMatcher().isMatch(srcChars, start, start, len) &gt; 0
		    || getQuoteMatcher().isMatch(srcChars, start, start, len) &gt; 0) {
		break;
	    }
	    start += removeLen;
	}

	// handle reaching end
	if (start &gt;= len) {
	    addToken(tokenList, StringUtils.EMPTY);
	    return -1;
	}

	// handle empty token
	final int delimLen = getDelimiterMatcher().isMatch(srcChars, start, start, len);
	if (delimLen &gt; 0) {
	    addToken(tokenList, StringUtils.EMPTY);
	    return start + delimLen;
	}

	// handle found token
	final int quoteLen = getQuoteMatcher().isMatch(srcChars, start, start, len);
	if (quoteLen &gt; 0) {
	    return readWithQuotes(srcChars, start + quoteLen, len, workArea, tokenList, start, quoteLen);
	}
	return readWithQuotes(srcChars, start, len, workArea, tokenList, 0, 0);
    }

    /**
     * Adds a token to a list, paying attention to the parameters we've set.
     *
     * @param list  the list to add to
     * @param tok  the token to add
     */
    private void addToken(final List&lt;String&gt; list, String tok) {
	if (StringUtils.isEmpty(tok)) {
	    if (isIgnoreEmptyTokens()) {
		return;
	    }
	    if (isEmptyTokenAsNull()) {
		tok = null;
	    }
	}
	list.add(tok);
    }

    /**
     * Gets the ignored character matcher.
     * &lt;p&gt;
     * These characters are ignored when parsing the String, unless they are
     * within a quoted region.
     * The default value is not to ignore anything.
     *
     * @return the ignored matcher in use
     */
    public StrMatcher getIgnoredMatcher() {
	return ignoredMatcher;
    }

    /**
     * Gets the trimmer character matcher.
     * &lt;p&gt;
     * These characters are trimmed off on each side of the delimiter
     * until the token or quote is found.
     * The default value is not to trim anything.
     *
     * @return the trimmer matcher in use
     */
    public StrMatcher getTrimmerMatcher() {
	return trimmerMatcher;
    }

    /**
     * Gets the field delimiter matcher.
     *
     * @return the delimiter matcher in use
     */
    public StrMatcher getDelimiterMatcher() {
	return this.delimMatcher;
    }

    /**
     * Gets the quote matcher currently in use.
     * &lt;p&gt;
     * The quote character is used to wrap data between the tokens.
     * This enables delimiters to be entered as data.
     * The default value is '"' (double quote).
     *
     * @return the quote matcher in use
     */
    public StrMatcher getQuoteMatcher() {
	return quoteMatcher;
    }

    /**
     * Reads a possibly quoted string token.
     *
     * @param srcChars  the character array being tokenized
     * @param start  the first character of field
     * @param len  the length of the character array being tokenized
     * @param workArea  a temporary work area
     * @param tokenList  the list of parsed tokens
     * @param quoteStart  the start position of the matched quote, 0 if no quoting
     * @param quoteLen  the length of the matched quote, 0 if no quoting
     * @return the starting position of the next field (the character
     *  immediately after the delimiter, or if end of string found,
     *  then the length of string
     */
    private int readWithQuotes(final char[] srcChars, final int start, final int len, final StrBuilder workArea,
	    final List&lt;String&gt; tokenList, final int quoteStart, final int quoteLen) {
	// Loop until we've found the end of the quoted
	// string or the end of the input
	workArea.clear();
	int pos = start;
	boolean quoting = quoteLen &gt; 0;
	int trimStart = 0;

	while (pos &lt; len) {
	    // quoting mode can occur several times throughout a string
	    // we must switch between quoting and non-quoting until we
	    // encounter a non-quoted delimiter, or end of string
	    if (quoting) {
		// In quoting mode

		// If we've found a quote character, see if it's
		// followed by a second quote.  If so, then we need
		// to actually put the quote character into the token
		// rather than end the token.
		if (isQuote(srcChars, pos, len, quoteStart, quoteLen)) {
		    if (isQuote(srcChars, pos + quoteLen, len, quoteStart, quoteLen)) {
			// matched pair of quotes, thus an escaped quote
			workArea.append(srcChars, pos, quoteLen);
			pos += quoteLen * 2;
			trimStart = workArea.size();
			continue;
		    }

		    // end of quoting
		    quoting = false;
		    pos += quoteLen;
		    continue;
		}

		// copy regular character from inside quotes
		workArea.append(srcChars[pos++]);
		trimStart = workArea.size();

	    } else {
		// Not in quoting mode

		// check for delimiter, and thus end of token
		final int delimLen = getDelimiterMatcher().isMatch(srcChars, pos, start, len);
		if (delimLen &gt; 0) {
		    // return condition when end of token found
		    addToken(tokenList, workArea.substring(0, trimStart));
		    return pos + delimLen;
		}

		// check for quote, and thus back into quoting mode
		if (quoteLen &gt; 0 && isQuote(srcChars, pos, len, quoteStart, quoteLen)) {
		    quoting = true;
		    pos += quoteLen;
		    continue;
		}

		// check for ignored (outside quotes), and ignore
		final int ignoredLen = getIgnoredMatcher().isMatch(srcChars, pos, start, len);
		if (ignoredLen &gt; 0) {
		    pos += ignoredLen;
		    continue;
		}

		// check for trimmed character
		// don't yet know if its at the end, so copy to workArea
		// use trimStart to keep track of trim at the end
		final int trimmedLen = getTrimmerMatcher().isMatch(srcChars, pos, start, len);
		if (trimmedLen &gt; 0) {
		    workArea.append(srcChars, pos, trimmedLen);
		    pos += trimmedLen;
		    continue;
		}

		// copy regular character from outside quotes
		workArea.append(srcChars[pos++]);
		trimStart = workArea.size();
	    }
	}

	// return condition when end of string found
	addToken(tokenList, workArea.substring(0, trimStart));
	return -1;
    }

    /**
     * Gets whether the tokenizer currently ignores empty tokens.
     * The default for this property is true.
     *
     * @return true if empty tokens are not returned
     */
    public boolean isIgnoreEmptyTokens() {
	return ignoreEmptyTokens;
    }

    /**
     * Gets whether the tokenizer currently returns empty tokens as null.
     * The default for this property is false.
     *
     * @return true if empty tokens are returned as null
     */
    public boolean isEmptyTokenAsNull() {
	return this.emptyAsNull;
    }

    /**
     * Checks if the characters at the index specified match the quote
     * already matched in readNextToken().
     *
     * @param srcChars  the character array being tokenized
     * @param pos  the position to check for a quote
     * @param len  the length of the character array being tokenized
     * @param quoteStart  the start position of the matched quote, 0 if no quoting
     * @param quoteLen  the length of the matched quote, 0 if no quoting
     * @return true if a quote is matched
     */
    private boolean isQuote(final char[] srcChars, final int pos, final int len, final int quoteStart,
	    final int quoteLen) {
	for (int i = 0; i &lt; quoteLen; i++) {
	    if (pos + i &gt;= len || srcChars[pos + i] != srcChars[quoteStart + i]) {
		return false;
	    }
	}
	return true;
    }

}

