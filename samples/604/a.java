class FastDatePrinter implements DatePrinter, Serializable {
    /**
     * &lt;p&gt;Gets an appropriate rule for the padding required.&lt;/p&gt;
     *
     * @param field  the field to get a rule for
     * @param padding  the padding required
     * @return a new rule with the correct padding
     */
    protected NumberRule selectNumberRule(final int field, final int padding) {
	switch (padding) {
	case 1:
	    return new UnpaddedNumberField(field);
	case 2:
	    return new TwoDigitNumberField(field);
	default:
	    return new PaddedNumberField(field, padding);
	}
    }

    class UnpaddedNumberField implements NumberRule {
	/**
	 * Constructs an instance of {@code UnpadedNumberField} with the specified field.
	 *
	 * @param field the field
	 */
	UnpaddedNumberField(final int field) {
	    mField = field;
	}

    }

    class TwoDigitNumberField implements NumberRule {
	/**
	 * Constructs an instance of {@code TwoDigitNumberField} with the specified field.
	 *
	 * @param field the field
	 */
	TwoDigitNumberField(final int field) {
	    mField = field;
	}

    }

    class PaddedNumberField implements NumberRule {
	/**
	 * Constructs an instance of {@code PaddedNumberField}.
	 *
	 * @param field the field
	 * @param size size of the output field
	 */
	PaddedNumberField(final int field, final int size) {
	    if (size &lt; 3) {
		// Should use UnpaddedNumberField or TwoDigitNumberField.
		throw new IllegalArgumentException();
	    }
	    mField = field;
	    mSize = size;
	}

    }

}

