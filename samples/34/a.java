import javax.swing.tree.DefaultTreeSelectionModel;

class ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel {
    /**
     * This is overridden to set {@code updatingListSelectionModel}
     * and message super. This is the only place DefaultTreeSelectionModel
     * alters the ListSelectionModel.
     */
    @Override
    public void resetRowSelection() {
	if (!updatingListSelectionModel) {
	    updatingListSelectionModel = true;
	    try {
		super.resetRowSelection();
	    } finally {
		updatingListSelectionModel = false;
	    }
	}
	// Notice how we don't message super if
	// updatingListSelectionModel is true. If
	// updatingListSelectionModel is true, it implies the
	// ListSelectionModel has already been updated and the
	// paths are the only thing that needs to be updated.
    }

    /** Set to true when we are updating the ListSelectionModel. */
    private boolean updatingListSelectionModel;

}

