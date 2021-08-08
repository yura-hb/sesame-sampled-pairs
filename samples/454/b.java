import static java.time.temporal.ChronoUnit.WEEKS;
import static java.time.temporal.ChronoUnit.YEARS;

class WeekFields implements Serializable {
    class ComputedDayOfField implements TemporalField {
	/**
	 * Returns a field to access the week of year,
	 * computed based on a WeekFields.
	 * @see WeekFields#weekOfYear()
	 */
	static ComputedDayOfField ofWeekOfYearField(WeekFields weekDef) {
	    return new ComputedDayOfField("WeekOfYear", weekDef, WEEKS, YEARS, WEEK_OF_YEAR_RANGE);
	}

	private static final ValueRange WEEK_OF_YEAR_RANGE = ValueRange.of(0, 1, 52, 54);
	private final String name;
	private final WeekFields weekDef;
	private final TemporalUnit baseUnit;
	private final TemporalUnit rangeUnit;
	private final ValueRange range;

	private ComputedDayOfField(String name, WeekFields weekDef, TemporalUnit baseUnit, TemporalUnit rangeUnit,
		ValueRange range) {
	    this.name = name;
	    this.weekDef = weekDef;
	    this.baseUnit = baseUnit;
	    this.rangeUnit = rangeUnit;
	    this.range = range;
	}

    }

}

