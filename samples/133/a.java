import java.util.Calendar;
import java.util.Date;

class DateUtils {
    /**
     * Determines how two calendars compare up to no more than the specified
     * most significant field.
     *
     * @param cal1 the first calendar, not &lt;code&gt;null&lt;/code&gt;
     * @param cal2 the second calendar, not &lt;code&gt;null&lt;/code&gt;
     * @param field the field from {@code Calendar}
     * @return a negative integer, zero, or a positive integer as the first
     * calendar is less than, equal to, or greater than the second.
     * @throws IllegalArgumentException if any argument is &lt;code&gt;null&lt;/code&gt;
     * @see #truncate(Calendar, int)
     * @see #truncatedCompareTo(Date, Date, int)
     * @since 3.0
     */
    public static int truncatedCompareTo(final Calendar cal1, final Calendar cal2, final int field) {
	final Calendar truncatedCal1 = truncate(cal1, field);
	final Calendar truncatedCal2 = truncate(cal2, field);
	return truncatedCal1.compareTo(truncatedCal2);
    }

    private static final int[][] fields = { { Calendar.MILLISECOND }, { Calendar.SECOND }, { Calendar.MINUTE },
	    { Calendar.HOUR_OF_DAY, Calendar.HOUR }, { Calendar.DATE, Calendar.DAY_OF_MONTH, Calendar.AM_PM
	    /* Calendar.DAY_OF_YEAR, Calendar.DAY_OF_WEEK, Calendar.DAY_OF_WEEK_IN_MONTH */
	    }, { Calendar.MONTH, SEMI_MONTH }, { Calendar.YEAR }, { Calendar.ERA } };
    /**
     * This is half a month, so this represents whether a date is in the top
     * or bottom half of the month.
     */
    public static final int SEMI_MONTH = 1001;

    /**
     * &lt;p&gt;Truncates a date, leaving the field specified as the most
     * significant field.&lt;/p&gt;
     *
     * &lt;p&gt;For example, if you had the date-time of 28 Mar 2002
     * 13:45:01.231, if you passed with HOUR, it would return 28 Mar
     * 2002 13:00:00.000.  If this was passed with MONTH, it would
     * return 1 Mar 2002 0:00:00.000.&lt;/p&gt;
     *
     * @param date  the date to work with, not null
     * @param field  the field from {@code Calendar} or &lt;code&gt;SEMI_MONTH&lt;/code&gt;
     * @return the different truncated date, not null
     * @throws IllegalArgumentException if the date is &lt;code&gt;null&lt;/code&gt;
     * @throws ArithmeticException if the year is over 280 million
     */
    public static Calendar truncate(final Calendar date, final int field) {
	if (date == null) {
	    throw new IllegalArgumentException("The date must not be null");
	}
	final Calendar truncated = (Calendar) date.clone();
	modify(truncated, field, ModifyType.TRUNCATE);
	return truncated;
    }

    /**
     * &lt;p&gt;Internal calculation method.&lt;/p&gt;
     *
     * @param val  the calendar, not null
     * @param field  the field constant
     * @param modType  type to truncate, round or ceiling
     * @throws ArithmeticException if the year is over 280 million
     */
    private static void modify(final Calendar val, final int field, final ModifyType modType) {
	if (val.get(Calendar.YEAR) &gt; 280000000) {
	    throw new ArithmeticException("Calendar value too large for accurate calculations");
	}

	if (field == Calendar.MILLISECOND) {
	    return;
	}

	// ----------------- Fix for LANG-59 ---------------------- START ---------------
	// see http://issues.apache.org/jira/browse/LANG-59
	//
	// Manually truncate milliseconds, seconds and minutes, rather than using
	// Calendar methods.

	final Date date = val.getTime();
	long time = date.getTime();
	boolean done = false;

	// truncate milliseconds
	final int millisecs = val.get(Calendar.MILLISECOND);
	if (ModifyType.TRUNCATE == modType || millisecs &lt; 500) {
	    time = time - millisecs;
	}
	if (field == Calendar.SECOND) {
	    done = true;
	}

	// truncate seconds
	final int seconds = val.get(Calendar.SECOND);
	if (!done && (ModifyType.TRUNCATE == modType || seconds &lt; 30)) {
	    time = time - (seconds * 1000L);
	}
	if (field == Calendar.MINUTE) {
	    done = true;
	}

	// truncate minutes
	final int minutes = val.get(Calendar.MINUTE);
	if (!done && (ModifyType.TRUNCATE == modType || minutes &lt; 30)) {
	    time = time - (minutes * 60000L);
	}

	// reset time
	if (date.getTime() != time) {
	    date.setTime(time);
	    val.setTime(date);
	}
	// ----------------- Fix for LANG-59 ----------------------- END ----------------

	boolean roundUp = false;
	for (final int[] aField : fields) {
	    for (final int element : aField) {
		if (element == field) {
		    //This is our field... we stop looping
		    if (modType == ModifyType.CEILING || modType == ModifyType.ROUND && roundUp) {
			if (field == SEMI_MONTH) {
			    //This is a special case that's hard to generalize
			    //If the date is 1, we round up to 16, otherwise
			    //  we subtract 15 days and add 1 month
			    if (val.get(Calendar.DATE) == 1) {
				val.add(Calendar.DATE, 15);
			    } else {
				val.add(Calendar.DATE, -15);
				val.add(Calendar.MONTH, 1);
			    }
			    // ----------------- Fix for LANG-440 ---------------------- START ---------------
			} else if (field == Calendar.AM_PM) {
			    // This is a special case
			    // If the time is 0, we round up to 12, otherwise
			    //  we subtract 12 hours and add 1 day
			    if (val.get(Calendar.HOUR_OF_DAY) == 0) {
				val.add(Calendar.HOUR_OF_DAY, 12);
			    } else {
				val.add(Calendar.HOUR_OF_DAY, -12);
				val.add(Calendar.DATE, 1);
			    }
			    // ----------------- Fix for LANG-440 ---------------------- END ---------------
			} else {
			    //We need at add one to this field since the
			    //  last number causes us to round up
			    val.add(aField[0], 1);
			}
		    }
		    return;
		}
	    }
	    //We have various fields that are not easy roundings
	    int offset = 0;
	    boolean offsetSet = false;
	    //These are special types of fields that require different rounding rules
	    switch (field) {
	    case SEMI_MONTH:
		if (aField[0] == Calendar.DATE) {
		    //If we're going to drop the DATE field's value,
		    //  we want to do this our own way.
		    //We need to subtrace 1 since the date has a minimum of 1
		    offset = val.get(Calendar.DATE) - 1;
		    //If we're above 15 days adjustment, that means we're in the
		    //  bottom half of the month and should stay accordingly.
		    if (offset &gt;= 15) {
			offset -= 15;
		    }
		    //Record whether we're in the top or bottom half of that range
		    roundUp = offset &gt; 7;
		    offsetSet = true;
		}
		break;
	    case Calendar.AM_PM:
		if (aField[0] == Calendar.HOUR_OF_DAY) {
		    //If we're going to drop the HOUR field's value,
		    //  we want to do this our own way.
		    offset = val.get(Calendar.HOUR_OF_DAY);
		    if (offset &gt;= 12) {
			offset -= 12;
		    }
		    roundUp = offset &gt;= 6;
		    offsetSet = true;
		}
		break;
	    default:
		break;
	    }
	    if (!offsetSet) {
		final int min = val.getActualMinimum(aField[0]);
		final int max = val.getActualMaximum(aField[0]);
		//Calculate the offset from the minimum allowed value
		offset = val.get(aField[0]) - min;
		//Set roundUp if this is more than half way between the minimum and maximum
		roundUp = offset &gt; ((max - min) / 2);
	    }
	    //We need to remove this field
	    if (offset != 0) {
		val.set(aField[0], val.get(aField[0]) - offset);
	    }
	}
	throw new IllegalArgumentException("The field " + field + " is not supported");

    }

}

