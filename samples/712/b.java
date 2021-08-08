import java.io.PrintStream;
import java.lang.module.ModuleDescriptor.Requires.Modifier;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import jdk.internal.module.ModuleReferenceImpl;
import jdk.internal.module.ModuleTarget;

class Resolver {
    /**
     * Resolves the given named modules.
     *
     * @throws ResolutionException
     */
    Resolver resolve(Collection&lt;String&gt; roots) {

	// create the visit stack to get us started
	Deque&lt;ModuleDescriptor&gt; q = new ArrayDeque&lt;&gt;();
	for (String root : roots) {

	    // find root module
	    ModuleReference mref = findWithBeforeFinder(root);
	    if (mref == null) {

		if (findInParent(root) != null) {
		    // in parent, nothing to do
		    continue;
		}

		mref = findWithAfterFinder(root);
		if (mref == null) {
		    findFail("Module %s not found", root);
		}
	    }

	    if (isTracing()) {
		trace("root %s", nameAndInfo(mref));
	    }

	    addFoundModule(mref);
	    q.push(mref.descriptor());
	}

	resolve(q);

	return this;
    }

    private final ModuleFinder beforeFinder;
    private final List&lt;Configuration&gt; parents;
    private final ModuleFinder afterFinder;
    private final PrintStream traceOutput;
    private final Map&lt;String, ModuleReference&gt; nameToReference = new HashMap&lt;&gt;();
    private boolean haveAllAutomaticModules;
    private String targetPlatform;

    /**
     * Invokes the beforeFinder to find method to find the given module.
     */
    private ModuleReference findWithBeforeFinder(String mn) {

	return beforeFinder.find(mn).orElse(null);

    }

    /**
     * Find a module of the given name in the parent configurations
     */
    private ResolvedModule findInParent(String mn) {
	for (Configuration parent : parents) {
	    Optional&lt;ResolvedModule&gt; om = parent.findModule(mn);
	    if (om.isPresent())
		return om.get();
	}
	return null;
    }

    /**
     * Invokes the afterFinder to find method to find the given module.
     */
    private ModuleReference findWithAfterFinder(String mn) {
	return afterFinder.find(mn).orElse(null);
    }

    /**
     * Throw FindException with the given format string and arguments
     */
    private static void findFail(String fmt, Object... args) {
	String msg = String.format(fmt, args);
	throw new FindException(msg);
    }

    /**
     * Tracing support
     */

    private boolean isTracing() {
	return traceOutput != null;
    }

    private String nameAndInfo(ModuleReference mref) {
	ModuleDescriptor descriptor = mref.descriptor();
	StringBuilder sb = new StringBuilder(descriptor.name());
	mref.location().ifPresent(uri -&gt; sb.append(" " + uri));
	if (descriptor.isAutomatic())
	    sb.append(" automatic");
	return sb.toString();
    }

    private void trace(String fmt, Object... args) {
	if (traceOutput != null) {
	    traceOutput.format(fmt, args);
	    traceOutput.println();
	}
    }

    /**
     * Add the module to the nameToReference map. Also check any constraints on
     * the target platform with the constraints of other modules.
     */
    private void addFoundModule(ModuleReference mref) {
	String mn = mref.descriptor().name();

	if (mref instanceof ModuleReferenceImpl) {
	    ModuleTarget target = ((ModuleReferenceImpl) mref).moduleTarget();
	    if (target != null)
		checkTargetPlatform(mn, target);
	}

	nameToReference.put(mn, mref);
    }

    /**
     * Resolve all modules in the given queue. On completion the queue will be
     * empty and any resolved modules will be added to {@code nameToReference}.
     *
     * @return The set of module resolved by this invocation of resolve
     */
    private Set&lt;ModuleDescriptor&gt; resolve(Deque&lt;ModuleDescriptor&gt; q) {
	Set&lt;ModuleDescriptor&gt; resolved = new HashSet&lt;&gt;();

	while (!q.isEmpty()) {
	    ModuleDescriptor descriptor = q.poll();
	    assert nameToReference.containsKey(descriptor.name());

	    // if the module is an automatic module then all automatic
	    // modules need to be resolved
	    if (descriptor.isAutomatic() && !haveAllAutomaticModules) {
		addFoundAutomaticModules().forEach(mref -&gt; {
		    ModuleDescriptor other = mref.descriptor();
		    q.offer(other);
		    if (isTracing()) {
			trace("%s requires %s", descriptor.name(), nameAndInfo(mref));
		    }
		});
		haveAllAutomaticModules = true;
	    }

	    // process dependences
	    for (ModuleDescriptor.Requires requires : descriptor.requires()) {

		// only required at compile-time
		if (requires.modifiers().contains(Modifier.STATIC))
		    continue;

		String dn = requires.name();

		// find dependence
		ModuleReference mref = findWithBeforeFinder(dn);
		if (mref == null) {

		    if (findInParent(dn) != null) {
			// dependence is in parent
			continue;
		    }

		    mref = findWithAfterFinder(dn);
		    if (mref == null) {
			findFail("Module %s not found, required by %s", dn, descriptor.name());
		    }
		}

		if (isTracing() && !dn.equals("java.base")) {
		    trace("%s requires %s", descriptor.name(), nameAndInfo(mref));
		}

		if (!nameToReference.containsKey(dn)) {
		    addFoundModule(mref);
		    q.offer(mref.descriptor());
		}

	    }

	    resolved.add(descriptor);
	}

	return resolved;
    }

    /**
     * Check that the module's constraints on the target platform does
     * conflict with the constraint of other modules resolved so far.
     */
    private void checkTargetPlatform(String mn, ModuleTarget target) {
	String value = target.targetPlatform();
	if (value != null) {
	    if (targetPlatform == null) {
		targetPlatform = value;
	    } else {
		if (!value.equals(targetPlatform)) {
		    findFail("Module %s has constraints on target platform (%s)"
			    + " that conflict with other modules: %s", mn, value, targetPlatform);
		}
	    }
	}
    }

    /**
     * Add all automatic modules that have not already been found to the
     * nameToReference map.
     */
    private Set&lt;ModuleReference&gt; addFoundAutomaticModules() {
	Set&lt;ModuleReference&gt; result = new HashSet&lt;&gt;();
	findAll().forEach(mref -&gt; {
	    String mn = mref.descriptor().name();
	    if (mref.descriptor().isAutomatic() && !nameToReference.containsKey(mn)) {
		addFoundModule(mref);
		result.add(mref);
	    }
	});
	return result;
    }

    /**
     * Returns the set of all modules that are observable with the before
     * and after ModuleFinders.
     */
    private Set&lt;ModuleReference&gt; findAll() {
	Set&lt;ModuleReference&gt; beforeModules = beforeFinder.findAll();
	Set&lt;ModuleReference&gt; afterModules = afterFinder.findAll();

	if (afterModules.isEmpty())
	    return beforeModules;

	if (beforeModules.isEmpty() && parents.size() == 1 && parents.get(0) == Configuration.empty())
	    return afterModules;

	Set&lt;ModuleReference&gt; result = new HashSet&lt;&gt;(beforeModules);
	for (ModuleReference mref : afterModules) {
	    String name = mref.descriptor().name();
	    if (!beforeFinder.find(name).isPresent() && findInParent(name) == null) {
		result.add(mref);
	    }
	}

	return result;
    }

}

