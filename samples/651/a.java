import java.util.concurrent.LinkedBlockingQueue;

abstract class BaseTransport implements Transport {
    /**
     * This method takes 1 message from "incoming messages" queue, blocking if queue is empty
     *
     * @return
     */
    @Override
    public VoidMessage takeMessage() {
	if (threadingModel != ThreadingModel.SAME_THREAD) {
	    try {
		return messages.take();
	    } catch (InterruptedException e) {
		// probably we don't want to do anything here
		return null;
	    } catch (Exception e) {
		throw new RuntimeException(e);
	    }
	} else {
	    /**
	     * PLEASE NOTE: This branch is suitable for debugging only, should never be used in wild life
	     */
	    // we do inplace poll
	    if (subscriptionForShards != null)
		subscriptionForShards.poll(messageHandlerForShards, 512);

	    subscriptionForClients.poll(messageHandlerForClients, 512);

	    return messages.poll();
	}
    }

    protected ThreadingModel threadingModel = ThreadingModel.DEDICATED_THREADS;
    protected LinkedBlockingQueue&lt;VoidMessage&gt; messages = new LinkedBlockingQueue&lt;&gt;();
    protected Subscription subscriptionForShards;
    protected FragmentAssembler messageHandlerForShards;
    protected Subscription subscriptionForClients;
    protected FragmentAssembler messageHandlerForClients;

}

