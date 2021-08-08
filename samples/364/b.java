import java.io.StringReader;
import java.util.List;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

class EntityCharacterEventOrder {
    /**
    public static void main(String[] args) {
        TestRunner.run(JDK6770436Test.class);
    }
    */
    @Test
    public void entityCallbackOrderJava() throws SAXException, IOException {
	final String input = "&lt;element&gt; &amp; some more text&lt;/element&gt;";

	final MockContentHandler handler = new MockContentHandler();
	final XMLReader xmlReader = XMLReaderFactory.createXMLReader();

	xmlReader.setContentHandler(handler);
	xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);

	xmlReader.parse(new InputSource(new StringReader(input)));

	final List&lt;String&gt; events = handler.getEvents();
	printEvents(events);
	assertCallbackOrder(events); //regression from JDK5
    }

    private void printEvents(final List&lt;String&gt; events) {
	events.stream().forEach((e) -&gt; {
	    System.out.println(e);
	});
    }

    private void assertCallbackOrder(final List&lt;String&gt; events) {
	assertEquals("startDocument", events.get(0));
	assertEquals("startElement 'element'", events.get(1));
	assertEquals("characters ' '", events.get(2));
	assertEquals("startEntity 'amp'", events.get(3));
	assertEquals("characters '&'", events.get(4));
	assertEquals("endEntity 'amp'", events.get(5));
	assertEquals("characters ' some more text'", events.get(6));
	assertEquals("endElement 'element'", events.get(7));
	assertEquals("endDocument", events.get(8));
    }

    class MockContentHandler extends DefaultHandler2 {
	public List&lt;String&gt; getEvents() {
	    return events;
	}

    }

}

