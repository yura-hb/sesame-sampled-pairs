abstract class AbstractStepInterpolator implements StepInterpolator {
    /** Store the current step time.
    * @param t current time
    */
    public void storeTime(final double t) {

	globalCurrentTime = t;
	softCurrentTime = globalCurrentTime;
	h = globalCurrentTime - globalPreviousTime;
	setInterpolatedTime(t);

	// the step is not finalized anymore
	finalized = false;

    }

    /** global current time */
    private double globalCurrentTime;
    /** soft current time */
    private double softCurrentTime;
    /** current time step */
    protected double h;
    /** global previous time */
    private double globalPreviousTime;
    /** indicate if the step has been finalized or not. */
    private boolean finalized;
    /** interpolated time */
    protected double interpolatedTime;
    /** indicator for dirty state. */
    private boolean dirtyState;

    /** {@inheritDoc} */
    @Override
    public void setInterpolatedTime(final double time) {
	interpolatedTime = time;
	dirtyState = true;
    }

}

