import java.text.CollationKey;
import java.text.Collator;
import java.util.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import jdk.javadoc.internal.doclets.toolkit.BaseConfiguration;
import static javax.lang.model.element.ElementKind.*;

class Utils {
    /**
     * Compares two elements.
     * @param e1 first Element
     * @param e2 second Element
     * @return a true if they are the same, false otherwise.
     */
    public boolean elementsEqual(Element e1, Element e2) {
	if (e1.getKind() != e2.getKind()) {
	    return false;
	}
	String s1 = getSimpleName(e1);
	String s2 = getSimpleName(e2);
	if (compareStrings(s1, s2) == 0) {
	    String f1 = getFullyQualifiedName(e1, true);
	    String f2 = getFullyQualifiedName(e2, true);
	    return compareStrings(f1, f2) == 0;
	}
	return false;
    }

    private final Map&lt;Element, String&gt; nameCache = new LinkedHashMap&lt;&gt;();
    private SimpleElementVisitor9&lt;String, Void&gt; snvisitor = null;
    private DocCollator tertiaryCollator = null;
    public final BaseConfiguration configuration;
    private DocCollator secondaryCollator = null;

    /**
     * Returns the name of the element after the last dot of the package name.
     * This emulates the behavior of the old doclet.
     * @param e an element whose name is required
     * @return the name
     */
    public String getSimpleName(Element e) {
	return nameCache.computeIfAbsent(e, this::getSimpleName0);
    }

    /**
     * A general purpose case insensitive String comparator, which compares
     * two Strings using a Collator strength of "TERTIARY".
     *
     * @param s1 first String to compare.
     * @param s2 second String to compare.
     * @return a negative integer, zero, or a positive integer as the first
     *         argument is less than, equal to, or greater than the second.
     */
    public int compareStrings(String s1, String s2) {
	return compareStrings(true, s1, s2);
    }

    public String getFullyQualifiedName(Element e, final boolean outer) {
	return new SimpleElementVisitor9&lt;String, Void&gt;() {
	    @Override
	    public String visitModule(ModuleElement e, Void p) {
		return e.getQualifiedName().toString();
	    }

	    @Override
	    public String visitPackage(PackageElement e, Void p) {
		return e.getQualifiedName().toString();
	    }

	    @Override
	    public String visitType(TypeElement e, Void p) {
		return e.getQualifiedName().toString();
	    }

	    @Override
	    protected String defaultAction(Element e, Void p) {
		return outer ? visit(e.getEnclosingElement()) : e.getSimpleName().toString();
	    }
	}.visit(e);
    }

    private String getSimpleName0(Element e) {
	if (snvisitor == null) {
	    snvisitor = new SimpleElementVisitor9&lt;String, Void&gt;() {
		@Override
		public String visitModule(ModuleElement e, Void p) {
		    return e.getQualifiedName().toString(); // temp fix for 8182736
		}

		@Override
		public String visitType(TypeElement e, Void p) {
		    StringBuilder sb = new StringBuilder(e.getSimpleName());
		    Element enclosed = e.getEnclosingElement();
		    while (enclosed != null && (enclosed.getKind().isClass() || enclosed.getKind().isInterface())) {
			sb.insert(0, enclosed.getSimpleName() + ".");
			enclosed = enclosed.getEnclosingElement();
		    }
		    return sb.toString();
		}

		@Override
		public String visitExecutable(ExecutableElement e, Void p) {
		    if (e.getKind() == CONSTRUCTOR || e.getKind() == STATIC_INIT) {
			return e.getEnclosingElement().getSimpleName().toString();
		    }
		    return e.getSimpleName().toString();
		}

		@Override
		protected String defaultAction(Element e, Void p) {
		    return e.getSimpleName().toString();
		}
	    };
	}
	return snvisitor.visit(e);
    }

    private int compareStrings(boolean caseSensitive, String s1, String s2) {
	if (caseSensitive) {
	    if (tertiaryCollator == null) {
		tertiaryCollator = new DocCollator(configuration.locale, Collator.TERTIARY);
	    }
	    return tertiaryCollator.compare(s1, s2);
	}
	if (secondaryCollator == null) {
	    secondaryCollator = new DocCollator(configuration.locale, Collator.SECONDARY);
	}
	return secondaryCollator.compare(s1, s2);
    }

    class DocCollator {
	private final Map&lt;Element, String&gt; nameCache = new LinkedHashMap&lt;&gt;();
	private SimpleElementVisitor9&lt;String, Void&gt; snvisitor = null;
	private DocCollator tertiaryCollator = null;
	public final BaseConfiguration configuration;
	private DocCollator secondaryCollator = null;

	private DocCollator(Locale locale, int strength) {
	    instance = Collator.getInstance(locale);
	    instance.setStrength(strength);

	    keys = new LinkedHashMap&lt;String, CollationKey&gt;(MAX_SIZE + 1, 0.75f, true) {
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry&lt;String, CollationKey&gt; eldest) {
		    return size() &gt; MAX_SIZE;
		}
	    };
	}

	public int compare(String s1, String s2) {
	    return getKey(s1).compareTo(getKey(s2));
	}

	CollationKey getKey(String s) {
	    return keys.computeIfAbsent(s, instance::getCollationKey);
	}

    }

}

