import static jdk.vm.ci.hotspot.HotSpotVMConfig.config;
import static jdk.vm.ci.hotspot.UnsafeAccess.UNSAFE;
import jdk.vm.ci.meta.JavaKind;

class HotSpotResolvedObjectTypeImpl extends HotSpotResolvedJavaType
	implements HotSpotResolvedObjectType, MetaspaceWrapperObject {
    /**
     * Gets the metaspace Klass for this type.
     */
    long getMetaspaceKlass() {
	if (HotSpotJVMCIRuntime.getHostWordKind() == JavaKind.Long) {
	    return UNSAFE.getLong(javaClass, config().klassOffset);
	}
	return UNSAFE.getInt(javaClass, config().klassOffset) & 0xFFFFFFFFL;
    }

    /**
     * The Java class this type represents.
     */
    private final Class&lt;?&gt; javaClass;

}

