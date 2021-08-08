class BuildNotifier {
    /**
    * Check whether the build has been canceled.
    */
    public void checkCancel() {
	if (this.monitor != null && this.monitor.isCanceled())
	    throw new OperationCanceledException();
    }

    protected IProgressMonitor monitor;

}

