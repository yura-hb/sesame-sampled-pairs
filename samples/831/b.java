class VM {
    /**
     * Returns {@code true} if the VM has been shutdown
     */
    public static boolean isShutdown() {
	return initLevel == SYSTEM_SHUTDOWN;
    }

    private static volatile int initLevel;
    private static final int SYSTEM_SHUTDOWN = 5;

}

