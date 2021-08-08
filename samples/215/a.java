import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Map;

class KeyEventTranslator {
    /**
     * Pass this an event from
     * {@link KeyEventWorkaround#processKeyEvent(java.awt.event.KeyEvent)}.
     * 
     * @since jEdit 4.2pre3
     */
    public static Key translateKeyEvent(KeyEvent evt) {
	int modifiers = evt.getModifiers();
	Key returnValue = null;

	switch (evt.getID()) {
	case KeyEvent.KEY_PRESSED:
	    int keyCode = evt.getKeyCode();
	    if ((keyCode &gt;= KeyEvent.VK_0 && keyCode &lt;= KeyEvent.VK_9)
		    || (keyCode &gt;= KeyEvent.VK_A && keyCode &lt;= KeyEvent.VK_Z)) {
		if (KeyEventWorkaround.ALTERNATIVE_DISPATCHER)
		    return null;
		else {
		    returnValue = new Key(modifiersToString(modifiers),
			    // fc, 12.5.2005: changed to upper case as Freemind seems to
			    // need this.
			    '\0', Character.toUpperCase((char) keyCode));
		}
	    } else {
		if (keyCode == KeyEvent.VK_TAB) {
		    evt.consume();
		    returnValue = new Key(modifiersToString(modifiers), keyCode, '\0');
		} else if (keyCode == KeyEvent.VK_SPACE) {
		    // for SPACE or S+SPACE we pass the
		    // key typed since international
		    // keyboards sometimes produce a
		    // KEY_PRESSED SPACE but not a
		    // KEY_TYPED SPACE, eg if you have to
		    // do a "&lt;space&gt; to insert ".
		    if ((modifiers & ~InputEvent.SHIFT_MASK) == 0)
			returnValue = null;
		    else {
			returnValue = new Key(modifiersToString(modifiers), 0, ' ');
		    }
		} else {
		    returnValue = new Key(modifiersToString(modifiers), keyCode, '\0');
		}
	    }
	    break;
	case KeyEvent.KEY_TYPED:
	    char ch = evt.getKeyChar();

	    switch (ch) {
	    case '\n':
	    case '\t':
	    case '\b':
		return null;
	    case ' ':
		if ((modifiers & ~InputEvent.SHIFT_MASK) != 0)
		    return null;
	    }

	    int ignoreMods;
	    if (KeyEventWorkaround.ALT_KEY_PRESSED_DISABLED) {
		/* on MacOS, A+ can be user input */
		ignoreMods = (InputEvent.SHIFT_MASK | InputEvent.ALT_GRAPH_MASK | InputEvent.ALT_MASK);
	    } else {
		/* on MacOS, A+ can be user input */
		ignoreMods = (InputEvent.SHIFT_MASK | InputEvent.ALT_GRAPH_MASK);
	    }

	    if ((modifiers & InputEvent.ALT_GRAPH_MASK) == 0 && evt.getWhen() - KeyEventWorkaround.lastKeyTime &lt; 750
		    && (KeyEventWorkaround.modifiers & ~ignoreMods) != 0) {
		if (KeyEventWorkaround.ALTERNATIVE_DISPATCHER) {
		    returnValue = new Key(modifiersToString(modifiers), 0, ch);
		} else
		    return null;
	    } else {
		if (ch == ' ') {
		    returnValue = new Key(modifiersToString(modifiers), 0, ch);
		} else
		    returnValue = new Key(null, 0, ch);
	    }
	    break;
	default:
	    return null;
	}

	/*
	 * I guess translated events do not have the 'evt' field set so
	 * consuming won't work. I don't think this is a problem as nothing uses
	 * translation anyway
	 */
	Key trans = transMap.get(returnValue);
	if (trans == null)
	    return returnValue;
	else
	    return trans;
    }

    private static Map&lt;Key, Key&gt; transMap = new HashMap&lt;&gt;();
    static int c, a, m, s;
    static int c, a, m, s;
    static int c, a, m, s;
    static int c, a, m, s;

    public static String modifiersToString(int mods) {
	StringBuffer buf = null;

	if ((mods & InputEvent.CTRL_MASK) != 0) {
	    buf = new StringBuffer();

	    buf.append(getSymbolicModifierName(InputEvent.CTRL_MASK));
	}
	if ((mods & InputEvent.ALT_MASK) != 0) {
	    if (buf == null)
		buf = new StringBuffer();
	    else
		buf.append(GrabKeyDialog.MODIFIER_SEPARATOR);
	    buf.append(getSymbolicModifierName(InputEvent.ALT_MASK));
	}
	if ((mods & InputEvent.META_MASK) != 0) {
	    if (buf == null)
		buf = new StringBuffer();
	    else
		buf.append(GrabKeyDialog.MODIFIER_SEPARATOR);
	    buf.append(getSymbolicModifierName(InputEvent.META_MASK));
	}
	if ((mods & InputEvent.SHIFT_MASK) != 0) {
	    if (buf == null)
		buf = new StringBuffer();
	    else
		buf.append(GrabKeyDialog.MODIFIER_SEPARATOR);
	    buf.append(getSymbolicModifierName(InputEvent.SHIFT_MASK));
	}

	if (buf == null)
	    return null;
	else
	    return buf.toString();
    }

    /**
     * Returns a the symbolic modifier name for the specified Java modifier
     * flag.
     * 
     * @param mod
     *            A modifier constant from &lt;code&gt;InputEvent&lt;/code&gt;
     * 
     * @since jEdit 4.2pre3
     */
    public static String getSymbolicModifierName(int mod) {
	if ((mod & c) != 0)
	    return "control";
	else if ((mod & a) != 0)
	    return "alt";
	else if ((mod & m) != 0)
	    return "meta";
	else if ((mod & s) != 0)
	    return "shift";
	else
	    return "";
    }

    class Key {
	private static Map&lt;Key, Key&gt; transMap = new HashMap&lt;&gt;();
	static int c, a, m, s;
	static int c, a, m, s;
	static int c, a, m, s;
	static int c, a, m, s;

	public Key(String modifiers, int key, char input) {
	    this.modifiers = modifiers;
	    this.key = key;
	    this.input = input;
	}

    }

}

