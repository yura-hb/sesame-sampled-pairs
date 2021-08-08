import jdk.javadoc.internal.doclets.formats.html.markup.Table;
import java.util.Map;
import java.util.Set;
import jdk.javadoc.doclet.DocletEnvironment.ModuleMode;
import jdk.javadoc.internal.doclets.formats.html.markup.ContentBuilder;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlTag;
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlTree;
import jdk.javadoc.internal.doclets.toolkit.Content;

class ModuleWriterImpl extends HtmlDocletWriter implements ModuleSummaryWriter {
    /**
     * Add the provides list for the module.
     *
     * @param table the table to which the services provided will be added
     */
    public void addProvidesList(Table table) {
	SortedSet&lt;TypeElement&gt; implSet;
	Content description;
	for (Map.Entry&lt;TypeElement, SortedSet&lt;TypeElement&gt;&gt; entry : provides.entrySet()) {
	    TypeElement srv = entry.getKey();
	    if (!displayServiceDirective(srv, providesTrees)) {
		continue;
	    }
	    implSet = entry.getValue();
	    Content srvLinkContent = getLink(new LinkInfoImpl(configuration, LinkInfoImpl.Kind.PACKAGE, srv));
	    Content desc = new ContentBuilder();
	    if (display(providesTrees)) {
		description = providesTrees.get(srv);
		desc.addContent(
			(description != null && !description.isEmpty()) ? HtmlTree.DIV(HtmlStyle.block, description)
				: Contents.SPACE);
	    } else {
		desc.addContent(Contents.SPACE);
	    }
	    // Only display the implementation details in the "all" mode.
	    if (moduleMode == ModuleMode.ALL && !implSet.isEmpty()) {
		desc.addContent(new HtmlTree(HtmlTag.BR));
		desc.addContent("(");
		HtmlTree implSpan = HtmlTree.SPAN(HtmlStyle.implementationLabel, contents.implementation);
		desc.addContent(implSpan);
		desc.addContent(Contents.SPACE);
		String sep = "";
		for (TypeElement impl : implSet) {
		    desc.addContent(sep);
		    desc.addContent(getLink(new LinkInfoImpl(configuration, LinkInfoImpl.Kind.PACKAGE, impl)));
		    sep = ", ";
		}
		desc.addContent(")");
	    }
	    table.addRow(srvLinkContent, desc);
	}
    }

    /**
     * Map of services provided by this module, and set of its implementations.
     */
    private final Map&lt;TypeElement, SortedSet&lt;TypeElement&gt;&gt; provides = new TreeMap&lt;&gt;(utils.makeAllClassesComparator());
    /**
     * Map of services provided by the module and specified using @provides javadoc tag, and
     * description.
     */
    private final Map&lt;TypeElement, Content&gt; providesTrees = new TreeMap&lt;&gt;(utils.makeAllClassesComparator());
    /**
     * The module mode for this javadoc run. It can be set to "api" or "all".
     */
    private final ModuleMode moduleMode;

    private boolean displayServiceDirective(TypeElement typeElement, Map&lt;TypeElement, Content&gt; tagsMap) {
	return moduleMode == ModuleMode.ALL || tagsMap.containsKey(typeElement);
    }

    /**
     * Returns true if there are elements to be displayed.
     *
     * @param section map of elements.
     * @return true if there are elements to be displayed
     */
    public boolean display(Map&lt;? extends Element, ?&gt; section) {
	return section != null && !section.isEmpty();
    }

}

