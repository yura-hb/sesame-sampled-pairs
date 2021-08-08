abstract class BandStructure {
    class IntBand extends ValueBand {
	/** Return the occurrence count of a specific value in this band. */
	public int getIntCount(int value) {
	    assert (phase() == DISBURSE_PHASE);
	    // assert that this is the whole pass; no other reads allowed
	    assert (valuesRemainingForDebug() == length());
	    int total = 0;
	    for (int k = length(); k &gt; 0; k--) {
		if (getInt() == value) {
		    total += 1;
		}
	    }
	    resetForSecondPass();
	    return total;
	}

	public int getInt() {
	    return getValue();
	}

    }

    public static final int DISBURSE_PHASE = 6;
    boolean optDebugBands = p200.getBoolean(Utils.COM_PREFIX + "debug.bands");

    abstract class Band {
	public static final int DISBURSE_PHASE = 6;
	boolean optDebugBands = p200.getBoolean(Utils.COM_PREFIX + "debug.bands");

	int phase() {
	    return phase;
	}

    }

    class ValueBand extends Band {
	public static final int DISBURSE_PHASE = 6;
	boolean optDebugBands = p200.getBoolean(Utils.COM_PREFIX + "debug.bands");

	@Override
	protected int valuesRemainingForDebug() {
	    return length - valuesDisbursed;
	}

	@Override
	public int length() {
	    return length;
	}

	/** Reset for another pass over the same value set. */
	public void resetForSecondPass() {
	    assert (phase() == DISBURSE_PHASE);
	    assert (valuesDisbursed == length()); // 1st pass is complete
	    valuesDisbursed = 0;
	}

	/** Disburse one value. */
	protected int getValue() {
	    assert (phase() == DISBURSE_PHASE);
	    // when debugging return a zero if lengths are zero
	    if (optDebugBands && length == 0 && valuesDisbursed == length)
		return 0;
	    assert (valuesDisbursed &lt;= length);
	    return values[valuesDisbursed++];
	}

    }

}

