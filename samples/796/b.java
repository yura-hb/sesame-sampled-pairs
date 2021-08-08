import java.util.stream.Collectors;

abstract class InnerClassesTestBase extends TestResult {
    /**
     * Methods returns flags which must have type.
     *
     * @param type class, interface, enum or annotation
     * @param mods modifiers
     * @return set of access flags
     */
    protected Set&lt;String&gt; getFlags(ClassType type, List&lt;Modifier&gt; mods) {
	Set&lt;String&gt; flags = mods.stream().map(Modifier::getString).filter(str -&gt; !str.isEmpty())
		.map(str -&gt; "ACC_" + str.toUpperCase()).collect(Collectors.toSet());
	type.addSpecificFlags(flags);
	return flags;
    }

    class Modifier extends Enum&lt;Modifier&gt; {
	public String getString() {
	    return str;
	}

    }

    class ClassType extends Enum&lt;ClassType&gt; {
	public abstract void addSpecificFlags(Set&lt;String&gt; flags);

    }

}

