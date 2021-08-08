import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class AtomicAllocator implements Allocator {
    /**
     * Consume and apply configuration passed in as argument
     *
     * PLEASE NOTE: This method should only be used BEFORE any calculations were started.
     *
     * @param configuration configuration bean to be applied
     */
    @Override
    public void applyConfiguration(@NonNull Configuration configuration) {
	if (!wasInitialised.get()) {
	    globalLock.writeLock().lock();

	    this.configuration = configuration;

	    globalLock.writeLock().unlock();
	}
    }

    private final AtomicBoolean wasInitialised = new AtomicBoolean(false);
    private ReentrantReadWriteLock globalLock = new ReentrantReadWriteLock();
    private Configuration configuration;

}

