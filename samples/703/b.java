import java.awt.Container;
import java.awt.Frame;
import java.awt.Window;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Hashtable;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.Operator;

class Dumper {
    /**
     * Prints component hierarchy (GUI dump) into file.
     *
     * @param comp a component to get information from.
     * @param fileName a file to write to.
     * @throws FileNotFoundException
     */
    public static void dumpComponent(Component comp, String fileName) throws FileNotFoundException {
	dumpComponent(comp, new PrintWriter(new FileOutputStream(fileName)));
    }

    private static final String tabIncrease = "  ";

    public static void dumpComponent(Component comp, PrintWriter writer) {
	dumpComponent(comp, writer, new DumpController() {
	    @Override
	    public boolean onComponentDump(Component comp) {
		return true;
	    }

	    @Override
	    public boolean onPropertyDump(Component comp, String name, String value) {
		return true;
	    }
	});
    }

    /**
     * Prints component hierarchy (GUI dump) starting from {@code comp}
     * component.
     *
     * @param comp a component to get information from.
     * @param writer a writer to write to.
     */
    public static void dumpComponent(Component comp, final PrintWriter writer, final DumpController listener) {
	QueueTool qt = new QueueTool();
	Component[] comps;
	if (comp != null) {
	    comps = new Component[1];
	    comps[0] = comp;
	} else {
	    comps = Frame.getFrames();
	}
	final Component[] comps_final = comps;
	qt.invokeSmoothly(new QueueAction&lt;Void&gt;("dumpComponent") {
	    @Override
	    public Void launch() throws Exception {
		printHeader(writer);
		dumpSome("dump", comps_final, writer, "", listener);
		writer.flush();
		return null;
	    }
	});
    }

    private static void printHeader(PrintWriter writer) {
	writer.println("&lt;?xml version=\"1.0\"?&gt;");
	writer.println("&lt;!DOCTYPE dump [");
	printDTD(writer, tabIncrease);
	writer.println("]&gt;");
    }

    private static void dumpSome(String tag, Component[] comps, PrintWriter writer, String tab,
	    DumpController listener) {
	if (comps.length &gt; 0) {
	    printTagStart(writer, tag, tab);
	    for (Component comp : comps) {
		dumpOne(comp, writer, tab + tabIncrease, listener);
	    }
	    printTagEnd(writer, tag, tab);
	}
    }

    private static void printDTD(PrintWriter writer, String tab) {
	writer.println(tab + "&lt;!ELEMENT dump (component*)&gt;");
	writer.println(tab + "&lt;!ELEMENT component (property+, subcomponents?, subwindows?, exception?)&gt;");
	writer.println(tab + "&lt;!ELEMENT subcomponents (component+)&gt;");
	writer.println(tab + "&lt;!ELEMENT subwindows (component+)&gt;");
	writer.println(tab + "&lt;!ELEMENT property EMPTY&gt;");
	writer.println(tab + "&lt;!ELEMENT exception EMPTY&gt;");
	writer.println(tab + "&lt;!ATTLIST component");
	writer.println(tab + "          operator CDATA #IMPLIED&gt;");
	writer.println(tab + "&lt;!ATTLIST exception");
	writer.println(tab + "          toString CDATA #REQUIRED&gt;");
	writer.println(tab + "&lt;!ATTLIST property");
	writer.println(tab + "          name  CDATA #REQUIRED");
	writer.println(tab + "          value CDATA #REQUIRED&gt;");
    }

    private static void printTagStart(PrintWriter writer, String tag, String tab) {
	writer.println(tab + "&lt;" + tag + "&gt;");
    }

    private static void dumpOne(Component component, PrintWriter writer, String tab, DumpController listener) {
	//whether to dump at all
	boolean toDump = listener.onComponentDump(component);
	if (toDump) {
	    try {
		Operator oper = Operator.createOperator(component);
		Hashtable&lt;String, Object&gt; componentDump = oper.getDump();
		printTagOpening(writer, "component", tab);
		writer.print(" operator=\"" + oper.getClass().getName() + "\"");
		printTagClosing(writer, "component");
		Object[] keys = componentDump.keySet().toArray();
		Arrays.sort(keys);
		String name, value;
		for (Object key : keys) {
		    name = (String) key;
		    value = ((String) componentDump.get(key));
		    if (listener.onPropertyDump(component, name, value)) {
			printEmptyTagOpening(writer, "property", tab + tabIncrease);
			writer.print(" name=\"" + escape(name) + "\" value=\"" + escape(value) + "\"");
			printEmptyTagClosing(writer, "property");
		    }
		}
	    } catch (Exception e) {
		JemmyProperties.getCurrentOutput().printStackTrace(e);
		printTagStart(writer, "component", tab);
		printEmptyTagOpening(writer, "exception", tab + tabIncrease);
		writer.print(" toString=\"" + escape(e.toString()) + "\"");
		printEmptyTagClosing(writer, "exception");
	    }
	}
	if (component instanceof Window) {
	    dumpSome("subwindows", ((Window) component).getOwnedWindows(), writer, tab + tabIncrease, listener);
	}
	if (component instanceof Container) {
	    dumpSome("subcomponents", ((Container) component).getComponents(), writer, tab + tabIncrease, listener);
	}
	if (toDump) {
	    printTagEnd(writer, "component", tab);
	}
    }

    private static void printTagEnd(PrintWriter writer, String tag, String tab) {
	writer.println(tab + "&lt;/" + tag + "&gt;");
    }

    private static void printTagOpening(PrintWriter writer, String tag, String tab) {
	writer.print(tab + "&lt;" + tag);
    }

    private static void printTagClosing(PrintWriter writer, String tag) {
	writer.println("&gt;");
    }

    private static void printEmptyTagOpening(PrintWriter writer, String tag, String tab) {
	writer.print(tab + "&lt;" + tag);
    }

    public static String escape(String str) {
	return str.replaceAll("&", "&amp;").replaceAll("&lt;", "&lt;").replaceAll("&gt;", "&gt;").replaceAll("\"", "&quot;");
    }

    private static void printEmptyTagClosing(PrintWriter writer, String tag) {
	writer.println("/&gt;");
    }

}

