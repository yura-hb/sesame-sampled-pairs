abstract class BandStructure {
    abstract class Band {
	/** Expect a certain number of values. */
	void expectLength(int l) {
	    assert (assertPhase(this, EXPECT_PHASE));
	    assert (valuesExpected == 0); // all at once
	    assert (l &gt;= 0);
	    valuesExpected = l;
	}

	private int valuesExpected;
	private int phase = NO_PHASE;

	int phase() {
	    return phase;
	}

    }

    public static final int EXPECT_PHASE = 2;

    static boolean assertPhase(Band b, int phaseExpected) {
	if (b.phase() != phaseExpected) {
	    Utils.log.warning("phase expected " + phaseExpected + " was " + b.phase() + " in " + b);
	    return false;
	}
	return true;
    }

}

