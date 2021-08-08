import java.nio.channels.*;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

abstract class Port extends AsynchronousChannelGroupImpl {
    /**
     * Register channel identified by its file descriptor
     */
    final void register(int fd, PollableChannel ch) {
	fdToChannelLock.writeLock().lock();
	try {
	    if (isShutdown())
		throw new ShutdownChannelGroupException();
	    fdToChannel.put(Integer.valueOf(fd), ch);
	} finally {
	    fdToChannelLock.writeLock().unlock();
	}
    }

    protected final ReadWriteLock fdToChannelLock = new ReentrantReadWriteLock();
    protected final Map&lt;Integer, PollableChannel&gt; fdToChannel = new HashMap&lt;Integer, PollableChannel&gt;();

}

