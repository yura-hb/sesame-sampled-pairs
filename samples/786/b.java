import java.io.File;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jdk.tools.jlink.internal.plugins.ExcludeJmodSectionPlugin;
import jdk.tools.jlink.plugin.Plugin;
import jdk.tools.jlink.plugin.PluginException;
import jdk.tools.jlink.internal.plugins.DefaultCompressPlugin;
import jdk.internal.module.ModulePath;

class TaskHelper {
    class OptionsHelper&lt;T&gt; {
	/**
	 * Handles all options.  This method stops processing the argument
	 * at the first non-option argument i.e. not starts with `-`, or
	 * at the first terminal option and returns the remaining arguments,
	 * if any.
	 */
	public List&lt;String&gt; handleOptions(T task, String[] args) throws BadArgs {
	    // findbugs warning, copy instead of keeping a reference.
	    command = Arrays.copyOf(args, args.length);

	    // Must extract it prior to do any option analysis.
	    // Required to interpret custom plugin options.
	    // Unit tests can call Task multiple time in same JVM.
	    pluginOptions = new PluginsHelper(null);

	    // process options
	    for (int i = 0; i &lt; args.length; i++) {
		if (args[i].startsWith("-")) {
		    String name = args[i];
		    PluginOption pluginOption = null;
		    Option&lt;T&gt; option = getOption(name);
		    if (option == null) {
			pluginOption = pluginOptions.getOption(name);
			if (pluginOption == null) {
			    throw new BadArgs("err.unknown.option", name).showUsage(true);
			}
		    }
		    Option&lt;?&gt; opt = pluginOption == null ? option : pluginOption;
		    String param = null;
		    if (opt.hasArg) {
			if (name.startsWith("--") && name.indexOf('=') &gt; 0) {
			    param = name.substring(name.indexOf('=') + 1, name.length());
			} else if (i + 1 &lt; args.length) {
			    param = args[++i];
			}
			if (param == null || param.isEmpty()
				|| (param.length() &gt;= 2 && param.charAt(0) == '-' && param.charAt(1) == '-')) {
			    throw new BadArgs("err.missing.arg", name).showUsage(true);
			}
		    }
		    if (pluginOption != null) {
			pluginOption.process(pluginOptions, name, param);
		    } else {
			option.process(task, name, param);
			if (option.isTerminal()) {
			    return ++i &lt; args.length
				    ? Stream.of(Arrays.copyOfRange(args, i, args.length)).collect(Collectors.toList())
				    : Collections.emptyList();

			}
		    }
		    if (opt.ignoreRest()) {
			i = args.length;
		    }
		} else {
		    return Stream.of(Arrays.copyOfRange(args, i, args.length)).collect(Collectors.toList());
		}
	    }
	    return Collections.emptyList();
	}

	private String[] command;
	private final List&lt;Option&lt;T&gt;&gt; options;

	private Option&lt;T&gt; getOption(String name) {
	    for (Option&lt;T&gt; o : options) {
		if (o.matches(name)) {
		    return o;
		}
	    }
	    return null;
	}

    }

    private PluginsHelper pluginOptions;
    private final ResourceBundleHelper bundleHelper;

    static ModuleLayer createPluginsLayer(List&lt;Path&gt; paths) {

	Path[] dirs = paths.toArray(new Path[0]);
	ModuleFinder finder = ModulePath.of(Runtime.version(), true, dirs);
	Configuration bootConfiguration = ModuleLayer.boot().configuration();
	try {
	    Configuration cf = bootConfiguration.resolveAndBind(ModuleFinder.of(), finder, Collections.emptySet());
	    ClassLoader scl = ClassLoader.getSystemClassLoader();
	    return ModuleLayer.boot().defineModulesWithOneLoader(cf, scl);
	} catch (Exception ex) {
	    // Malformed plugin modules (e.g.: same package in multiple modules).
	    throw new PluginException("Invalid modules in the plugins path: " + ex);
	}
    }

    public BadArgs newBadArgs(String key, Object... args) {
	return new BadArgs(key, args);
    }

    class PluginsHelper {
	private PluginsHelper pluginOptions;
	private final ResourceBundleHelper bundleHelper;

	private PluginsHelper(String pp) throws BadArgs {

	    if (pp != null) {
		String[] dirs = pp.split(File.pathSeparator);
		List&lt;Path&gt; paths = new ArrayList&lt;&gt;(dirs.length);
		for (String dir : dirs) {
		    paths.add(Paths.get(dir));
		}

		pluginsLayer = createPluginsLayer(paths);
	    }

	    plugins = PluginRepository.getPlugins(pluginsLayer);

	    Set&lt;String&gt; optionsSeen = new HashSet&lt;&gt;();
	    for (Plugin plugin : plugins) {
		if (!Utils.isDisabled(plugin)) {
		    addOrderedPluginOptions(plugin, optionsSeen);
		}
	    }
	    mainOptions.add(new PluginOption(true, (task, opt, arg) -&gt; {
		for (Plugin plugin : plugins) {
		    if (plugin.getName().equals(arg)) {
			pluginToMaps.remove(plugin);
			return;
		    }
		}
		throw newBadArgs("err.no.such.plugin", arg);
	    }, false, "--disable-plugin"));
	    mainOptions.add(new PluginOption(true, (task, opt, arg) -&gt; {
		Path path = Paths.get(arg);
		if (!Files.exists(path) || !Files.isDirectory(path)) {
		    throw newBadArgs("err.image.must.exist", path);
		}
		existingImage = path.toAbsolutePath();
	    }, true, "--post-process-path"));
	    mainOptions.add(new PluginOption(true, (task, opt, arg) -&gt; {
		lastSorter = arg;
	    }, true, "--resources-last-sorter"));
	    mainOptions.add(new PluginOption(false, (task, opt, arg) -&gt; {
		listPlugins = true;
	    }, false, "--list-plugins"));
	}

	private PluginOption getOption(String name) throws BadArgs {
	    for (PluginOption o : pluginsOptions) {
		if (o.matches(name)) {
		    return o;
		}
	    }
	    for (PluginOption o : mainOptions) {
		if (o.matches(name)) {
		    return o;
		}
	    }
	    return null;
	}

	private void addOrderedPluginOptions(Plugin plugin, Set&lt;String&gt; optionsSeen) throws BadArgs {
	    String option = plugin.getOption();
	    if (option == null) {
		return;
	    }

	    // make sure that more than one plugin does not use the same option!
	    if (optionsSeen.contains(option)) {
		throw new BadArgs("err.plugin.mutiple.options", option);
	    }
	    optionsSeen.add(option);

	    PluginOption plugOption = new PluginOption(plugin.hasArguments(), (task, opt, arg) -&gt; {
		if (!Utils.isFunctional(plugin)) {
		    throw newBadArgs("err.provider.not.functional", option);
		}

		if (!plugin.hasArguments()) {
		    addEmptyArgumentMap(plugin);
		    return;
		}

		Map&lt;String, String&gt; m = addArgumentMap(plugin);
		// handle one or more arguments
		if (arg.indexOf(':') == -1) {
		    // single argument case
		    m.put(option, arg);
		} else {
		    // This option can accept more than one arguments
		    // like --option_name=arg_value:arg2=value2:arg3=value3

		    // ":" followed by word char condition takes care of args that
		    // like Windows absolute paths "C:\foo", "C:/foo" [cygwin] etc.
		    // This enforces that key names start with a word character.
		    String[] args = arg.split(":(?=\\w)", -1);
		    String firstArg = args[0];
		    if (firstArg.isEmpty()) {
			throw newBadArgs("err.provider.additional.arg.error", option, arg);
		    }
		    m.put(option, firstArg);
		    // process the additional arguments
		    for (int i = 1; i &lt; args.length; i++) {
			String addArg = args[i];
			int eqIdx = addArg.indexOf('=');
			if (eqIdx == -1) {
			    throw newBadArgs("err.provider.additional.arg.error", option, arg);
			}

			String addArgName = addArg.substring(0, eqIdx);
			String addArgValue = addArg.substring(eqIdx + 1);
			if (addArgName.isEmpty() || addArgValue.isEmpty()) {
			    throw newBadArgs("err.provider.additional.arg.error", option, arg);
			}
			m.put(addArgName, addArgValue);
		    }
		}
	    }, false, "--" + option);
	    pluginsOptions.add(plugOption);

	    if (Utils.isFunctional(plugin)) {
		if (Utils.isAutoEnabled(plugin)) {
		    addEmptyArgumentMap(plugin);
		}

		if (plugin instanceof DefaultCompressPlugin) {
		    plugOption = new PluginOption(false, (task, opt, arg) -&gt; {
			Map&lt;String, String&gt; m = addArgumentMap(plugin);
			m.put(DefaultCompressPlugin.NAME, DefaultCompressPlugin.LEVEL_2);
		    }, false, "--compress", "-c");
		    mainOptions.add(plugOption);
		} else if (plugin instanceof StripDebugPlugin) {
		    plugOption = new PluginOption(false, (task, opt, arg) -&gt; {
			addArgumentMap(plugin);
		    }, false, "--strip-debug", "-G");
		    mainOptions.add(plugOption);
		} else if (plugin instanceof ExcludeJmodSectionPlugin) {
		    plugOption = new PluginOption(false, (task, opt, arg) -&gt; {
			Map&lt;String, String&gt; m = addArgumentMap(plugin);
			m.put(ExcludeJmodSectionPlugin.NAME, ExcludeJmodSectionPlugin.MAN_PAGES);
		    }, false, "--no-man-pages");
		    mainOptions.add(plugOption);

		    plugOption = new PluginOption(false, (task, opt, arg) -&gt; {
			Map&lt;String, String&gt; m = addArgumentMap(plugin);
			m.put(ExcludeJmodSectionPlugin.NAME, ExcludeJmodSectionPlugin.INCLUDE_HEADER_FILES);
		    }, false, "--no-header-files");
		    mainOptions.add(plugOption);
		}
	    }
	}

	private void addEmptyArgumentMap(Plugin plugin) {
	    argListFor(plugin).add(Collections.emptyMap());
	}

	private Map&lt;String, String&gt; addArgumentMap(Plugin plugin) {
	    Map&lt;String, String&gt; map = new HashMap&lt;&gt;();
	    argListFor(plugin).add(map);
	    return map;
	}

	private List&lt;Map&lt;String, String&gt;&gt; argListFor(Plugin plugin) {
	    List&lt;Map&lt;String, String&gt;&gt; mapList = pluginToMaps.get(plugin);
	    if (mapList == null) {
		mapList = new ArrayList&lt;&gt;();
		pluginToMaps.put(plugin, mapList);
	    }
	    return mapList;
	}

    }

    class BadArgs extends Exception {
	private PluginsHelper pluginOptions;
	private final ResourceBundleHelper bundleHelper;

	private BadArgs(String key, Object... args) {
	    super(bundleHelper.getMessage(key, args));
	    this.key = key;
	    this.args = args;
	}

	public BadArgs showUsage(boolean b) {
	    showUsage = b;
	    return this;
	}

    }

    class Option&lt;T&gt; implements Comparable&lt;T&gt; {
	private PluginsHelper pluginOptions;
	private final ResourceBundleHelper bundleHelper;

	void process(T task, String opt, String arg) throws BadArgs {
	    processing.process(task, opt, arg);
	}

	public boolean isTerminal() {
	    return terminalOption;
	}

	public boolean ignoreRest() {
	    return false;
	}

	public boolean matches(String opt) {
	    return opt.equals(name) || opt.equals(shortname) || opt.equals(shortname2)
		    || hasArg && opt.startsWith("--") && opt.startsWith(name + "=");
	}

	public Option(boolean hasArg, Processing&lt;T&gt; processing, boolean hidden, String name, String shortname,
		boolean isTerminal) {
	    this(hasArg, processing, false, name, shortname, "", isTerminal);
	}

	public Option(boolean hasArg, Processing&lt;T&gt; processing, boolean hidden, String name, String shortname,
		String shortname2, boolean isTerminal) {
	    if (!name.startsWith("--")) {
		throw new RuntimeException("option name missing --, " + name);
	    }
	    if (!shortname.isEmpty() && !shortname.startsWith("-")) {
		throw new RuntimeException("short name missing -, " + shortname);
	    }

	    this.hasArg = hasArg;
	    this.processing = processing;
	    this.hidden = hidden;
	    this.name = name;
	    this.shortname = shortname;
	    this.shortname2 = shortname2;
	    this.terminalOption = isTerminal;
	}

	interface Processing&lt;T&gt; {
	    final boolean hasArg;
	    final Processing&lt;T&gt; processing;
	    final boolean terminalOption;
	    final String name;
	    final String shortname;
	    final String shortname2;
	    final boolean hidden;

	    void process(T task, String opt, String arg) throws BadArgs;

	}

    }

    class PluginOption extends Option&lt;PluginsHelper&gt; {
	private PluginsHelper pluginOptions;
	private final ResourceBundleHelper bundleHelper;

	public PluginOption(boolean hasArg, Processing&lt;PluginsHelper&gt; processing, boolean hidden, String name) {
	    super(hasArg, processing, hidden, name, "", false);
	}

	public PluginOption(boolean hasArg, Processing&lt;PluginsHelper&gt; processing, boolean hidden, String name,
		String shortname) {
	    super(hasArg, processing, hidden, name, shortname, false);
	}

    }

    class ResourceBundleHelper {
	private PluginsHelper pluginOptions;
	private final ResourceBundleHelper bundleHelper;

	String getMessage(String key, Object... args) {
	    String val;
	    try {
		val = bundle.getString(key);
	    } catch (MissingResourceException e) {
		// XXX OK, check in plugin bundle
		val = pluginBundle.getString(key);
	    }
	    return MessageFormat.format(val, args);
	}

    }

}

