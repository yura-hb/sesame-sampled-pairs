import java.awt.Robot;
import java.awt.event.KeyEvent;

class ExtendedRobot extends Robot {
    /**
     * Types given array of characters one by one
     *
     * @param   symbols Array of characters to be typed
     *
     * @see     #type(char)
     */
    public void type(char[] symbols) {
	for (int i = 0; i &lt; symbols.length; i++) {
	    type(symbols[i]);
	}
    }

    private static int DEFAULT_SPEED = 20;

    /**
     * Types given character
     *
     * @param   c   Character to be typed (e.g. {@code 'a'})
     *
     * @see     #type(int)
     * @see     java.awt.event.KeyEvent
     */
    public void type(char c) {
	type(KeyEvent.getExtendedKeyCodeForChar(c));
    }

    /**
     * Successively presses and releases a given key.
     * &lt;p&gt;
     * Key codes that have more than one physical key associated with them
     * (e.g. {@code KeyEvent.VK_SHIFT} could mean either the
     * left or right shift key) will map to the left key.
     *
     * @param   keycode Key to press (e.g. {@code KeyEvent.VK_A})
     * @throws  IllegalArgumentException if {@code keycode} is not
     *          a valid key
     *
     * @see     java.awt.Robot#keyPress(int)
     * @see     java.awt.Robot#keyRelease(int)
     * @see     java.awt.event.KeyEvent
     */
    public void type(int keycode) {
	keyPress(keycode);
	waitForIdle(DEFAULT_SPEED);
	keyRelease(keycode);
	waitForIdle(DEFAULT_SPEED);
    }

    /**
     * Waits until all events currently on the event queue have been processed with given
     * delay after syncing threads. It uses more advanced method of synchronizing threads
     * unlike {@link java.awt.Robot#waitForIdle()}
     *
     * @param   delayValue  Additional delay length in milliseconds to wait until thread
     *                      sync been completed
     * @throws  sun.awt.SunToolkit.IllegalThreadException if called on the AWT event
     *          dispatching thread
     */
    public synchronized void waitForIdle(int delayValue) {
	super.waitForIdle();
	delay(delayValue);
    }

}

