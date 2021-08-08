import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.ResourceBundle;
import jdk.internal.jline.Terminal;
import jdk.internal.jline.Terminal2;
import jdk.internal.jline.internal.Ansi;
import jdk.internal.jline.internal.Curses;
import jdk.internal.jline.internal.Log;
import jdk.internal.jline.internal.NonBlockingInputStream;

class ConsoleReader implements Closeable {
    /**
     * Output the specified {@link Collection} in proper columns.
     */
    public void printColumns(final Collection&lt;? extends CharSequence&gt; items) throws IOException {
	if (items == null || items.isEmpty()) {
	    return;
	}

	int width = getTerminal().getWidth();
	int height = getTerminal().getHeight();

	int maxWidth = 0;
	for (CharSequence item : items) {
	    // we use 0 here, as we don't really support tabulations inside candidates
	    int len = wcwidth(Ansi.stripAnsi(item.toString()), 0);
	    maxWidth = Math.max(maxWidth, len);
	}
	maxWidth = maxWidth + 3;
	Log.debug("Max width: ", maxWidth);

	int showLines;
	if (isPaginationEnabled()) {
	    showLines = height - 1; // page limit
	} else {
	    showLines = Integer.MAX_VALUE;
	}

	StringBuilder buff = new StringBuilder();
	int realLength = 0;
	for (CharSequence item : items) {
	    if ((realLength + maxWidth) &gt; width) {
		rawPrintln(buff.toString());
		buff.setLength(0);
		realLength = 0;

		if (--showLines == 0) {
		    // Overflow
		    print(resources.getString("DISPLAY_MORE"));
		    flush();
		    int c = readCharacter();
		    if (c == '\r' || c == '\n') {
			// one step forward
			showLines = 1;
		    } else if (c != 'q') {
			// page forward
			showLines = height - 1;
		    }

		    tputs("carriage_return");
		    if (c == 'q') {
			// cancel
			break;
		    }
		}
	    }

	    // NOTE: toString() is important here due to AnsiString being retarded
	    buff.append(item.toString());
	    int strippedItemLength = wcwidth(Ansi.stripAnsi(item.toString()), 0);
	    for (int i = 0; i &lt; (maxWidth - strippedItemLength); i++) {
		buff.append(' ');
	    }
	    realLength += maxWidth;
	}

	if (buff.length() &gt; 0) {
	    rawPrintln(buff.toString());
	}
    }

    private static final ResourceBundle resources = ResourceBundle
	    .getBundle(CandidateListCompletionHandler.class.getName());
    private final Terminal2 terminal;
    private boolean paginationEnabled;
    private final Writer out;
    private boolean cursorOk;
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private Reader reader;
    private static final int ESCAPE = 27;
    private NonBlockingInputStream in;
    private long escapeTimeout;
    public static final int TAB_WIDTH = 8;
    private int promptLen;
    private final CursorBuffer buf = new CursorBuffer();
    private Character mask;
    public static final char NULL_MASK = 0;

    public Terminal getTerminal() {
	return terminal;
    }

    int wcwidth(CharSequence str, int pos) {
	return wcwidth(str, 0, str.length(), pos);
    }

    /**
     * Whether to use pagination when the number of rows of candidates exceeds the height of the terminal.
     */
    public boolean isPaginationEnabled() {
	return paginationEnabled;
    }

    private void rawPrintln(final String s) throws IOException {
	rawPrint(s);
	println();
    }

    /**
     * Output the specified string to the output stream (but not the buffer).
     */
    public void print(final CharSequence s) throws IOException {
	rawPrint(s.toString());
    }

    /**
     * Flush the console output stream. This is important for printout out single characters (like a backspace or
     * keyboard) that we want the console to handle immediately.
     */
    public void flush() throws IOException {
	out.flush();
    }

    /**
     * Read a character from the console.
     *
     * @return the character, or -1 if an EOF is received.
     */
    public int readCharacter() throws IOException {
	return readCharacter(false);
    }

    private boolean tputs(String cap, Object... params) throws IOException {
	String str = terminal.getStringCapability(cap);
	if (str == null) {
	    return false;
	}
	Curses.tputs(out, str, params);
	return true;
    }

    int wcwidth(CharSequence str, int start, int end, int pos) {
	int cur = pos;
	for (int i = start; i &lt; end;) {
	    int ucs;
	    char c1 = str.charAt(i++);
	    if (!Character.isHighSurrogate(c1) || i &gt;= end) {
		ucs = c1;
	    } else {
		char c2 = str.charAt(i);
		if (Character.isLowSurrogate(c2)) {
		    i++;
		    ucs = Character.toCodePoint(c1, c2);
		} else {
		    ucs = c1;
		}
	    }
	    cur += wcwidth(ucs, cur);
	}
	return cur - pos;
    }

    final void rawPrint(final String str) throws IOException {
	out.write(str);
	cursorOk = false;
    }

    /**
     * Output a platform-dependent newline.
     */
    public void println() throws IOException {
	rawPrint(LINE_SEPARATOR);
    }

    /**
     * Read a character from the console.  If boolean parameter is "true", it will check whether the keystroke was an "alt-" key combination, and
     * if so add 1000 to the value returned.  Better way...?
     *
     * @return the character, or -1 if an EOF is received.
     */
    public int readCharacter(boolean checkForAltKeyCombo) throws IOException {
	int c = reader.read();
	if (c &gt;= 0) {
	    Log.trace("Keystroke: ", c);
	    // clear any echo characters
	    if (terminal.isSupported()) {
		clearEcho(c);
	    }
	    if (c == ESCAPE && checkForAltKeyCombo && in.peek(escapeTimeout) &gt;= 32) {
		/* When ESC is encountered and there is a pending
		 * character in the pushback queue, then it seems to be
		 * an Alt-[key] combination.  Is this true, cross-platform?
		 * It's working for me on Debian GNU/Linux at the moment anyway.
		 * I removed the "isNonBlockingEnabled" check, though it was
		 * in the similar code in "readLine(String prompt, final Character mask)" (way down),
		 * as I am not sure / didn't look up what it's about, and things are working so far w/o it.
		 */
		int next = reader.read();
		// with research, there's probably a much cleaner way to do this, but, this is now it flags an Alt key combination for now:
		next = next + 1000;
		return next;
	    }
	}
	return c;
    }

    int wcwidth(int ucs, int pos) {
	if (ucs == '\t') {
	    return nextTabStop(pos);
	} else if (ucs &lt; 32) {
	    return 2;
	} else {
	    int w = WCWidth.wcwidth(ucs);
	    return w &gt; 0 ? w : 0;
	}
    }

    /**
     * Clear the echoed characters for the specified character code.
     */
    private int clearEcho(final int c) throws IOException {
	// if the terminal is not echoing, then ignore
	if (!terminal.isEchoEnabled()) {
	    return 0;
	}

	// otherwise, clear
	int pos = getCursorPosition();
	int num = wcwidth(c, pos);
	moveCursorFromTo(pos + num, pos);
	drawBuffer(num);

	return num;
    }

    int nextTabStop(int pos) {
	int tabWidth = TAB_WIDTH;
	int width = getTerminal().getWidth();
	int mod = (pos + tabWidth - 1) % tabWidth;
	int npos = pos + tabWidth - mod;
	return npos &lt; width ? npos - pos : width - pos;
    }

    int getCursorPosition() {
	return promptLen + wcwidth(buf.buffer, 0, buf.cursor, promptLen);
    }

    private void moveCursorFromTo(int i0, int i1) throws IOException {
	if (i0 == i1)
	    return;
	int width = getTerminal().getWidth();
	int l0 = i0 / width;
	int c0 = i0 % width;
	int l1 = i1 / width;
	int c1 = i1 % width;
	if (l0 == l1 + 1) {
	    if (!tputs("cursor_up")) {
		tputs("parm_up_cursor", 1);
	    }
	} else if (l0 &gt; l1) {
	    if (!tputs("parm_up_cursor", l0 - l1)) {
		for (int i = l1; i &lt; l0; i++) {
		    tputs("cursor_up");
		}
	    }
	} else if (l0 &lt; l1) {
	    tputs("carriage_return");
	    rawPrint('\n', l1 - l0);
	    c0 = 0;
	}
	if (c0 == c1 - 1) {
	    tputs("cursor_right");
	} else if (c0 == c1 + 1) {
	    tputs("cursor_left");
	} else if (c0 &lt; c1) {
	    if (!tputs("parm_right_cursor", c1 - c0)) {
		for (int i = c0; i &lt; c1; i++) {
		    tputs("cursor_right");
		}
	    }
	} else if (c0 &gt; c1) {
	    if (!tputs("parm_left_cursor", c0 - c1)) {
		for (int i = c1; i &lt; c0; i++) {
		    tputs("cursor_left");
		}
	    }
	}
	cursorOk = true;
    }

    /**
     * Redraw the rest of the buffer from the cursor onwards. This is necessary
     * for inserting text into the buffer.
     *
     * @param clear the number of characters to clear after the end of the buffer
     */
    private void drawBuffer(final int clear) throws IOException {
	// debug ("drawBuffer: " + clear);
	int nbChars = buf.length() - buf.cursor;
	if (buf.cursor != buf.length() || clear != 0) {
	    if (mask != null) {
		if (mask != NULL_MASK) {
		    rawPrint(mask, nbChars);
		} else {
		    nbChars = 0;
		}
	    } else {
		fmtPrint(buf.buffer, buf.cursor, buf.length());
	    }
	}
	int cursorPos = promptLen + wcwidth(buf.buffer, 0, buf.length(), promptLen);
	if (terminal.hasWeirdWrap() && !cursorOk) {
	    int width = terminal.getWidth();
	    // best guess on whether the cursor is in that weird location...
	    // Need to do this without calling ansi cursor location methods
	    // otherwise it breaks paste of wrapped lines in xterm.
	    if (cursorPos &gt; 0 && (cursorPos % width == 0)) {
		// the following workaround is reverse-engineered from looking
		// at what bash sent to the terminal in the same situation
		rawPrint(' '); // move cursor to next line by printing dummy space
		tputs("carriage_return"); // CR / not newline.
	    }
	    cursorOk = true;
	}
	clearAhead(clear, cursorPos);
	back(nbChars);
    }

    private void rawPrint(final char c, final int num) throws IOException {
	for (int i = 0; i &lt; num; i++) {
	    rawPrint(c);
	}
    }

    private int fmtPrint(final CharSequence buff, int start, int end) throws IOException {
	return fmtPrint(buff, start, end, getCursorPosition());
    }

    /**
     * Raw output printing
     */
    final void rawPrint(final int c) throws IOException {
	out.write(c);
	cursorOk = false;
    }

    /**
     * Clear ahead the specified number of characters without moving the cursor.
     *
     * @param num the number of characters to clear
     * @param pos the current screen cursor position
     */
    private void clearAhead(int num, final int pos) throws IOException {
	if (num == 0)
	    return;

	int width = terminal.getWidth();
	// Use kill line
	if (terminal.getStringCapability("clr_eol") != null) {
	    int cur = pos;
	    int c0 = cur % width;
	    // Erase end of current line
	    int nb = Math.min(num, width - c0);
	    tputs("clr_eol");
	    num -= nb;
	    // Loop
	    while (num &gt; 0) {
		// Move to beginning of next line
		int prev = cur;
		cur = cur - cur % width + width;
		moveCursorFromTo(prev, cur);
		// Erase
		nb = Math.min(num, width);
		tputs("clr_eol");
		num -= nb;
	    }
	    moveCursorFromTo(cur, pos);
	}
	// Terminal does not wrap on the right margin
	else if (!terminal.getBooleanCapability("auto_right_margin")) {
	    int cur = pos;
	    int c0 = cur % width;
	    // Erase end of current line
	    int nb = Math.min(num, width - c0);
	    rawPrint(' ', nb);
	    num -= nb;
	    cur += nb;
	    // Loop
	    while (num &gt; 0) {
		// Move to beginning of next line
		moveCursorFromTo(cur, ++cur);
		// Erase
		nb = Math.min(num, width);
		rawPrint(' ', nb);
		num -= nb;
		cur += nb;
	    }
	    moveCursorFromTo(cur, pos);
	}
	// Simple erasure
	else {
	    rawPrint(' ', num);
	    moveCursorFromTo(pos + num, pos);
	}
    }

    /**
     * Move the visual cursor backward without modifying the buffer cursor.
     */
    protected void back(final int num) throws IOException {
	if (num == 0)
	    return;
	int i0 = promptLen + wcwidth(buf.buffer, 0, buf.cursor, promptLen);
	int i1 = i0 + ((mask != null) ? num : wcwidth(buf.buffer, buf.cursor, buf.cursor + num, i0));
	moveCursorFromTo(i1, i0);
    }

    private int fmtPrint(final CharSequence buff, int start, int end, int cursorPos) throws IOException {
	checkNotNull(buff);
	for (int i = start; i &lt; end; i++) {
	    char c = buff.charAt(i);
	    if (c == '\t') {
		int nb = nextTabStop(cursorPos);
		cursorPos += nb;
		while (nb-- &gt; 0) {
		    out.write(' ');
		}
	    } else if (c &lt; 32) {
		out.write('^');
		out.write((char) (c + '@'));
		cursorPos += 2;
	    } else {
		int w = WCWidth.wcwidth(c);
		if (w &gt; 0) {
		    out.write(c);
		    cursorPos += w;
		}
	    }
	}
	cursorOk = false;
	return cursorPos;
    }

}

