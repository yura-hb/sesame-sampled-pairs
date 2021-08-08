import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;
import sun.awt.SunToolkit;

abstract class JComponent extends Container implements Serializable, HasGetTransferHandler {
    /**
     * Sets the font for this component.
     *
     * @param font the desired &lt;code&gt;Font&lt;/code&gt; for this component
     * @see java.awt.Component#getFont
     */
    @BeanProperty(preferred = true, visualUpdate = true, description = "The font for the component.")
    public void setFont(Font font) {
	Font oldFont = getFont();
	super.setFont(font);
	// font already bound in AWT1.2
	if (font != oldFont) {
	    revalidate();
	    repaint();
	}
    }

    private transient AtomicBoolean revalidateRunnableScheduled = new AtomicBoolean(false);

    /**
     * Supports deferred automatic layout.
     * &lt;p&gt;
     * Calls &lt;code&gt;invalidate&lt;/code&gt; and then adds this component's
     * &lt;code&gt;validateRoot&lt;/code&gt; to a list of components that need to be
     * validated.  Validation will occur after all currently pending
     * events have been dispatched.  In other words after this method
     * is called,  the first validateRoot (if any) found when walking
     * up the containment hierarchy of this component will be validated.
     * By default, &lt;code&gt;JRootPane&lt;/code&gt;, &lt;code&gt;JScrollPane&lt;/code&gt;,
     * and &lt;code&gt;JTextField&lt;/code&gt; return true
     * from &lt;code&gt;isValidateRoot&lt;/code&gt;.
     * &lt;p&gt;
     * This method will automatically be called on this component
     * when a property value changes such that size, location, or
     * internal layout of this component has been affected.  This automatic
     * updating differs from the AWT because programs generally no
     * longer need to invoke &lt;code&gt;validate&lt;/code&gt; to get the contents of the
     * GUI to update.
     *
     * @see java.awt.Component#invalidate
     * @see java.awt.Container#validate
     * @see #isValidateRoot
     * @see RepaintManager#addInvalidComponent
     */
    public void revalidate() {
	if (getParent() == null) {
	    // Note: We don't bother invalidating here as once added
	    // to a valid parent invalidate will be invoked (addImpl
	    // invokes addNotify which will invoke invalidate on the
	    // new Component). Also, if we do add a check to isValid
	    // here it can potentially be called before the constructor
	    // which was causing some people grief.
	    return;
	}
	if (SunToolkit.isDispatchThreadForAppContext(this)) {
	    invalidate();
	    RepaintManager.currentManager(this).addInvalidComponent(this);
	} else {
	    // To avoid a flood of Runnables when constructing GUIs off
	    // the EDT, a flag is maintained as to whether or not
	    // a Runnable has been scheduled.
	    if (revalidateRunnableScheduled.getAndSet(true)) {
		return;
	    }
	    SunToolkit.executeOnEventHandlerThread(this, () -&gt; {
		revalidateRunnableScheduled.set(false);
		revalidate();
	    });
	}
    }

}

