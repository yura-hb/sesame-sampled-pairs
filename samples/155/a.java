import org.nd4j.aeron.ipc.AeronNDArrayPublisher;

class PublishingListener implements NDArrayCallback {
    /**
     * A listener for ndarray message
     *
     * @param message the message for the callback
     */
    @Override
    public void onNDArrayMessage(NDArrayMessage message) {
	try (AeronNDArrayPublisher publisher = AeronNDArrayPublisher.builder().streamId(streamId).ctx(aeronContext)
		.channel(masterUrl).build()) {
	    publisher.publish(message);
	    log.debug("NDArray PublishingListener publishing to channel " + masterUrl + ":" + streamId);
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}

    }

    private int streamId;
    private Aeron.Context aeronContext;
    private String masterUrl;

}

