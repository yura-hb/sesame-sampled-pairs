class JavaElementDelta extends SimpleDelta implements IJavaElementDelta {
    /**
    * Returns a string representation of this delta's
    * structure suitable for debug purposes.
    *
    * @see #toString()
    */
    public String toDebugString(int depth) {
	StringBuffer buffer = new StringBuffer();
	for (int i = 0; i &lt; depth; i++) {
	    buffer.append('\t');
	}
	buffer.append(((JavaElement) getElement()).toDebugString());
	toDebugString(buffer);
	IJavaElementDelta[] children = getAffectedChildren();
	if (children != null) {
	    for (int i = 0; i &lt; children.length; ++i) {
		buffer.append("\n"); //$NON-NLS-1$
		buffer.append(((JavaElementDelta) children[i]).toDebugString(depth + 1));
	    }
	}
	for (int i = 0; i &lt; this.resourceDeltasCounter; i++) {
	    buffer.append("\n");//$NON-NLS-1$
	    for (int j = 0; j &lt; depth + 1; j++) {
		buffer.append('\t');
	    }
	    IResourceDelta resourceDelta = this.resourceDeltas[i];
	    buffer.append(resourceDelta.toString());
	    buffer.append("["); //$NON-NLS-1$
	    switch (resourceDelta.getKind()) {
	    case IResourceDelta.ADDED:
		buffer.append('+');
		break;
	    case IResourceDelta.REMOVED:
		buffer.append('-');
		break;
	    case IResourceDelta.CHANGED:
		buffer.append('*');
		break;
	    default:
		buffer.append('?');
		break;
	    }
	    buffer.append("]"); //$NON-NLS-1$
	}
	IJavaElementDelta[] annotations = getAnnotationDeltas();
	if (annotations != null) {
	    for (int i = 0; i &lt; annotations.length; ++i) {
		buffer.append("\n"); //$NON-NLS-1$
		buffer.append(((JavaElementDelta) annotations[i]).toDebugString(depth + 1));
	    }
	}
	return buffer.toString();
    }

    /**
     * Counter of resource deltas
     */
    int resourceDeltasCounter;
    /**
     * Collection of resource deltas that correspond to non java resources deltas.
     */
    IResourceDelta[] resourceDeltas = null;
    IJavaElement changedElement;
    /**
     * @see #getAffectedChildren()
     */
    IJavaElementDelta[] affectedChildren = EMPTY_DELTA;
    IJavaElementDelta[] annotationDeltas = EMPTY_DELTA;

    /**
    * @see IJavaElementDelta
    */
    @Override
    public IJavaElement getElement() {
	return this.changedElement;
    }

    /**
    * @see IJavaElementDelta
    */
    @Override
    public IJavaElementDelta[] getAffectedChildren() {
	return this.affectedChildren;
    }

    @Override
    public IJavaElementDelta[] getAnnotationDeltas() {
	return this.annotationDeltas;
    }

}

