import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

class EventBus {
    /** Handles the given exception thrown by a subscriber with the given context. */
    void handleSubscriberException(Throwable e, SubscriberExceptionContext context) {
	checkNotNull(e);
	checkNotNull(context);
	try {
	    exceptionHandler.handleException(e, context);
	} catch (Throwable e2) {
	    // if the handler threw an exception... well, just log it
	    logger.log(Level.SEVERE,
		    String.format(Locale.ROOT, "Exception %s thrown while handling exception: %s", e2, e), e2);
	}
    }

    private final SubscriberExceptionHandler exceptionHandler;
    private static final Logger logger = Logger.getLogger(EventBus.class.getName());

}

