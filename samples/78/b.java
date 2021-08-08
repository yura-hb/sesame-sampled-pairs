import javax.lang.model.element.*;

class SimpleElementVisitor6&lt;R, P&gt; extends AbstractElementVisitor6&lt;R, P&gt; {
    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation calls {@code defaultAction}, unless the
     * element is a {@code RESOURCE_VARIABLE} in which case {@code
     * visitUnknown} is called.
     *
     * @param e {@inheritDoc}
     * @param p {@inheritDoc}
     * @return  {@inheritDoc}
     */
    public R visitVariable(VariableElement e, P p) {
	if (e.getKind() != ElementKind.RESOURCE_VARIABLE)
	    return defaultAction(e, p);
	else
	    return visitUnknown(e, p);
    }

    /**
     * Default value to be returned; {@link #defaultAction
     * defaultAction} returns this value unless the method is
     * overridden.
     */
    protected final R DEFAULT_VALUE;

    /**
     * The default action for visit methods.
     *
     * @implSpec The implementation in this class just returns {@link
     * #DEFAULT_VALUE}; subclasses will commonly override this method.
     *
     * @param e the element to process
     * @param p a visitor-specified parameter
     * @return {@code DEFAULT_VALUE} unless overridden
     */
    protected R defaultAction(Element e, P p) {
	return DEFAULT_VALUE;
    }

}

