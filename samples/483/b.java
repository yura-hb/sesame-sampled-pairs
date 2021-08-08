import sun.awt.im.InputMethodContext;

class InputContext {
    /**
     * Returns a new InputContext instance.
     * @return a new InputContext instance
     */
    public static InputContext getInstance() {
	return new sun.awt.im.InputMethodContext();
    }

}

