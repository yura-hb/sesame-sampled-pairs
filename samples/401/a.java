import java.io.FilterWriter;
import java.io.Writer;

class XHTMLWriter extends FixedHTMLWriter {
    class XHTMLFilterWriter extends FilterWriter {
	/**
		 * Write a single char to the Writer.
		 * 
		 * @param c
		 *            Char to be written
		 */
	public void write(int c) throws IOException {
	    if (insideValue) {
		// We're currently within a tag attribute's value.
		// Take care for proper HTML escaping.
		if (c == '&') {
		    super.write("&amp;", 0, 5);
		    return;
		} else if (c == '&lt;') {
		    super.write("&lt;", 0, 4);
		    return;
		} else if (c == '&gt;') {
		    super.write("&gt;", 0, 4);
		    return;
		} else if (c == '"') { // leaving the value
		    insideValue = false;
		}
	    } else if (insideTag) {
		// We're inside a tag. Add a slash to the closing tag bracket
		// for
		// certain tags (like img, br, hr, input, ... ).
		if (readTag) {
		    if (c == ' ' || c == '&gt;') { // tag name ends
			readTag = false;
		    } else {
			tag += (char) c; // collect tag name here
		    }
		}
		if (c == '"') { // attribute value begins
		    insideValue = true;
		} else if (c == '&gt;') { // check if this is a "certain tag"
		    if (tag.equals("img") || tag.equals("br") || tag.equals("hr") || tag.equals("input")
			    || tag.equals("meta") || tag.equals("link") || tag.equals("area") || tag.equals("base")
			    || tag.equals("basefont") || tag.equals("frame") || tag.equals("iframe")
			    || tag.equals("col")) {
			super.write(" /"); // add slash to the closing bracket
		    }
		    insideTag = false;
		    readTag = false;
		}
	    } else if (c == '&lt;') {
		// We're just at the very beginning of a tag.
		tag = "";
		insideTag = true;
		readTag = true;
	    }
	    super.write(c);
	}

	private boolean insideValue = false;
	private boolean insideTag = false;
	private boolean readTag = false;
	private String tag = "";

    }

}

