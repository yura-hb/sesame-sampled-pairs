import java.text.CharacterIterator;

class DictionaryBasedBreakIterator extends RuleBasedBreakIterator {
    /**
     * Advances the iterator one step backwards.
     * @return The position of the last boundary position before the
     * current iteration position
     */
    @Override
    public int previous() {
	CharacterIterator text = getText();

	// if we have cached break positions and we're still in the range
	// covered by them, just move one step backward in the cache
	if (cachedBreakPositions != null && positionInCache &gt; 0) {
	    --positionInCache;
	    text.setIndex(cachedBreakPositions[positionInCache]);
	    return cachedBreakPositions[positionInCache];
	}

	// otherwise, dump the cache and use the inherited previous() method to move
	// backward.  This may fill up the cache with new break positions, in which
	// case we have to mark our position in the cache
	else {
	    cachedBreakPositions = null;
	    int result = super.previous();
	    if (cachedBreakPositions != null) {
		positionInCache = cachedBreakPositions.length - 2;
	    }
	    return result;
	}
    }

    /**
     * when a range of characters is divided up using the dictionary, the break
     * positions that are discovered are stored here, preventing us from having
     * to use either the dictionary or the state table again until the iterator
     * leaves this range of text
     */
    private int[] cachedBreakPositions;
    /**
     * if cachedBreakPositions is not null, this indicates which item in the
     * cache the current iteration position refers to
     */
    private int positionInCache;

}

