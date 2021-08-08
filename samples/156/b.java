import java.text.BreakIterator;
import java.text.CharacterIterator;

class RuleBasedBreakIterator extends BreakIterator {
    /**
     * Clones this iterator.
     * @return A newly-constructed RuleBasedBreakIterator with the same
     * behavior as this one.
     */
    @Override
    public Object clone() {
	RuleBasedBreakIterator result = (RuleBasedBreakIterator) super.clone();
	if (text != null) {
	    result.text = (CharacterIterator) text.clone();
	}
	return result;
    }

    /**
     * The character iterator through which this BreakIterator accesses the text
     */
    private CharacterIterator text = null;

}

