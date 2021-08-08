class JScrollBar extends JComponent implements Adjustable, Accessible {
    class AccessibleJScrollBar extends AccessibleJComponent implements AccessibleValue {
	/**
	 * Get the maximum accessible value of this object.
	 *
	 * @return The maximum value of this object.
	 */
	public Number getMaximumAccessibleValue() {
	    // TIGER - 4422362
	    return model.getMaximum() - model.getExtent();
	}

    }

    /**
     * The model that represents the scrollbar's minimum, maximum, extent
     * (aka "visibleAmount") and current value.
     * @see #setModel
     */
    protected BoundedRangeModel model;

}

