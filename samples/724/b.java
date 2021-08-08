import java.lang.module.Configuration;
import java.lang.module.ResolvedModule;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jdk.internal.loader.ClassLoaders;

class ModuleLoaderMap {
    /**
     * Returns the function to map modules in the given configuration to the
     * built-in class loaders.
     */
    static Function&lt;String, ClassLoader&gt; mappingFunction(Configuration cf) {
	Set&lt;String&gt; bootModules = bootModules();
	Set&lt;String&gt; platformModules = platformModules();

	ClassLoader platformClassLoader = ClassLoaders.platformClassLoader();
	ClassLoader appClassLoader = ClassLoaders.appClassLoader();

	Map&lt;String, ClassLoader&gt; map = new HashMap&lt;&gt;();
	for (ResolvedModule resolvedModule : cf.modules()) {
	    String mn = resolvedModule.name();
	    if (!bootModules.contains(mn)) {
		if (platformModules.contains(mn)) {
		    map.put(mn, platformClassLoader);
		} else {
		    map.put(mn, appClassLoader);
		}
	    }
	}
	return new Mapper(map);
    }

    /**
     * Returns the names of the modules defined to the boot loader.
     */
    public static Set&lt;String&gt; bootModules() {
	// The list of boot modules generated at build time.
	String[] BOOT_MODULES = new String[] { "@@BOOT_MODULE_NAMES@@" };
	Set&lt;String&gt; bootModules = new HashSet&lt;&gt;(BOOT_MODULES.length);
	for (String mn : BOOT_MODULES) {
	    bootModules.add(mn);
	}
	return bootModules;
    }

    /**
     * Returns the names of the modules defined to the platform loader.
     */
    public static Set&lt;String&gt; platformModules() {
	// The list of platform modules generated at build time.
	String[] PLATFORM_MODULES = new String[] { "@@PLATFORM_MODULE_NAMES@@" };
	Set&lt;String&gt; platformModules = new HashSet&lt;&gt;(PLATFORM_MODULES.length);
	for (String mn : PLATFORM_MODULES) {
	    platformModules.add(mn);
	}
	return platformModules;
    }

    class Mapper implements Function&lt;String, ClassLoader&gt; {
	Mapper(Map&lt;String, ClassLoader&gt; map) {
	    this.map = map; // defensive copy not needed
	}

    }

}

