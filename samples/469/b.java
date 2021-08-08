class Condition extends Enum&lt;Condition&gt; {
    /**
     * Negate this conditional.
     *
     * @return the condition that represents the negation
     */
    public final Condition negate() {
	switch (this) {
	case EQ:
	    return NE;
	case NE:
	    return EQ;
	case LT:
	    return GE;
	case LE:
	    return GT;
	case GT:
	    return LE;
	case GE:
	    return LT;
	case BT:
	    return AE;
	case BE:
	    return AT;
	case AT:
	    return BE;
	case AE:
	    return BT;
	}
	throw new IllegalArgumentException(this.toString());
    }

}

