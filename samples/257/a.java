class MindMapHookFactory extends HookFactoryAdapter {
    /**
     * Do not call this method directly. Call ModeController.createNodeHook
     * instead.
     */
    public NodeHook createNodeHook(String hookName) {
	logger.finest("CreateNodeHook: " + hookName);
	HookDescriptorPluginAction descriptor = getHookDescriptor(hookName);
	return (NodeHook) createJavaHook(hookName, descriptor);
    }

    protected static java.util.logging.Logger logger = null;
    private static HashMap&lt;String, HookDescriptorPluginAction&gt; pluginInfo = null;

    /**
     */
    private HookDescriptorPluginAction getHookDescriptor(String hookName) {
	HookDescriptorPluginAction descriptor = (HookDescriptorPluginAction) pluginInfo.get(hookName);
	if (hookName == null || descriptor == null)
	    throw new IllegalArgumentException("Unknown hook name " + hookName);
	return descriptor;
    }

    private MindMapHook createJavaHook(String hookName, HookDescriptorPluginAction descriptor) {
	try {
	    // constructed.
	    ClassLoader loader = descriptor.getPluginClassLoader();
	    Class hookClass = Class.forName(descriptor.getClassName(), true, loader);
	    MindMapHook hook = (MindMapHook) hookClass.newInstance();
	    decorateHook(hookName, descriptor, hook);
	    return hook;
	} catch (Throwable e) {
	    String path = "";
	    for (PluginClasspath plPath : descriptor.getPluginClasspath()) {
		path += plPath.getJar() + ";";
	    }
	    freemind.main.Resources.getInstance().logException(e, "Error occurred loading hook: "
		    + descriptor.getClassName() + "\nClasspath: " + path + "\nException:");
	    return null;
	}
    }

    private void decorateHook(String hookName, final HookDescriptorPluginAction descriptor, MindMapHook hook) {
	hook.setProperties(descriptor.getProperties());
	hook.setName(hookName);
	PluginBaseClassSearcher pluginBaseClassSearcher = new PluginBaseClassSearcher() {

	    public Object getPluginBaseObject() {
		return getPluginBaseClass(descriptor);
	    }
	};
	hook.setPluginBaseClass(pluginBaseClassSearcher);
    }

    /**
     * A plugin base class is a common registration class of multiple plugins.
     * It is useful to embrace several related plugins (example: EncryptedNote
     * -&gt; Registration).
     * 
     * @return the base class if declared and successfully instanciated or NULL.
     */
    public Object getPluginBaseClass(String hookName) {
	logger.finest("getPluginBaseClass: " + hookName);
	HookDescriptorPluginAction descriptor = getHookDescriptor(hookName);
	return getPluginBaseClass(descriptor);
    }

}

