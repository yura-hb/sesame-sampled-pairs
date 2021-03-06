import sun.java2d.SunGraphicsEnvironment;

abstract class GraphicsEnvironment {
    /**
     * Returns the Point where Windows should be centered.
     * It is recommended that centered Windows be checked to ensure they fit
     * within the available display area using getMaximumWindowBounds().
     * @return the point where Windows should be centered
     *
     * @exception HeadlessException if isHeadless() returns true
     * @see #getMaximumWindowBounds
     * @since 1.4
     */
    public Point getCenterPoint() throws HeadlessException {
	// Default implementation: return the center of the usable bounds of the
	// default screen device.
	Rectangle usableBounds = SunGraphicsEnvironment.getUsableBounds(getDefaultScreenDevice());
	return new Point((usableBounds.width / 2) + usableBounds.x, (usableBounds.height / 2) + usableBounds.y);
    }

    /**
     * Returns the default screen {@code GraphicsDevice}.
     * @return the {@code GraphicsDevice} that represents the
     * default screen device
     * @exception HeadlessException if isHeadless() returns true
     * @see #isHeadless()
     */
    public abstract GraphicsDevice getDefaultScreenDevice() throws HeadlessException;

}

