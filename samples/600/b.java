import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.sun.tools.javac.util.ListBuffer;

class Comment {
    /**
     * Return array of tags for the locale specific first sentence in the text.
     */
    static Tag[] firstSentenceTags(DocImpl holder, String text) {
	DocLocale doclocale = holder.env.doclocale;
	return getInlineTags(holder, doclocale.localeSpecificFirstSentence(holder, text));
    }

    /** regex for case-insensitive match for {@literal &lt;pre&gt; } and  {@literal &lt;/pre&gt; }. */
    private static final Pattern prePat = Pattern.compile("(?i)&lt;(/?)pre&gt;");

    /**
     * Return array of tags with text and inline See Tags for a Doc comment.
     */
    static Tag[] getInlineTags(DocImpl holder, String inlinetext) {
	ListBuffer&lt;Tag&gt; taglist = new ListBuffer&lt;&gt;();
	int delimend = 0, textstart = 0, len = inlinetext.length();
	boolean inPre = false;
	DocEnv docenv = holder.env;

	if (len == 0) {
	    return taglist.toArray(new Tag[taglist.length()]);
	}
	while (true) {
	    int linkstart;
	    if ((linkstart = inlineTagFound(holder, inlinetext, textstart)) == -1) {
		taglist.append(new TagImpl(holder, "Text", inlinetext.substring(textstart)));
		break;
	    } else {
		inPre = scanForPre(inlinetext, textstart, linkstart, inPre);
		int seetextstart = linkstart;
		for (int i = linkstart; i &lt; inlinetext.length(); i++) {
		    char c = inlinetext.charAt(i);
		    if (Character.isWhitespace(c) || c == '}') {
			seetextstart = i;
			break;
		    }
		}
		String linkName = inlinetext.substring(linkstart + 2, seetextstart);
		if (!(inPre && (linkName.equals("code") || linkName.equals("literal")))) {
		    //Move past the white space after the inline tag name.
		    while (Character.isWhitespace(inlinetext.charAt(seetextstart))) {
			if (inlinetext.length() &lt;= seetextstart) {
			    taglist.append(new TagImpl(holder, "Text", inlinetext.substring(textstart, seetextstart)));
			    docenv.warning(holder, "tag.Improper_Use_Of_Link_Tag", inlinetext);
			    return taglist.toArray(new Tag[taglist.length()]);
			} else {
			    seetextstart++;
			}
		    }
		}
		taglist.append(new TagImpl(holder, "Text", inlinetext.substring(textstart, linkstart)));
		textstart = seetextstart; // this text is actually seetag
		if ((delimend = findInlineTagDelim(inlinetext, textstart)) == -1) {
		    //Missing closing '}' character.
		    // store the text as it is with the {@link.
		    taglist.append(new TagImpl(holder, "Text", inlinetext.substring(textstart)));
		    docenv.warning(holder, "tag.End_delimiter_missing_for_possible_SeeTag", inlinetext);
		    return taglist.toArray(new Tag[taglist.length()]);
		} else {
		    //Found closing '}' character.
		    if (linkName.equals("see") || linkName.equals("link") || linkName.equals("linkplain")) {
			taglist.append(
				new SeeTagImpl(holder, "@" + linkName, inlinetext.substring(textstart, delimend)));
		    } else {
			taglist.append(new TagImpl(holder, "@" + linkName, inlinetext.substring(textstart, delimend)));
		    }
		    textstart = delimend + 1;
		}
	    }
	    if (textstart == inlinetext.length()) {
		break;
	    }
	}
	return taglist.toArray(new Tag[taglist.length()]);
    }

    /**
     * Recursively search for the characters '{', '@', followed by
     * name of inline tag and white space,
     * if found
     *    return the index of the text following the white space.
     * else
     *    return -1.
     */
    private static int inlineTagFound(DocImpl holder, String inlinetext, int start) {
	DocEnv docenv = holder.env;
	int linkstart = inlinetext.indexOf("{@", start);
	if (start == inlinetext.length() || linkstart == -1) {
	    return -1;
	} else if (inlinetext.indexOf('}', linkstart) == -1) {
	    //Missing '}'.
	    docenv.warning(holder, "tag.Improper_Use_Of_Link_Tag",
		    inlinetext.substring(linkstart, inlinetext.length()));
	    return -1;
	} else {
	    return linkstart;
	}
    }

    private static boolean scanForPre(String inlinetext, int start, int end, boolean inPre) {
	Matcher m = prePat.matcher(inlinetext).region(start, end);
	while (m.find()) {
	    inPre = m.group(1).isEmpty();
	}
	return inPre;
    }

    /**
     * Recursively find the index of the closing '}' character for an inline tag
     * and return it.  If it can't be found, return -1.
     * @param inlineText the text to search in.
     * @param searchStart the index of the place to start searching at.
     * @return the index of the closing '}' character for an inline tag.
     * If it can't be found, return -1.
     */
    private static int findInlineTagDelim(String inlineText, int searchStart) {
	int delimEnd, nestedOpenBrace;
	if ((delimEnd = inlineText.indexOf("}", searchStart)) == -1) {
	    return -1;
	} else if (((nestedOpenBrace = inlineText.indexOf("{", searchStart)) != -1) && nestedOpenBrace &lt; delimEnd) {
	    //Found a nested open brace.
	    int nestedCloseBrace = findInlineTagDelim(inlineText, nestedOpenBrace + 1);
	    return (nestedCloseBrace != -1) ? findInlineTagDelim(inlineText, nestedCloseBrace + 1) : -1;
	} else {
	    return delimEnd;
	}
    }

}

