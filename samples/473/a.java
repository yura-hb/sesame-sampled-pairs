import org.nd4j.parameterserver.distributed.logic.RetransmissionHandler;
import org.nd4j.parameterserver.distributed.messages.*;
import java.util.Map;

class RoutedTransport extends BaseTransport {
    /**
     * This method implements Shard -&gt; Client comms
     *
     * @param message
     */
    @Override
    protected void sendFeedbackToClient(VoidMessage message) {
	/*
	    PLEASE NOTE: In this case we don't change target. We just discard message if something goes wrong.
	 */
	// TODO: discard message if it's not sent for enough time?
	long targetAddress = message.getOriginatorId();

	if (targetAddress == originatorId) {
	    completed.put(message.getTaskId(), (MeaningfulMessage) message);
	    return;
	}

	RetransmissionHandler.TransmissionStatus result;

	//log.info("sI_{} trying to send back {}/{}", shardIndex, targetAddress, message.getClass().getSimpleName());

	RemoteConnection connection = clients.get(targetAddress);
	boolean delivered = false;

	if (connection == null) {
	    log.info("Can't get client with address [{}]", targetAddress);
	    log.info("Known clients: {}", clients.keySet());
	    throw new RuntimeException();
	}

	while (!delivered) {
	    synchronized (connection.locker) {
		result = RetransmissionHandler
			.getTransmissionStatus(connection.getPublication().offer(message.asUnsafeBuffer()));
	    }

	    switch (result) {
	    case ADMIN_ACTION:
	    case BACKPRESSURE: {
		try {
		    Thread.sleep(voidConfiguration.getRetransmitTimeout());
		} catch (Exception e) {
		}
	    }
		break;
	    case NOT_CONNECTED: {
		// client dead? sleep and forget
		// TODO: we might want to delay this message & move it to separate queue?
		try {
		    Thread.sleep(voidConfiguration.getRetransmitTimeout());
		} catch (Exception e) {
		}
	    }
	    // do not break here, we can't do too much here, if client is dead
	    case MESSAGE_SENT:
		delivered = true;
		break;
	    }
	}
    }

    protected Map&lt;Long, RemoteConnection&gt; clients = new ConcurrentHashMap&lt;&gt;();

}

