import static com.sun.tools.jdeps.Graph.*;
import static com.sun.tools.jdeps.Module.*;
import static java.lang.module.ModuleDescriptor.Requires.Modifier.*;
import java.lang.module.ModuleDescriptor;
import java.util.Set;
import java.util.stream.Stream;

class ModuleAnalyzer {
    class ModuleDeps {
	/**
	 * Apply the transitive reduction on the module graph
	 * and returns the corresponding ModuleDescriptor
	 */
	ModuleDescriptor reduced() {
	    Graph&lt;Module&gt; g = buildReducedGraph();
	    return descriptor(requiresTransitive, g.adjacentNodes(root));
	}

	Set&lt;Module&gt; requiresTransitive;
	final Module root;
	Set&lt;Module&gt; requires;

	private Graph&lt;Module&gt; buildReducedGraph() {
	    ModuleGraphBuilder rpBuilder = new ModuleGraphBuilder(configuration);
	    rpBuilder.addModule(root);
	    requiresTransitive.stream().forEach(m -&gt; rpBuilder.addEdge(root, m));

	    // requires transitive graph
	    Graph&lt;Module&gt; rbg = rpBuilder.build().reduce();

	    ModuleGraphBuilder gb = new ModuleGraphBuilder(configuration);
	    gb.addModule(root);
	    requires.stream().forEach(m -&gt; gb.addEdge(root, m));

	    // transitive reduction
	    Graph&lt;Module&gt; newGraph = gb.buildGraph().reduce(rbg);
	    if (DEBUG) {
		System.err.println("after transitive reduction: ");
		newGraph.printGraph(log);
	    }
	    return newGraph;
	}

	private ModuleDescriptor descriptor(Set&lt;Module&gt; requiresTransitive, Set&lt;Module&gt; requires) {

	    ModuleDescriptor.Builder builder = ModuleDescriptor.newModule(root.name());

	    if (!root.name().equals(JAVA_BASE))
		builder.requires(Set.of(MANDATED), JAVA_BASE);

	    requiresTransitive.stream().filter(m -&gt; !m.name().equals(JAVA_BASE)).map(Module::name)
		    .forEach(mn -&gt; builder.requires(Set.of(TRANSITIVE), mn));

	    requires.stream().filter(m -&gt; !requiresTransitive.contains(m)).filter(m -&gt; !m.name().equals(JAVA_BASE))
		    .map(Module::name).forEach(mn -&gt; builder.requires(mn));

	    return builder.build();
	}

    }

    private final JdepsConfiguration configuration;
    private final PrintWriter log;
    private static final String JAVA_BASE = "java.base";

}

