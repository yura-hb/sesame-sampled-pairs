import org.eclipse.jdt.core.compiler.IProblem;

class DefaultProblem extends CategorizedProblem {
    /**
    * Returns the marker type associated to this problem.
    * @see org.eclipse.jdt.core.compiler.CategorizedProblem#getMarkerType()
    */
    @Override
    public String getMarkerType() {
	return this.id == IProblem.Task ? MARKER_TYPE_TASK : MARKER_TYPE_PROBLEM;
    }

    private int id;
    private static final String MARKER_TYPE_TASK = "org.eclipse.jdt.core.task";
    private static final String MARKER_TYPE_PROBLEM = "org.eclipse.jdt.core.problem";

}

