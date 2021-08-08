import java.nio.file.ClosedWatchServiceException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class PollingWatchService extends AbstractWatchService {
    /**
     * Register the given file with this watch service
     */
    @Override
    WatchKey register(final Path path, WatchEvent.Kind&lt;?&gt;[] events, WatchEvent.Modifier... modifiers)
	    throws IOException {
	// check events - CCE will be thrown if there are invalid elements
	final Set&lt;WatchEvent.Kind&lt;?&gt;&gt; eventSet = new HashSet&lt;&gt;(events.length);
	for (WatchEvent.Kind&lt;?&gt; event : events) {
	    // standard events
	    if (event == StandardWatchEventKinds.ENTRY_CREATE || event == StandardWatchEventKinds.ENTRY_MODIFY
		    || event == StandardWatchEventKinds.ENTRY_DELETE) {
		eventSet.add(event);
		continue;
	    }

	    // OVERFLOW is ignored
	    if (event == StandardWatchEventKinds.OVERFLOW) {
		continue;
	    }

	    // null/unsupported
	    if (event == null)
		throw new NullPointerException("An element in event set is 'null'");
	    throw new UnsupportedOperationException(event.name());
	}
	if (eventSet.isEmpty())
	    throw new IllegalArgumentException("No events to register");

	// Extended modifiers may be used to specify the sensitivity level
	int sensitivity = 10;
	if (modifiers.length &gt; 0) {
	    for (WatchEvent.Modifier modifier : modifiers) {
		if (modifier == null)
		    throw new NullPointerException();

		if (ExtendedOptions.SENSITIVITY_HIGH.matches(modifier)) {
		    sensitivity = ExtendedOptions.SENSITIVITY_HIGH.parameter();
		} else if (ExtendedOptions.SENSITIVITY_MEDIUM.matches(modifier)) {
		    sensitivity = ExtendedOptions.SENSITIVITY_MEDIUM.parameter();
		} else if (ExtendedOptions.SENSITIVITY_LOW.matches(modifier)) {
		    sensitivity = ExtendedOptions.SENSITIVITY_LOW.parameter();
		} else {
		    throw new UnsupportedOperationException("Modifier not supported");
		}
	    }
	}

	// check if watch service is closed
	if (!isOpen())
	    throw new ClosedWatchServiceException();

	// registration is done in privileged block as it requires the
	// attributes of the entries in the directory.
	try {
	    int value = sensitivity;
	    return AccessController.doPrivileged(new PrivilegedExceptionAction&lt;PollingWatchKey&gt;() {
		@Override
		public PollingWatchKey run() throws IOException {
		    return doPrivilegedRegister(path, eventSet, value);
		}
	    });
	} catch (PrivilegedActionException pae) {
	    Throwable cause = pae.getCause();
	    if (cause != null && cause instanceof IOException)
		throw (IOException) cause;
	    throw new AssertionError(pae);
	}
    }

    private final Map&lt;Object, PollingWatchKey&gt; map = new HashMap&lt;&gt;();
    private final ScheduledExecutorService scheduledExecutor;

    private PollingWatchKey doPrivilegedRegister(Path path, Set&lt;? extends WatchEvent.Kind&lt;?&gt;&gt; events,
	    int sensitivityInSeconds) throws IOException {
	// check file is a directory and get its file key if possible
	BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
	if (!attrs.isDirectory()) {
	    throw new NotDirectoryException(path.toString());
	}
	Object fileKey = attrs.fileKey();
	if (fileKey == null)
	    throw new AssertionError("File keys must be supported");

	// grab close lock to ensure that watch service cannot be closed
	synchronized (closeLock()) {
	    if (!isOpen())
		throw new ClosedWatchServiceException();

	    PollingWatchKey watchKey;
	    synchronized (map) {
		watchKey = map.get(fileKey);
		if (watchKey == null) {
		    // new registration
		    watchKey = new PollingWatchKey(path, this, fileKey);
		    map.put(fileKey, watchKey);
		} else {
		    // update to existing registration
		    watchKey.disable();
		}
	    }
	    watchKey.enable(events, sensitivityInSeconds);
	    return watchKey;
	}

    }

    class PollingWatchKey extends AbstractWatchKey {
	private final Map&lt;Object, PollingWatchKey&gt; map = new HashMap&lt;&gt;();
	private final ScheduledExecutorService scheduledExecutor;

	PollingWatchKey(Path dir, PollingWatchService watcher, Object fileKey) throws IOException {
	    super(dir, watcher);
	    this.fileKey = fileKey;
	    this.valid = true;
	    this.tickCount = 0;
	    this.entries = new HashMap&lt;Path, CacheEntry&gt;();

	    // get the initial entries in the directory
	    try (DirectoryStream&lt;Path&gt; stream = Files.newDirectoryStream(dir)) {
		for (Path entry : stream) {
		    // don't follow links
		    long lastModified = Files.getLastModifiedTime(entry, LinkOption.NOFOLLOW_LINKS).toMillis();
		    entries.put(entry.getFileName(), new CacheEntry(lastModified, tickCount));
		}
	    } catch (DirectoryIteratorException e) {
		throw e.getCause();
	    }
	}

	void disable() {
	    synchronized (this) {
		if (poller != null)
		    poller.cancel(false);
	    }
	}

	void enable(Set&lt;? extends WatchEvent.Kind&lt;?&gt;&gt; events, long period) {
	    synchronized (this) {
		// update the events
		this.events = events;

		// create the periodic task
		Runnable thunk = new Runnable() {
		    public void run() {
			poll();
		    }
		};
		this.poller = scheduledExecutor.scheduleAtFixedRate(thunk, period, period, TimeUnit.SECONDS);
	    }
	}

	/**
	 * Polls the directory to detect for new files, modified files, or
	 * deleted files.
	 */
	synchronized void poll() {
	    if (!valid) {
		return;
	    }

	    // update tick
	    tickCount++;

	    // open directory
	    DirectoryStream&lt;Path&gt; stream = null;
	    try {
		stream = Files.newDirectoryStream(watchable());
	    } catch (IOException x) {
		// directory is no longer accessible so cancel key
		cancel();
		signal();
		return;
	    }

	    // iterate over all entries in directory
	    try {
		for (Path entry : stream) {
		    long lastModified = 0L;
		    try {
			lastModified = Files.getLastModifiedTime(entry, LinkOption.NOFOLLOW_LINKS).toMillis();
		    } catch (IOException x) {
			// unable to get attributes of entry. If file has just
			// been deleted then we'll report it as deleted on the
			// next poll
			continue;
		    }

		    // lookup cache
		    CacheEntry e = entries.get(entry.getFileName());
		    if (e == null) {
			// new file found
			entries.put(entry.getFileName(), new CacheEntry(lastModified, tickCount));

			// queue ENTRY_CREATE if event enabled
			if (events.contains(StandardWatchEventKinds.ENTRY_CREATE)) {
			    signalEvent(StandardWatchEventKinds.ENTRY_CREATE, entry.getFileName());
			    continue;
			} else {
			    // if ENTRY_CREATE is not enabled and ENTRY_MODIFY is
			    // enabled then queue event to avoid missing out on
			    // modifications to the file immediately after it is
			    // created.
			    if (events.contains(StandardWatchEventKinds.ENTRY_MODIFY)) {
				signalEvent(StandardWatchEventKinds.ENTRY_MODIFY, entry.getFileName());
			    }
			}
			continue;
		    }

		    // check if file has changed
		    if (e.lastModified != lastModified) {
			if (events.contains(StandardWatchEventKinds.ENTRY_MODIFY)) {
			    signalEvent(StandardWatchEventKinds.ENTRY_MODIFY, entry.getFileName());
			}
		    }
		    // entry in cache so update poll time
		    e.update(lastModified, tickCount);

		}
	    } catch (DirectoryIteratorException e) {
		// ignore for now; if the directory is no longer accessible
		// then the key will be cancelled on the next poll
	    } finally {

		// close directory stream
		try {
		    stream.close();
		} catch (IOException x) {
		    // ignore
		}
	    }

	    // iterate over cache to detect entries that have been deleted
	    Iterator&lt;Map.Entry&lt;Path, CacheEntry&gt;&gt; i = entries.entrySet().iterator();
	    while (i.hasNext()) {
		Map.Entry&lt;Path, CacheEntry&gt; mapEntry = i.next();
		CacheEntry entry = mapEntry.getValue();
		if (entry.lastTickCount() != tickCount) {
		    Path name = mapEntry.getKey();
		    // remove from map and queue delete event (if enabled)
		    i.remove();
		    if (events.contains(StandardWatchEventKinds.ENTRY_DELETE)) {
			signalEvent(StandardWatchEventKinds.ENTRY_DELETE, name);
		    }
		}
	    }
	}

	@Override
	public void cancel() {
	    valid = false;
	    synchronized (map) {
		map.remove(fileKey());
	    }
	    disable();
	}

	Object fileKey() {
	    return fileKey;
	}

    }

    class CacheEntry {
	private final Map&lt;Object, PollingWatchKey&gt; map = new HashMap&lt;&gt;();
	private final ScheduledExecutorService scheduledExecutor;

	CacheEntry(long lastModified, int lastTickCount) {
	    this.lastModified = lastModified;
	    this.lastTickCount = lastTickCount;
	}

	void update(long lastModified, int tickCount) {
	    this.lastModified = lastModified;
	    this.lastTickCount = tickCount;
	}

	int lastTickCount() {
	    return lastTickCount;
	}

    }

}

