import java.awt.peer.TaskbarPeer;

class Taskbar {
    /**
     * Changes this application's icon to the provided image.
     *
     * @param image to change
     * @throws SecurityException if a security manager exists and it denies the
     * {@code RuntimePermission("canProcessApplicationEvents")} permission.
     * @throws UnsupportedOperationException if the current platform
     * does not support the {@link Taskbar.Feature#ICON_IMAGE} feature
     */
    public void setIconImage(final Image image) {
	checkEventsProcessingPermission();
	checkFeatureSupport(Feature.ICON_IMAGE);
	peer.setIconImage(image);
    }

    private TaskbarPeer peer;

    /**
     *  Calls to the security manager's {@code checkPermission} method with
     *  an {@code RuntimePermission("canProcessApplicationEvents")} permissions.
     */
    private void checkEventsProcessingPermission() {
	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    sm.checkPermission(new RuntimePermission("canProcessApplicationEvents"));
	}
    }

    /**
     * Checks if the feature type is supported.
     *
     * @param featureType the action type in question
     * @throws UnsupportedOperationException if the specified action type is not
     *         supported on the current platform
     */
    private void checkFeatureSupport(Feature featureType) {
	if (!isSupported(featureType)) {
	    throw new UnsupportedOperationException(
		    "The " + featureType.name() + " feature is not supported on the current platform!");
	}
    }

    /**
     * Tests whether a {@code Feature} is supported on the current platform.
     * @param feature the specified {@link Feature}
     * @return true if the specified feature is supported on the current platform
     */
    public boolean isSupported(Feature feature) {
	return peer.isSupported(feature);
    }

}

