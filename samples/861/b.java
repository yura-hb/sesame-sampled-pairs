import javax.swing.text.html.parser.*;
import java.io.DataOutputStream;
import java.util.Hashtable;
import java.util.Vector;
import java.util.BitSet;

class DTDBuilder extends DTD {
    /**
     * Save an element to a stream.
     */

    public void saveElement(DataOutputStream out, Element elem) throws IOException {

	out.writeShort(getNameId(elem.getName()));
	out.writeByte(elem.getType());

	byte flags = 0;
	if (elem.omitStart()) {
	    flags |= 0x01;
	}
	if (elem.omitEnd()) {
	    flags |= 0x02;
	}
	out.writeByte(flags);
	saveContentModel(out, elem.getContent());

	// Exclusions
	if (elem.exclusions == null) {
	    out.writeShort(0);
	} else {
	    short num = 0;
	    for (int i = 0; i &lt; elem.exclusions.size(); i++) {
		if (elem.exclusions.get(i)) {
		    num++;
		}
	    }
	    out.writeShort(num);
	    for (int i = 0; i &lt; elem.exclusions.size(); i++) {
		if (elem.exclusions.get(i)) {
		    out.writeShort(getNameId(getElement(i).getName()));
		}
	    }
	}

	// Inclusions
	if (elem.inclusions == null) {
	    out.writeShort(0);
	} else {
	    short num = 0;
	    for (int i = 0; i &lt; elem.inclusions.size(); i++) {
		if (elem.inclusions.get(i)) {
		    num++;
		}
	    }
	    out.writeShort(num);
	    for (int i = 0; i &lt; elem.inclusions.size(); i++) {
		if (elem.inclusions.get(i)) {
		    out.writeShort(getNameId(getElement(i).getName()));
		}
	    }
	}

	// Attributes
	{
	    short numAtts = 0;
	    for (AttributeList atts = elem.getAttributes(); atts != null; atts = atts.getNext()) {
		numAtts++;
	    }
	    out.writeByte(numAtts);
	    for (AttributeList atts = elem.getAttributes(); atts != null; atts = atts.getNext()) {
		out.writeShort(getNameId(atts.getName()));
		out.writeByte(atts.getType());
		out.writeByte(atts.getModifier());
		if (atts.getValue() == null) {
		    out.writeShort(-1);
		} else {
		    out.writeShort(getNameId(atts.getValue()));
		}
		if (atts.values == null) {
		    out.writeShort(0);
		} else {
		    out.writeShort((short) atts.values.size());
		    for (int i = 0; i &lt; atts.values.size(); i++) {
			String s = (String) atts.values.elementAt(i);
			out.writeShort(getNameId(s));
		    }
		}
	    }
	}
    }

    private Hashtable&lt;String, Integer&gt; namesHash = new Hashtable&lt;&gt;();
    private Vector&lt;String&gt; namesVector = new Vector&lt;&gt;();

    private short getNameId(String name) {
	Integer o = namesHash.get(name);
	if (o != null) {
	    return (short) o.intValue();
	}
	int i = namesVector.size();
	namesVector.addElement(name);
	namesHash.put(name, new Integer(i));
	return (short) i;
    }

    /**
     * Save a content model to a stream. This does a
     * recursive decent of the entire model.
     */
    public void saveContentModel(DataOutputStream out, ContentModel model) throws IOException {
	if (model == null) {
	    out.writeByte(0);
	} else if (model.content instanceof ContentModel) {
	    out.writeByte(1);
	    out.writeByte(model.type);
	    saveContentModel(out, (ContentModel) model.content);

	    saveContentModel(out, model.next);
	} else if (model.content instanceof Element) {
	    out.writeByte(2);
	    out.writeByte(model.type);
	    out.writeShort(getNameId(((Element) model.content).getName()));

	    saveContentModel(out, model.next);
	}
    }

}

