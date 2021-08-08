import com.google.common.base.Throwables;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executors;

class MoreExecutors {
    /**
    * Returns a default thread factory used to create new threads.
    *
    * &lt;p&gt;On AppEngine, returns {@code ThreadManager.currentRequestThreadFactory()}. Otherwise,
    * returns {@link Executors#defaultThreadFactory()}.
    *
    * @since 14.0
    */
    @Beta
    @GwtIncompatible // concurrency
    public static ThreadFactory platformThreadFactory() {
	if (!isAppEngine()) {
	    return Executors.defaultThreadFactory();
	}
	try {
	    return (ThreadFactory) Class.forName("com.google.appengine.api.ThreadManager")
		    .getMethod("currentRequestThreadFactory").invoke(null);
	} catch (IllegalAccessException | ClassNotFoundException | NoSuchMethodException e) {
	    throw new RuntimeException("Couldn't invoke ThreadManager.currentRequestThreadFactory", e);
	} catch (InvocationTargetException e) {
	    throw Throwables.propagate(e.getCause());
	}
    }

    @GwtIncompatible // TODO
    private static boolean isAppEngine() {
	if (System.getProperty("com.google.appengine.runtime.environment") == null) {
	    return false;
	}
	try {
	    // If the current environment is null, we're not inside AppEngine.
	    return Class.forName("com.google.apphosting.api.ApiProxy").getMethod("getCurrentEnvironment")
		    .invoke(null) != null;
	} catch (ClassNotFoundException e) {
	    // If ApiProxy doesn't exist, we're not on AppEngine at all.
	    return false;
	} catch (InvocationTargetException e) {
	    // If ApiProxy throws an exception, we're not in a proper AppEngine environment.
	    return false;
	} catch (IllegalAccessException e) {
	    // If the method isn't accessible, we're not on a supported version of AppEngine;
	    return false;
	} catch (NoSuchMethodException e) {
	    // If the method doesn't exist, we're not on a supported version of AppEngine;
	    return false;
	}
    }

}

