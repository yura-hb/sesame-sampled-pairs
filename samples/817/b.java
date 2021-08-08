import java.io.Writer;
import jdk.internal.jline.Terminal;
import jdk.internal.jline.Terminal2;
import jdk.internal.jline.internal.Curses;

class ConsoleReader implements Closeable {
    /**
     * Issue a backspace.
     *
     * @return true if successful
     */
    public boolean backspace() throws IOException {
	return backspace(1) == 1;
    }

    private final CursorBuffer buf = new CursorBuffer();
    private int promptLen;
    private Character mask;
    public static final char NULL_MASK = 0;
    private final Terminal2 terminal;
    private boolean cursorOk;
    private final Writer out;
    public static final int TAB_WIDTH = 8;

    /**
     * Issue &lt;em&gt;num&lt;/em&gt; backspaces.
     *
     * @return the number of characters backed up
     */
    private int backspace(final int num) throws IOException {
	if (buf.cursor == 0) {
	    return 0;
	}

	int count = -moveCursor(-num);
	int clear = wcwidth(buf.buffer, buf.cursor, buf.cursor + count, getCursorPosition());
	buf.buffer.delete(buf.cursor, buf.cursor + count);

	drawBuffer(clear);
	return count;
    }

    /**
     * Move the cursor &lt;i&gt;where&lt;/i&gt; characters.
     *
     * @param num   If less than 0, move abs(&lt;i&gt;where&lt;/i&gt;) to the left, otherwise move &lt;i&gt;where&lt;/i&gt; to the right.
     * @return      The number of spaces we moved
     */
    public int moveCursor(final int num) throws IOException {
	int where = num;

	if ((buf.cursor == 0) && (where &lt;= 0)) {
	    return 0;
	}

	if ((buf.cursor == buf.buffer.length()) && (where &gt;= 0)) {
	    return 0;
	}

	if ((buf.cursor + where) &lt; 0) {
	    where = -buf.cursor;
	} else if ((buf.cursor + where) &gt; buf.buffer.length()) {
	    where = buf.buffer.length() - buf.cursor;
	}

	moveInternal(where);

	return where;
    }

    int getCursorPosition() {
	return promptLen + wcwidth(buf.buffer, 0, buf.cursor, promptLen);
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

    /**
     * Move the cursor &lt;i&gt;where&lt;/i&gt; characters, without checking the current buffer.
     *
     * @param where the number of characters to move to the right or left.
     */
    private void moveInternal(final int where) throws IOException {
	// debug ("move cursor " + where + " ("
	// + buf.cursor + " =&gt; " + (buf.cursor + where) + ")");
	buf.cursor += where;

	int i0;
	int i1;
	if (mask == null) {
	    if (where &lt; 0) {
		i1 = promptLen + wcwidth(buf.buffer, 0, buf.cursor, promptLen);
		i0 = i1 + wcwidth(buf.buffer, buf.cursor, buf.cursor - where, i1);
	    } else {
		i0 = promptLen + wcwidth(buf.buffer, 0, buf.cursor - where, promptLen);
		i1 = i0 + wcwidth(buf.buffer, buf.cursor - where, buf.cursor, i0);
	    }
	} else if (mask != NULL_MASK) {
	    i1 = promptLen + buf.cursor;
	    i0 = i1 - where;
	} else {
	    return;
	}
	moveCursorFromTo(i0, i1);
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

    private boolean tputs(String cap, Object... params) throws IOException {
	String str = terminal.getStringCapability(cap);
	if (str == null) {
	    return false;
	}
	Curses.tputs(out, str, params);
	return true;
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

    int nextTabStop(int pos) {
	int tabWidth = TAB_WIDTH;
	int width = getTerminal().getWidth();
	int mod = (pos + tabWidth - 1) % tabWidth;
	int npos = pos + tabWidth - mod;
	return npos &lt; width ? npos - pos : width - pos;
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

    public Terminal getTerminal() {
	return terminal;
    }

}

