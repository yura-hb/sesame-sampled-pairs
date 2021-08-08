import java.io.ByteArrayOutputStream;
import jdk.test.lib.process.OutputBuffer;

class LingeredApp {
    /**
     *
     * @return OutputBuffer object for the LingeredApp's output. Can only be called
     * after LingeredApp has exited.
     */
    public OutputBuffer getOutput() {
	if (appProcess.isAlive()) {
	    throw new RuntimeException("Process is still alive. Can't get its output.");
	}
	if (output == null) {
	    output = new OutputBuffer(stdoutBuffer.toString(), stderrBuffer.toString());
	}
	return output;
    }

    protected Process appProcess;
    protected OutputBuffer output;
    private ByteArrayOutputStream stdoutBuffer;
    private ByteArrayOutputStream stderrBuffer;

}

