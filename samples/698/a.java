import java.util.ArrayList;
import java.util.List;
import com.puppycrawl.tools.checkstyle.api.Configuration;

abstract class AbstractModuleTestSupport extends AbstractPathTestSupport {
    /**
     * Returns a list of all {@link Configuration} instances for the given module name.
     * @param moduleName module name.
     * @return {@link Configuration} instance for the given module name.
     */
    protected static List&lt;Configuration&gt; getModuleConfigs(String moduleName) {
	final List&lt;Configuration&gt; result = new ArrayList&lt;&gt;();
	for (Configuration currentConfig : CONFIGURATION.getChildren()) {
	    if ("TreeWalker".equals(currentConfig.getName())) {
		for (Configuration moduleConfig : currentConfig.getChildren()) {
		    if (moduleName.equals(moduleConfig.getName())) {
			result.add(moduleConfig);
		    }
		}
	    } else if (moduleName.equals(currentConfig.getName())) {
		result.add(currentConfig);
	    }
	}
	return result;
    }

    private static final Configuration CONFIGURATION;

}

