import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class BuildState {
    /**
     * Calculate the package dependents (ie the reverse of the dependencies).
     */
    public void calculateDependents() {
	dependents = new HashMap&lt;&gt;();

	for (String s : packages.keySet()) {
	    Package p = packages.get(s);

	    // Collect all dependencies of the classes in this package
	    Set&lt;String&gt; deps = p.typeDependencies() // maps fqName -&gt; set of dependencies
		    .values().stream().reduce(Collections.emptySet(), Util::union);

	    // Now reverse the direction

	    for (String dep : deps) {
		// Add the dependent information to the global dependent map.
		String depPkgStr = ":" + dep.substring(0, dep.lastIndexOf('.'));
		dependents.merge(depPkgStr, Collections.singleton(s), Util::union);

		// Also add the dependent information to the package specific map.
		// Normally, you do not compile java.lang et al. Therefore
		// there are several packages that p depends upon that you
		// do not have in your state database. This is perfectly fine.
		Package dp = packages.get(depPkgStr);
		if (dp != null) {
		    // But this package did exist in the state database.
		    dp.addDependent(p.name());
		}
	    }
	}
    }

    private Map&lt;String, Set&lt;String&gt;&gt; dependents = new HashMap&lt;&gt;();
    private Map&lt;String, Package&gt; packages = new HashMap&lt;&gt;();

}

