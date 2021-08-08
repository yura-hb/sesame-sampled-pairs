import java.util.*;
import java.util.function.Function;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;

class NameLookup implements SuffixConstants {
    /** Internal utility, which is able to answer explicit and automatic modules. */
    static IModuleDescription getModuleDescription(IPackageFragmentRoot root,
	    Map&lt;IPackageFragmentRoot, IModuleDescription&gt; cache,
	    Function&lt;IPackageFragmentRoot, IClasspathEntry&gt; rootToEntry) {
	IModuleDescription module = cache.get(root);
	if (module != null)
	    return module != NO_MODULE ? module : null;
	try {
	    if (root.getKind() == IPackageFragmentRoot.K_SOURCE)
		module = root.getJavaProject().getModuleDescription(); // from any root in this project
	    else
		module = root.getModuleDescription();
	} catch (JavaModelException e) {
	    cache.put(root, NO_MODULE);
	    return null;
	}
	if (module == null) {
	    // 2nd attempt: try automatic module:
	    IClasspathEntry classpathEntry = rootToEntry.apply(root);
	    if (classpathEntry instanceof ClasspathEntry) {
		if (((ClasspathEntry) classpathEntry).isModular()) {
		    // modular but no module-info implies this is an automatic module
		    module = ((PackageFragmentRoot) root).getAutomaticModuleDescription(classpathEntry);
		}
	    }
	}
	cache.put(root, module != null ? module : NO_MODULE);
	return module;
    }

    private static IModuleDescription NO_MODULE = new SourceModule(null, "Not a module") {
	/* empty */ };

}

