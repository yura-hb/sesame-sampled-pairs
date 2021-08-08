import java.util.*;
import javax.swing.text.*;

class RTFReader extends RTFParser {
    abstract class AttributeTrackingDestination implements Destination {
	/**
	* Calculates the current section attributes
	* from the current parser state.
	*
	* @return a newly created MutableAttributeSet.
	*/
	public AttributeSet currentSectionAttributes() {
	    MutableAttributeSet attributes = new SimpleAttributeSet(sectionAttributes);

	    Style sectionStyle = (Style) parserState.get("sectionStyle");
	    if (sectionStyle != null)
		attributes.setResolveParent(sectionStyle);

	    return attributes;
	}

	/** This is the "sec" element of parserState, cached for
	 *  more efficient use */
	MutableAttributeSet sectionAttributes;

    }

    /** Miscellaneous information about the parser's state. This
    *  dictionary is saved and restored when an RTF group begins
    *  or ends. */
    Dictionary&lt;Object, Object&gt; parserState;

}

