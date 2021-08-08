import java.awt.*;

class JTabbedPane extends JComponent implements Serializable, Accessible, SwingConstants {
    /**
     * Sets the model to be used with this tabbedpane.
     *
     * @param model the model to be used
     * @see #getModel
     */
    @BeanProperty(description = "The tabbedpane's SingleSelectionModel.")
    public void setModel(SingleSelectionModel model) {
	SingleSelectionModel oldModel = getModel();

	if (oldModel != null) {
	    oldModel.removeChangeListener(changeListener);
	    changeListener = null;
	}

	this.model = model;

	if (model != null) {
	    changeListener = createChangeListener();
	    model.addChangeListener(changeListener);
	}

	firePropertyChange("model", oldModel, model);
	repaint();
    }

    /**
     * The &lt;code&gt;changeListener&lt;/code&gt; is the listener we add to the
     * model.
     */
    protected ChangeListener changeListener = null;
    /** The default selection model */
    protected SingleSelectionModel model;

    /**
     * Returns the model associated with this tabbedpane.
     *
     * @return the {@code SingleSelectionModel} associated with this tabbedpane
     * @see #setModel
     */
    public SingleSelectionModel getModel() {
	return model;
    }

    /**
     * Subclasses that want to handle &lt;code&gt;ChangeEvents&lt;/code&gt; differently
     * can override this to return a subclass of &lt;code&gt;ModelListener&lt;/code&gt; or
     * another &lt;code&gt;ChangeListener&lt;/code&gt; implementation.
     *
     * @return a {@code ChangeListener}
     * @see #fireStateChanged
     */
    protected ChangeListener createChangeListener() {
	return new ModelListener();
    }

}

