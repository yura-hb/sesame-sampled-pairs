import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

class Subscriber {
    /** Dispatches {@code event} to this subscriber using the proper executor. */
    final void dispatchEvent(final Object event) {
	executor.execute(new Runnable() {
	    @Override
	    public void run() {
		try {
		    invokeSubscriberMethod(event);
		} catch (InvocationTargetException e) {
		    bus.handleSubscriberException(e.getCause(), context(event));
		}
	    }
	});
    }

    /** Executor to use for dispatching events to this subscriber. */
    private final Executor executor;
    /** The event bus this subscriber belongs to. */
    @Weak
    private EventBus bus;
    /** Subscriber method. */
    private final Method method;
    /** The object with the subscriber method. */
    @VisibleForTesting
    final Object target;

    /**
    * Invokes the subscriber method. This method can be overridden to make the invocation
    * synchronized.
    */
    @VisibleForTesting
    void invokeSubscriberMethod(Object event) throws InvocationTargetException {
	try {
	    method.invoke(target, checkNotNull(event));
	} catch (IllegalArgumentException e) {
	    throw new Error("Method rejected target/argument: " + event, e);
	} catch (IllegalAccessException e) {
	    throw new Error("Method became inaccessible: " + event, e);
	} catch (InvocationTargetException e) {
	    if (e.getCause() instanceof Error) {
		throw (Error) e.getCause();
	    }
	    throw e;
	}
    }

    /** Gets the context for the given event. */
    private SubscriberExceptionContext context(Object event) {
	return new SubscriberExceptionContext(bus, event, target, method);
    }

}

