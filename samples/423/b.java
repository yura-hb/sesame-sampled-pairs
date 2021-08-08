import java.util.ArrayList;

class MergeCollation {
    /**
     * adds a pattern to the current one.
     * @param pattern the new pattern to be added
     */
    public void addPattern(String pattern) throws ParseException {
	if (pattern == null)
	    return;

	PatternEntry.Parser parser = new PatternEntry.Parser(pattern);

	PatternEntry entry = parser.next();
	while (entry != null) {
	    fixEntry(entry);
	    entry = parser.next();
	}
    }

    private transient PatternEntry lastEntry = null;
    private final int BYTEPOWER = 3;
    private transient byte[] statusArray = new byte[8192];
    private final byte BITARRAYMASK = (byte) 0x1;
    private final int BYTEMASK = (1 &lt;&lt; BYTEPOWER) - 1;
    ArrayList&lt;PatternEntry&gt; patterns = new ArrayList&lt;&gt;();
    private transient StringBuffer excess = new StringBuffer();
    private transient PatternEntry saveEntry = null;

    private final void fixEntry(PatternEntry newEntry) throws ParseException {
	// check to see whether the new entry has the same characters as the previous
	// entry did (this can happen when a pattern declaring a difference between two
	// strings that are canonically equivalent is normalized).  If so, and the strength
	// is anything other than IDENTICAL or RESET, throw an exception (you can't
	// declare a string to be unequal to itself).       --rtg 5/24/99
	if (lastEntry != null && newEntry.chars.equals(lastEntry.chars)
		&& newEntry.extension.equals(lastEntry.extension)) {
	    if (newEntry.strength != Collator.IDENTICAL && newEntry.strength != PatternEntry.RESET) {
		throw new ParseException("The entries " + lastEntry + " and " + newEntry
			+ " are adjacent in the rules, but have conflicting "
			+ "strengths: A character can't be unequal to itself.", -1);
	    } else {
		// otherwise, just skip this entry and behave as though you never saw it
		return;
	    }
	}

	boolean changeLastEntry = true;
	if (newEntry.strength != PatternEntry.RESET) {
	    int oldIndex = -1;

	    if ((newEntry.chars.length() == 1)) {

		char c = newEntry.chars.charAt(0);
		int statusIndex = c &gt;&gt; BYTEPOWER;
		byte bitClump = statusArray[statusIndex];
		byte setBit = (byte) (BITARRAYMASK &lt;&lt; (c & BYTEMASK));

		if (bitClump != 0 && (bitClump & setBit) != 0) {
		    oldIndex = patterns.lastIndexOf(newEntry);
		} else {
		    // We're going to add an element that starts with this
		    // character, so go ahead and set its bit.
		    statusArray[statusIndex] = (byte) (bitClump | setBit);
		}
	    } else {
		oldIndex = patterns.lastIndexOf(newEntry);
	    }
	    if (oldIndex != -1) {
		patterns.remove(oldIndex);
	    }

	    excess.setLength(0);
	    int lastIndex = findLastEntry(lastEntry, excess);

	    if (excess.length() != 0) {
		newEntry.extension = excess + newEntry.extension;
		if (lastIndex != patterns.size()) {
		    lastEntry = saveEntry;
		    changeLastEntry = false;
		}
	    }
	    if (lastIndex == patterns.size()) {
		patterns.add(newEntry);
		saveEntry = newEntry;
	    } else {
		patterns.add(lastIndex, newEntry);
	    }
	}
	if (changeLastEntry) {
	    lastEntry = newEntry;
	}
    }

    private final int findLastEntry(PatternEntry entry, StringBuffer excessChars) throws ParseException {
	if (entry == null)
	    return 0;

	if (entry.strength != PatternEntry.RESET) {
	    // Search backwards for string that contains this one;
	    // most likely entry is last one

	    int oldIndex = -1;
	    if ((entry.chars.length() == 1)) {
		int index = entry.chars.charAt(0) &gt;&gt; BYTEPOWER;
		if ((statusArray[index] & (BITARRAYMASK &lt;&lt; (entry.chars.charAt(0) & BYTEMASK))) != 0) {
		    oldIndex = patterns.lastIndexOf(entry);
		}
	    } else {
		oldIndex = patterns.lastIndexOf(entry);
	    }
	    if ((oldIndex == -1))
		throw new ParseException("couldn't find last entry: " + entry, oldIndex);
	    return oldIndex + 1;
	} else {
	    int i;
	    for (i = patterns.size() - 1; i &gt;= 0; --i) {
		PatternEntry e = patterns.get(i);
		if (e.chars.regionMatches(0, entry.chars, 0, e.chars.length())) {
		    excessChars.append(entry.chars, e.chars.length(), entry.chars.length());
		    break;
		}
	    }
	    if (i == -1)
		throw new ParseException("couldn't find: " + entry, i);
	    return i + 1;
	}
    }

}

