import java.util.*;
import java.security.*;
import java.security.Provider.Service;

class KeyGenerator {
    /**
     * Generates a secret key.
     *
     * @return the new key
     */
    public final SecretKey generateKey() {
	if (serviceIterator == null) {
	    return spi.engineGenerateKey();
	}
	RuntimeException failure = null;
	KeyGeneratorSpi mySpi = spi;
	do {
	    try {
		return mySpi.engineGenerateKey();
	    } catch (RuntimeException e) {
		if (failure == null) {
		    failure = e;
		}
		mySpi = nextSpi(mySpi, true);
	    }
	} while (mySpi != null);
	throw failure;
    }

    private Iterator&lt;Service&gt; serviceIterator;
    private volatile KeyGeneratorSpi spi;
    private final Object lock = new Object();
    private int initType;
    private static final int I_SIZE = 4;
    private int initKeySize;
    private SecureRandom initRandom;
    private static final int I_PARAMS = 3;
    private AlgorithmParameterSpec initParams;
    private static final int I_RANDOM = 2;
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

