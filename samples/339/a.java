import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.env.AccessRule;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;

class ClasspathEntry implements IClasspathEntry {
    /**
     * Returns the XML encoding of the class path.
     */
    public void elementEncode(XMLWriter writer, IPath projectPath, boolean indent, boolean newLine, Map unknownElements,
	    boolean isReferencedEntry) {
	HashMap parameters = new HashMap();

	parameters.put(TAG_KIND, ClasspathEntry.kindToString(this.entryKind));

	IPath xmlPath = this.path;
	if (this.entryKind != IClasspathEntry.CPE_VARIABLE && this.entryKind != IClasspathEntry.CPE_CONTAINER) {
	    // translate to project relative from absolute (unless a device path)
	    if (xmlPath.isAbsolute()) {
		if (projectPath != null && projectPath.isPrefixOf(xmlPath)) {
		    if (xmlPath.segment(0).equals(projectPath.segment(0))) {
			xmlPath = xmlPath.removeFirstSegments(1);
			xmlPath = xmlPath.makeRelative();
		    } else {
			xmlPath = xmlPath.makeAbsolute();
		    }
		}
	    }
	}
	parameters.put(TAG_PATH, String.valueOf(xmlPath));

	if (this.sourceAttachmentPath != null) {
	    xmlPath = this.sourceAttachmentPath;
	    // translate to project relative from absolute
	    if (this.entryKind != IClasspathEntry.CPE_VARIABLE && projectPath != null
		    && projectPath.isPrefixOf(xmlPath)) {
		if (xmlPath.segment(0).equals(projectPath.segment(0))) {
		    xmlPath = xmlPath.removeFirstSegments(1);
		    xmlPath = xmlPath.makeRelative();
		}
	    }
	    parameters.put(TAG_SOURCEPATH, String.valueOf(xmlPath));
	}
	if (this.sourceAttachmentRootPath != null) {
	    parameters.put(TAG_ROOTPATH, String.valueOf(this.sourceAttachmentRootPath));
	}
	if (this.isExported) {
	    parameters.put(TAG_EXPORTED, "true");//$NON-NLS-1$
	}
	encodePatterns(this.inclusionPatterns, TAG_INCLUDING, parameters);
	encodePatterns(this.exclusionPatterns, TAG_EXCLUDING, parameters);
	if (this.entryKind == CPE_PROJECT && !this.combineAccessRules)
	    parameters.put(TAG_COMBINE_ACCESS_RULES, "false"); //$NON-NLS-1$

	// unknown attributes
	UnknownXmlElements unknownXmlElements = unknownElements == null ? null
		: (UnknownXmlElements) unknownElements.get(this.path);
	String[] unknownAttributes;
	if (unknownXmlElements != null && (unknownAttributes = unknownXmlElements.attributes) != null)
	    for (int i = 0, length = unknownAttributes.length; i &lt; length; i += 2) {
		String tagName = unknownAttributes[i];
		String tagValue = unknownAttributes[i + 1];
		parameters.put(tagName, tagValue);
	    }

	if (this.specificOutputLocation != null) {
	    IPath outputLocation = this.specificOutputLocation.removeFirstSegments(1);
	    outputLocation = outputLocation.makeRelative();
	    parameters.put(TAG_OUTPUT, String.valueOf(outputLocation));
	}

	boolean hasExtraAttributes = this.extraAttributes.length != 0;
	boolean hasRestrictions = getAccessRuleSet() != null; // access rule set is null if no access rules
	ArrayList unknownChildren = unknownXmlElements != null ? unknownXmlElements.children : null;
	boolean hasUnknownChildren = unknownChildren != null;

	/* close tag if no extra attributes, no restriction and no unknown children */
	String tagName = isReferencedEntry ? TAG_REFERENCED_ENTRY : TAG_CLASSPATHENTRY;
	writer.printTag(tagName, parameters, indent, newLine,
		!hasExtraAttributes && !hasRestrictions && !hasUnknownChildren);

	if (hasExtraAttributes)
	    encodeExtraAttributes(writer, indent, newLine);

	if (hasRestrictions)
	    encodeAccessRules(writer, indent, newLine);

	if (hasUnknownChildren)
	    encodeUnknownChildren(writer, indent, newLine, unknownChildren);

	if (hasExtraAttributes || hasRestrictions || hasUnknownChildren)
	    writer.endTag(tagName, indent, true/*insert new line*/);
    }

    public static final String TAG_KIND = "kind";
    /**
     * Describes the kind of classpath entry - one of
     * CPE_PROJECT, CPE_LIBRARY, CPE_SOURCE, CPE_VARIABLE or CPE_CONTAINER
     */
    public int entryKind;
    /**
     * The meaning of the path of a classpath entry depends on its entry kind:&lt;ul&gt;
     *	&lt;li&gt;Source code in the current project (&lt;code&gt;CPE_SOURCE&lt;/code&gt;) -
     *      The path associated with this entry is the absolute path to the root folder. &lt;/li&gt;
     *	&lt;li&gt;A binary library in the current project (&lt;code&gt;CPE_LIBRARY&lt;/code&gt;) - the path
     *		associated with this entry is the absolute path to the JAR (or root folder), and
     *		in case it refers to an external JAR, then there is no associated resource in
     *		the workbench.
     *	&lt;li&gt;A required project (&lt;code&gt;CPE_PROJECT&lt;/code&gt;) - the path of the entry denotes the
     *		path to the corresponding project resource.&lt;/li&gt;
     *  &lt;li&gt;A variable entry (&lt;code&gt;CPE_VARIABLE&lt;/code&gt;) - the first segment of the path
     *      is the name of a classpath variable. If this classpath variable
     *		is bound to the path &lt;it&gt;P&lt;/it&gt;, the path of the corresponding classpath entry
     *		is computed by appending to &lt;it&gt;P&lt;/it&gt; the segments of the returned
     *		path without the variable.&lt;/li&gt;
     *  &lt;li&gt; A container entry (&lt;code&gt;CPE_CONTAINER&lt;/code&gt;) - the first segment of the path is denoting
     *     the unique container identifier (for which a &lt;code&gt;ClasspathContainerInitializer&lt;/code&gt; could be
     * 	registered), and the remaining segments are used as additional hints for resolving the container entry to
     * 	an actual &lt;code&gt;IClasspathContainer&lt;/code&gt;.&lt;/li&gt;
     */
    public IPath path;
    public static final String TAG_PATH = "path";
    /**
     * Describes the path to the source archive associated with this
     * classpath entry, or &lt;code&gt;null&lt;/code&gt; if this classpath entry has no
     * source attachment.
     * &lt;p&gt;
     * Only library and variable classpath entries may have source attachments.
     * For library classpath entries, the result path (if present) locates a source
     * archive. For variable classpath entries, the result path (if present) has
     * an analogous form and meaning as the variable path, namely the first segment
     * is the name of a classpath variable.
     */
    public IPath sourceAttachmentPath;
    public static final String TAG_SOURCEPATH = "sourcepath";
    /**
     * Describes the path within the source archive where package fragments
     * are located. An empty path indicates that packages are located at
     * the root of the source archive. Returns a non-&lt;code&gt;null&lt;/code&gt; value
     * if and only if &lt;code&gt;getSourceAttachmentPath&lt;/code&gt; returns
     * a non-&lt;code&gt;null&lt;/code&gt; value.
     */
    public IPath sourceAttachmentRootPath;
    public static final String TAG_ROOTPATH = "rootpath";
    /**
     * The export flag
     */
    public boolean isExported;
    public static final String TAG_EXPORTED = "exported";
    /**
     * Patterns allowing to include/exclude portions of the resource tree denoted by this entry path.
     */
    private IPath[] inclusionPatterns;
    public static final String TAG_INCLUDING = "including";
    private IPath[] exclusionPatterns;
    public static final String TAG_EXCLUDING = "excluding";
    private boolean combineAccessRules;
    public static final String TAG_COMBINE_ACCESS_RULES = "combineaccessrules";
    /**
     * Specific output location (for this source entry)
     */
    public IPath specificOutputLocation;
    public static final String TAG_OUTPUT = "output";
    /**
     * The extra attributes
     */
    public IClasspathAttribute[] extraAttributes;
    public static final String TAG_REFERENCED_ENTRY = "referencedentry";
    public static final String TAG_CLASSPATHENTRY = "classpathentry";
    /**
     * A constant indicating an output location.
     */
    public static final int K_OUTPUT = 10;
    private AccessRuleSet accessRuleSet;
    public static final String TAG_ATTRIBUTES = "attributes";
    public static final String TAG_ATTRIBUTE_NAME = "name";
    public static final String TAG_ATTRIBUTE_VALUE = "value";
    public static final String TAG_ATTRIBUTE = "attribute";
    public static final String TAG_ACCESS_RULES = "accessrules";
    public static final String TAG_PATTERN = "pattern";
    public static final String TAG_NON_ACCESSIBLE = "nonaccessible";
    public static final String TAG_DISCOURAGED = "discouraged";
    public static final String TAG_ACCESSIBLE = "accessible";
    public static final String TAG_IGNORE_IF_BETTER = "ignoreifbetter";
    public static final String TAG_ACCESS_RULE = "accessrule";

    /**
     * Returns a &lt;code&gt;String&lt;/code&gt; for the kind of a class path entry.
     */
    static String kindToString(int kind) {

	switch (kind) {
	case IClasspathEntry.CPE_PROJECT:
	    return "src"; // backward compatibility //$NON-NLS-1$
	case IClasspathEntry.CPE_SOURCE:
	    return "src"; //$NON-NLS-1$
	case IClasspathEntry.CPE_LIBRARY:
	    return "lib"; //$NON-NLS-1$
	case IClasspathEntry.CPE_VARIABLE:
	    return "var"; //$NON-NLS-1$
	case IClasspathEntry.CPE_CONTAINER:
	    return "con"; //$NON-NLS-1$
	case ClasspathEntry.K_OUTPUT:
	    return "output"; //$NON-NLS-1$
	default:
	    return "unknown"; //$NON-NLS-1$
	}
    }

    /**
     * Encode some patterns into XML parameter tag
     */
    private static void encodePatterns(IPath[] patterns, String tag, Map parameters) {
	if (patterns != null && patterns.length &gt; 0) {
	    StringBuffer rule = new StringBuffer(10);
	    for (int i = 0, max = patterns.length; i &lt; max; i++) {
		if (i &gt; 0)
		    rule.append('|');
		rule.append(patterns[i]);
	    }
	    parameters.put(tag, String.valueOf(rule));
	}
    }

    public AccessRuleSet getAccessRuleSet() {
	return this.accessRuleSet;
    }

    void encodeExtraAttributes(XMLWriter writer, boolean indent, boolean newLine) {
	writer.startTag(TAG_ATTRIBUTES, indent);
	for (int i = 0; i &lt; this.extraAttributes.length; i++) {
	    IClasspathAttribute attribute = this.extraAttributes[i];
	    HashMap parameters = new HashMap();
	    parameters.put(TAG_ATTRIBUTE_NAME, attribute.getName());
	    parameters.put(TAG_ATTRIBUTE_VALUE, attribute.getValue());
	    writer.printTag(TAG_ATTRIBUTE, parameters, indent, newLine, true);
	}
	writer.endTag(TAG_ATTRIBUTES, indent, true/*insert new line*/);
    }

    void encodeAccessRules(XMLWriter writer, boolean indent, boolean newLine) {

	writer.startTag(TAG_ACCESS_RULES, indent);
	AccessRule[] rules = getAccessRuleSet().getAccessRules();
	for (int i = 0, length = rules.length; i &lt; length; i++) {
	    encodeAccessRule(rules[i], writer, indent, newLine);
	}
	writer.endTag(TAG_ACCESS_RULES, indent, true/*insert new line*/);
    }

    private void encodeUnknownChildren(XMLWriter writer, boolean indent, boolean newLine, ArrayList unknownChildren) {
	for (int i = 0, length = unknownChildren.size(); i &lt; length; i++) {
	    String child = (String) unknownChildren.get(i);
	    writer.printString(child, indent, false/*don't insert new line*/);
	}
    }

    private void encodeAccessRule(AccessRule accessRule, XMLWriter writer, boolean indent, boolean newLine) {

	HashMap parameters = new HashMap();
	parameters.put(TAG_PATTERN, new String(accessRule.pattern));

	switch (accessRule.getProblemId()) {
	case IProblem.ForbiddenReference:
	    parameters.put(TAG_KIND, TAG_NON_ACCESSIBLE);
	    break;
	case IProblem.DiscouragedReference:
	    parameters.put(TAG_KIND, TAG_DISCOURAGED);
	    break;
	default:
	    parameters.put(TAG_KIND, TAG_ACCESSIBLE);
	    break;
	}
	if (accessRule.ignoreIfBetter())
	    parameters.put(TAG_IGNORE_IF_BETTER, "true"); //$NON-NLS-1$

	writer.printTag(TAG_ACCESS_RULE, parameters, indent, newLine, true);

    }

}

