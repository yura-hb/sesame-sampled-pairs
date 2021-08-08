import java.util.logging.Level;
import java.util.logging.Logger;

class EventTypeAwareListener&lt;K, V&gt; implements CacheEntryCreatedListener&lt;K, V&gt;, CacheEntryUpdatedListener&lt;K, V&gt;,
	CacheEntryRemovedListener&lt;K, V&gt;, CacheEntryExpiredListener&lt;K, V&gt;, Closeable {
    /** Processes the event and logs if an exception is thrown. */
    @SuppressWarnings("PMD.SwitchStmtsShouldHaveDefault")
    public void dispatch(@NonNull JCacheEntryEvent&lt;K, V&gt; event) {
	try {
	    if (event.getSource().isClosed()) {
		return;
	    }
	    switch (event.getEventType()) {
	    case CREATED:
		onCreated(event);
		return;
	    case UPDATED:
		onUpdated(event);
		return;
	    case REMOVED:
		onRemoved(event);
		return;
	    case EXPIRED:
		onExpired(event);
		return;
	    }
	    throw new IllegalStateException("Unknown event type: " + event.getEventType());
	} catch (Exception e) {
	    logger.log(Level.WARNING, null, e);
	} catch (Throwable t) {
	    logger.log(Level.SEVERE, null, t);
	}
    }

    static final Logger logger = Logger.getLogger(EventTypeAwareListener.class.getName());
    final CacheEntryListener&lt;? super K, ? super V&gt; listener;

    @Override
    @SuppressWarnings("unchecked")
    public void onCreated(Iterable&lt;CacheEntryEvent&lt;? extends K, ? extends V&gt;&gt; events) {
	if (listener instanceof CacheEntryCreatedListener&lt;?, ?&gt;) {
	    ((CacheEntryCreatedListener&lt;K, V&gt;) listener).onCreated(events);
	}
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onUpdated(Iterable&lt;CacheEntryEvent&lt;? extends K, ? extends V&gt;&gt; events) {
	if (listener instanceof CacheEntryUpdatedListener&lt;?, ?&gt;) {
	    ((CacheEntryUpdatedListener&lt;K, V&gt;) listener).onUpdated(events);
	}
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onRemoved(Iterable&lt;CacheEntryEvent&lt;? extends K, ? extends V&gt;&gt; events) {
	if (listener instanceof CacheEntryRemovedListener&lt;?, ?&gt;) {
	    ((CacheEntryRemovedListener&lt;K, V&gt;) listener).onRemoved(events);
	}
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onExpired(Iterable&lt;CacheEntryEvent&lt;? extends K, ? extends V&gt;&gt; events) {
	if (listener instanceof CacheEntryExpiredListener&lt;?, ?&gt;) {
	    ((CacheEntryExpiredListener&lt;K, V&gt;) listener).onExpired(events);
	}
    }

}

