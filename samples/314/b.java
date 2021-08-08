class JavaClass extends AccessFlags implements Cloneable, Node, Comparable&lt;JavaClass&gt; {
    /**
     * @return Annotations on the class
     * @since 6.0
     */
    public AnnotationEntry[] getAnnotationEntries() {
	if (annotations == null) {
	    annotations = AnnotationEntry.createAnnotationEntries(getAttributes());
	}

	return annotations;
    }

    private AnnotationEntry[] annotations;
    private Attribute[] attributes;

    /**
     * @return Attributes of the class.
     */
    public Attribute[] getAttributes() {
	return attributes;
    }

}

