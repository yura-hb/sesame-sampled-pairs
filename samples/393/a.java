import java.text.NumberFormat;

abstract class BaseTestRunner implements TestListener {
    /**
     * Returns the formatted string of the elapsed time.
     */
    public String elapsedTimeAsString(long runTime) {
	return NumberFormat.getInstance().format((double) runTime / 1000);
    }

}

