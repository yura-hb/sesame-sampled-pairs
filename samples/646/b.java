class CompilerStats implements Comparable {
    /**
    * Returns the total elapsed time (between start and end)
    * @return the time spent between start and end
    */
    public long elapsedTime() {
	return this.endTime - this.startTime;
    }

    public long endTime;
    public long startTime;

}

