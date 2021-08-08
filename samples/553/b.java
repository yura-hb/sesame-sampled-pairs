import java.io.File;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer.FactoryType;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath.Attributes;
import org.eclipse.jdt.apt.core.internal.util.FactoryPathUtil;
import org.eclipse.jdt.core.IJavaProject;

class AnnotationProcessorFactoryLoader {
    /**
     * Convenience method: get the key set of the map returned by
     * @see #getJava5FactoriesAndAttributesForProject(IJavaProject) as a List.
     */
    public List&lt;AnnotationProcessorFactory&gt; getJava5FactoriesForProject(IJavaProject jproj) {
	Map&lt;AnnotationProcessorFactory, FactoryPath.Attributes&gt; factoriesAndAttrs = getJava5FactoriesAndAttributesForProject(
		jproj);
	final List&lt;AnnotationProcessorFactory&gt; factories = new ArrayList&lt;&gt;(factoriesAndAttrs.keySet());
	return Collections.unmodifiableList(factories);
    }

    private static final Object cacheMutex = new Object();
    private final Map&lt;IJavaProject, Map&lt;AnnotationProcessorFactory, FactoryPath.Attributes&gt;&gt; _project2Java5Factories = new HashMap&lt;&gt;();
    private final Map&lt;IJavaProject, ClassLoader&gt; _iterativeLoaders = new HashMap&lt;&gt;();
    private final Map&lt;IJavaProject, Map&lt;IServiceFactory, FactoryPath.Attributes&gt;&gt; _project2Java6Factories = new HashMap&lt;&gt;();
    private final Map&lt;IJavaProject, ClassLoader&gt; _batchLoaders = new HashMap&lt;&gt;();
    private final Map&lt;String, Set&lt;IJavaProject&gt;&gt; _container2Project = new HashMap&lt;&gt;();

    /**
     * @param jproj must not be null
     * @return order preserving map of annotation processor factories to their attributes.
     * The order of the annotation processor factories respects the order of factory 
     * containers in &lt;code&gt;jproj&lt;/code&gt;.  The map is unmodifiable, and may be empty but 
     * will not be null.
     */
    public Map&lt;AnnotationProcessorFactory, FactoryPath.Attributes&gt; getJava5FactoriesAndAttributesForProject(
	    IJavaProject jproj) {

	// We can't create problem markers inside synchronization -- see https://bugs.eclipse.org/bugs/show_bug.cgi?id=184923
	LoadFailureHandler failureHandler = new LoadFailureHandler(jproj);

	Map&lt;AnnotationProcessorFactory, FactoryPath.Attributes&gt; factories;
	synchronized (cacheMutex) {
	    factories = _project2Java5Factories.get(jproj);
	}

	if (factories == null) {
	    // Load the project
	    FactoryPath fp = FactoryPathUtil.getFactoryPath(jproj);
	    Map&lt;FactoryContainer, FactoryPath.Attributes&gt; containers = fp.getEnabledContainers();
	    loadFactories(containers, jproj, failureHandler);

	    failureHandler.reportFailureMarkers();

	    synchronized (cacheMutex) {
		factories = _project2Java5Factories.get(jproj);
	    }
	}

	if (factories != null) {
	    return Collections.unmodifiableMap(factories);
	} else {
	    return Collections.emptyMap();
	}
    }

    /**
     * Load all Java 5 and Java 6 processors on the factory path.  This also resets the
     * APT-related build problem markers.  Results are saved in the factory caches.
     * @param containers an ordered map.
     */
    private void loadFactories(Map&lt;FactoryContainer, FactoryPath.Attributes&gt; containers, IJavaProject project,
	    LoadFailureHandler failureHandler) {
	Map&lt;AnnotationProcessorFactory, FactoryPath.Attributes&gt; java5Factories = new LinkedHashMap&lt;&gt;();
	Map&lt;IServiceFactory, FactoryPath.Attributes&gt; java6Factories = new LinkedHashMap&lt;&gt;();

	removeAptBuildProblemMarkers(project);
	Set&lt;FactoryContainer&gt; badContainers = verifyFactoryPath(project);
	if (badContainers != null) {
	    for (FactoryContainer badFC : badContainers) {
		failureHandler.addFailedFactory(badFC.getId());
		containers.remove(badFC);
	    }
	}

	// Need to use the cached classloader if we have one
	ClassLoader iterativeClassLoader;
	synchronized (cacheMutex) {
	    iterativeClassLoader = _iterativeLoaders.get(project);
	    if (iterativeClassLoader == null) {
		iterativeClassLoader = _createIterativeClassLoader(containers);
		_iterativeLoaders.put(project, iterativeClassLoader);
	    }
	}

	ClassLoader batchClassLoader = _createBatchClassLoader(containers, project);

	for (Map.Entry&lt;FactoryContainer, FactoryPath.Attributes&gt; entry : containers.entrySet()) {
	    try {
		final FactoryContainer fc = entry.getKey();
		final FactoryPath.Attributes attr = entry.getValue();
		assert !attr.runInBatchMode() || (batchClassLoader != null);
		ClassLoader cl = attr.runInBatchMode() ? batchClassLoader : iterativeClassLoader;

		// First the Java 5 factories in this container...
		List&lt;AnnotationProcessorFactory&gt; java5FactoriesInContainer;
		java5FactoriesInContainer = loadJava5FactoryClasses(fc, cl, project, failureHandler);
		for (AnnotationProcessorFactory apf : java5FactoriesInContainer) {
		    java5Factories.put(apf, entry.getValue());
		}

		if (AptPlugin.canRunJava6Processors()) {
		    // Now the Java 6 factories.  Use the same classloader for the sake of sanity.
		    List&lt;IServiceFactory&gt; java6FactoriesInContainer;
		    java6FactoriesInContainer = loadJava6FactoryClasses(fc, cl, project, failureHandler);
		    for (IServiceFactory isf : java6FactoriesInContainer) {
			java6Factories.put(isf, entry.getValue());
		    }
		}
	    } catch (FileNotFoundException fnfe) {
		// it would be bizarre to get this, given that we already checked for file existence up above.
		AptPlugin.log(fnfe, Messages.AnnotationProcessorFactoryLoader_jarNotFound + fnfe.getLocalizedMessage());
	    } catch (IOException ioe) {
		AptPlugin.log(ioe, Messages.AnnotationProcessorFactoryLoader_ioError + ioe.getLocalizedMessage());
	    }
	}

	synchronized (cacheMutex) {
	    _project2Java5Factories.put(project, java5Factories);
	    _project2Java6Factories.put(project, java6Factories);
	}
    }

    /**
     * Remove APT build problem markers, e.g., "missing factory jar".
     * @param jproj if null, remove markers from all projects that have
     * factory paths associated with them.
     */
    private void removeAptBuildProblemMarkers(IJavaProject jproj) {
	// note that _project2Java6Factories.keySet() should be same as that for Java5.
	Set&lt;IJavaProject&gt; jprojects;
	synchronized (cacheMutex) {
	    jprojects = (jproj == null) ? new HashSet&lt;&gt;(_project2Java5Factories.keySet())
		    : Collections.singleton(jproj);
	}
	try {
	    for (IJavaProject jp : jprojects) {
		if (jp.exists()) {
		    IProject p = jp.getProject();
		    IMarker[] markers = p.findMarkers(AptPlugin.APT_LOADER_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
		    if (markers != null) {
			for (IMarker marker : markers)
			    marker.delete();
		    }
		}
	    }
	} catch (CoreException e) {
	    AptPlugin.log(e, "Unable to delete APT build problem marker"); //$NON-NLS-1$
	}
    }

    /**
     * Check the factory path for a project and ensure that all the
     * containers it lists are available.  Adds jar factory container
     * resources to the _container2Project cache, whether or not the
     * resource can actually be found.
     * 
     * @param jproj the project, or null to check all projects that
     * are in the cache.
     * @return a Set of all invalid containers, or null if all containers
     * on the path were valid.
     */
    private Set&lt;FactoryContainer&gt; verifyFactoryPath(IJavaProject jproj) {
	Set&lt;FactoryContainer&gt; badContainers = null;
	FactoryPath fp = FactoryPathUtil.getFactoryPath(jproj);
	Map&lt;FactoryContainer, FactoryPath.Attributes&gt; containers = fp.getEnabledContainers();
	for (FactoryContainer fc : containers.keySet()) {
	    if (fc instanceof JarFactoryContainer) {
		try {
		    final File jarFile = ((JarFactoryContainer) fc).getJarFile();
		    // if null, will add to bad container set below.
		    if (jarFile != null) {
			String key = jarFile.getCanonicalPath();
			addToResourcesMap(key, jproj);
		    }
		} catch (IOException e) {
		    // If there's something this malformed on the factory path,
		    // don't bother putting it on the resources map; we'll never
		    // get notified about a change to it anyway.  It should get
		    // reported either as a bad container (below) or as a failure
		    // to load (later on).
		}
	    }
	    if (!fc.exists()) {
		if (badContainers == null) {
		    badContainers = new HashSet&lt;&gt;();
		}
		badContainers.add(fc);
	    }
	}
	return badContainers;
    }

    /**
     * @param containers an ordered map.
     */
    private static ClassLoader _createIterativeClassLoader(Map&lt;FactoryContainer, FactoryPath.Attributes&gt; containers) {
	ArrayList&lt;File&gt; fileList = new ArrayList&lt;&gt;(containers.size());
	for (Map.Entry&lt;FactoryContainer, FactoryPath.Attributes&gt; entry : containers.entrySet()) {
	    FactoryPath.Attributes attr = entry.getValue();
	    FactoryContainer fc = entry.getKey();
	    if (!attr.runInBatchMode() && fc instanceof JarFactoryContainer) {
		JarFactoryContainer jfc = (JarFactoryContainer) fc;
		fileList.add(jfc.getJarFile());
	    }
	}

	ClassLoader cl;
	if (fileList.size() &gt; 0) {
	    cl = createClassLoader(fileList, getParentClassLoader());
	} else {
	    cl = getParentClassLoader();
	}
	return cl;
    }

    /**
     * Returns the batch class loader or null if none
     */
    private ClassLoader _createBatchClassLoader(Map&lt;FactoryContainer, FactoryPath.Attributes&gt; containers,
	    IJavaProject p) {
	ArrayList&lt;File&gt; fileList = new ArrayList&lt;&gt;(containers.size());
	for (Map.Entry&lt;FactoryContainer, FactoryPath.Attributes&gt; entry : containers.entrySet()) {
	    FactoryPath.Attributes attr = entry.getValue();
	    FactoryContainer fc = entry.getKey();
	    if (attr.runInBatchMode() && fc instanceof JarFactoryContainer) {

		JarFactoryContainer jfc = (JarFactoryContainer) fc;
		File f = jfc.getJarFile();
		fileList.add(f);

	    }
	}

	ClassLoader result = null;
	// Try to use the iterative CL as parent, so we can resolve classes within it
	synchronized (cacheMutex) {
	    ClassLoader parentCL = _iterativeLoaders.get(p);
	    if (parentCL == null) {
		parentCL = getParentClassLoader();
	    }

	    if (fileList.size() &gt; 0) {
		result = createClassLoader(fileList, parentCL);
		_batchLoaders.put(p, result);
	    }
	}
	return result;
    }

    private List&lt;AnnotationProcessorFactory&gt; loadJava5FactoryClasses(FactoryContainer fc, ClassLoader classLoader,
	    IJavaProject jproj, LoadFailureHandler failureHandler) throws IOException {
	Map&lt;String, String&gt; factoryNames = fc.getFactoryNames();
	List&lt;AnnotationProcessorFactory&gt; factories = new ArrayList&lt;&gt;();
	for (Entry&lt;String, String&gt; entry : factoryNames.entrySet()) {
	    if (AptPlugin.JAVA5_FACTORY_NAME.equals(entry.getValue())) {
		String factoryName = entry.getKey();
		AnnotationProcessorFactory factory;
		if (fc.getType() == FactoryType.PLUGIN)
		    factory = FactoryPluginManager.getJava5FactoryFromPlugin(factoryName);
		else
		    factory = (AnnotationProcessorFactory) loadInstance(factoryName, classLoader, jproj,
			    failureHandler);

		if (factory != null)
		    factories.add(factory);
	    }
	}
	return factories;
    }

    private List&lt;IServiceFactory&gt; loadJava6FactoryClasses(FactoryContainer fc, ClassLoader classLoader,
	    IJavaProject jproj, LoadFailureHandler failureHandler) throws IOException {
	Map&lt;String, String&gt; factoryNames = fc.getFactoryNames();
	List&lt;IServiceFactory&gt; factories = new ArrayList&lt;&gt;();
	for (Entry&lt;String, String&gt; entry : factoryNames.entrySet()) {
	    if (AptPlugin.JAVA6_FACTORY_NAME.equals(entry.getValue())) {
		String factoryName = entry.getKey();
		IServiceFactory factory = null;
		if (fc.getType() == FactoryType.PLUGIN) {
		    factory = FactoryPluginManager.getJava6FactoryFromPlugin(factoryName);
		} else {
		    Class&lt;?&gt; clazz;
		    try {
			clazz = classLoader.loadClass(factoryName);
			factory = new ClassServiceFactory(clazz);
		    } catch (ClassNotFoundException | ClassFormatError e) {
			AptPlugin.trace("Unable to load annotation processor " + factoryName, e); //$NON-NLS-1$
			failureHandler.addFailedFactory(factoryName);
		    }
		}

		if (factory != null)
		    factories.add(factory);
	    }
	}
	return factories;
    }

    /**
     * Add the resource/project pair 'key' -&gt; 'jproj' to the 
     * _container2Project map.
     * @param key the canonicalized pathname of the resource
     * @param jproj must not be null
     */
    private void addToResourcesMap(String key, IJavaProject jproj) {
	synchronized (cacheMutex) {
	    Set&lt;IJavaProject&gt; s = _container2Project.get(key);
	    if (s == null) {
		s = new HashSet&lt;&gt;();
		_container2Project.put(key, s);
	    }
	    s.add(jproj);
	}
    }

    private static ClassLoader getParentClassLoader() {
	final ClassLoader loaderForComSunMirrorClasses = AnnotationProcessorFactoryLoader.class.getClassLoader();
	final ClassLoader loaderForEverythingElse = ClassLoader.getSystemClassLoader();
	return new ClassLoader() {
	    @Override
	    protected Class&lt;?&gt; findClass(String name) throws ClassNotFoundException {
		if (name.startsWith("com.sun.mirror.")) { //$NON-NLS-1$
		    if (name.startsWith("com.sun.mirror.apt") //$NON-NLS-1$
			    || name.startsWith("com.sun.mirror.declaration") //$NON-NLS-1$
			    || name.startsWith("com.sun.mirror.type") //$NON-NLS-1$
			    || name.startsWith("com.sun.mirror.util")) { //$NON-NLS-1$
			return loaderForComSunMirrorClasses.loadClass(name);
		    }
		}
		return loaderForEverythingElse.loadClass(name);
	    }
	};
    }

    private static ClassLoader createClassLoader(List&lt;File&gt; files, ClassLoader parentCL) {
	//return new JarClassLoader(files, parentCL);
	List&lt;URL&gt; urls = new ArrayList&lt;&gt;(files.size());
	for (int i = 0; i &lt; files.size(); i++) {
	    try {
		urls.add(files.get(i).toURI().toURL());
	    } catch (MalformedURLException mue) {
		// ignore
	    }
	}
	URL[] urlArray = urls.toArray(new URL[urls.size()]);
	return new URLClassLoader(urlArray, parentCL);
    }

    /**
     * Wrapper around ClassLoader.loadClass().newInstance() to handle reporting of errors.
     */
    private Object loadInstance(String factoryName, ClassLoader cl, IJavaProject jproj,
	    LoadFailureHandler failureHandler) {
	Object f = null;
	try {
	    Class&lt;?&gt; c = cl.loadClass(factoryName);
	    f = c.newInstance();
	} catch (Exception e) {
	    AptPlugin.trace("Failed to load factory " + factoryName, e); //$NON-NLS-1$
	    failureHandler.addFailedFactory(factoryName);
	} catch (NoClassDefFoundError ncdfe) {
	    AptPlugin.trace("Failed to load " + factoryName, ncdfe); //$NON-NLS-1$
	    failureHandler.addFailedFactory(factoryName);
	}
	return f;
    }

}

