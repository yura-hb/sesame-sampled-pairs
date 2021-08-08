class AeronNDArraySubscriber implements AutoCloseable {
    /**
     * Returns the connection uri in the form of:
     * host:port:streamId
     * @return
     */
    public String connectionUrl() {
	String[] split = channel.replace("aeron:udp?endpoint=", "").split(":");
	String host = split[0];
	int port = Integer.parseInt(split[1]);
	return AeronConnectionInformation.of(host, port, streamId).toString();
    }

    private String channel;
    private int streamId = -1;

}

