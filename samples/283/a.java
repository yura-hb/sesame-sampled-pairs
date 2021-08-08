import org.nd4j.parameterserver.distributed.messages.VoidMessage;

abstract class BaseTransport implements Transport {
    /**
     * This message handler is responsible for receiving messages on Shard side
     *
     * @param buffer
     * @param offset
     * @param length
     * @param header
     */
    protected void shardMessageHandler(DirectBuffer buffer, int offset, int length, Header header) {
	/**
	 * All incoming messages here are supposed to be unicast messages.
	 */
	// TODO: implement fragmentation handler here PROBABLY. Or forbid messages &gt; MTU?
	//log.info("shardMessageHandler message request incoming...");
	byte[] data = new byte[length];
	buffer.getBytes(offset, data);

	VoidMessage message = VoidMessage.fromBytes(data);
	if (message.getMessageType() == 7) {
	    // if that's vector request message - it's special case, we don't send it to other shards yet
	    //log.info("Shortcut for vector request");
	    messages.add(message);
	} else {
	    // and send it away to other Shards
	    publicationForShards.offer(buffer, offset, length);
	}
    }

    protected LinkedBlockingQueue&lt;VoidMessage&gt; messages = new LinkedBlockingQueue&lt;&gt;();
    protected Publication publicationForShards;

}

