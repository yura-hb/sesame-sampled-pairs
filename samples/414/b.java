import java.time.temporal.ChronoUnit;

class TimeUnit extends Enum&lt;TimeUnit&gt; {
    /**
     * Converts this {@code TimeUnit} to the equivalent {@code ChronoUnit}.
     *
     * @return the converted equivalent ChronoUnit
     * @since 9
     */
    public ChronoUnit toChronoUnit() {
	switch (this) {
	case NANOSECONDS:
	    return ChronoUnit.NANOS;
	case MICROSECONDS:
	    return ChronoUnit.MICROS;
	case MILLISECONDS:
	    return ChronoUnit.MILLIS;
	case SECONDS:
	    return ChronoUnit.SECONDS;
	case MINUTES:
	    return ChronoUnit.MINUTES;
	case HOURS:
	    return ChronoUnit.HOURS;
	case DAYS:
	    return ChronoUnit.DAYS;
	default:
	    throw new AssertionError();
	}
    }

}

