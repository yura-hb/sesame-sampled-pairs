class ReconcilerTests9 extends ModifyingResourceTests {
    /**
    * Setup for the next test.
    */
    public void setUp() throws Exception {
	super.setUp();
	this.problemRequestor = new ProblemRequestor();
	this.wcOwner = new WorkingCopyOwner() {
	    public IProblemRequestor getProblemRequestor(ICompilationUnit unit) {
		return ReconcilerTests9.this.problemRequestor;
	    }
	};
	this.workingCopy = getCompilationUnit("Reconciler9/src/module-info.java").getWorkingCopy(this.wcOwner, null);
	this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
	startDeltas();
    }

    protected ProblemRequestor problemRequestor;
    protected ICompilationUnit workingCopy;

}

