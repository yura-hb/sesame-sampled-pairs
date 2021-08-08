import org.deeplearning4j.ui.play.PlayUIServer;

abstract class UIServer {
    /**
     * Get (and, initialize if necessary) the UI server.
     * Singleton pattern - all calls to getInstance() will return the same UI instance.
     *
     * @return UI instance for this JVM
     */
    public static synchronized UIServer getInstance() {
	if (uiServer == null) {
	    PlayUIServer playUIServer = new PlayUIServer();
	    playUIServer.runMain(new String[] { "--uiPort", String.valueOf(PlayUIServer.DEFAULT_UI_PORT) });
	    uiServer = playUIServer;
	}
	return uiServer;
    }

    private static UIServer uiServer;

}

