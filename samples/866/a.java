import java.util.concurrent.Semaphore;

class SerializingListener&lt;T&gt; implements VectorsListener&lt;T&gt; {
    /**
     * This method is called prior each processEvent call, to check if this specific VectorsListener implementation is viable for specific event
     *
     * @param event
     * @param argument
     * @return TRUE, if this event can and should be processed with this listener, FALSE otherwise
     */
    @Override
    public boolean validateEvent(ListenerEvent event, long argument) {
	try {
	    /**
	     * please note, since sequence vectors are multithreaded we need to stop processed while model is being saved
	     */
	    locker.acquire();

	    if (event == targetEvent && argument % targetFrequency == 0) {
		return true;
	    } else
		return false;
	} catch (Exception e) {
	    throw new RuntimeException(e);
	} finally {
	    locker.release();
	}
    }

    private Semaphore locker = new Semaphore(1);
    private ListenerEvent targetEvent = ListenerEvent.EPOCH;
    private int targetFrequency = 100000;

}

