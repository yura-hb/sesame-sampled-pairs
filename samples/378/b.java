class Client&lt;O, A, AS&gt; {
    /**
     * Shutdown the server at the url
     *
     * @param url url of the server
     */
    public static void serverShutdown(String url) {
	ClientUtils.post(url + ENVS_ROOT + SHUTDOWN, new JSONObject());
    }

    public static String ENVS_ROOT = V1_ROOT + "/envs/";
    public static String SHUTDOWN = "/shutdown/";

}

