class NullConsoleReader extends AbstractReader {
    /**
    * Continuously reads events that are coming from the event queue.
    */
    protected void readerLoop() {
	java.io.BufferedReader bufferedInput = new java.io.BufferedReader(new java.io.InputStreamReader(this.input));
	try {
	    int read = 0;
	    while (!this.isStopping && read != -1) {
		read = bufferedInput.read();
	    }
	} catch (java.io.IOException e) {
	}
    }

    private InputStream input;

}

