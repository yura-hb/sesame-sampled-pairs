import java.awt.peer.ComponentPeer;

abstract class Component implements ImageObserver, MenuContainer, Serializable {
    /**
     * Returns the component's preferred size.
     *
     * @return the component's preferred size
     * @deprecated As of JDK version 1.1,
     * replaced by {@code getPreferredSize()}.
     */
    @Deprecated
    public Dimension preferredSize() {
	/* Avoid grabbing the lock if a reasonable cached size value
	 * is available.
	 */
	Dimension dim = prefSize;
	if (dim == null || !(isPreferredSizeSet() || isValid())) {
	    synchronized (getTreeLock()) {
		prefSize = (peer != null) ? peer.getPreferredSize() : getMinimumSize();
		dim = prefSize;
	    }
	}
	return new Dimension(dim);
    }

    /**
     * Preferred size.
     * (This field perhaps should have been transient).
     *
     * @serial
     */
    Dimension prefSize;
    /**
     * The peer of the component. The peer implements the component's
     * behavior. The peer is set when the {@code Component} is
     * added to a container that also is a peer.
     * @see #addNotify
     * @see #removeNotify
     */
    transient volatile ComponentPeer peer;
    /**
     * Whether or not setPreferredSize has been invoked with a non-null value.
     */
    boolean prefSizeSet;
    /**
     * True when the object is valid. An invalid object needs to
     * be laid out. This flag is set to false when the object
     * size is changed.
     *
     * @serial
     * @see #isValid
     * @see #validate
     * @see #invalidate
     */
    private volatile boolean valid = false;
    /**
     * The locking object for AWT component-tree and layout operations.
     *
     * @see #getTreeLock
     */
    static final Object LOCK = new AWTTreeLock();
    /**
     * Minimum size.
     * (This field perhaps should have been transient).
     *
     * @serial
     */
    Dimension minSize;
    /**
     * Whether or not setMinimumSize has been invoked with a non-null value.
     */
    boolean minSizeSet;
    /**
     * The width of the component.
     *
     * @serial
     * @see #getSize
     */
    int width;
    /**
     * The height of the component.
     *
     * @serial
     * @see #getSize
     */
    int height;

    /**
     * Returns true if the preferred size has been set to a
     * non-{@code null} value otherwise returns false.
     *
     * @return true if {@code setPreferredSize} has been invoked
     *         with a non-null value.
     * @since 1.5
     */
    public boolean isPreferredSizeSet() {
	return prefSizeSet;
    }

    /**
     * Determines whether this component is valid. A component is valid
     * when it is correctly sized and positioned within its parent
     * container and all its children are also valid.
     * In order to account for peers' size requirements, components are invalidated
     * before they are first shown on the screen. By the time the parent container
     * is fully realized, all its components will be valid.
     * @return {@code true} if the component is valid, {@code false}
     * otherwise
     * @see #validate
     * @see #invalidate
     * @since 1.0
     */
    public boolean isValid() {
	return (peer != null) && valid;
    }

    /**
     * Gets this component's locking object (the object that owns the thread
     * synchronization monitor) for AWT component-tree and layout
     * operations.
     * @return this component's locking object
     */
    public final Object getTreeLock() {
	return LOCK;
    }

    /**
     * Gets the minimum size of this component.
     * @return a dimension object indicating this component's minimum size
     * @see #getPreferredSize
     * @see LayoutManager
     */
    public Dimension getMinimumSize() {
	return minimumSize();
    }

    /**
     * Returns the minimum size of this component.
     *
     * @return the minimum size of this component
     * @deprecated As of JDK version 1.1,
     * replaced by {@code getMinimumSize()}.
     */
    @Deprecated
    public Dimension minimumSize() {
	/* Avoid grabbing the lock if a reasonable cached size value
	 * is available.
	 */
	Dimension dim = minSize;
	if (dim == null || !(isMinimumSizeSet() || isValid())) {
	    synchronized (getTreeLock()) {
		minSize = (peer != null) ? peer.getMinimumSize() : size();
		dim = minSize;
	    }
	}
	return new Dimension(dim);
    }

    /**
     * Returns whether or not {@code setMinimumSize} has been
     * invoked with a non-null value.
     *
     * @return true if {@code setMinimumSize} has been invoked with a
     *              non-null value.
     * @since 1.5
     */
    public boolean isMinimumSizeSet() {
	return minSizeSet;
    }

    /**
     * Returns the size of this component in the form of a
     * {@code Dimension} object.
     *
     * @return the {@code Dimension} object that indicates the
     *         size of this component
     * @deprecated As of JDK version 1.1,
     * replaced by {@code getSize()}.
     */
    @Deprecated
    public Dimension size() {
	return new Dimension(width, height);
    }

}

