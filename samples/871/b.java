import java.util.*;
import java.security.*;
import java.security.Provider.Service;

class KeyGenerator {
    /**
     * Initializes this key generator.
     *
     * @param random the source of randomness for this generator
     */
    public final void init(SecureRandom random) {
	if (serviceIterator == null) {
	    spi.engineInit(random);
	    return;
	}
	RuntimeException failure = null;
	KeyGeneratorSpi mySpi = spi;
	do {
	    try {
		mySpi.engineInit(random);
		initType = I_RANDOM;
		initKeySize = 0;
		initParams = null;
		initRandom = random;
		return;
	    } catch (RuntimeException e) {
		if (failure == null) {
		    failure = e;
		}
		mySpi = nextSpi(mySpi, false);
	    }
	} while (mySpi != null);
	throw failure;
    }

    private Iterator&lt;Service&gt; serviceIterator;
    private volatile KeyGeneratorSpi spi;
    private int initType;
    private static final int I_RANDOM = 2;
    private int initKeySize;
    private AlgorithmParameterSpec initParams;
    private SecureRandom initRandom;
    private final Object lock = new Object();
    private static final int I_SIZE = 4;
    private static final int I_PARAMS = 3;
    private static final int I_NONE = 1;
    private Provider provider;

    /**
     * Update the active spi of this class and return the next
     * implementation for failover. If no more implementations are
     * available, this method returns null. However, the active spi of
     * this class is never set to null.
     */
    private KeyGeneratorSpi nextSpi(KeyGeneratorSpi oldSpi, boolean reinit) {
	synchronized (lock) {
	    // somebody else did a failover concurrently
	    // try that spi now
	    if ((oldSpi != null) && (oldSpi != spi)) {
		return spi;
	    }
	    if (serviceIterator == null) {
		return null;
	    }
	    while (serviceIterator.hasNext()) {
		Service s = serviceIterator.next();
		if (JceSecurity.canUseProvider(s.getProvider()) == false) {
		    continue;
		}
		try {
		    Object inst = s.newInstance(null);
		    // ignore non-spis
		    if (inst instanceof KeyGeneratorSpi == false) {
			continue;
		    }
		    KeyGeneratorSpi spi = (KeyGeneratorSpi) inst;
		    if (reinit) {
			if (initType == I_SIZE) {
			    spi.engineInit(initKeySize, initRandom);
			} else if (initType == I_PARAMS) {
			    spi.engineInit(initParams, initRandom);
			} else if (initType == I_RANDOM) {
			    spi.engineInit(initRandom);
			} else if (initType != I_NONE) {
			    throw new AssertionError("KeyGenerator initType: " + initType);
			}
		    }
		    provider = s.getProvider();
		    this.spi = spi;
		    return spi;
		} catch (Exception e) {
		    // ignore
		}
	    }
	    disableFailover();
	    return null;
	}
    }

    void disableFailover() {
	serviceIterator = null;
	initType = 0;
	initParams = null;
	initRandom = null;
    }

}

