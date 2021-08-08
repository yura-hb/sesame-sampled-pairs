import com.github.benmanes.caffeine.jcache.configuration.CaffeineConfiguration;
import com.github.benmanes.caffeine.jcache.management.JCacheStatisticsMXBean;
import com.github.benmanes.caffeine.jcache.management.JmxRegistration;
import com.github.benmanes.caffeine.jcache.management.JmxRegistration.MBeanType;

class CacheProxy&lt;K, V&gt; implements Cache&lt;K, V&gt; {
    /** Enables or disables the statistics JMX bean. */
    void enableStatistics(boolean enabled) {
	requireNotClosed();

	synchronized (configuration) {
	    if (enabled) {
		JmxRegistration.registerMXBean(this, statistics, MBeanType.Statistics);
	    } else {
		JmxRegistration.unregisterMXBean(this, MBeanType.Statistics);
	    }
	    statistics.enable(enabled);
	    configuration.setStatisticsEnabled(enabled);
	}
    }

    final CaffeineConfiguration&lt;K, V&gt; configuration;
    protected final JCacheStatisticsMXBean statistics;
    volatile boolean closed;

    /** Checks that the cache is not closed. */
    protected final void requireNotClosed() {
	if (isClosed()) {
	    throw new IllegalStateException();
	}
    }

    @Override
    public boolean isClosed() {
	return closed;
    }

}

