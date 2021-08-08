import com.sun.org.apache.xerces.internal.util.DatatypeMessageFormatter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.Duration;

class XMLGregorianCalendarImpl extends XMLGregorianCalendar implements Serializable, Cloneable {
    /**
     * &lt;p&gt;Create a Java instance of XML Schema builtin datatype time.&lt;/p&gt;
     *
     * @param hours number of hours
     * @param minutes number of minutes
     * @param seconds number of seconds
     * @param milliseconds number of milliseconds
     * @param timezone offset in minutes. {@link DatatypeConstants#FIELD_UNDEFINED} indicates optional field is not set.
     *
     * @return &lt;code&gt;XMLGregorianCalendar&lt;/code&gt; created from parameter values.
     *
     * @see DatatypeConstants#FIELD_UNDEFINED
     *
     * @throws IllegalArgumentException if any parameter is outside value
     * constraints for the field as specified in
     * &lt;a href="#datetimefieldmapping"&gt;date/time field mapping table&lt;/a&gt;.
     */
    public static XMLGregorianCalendar createTime(int hours, int minutes, int seconds, int milliseconds, int timezone) {

	return new XMLGregorianCalendarImpl(DatatypeConstants.FIELD_UNDEFINED, // year
		DatatypeConstants.FIELD_UNDEFINED, // month
		DatatypeConstants.FIELD_UNDEFINED, // day
		hours, minutes, seconds, milliseconds, timezone);
    }

    /**
     * &lt;p&gt;Year of this &lt;code&gt;XMLGregorianCalendar&lt;/code&gt;.&lt;/p&gt;
     */
    private int year = DatatypeConstants.FIELD_UNDEFINED;
    /**
     * &lt;p&gt;Eon of this &lt;code&gt;XMLGregorianCalendar&lt;/code&gt;.&lt;/p&gt;
     */
    private BigInteger eon = null;
    /**
     * &lt;p&gt;int constant; representing a billion.&lt;/p&gt;
     */
    private static final int BILLION_I = 1000000000;
    /**
     * &lt;p&gt;BigInteger constant; representing a billion.&lt;/p&gt;
     */
    private static final BigInteger BILLION_B = new BigInteger("1000000000");
    /**
     * Month index for MIN_ and MAX_FIELD_VALUES.
     */
    private static final int MONTH = 1;
    /**
     * &lt;p&gt;Month of this &lt;code&gt;XMLGregorianCalendar&lt;/code&gt;.&lt;/p&gt;
     */
    private int month = DatatypeConstants.FIELD_UNDEFINED;
    /**
     * Day index for MIN_ and MAX_FIELD_VALUES.
     */
    private static final int DAY = 2;
    /**
     * &lt;p&gt;Day of this &lt;code&gt;XMLGregorianCalendar&lt;/code&gt;.&lt;/p&gt;
     */
    private int day = DatatypeConstants.FIELD_UNDEFINED;
    /**
     * Timezone index for MIN_ and MAX_FIELD_VALUES
     */
    private static final int TIMEZONE = 7;
    /**
     * &lt;p&gt;Timezone of this &lt;code&gt;XMLGregorianCalendar&lt;/code&gt; in minutes.&lt;/p&gt;
     */
    private int timezone = DatatypeConstants.FIELD_UNDEFINED;
    private static final BigDecimal DECIMAL_ZERO = BigDecimal.valueOf(0);
    private static final BigDecimal DECIMAL_ONE = BigDecimal.valueOf(1);
    /**
     * &lt;p&gt;Fractional second of this &lt;code&gt;XMLGregorianCalendar&lt;/code&gt;.&lt;/p&gt;
     */
    private BigDecimal fractionalSecond = null;
    /**
     * &lt;p&gt;Hour of this &lt;code&gt;XMLGregorianCalendar&lt;/code&gt;.&lt;/p&gt;
     */
    private int hour = DatatypeConstants.FIELD_UNDEFINED;
    /**
     * &lt;p&gt;Minute of this &lt;code&gt;XMLGregorianCalendar&lt;/code&gt;.&lt;/p&gt;
     */
    private int minute = DatatypeConstants.FIELD_UNDEFINED;
    /**
     * &lt;p&gt;Second of this &lt;code&gt;XMLGregorianCalendar&lt;/code&gt;.&lt;/p&gt;
     */
    private int second = DatatypeConstants.FIELD_UNDEFINED;
    /** Backup values **/
    transient private BigInteger orig_eon;
    transient private int orig_year = DatatypeConstants.FIELD_UNDEFINED;
    transient private int orig_month = DatatypeConstants.FIELD_UNDEFINED;
    transient private int orig_day = DatatypeConstants.FIELD_UNDEFINED;
    transient private int orig_hour = DatatypeConstants.FIELD_UNDEFINED;
    transient private int orig_minute = DatatypeConstants.FIELD_UNDEFINED;
    transient private int orig_second = DatatypeConstants.FIELD_UNDEFINED;
    transient private BigDecimal orig_fracSeconds;
    transient private int orig_timezone = DatatypeConstants.FIELD_UNDEFINED;
    /**
     * field names indexed by YEAR..TIMEZONE.
     */
    private static final String FIELD_NAME[] = { "Year", "Month", "Day", "Hour", "Minute", "Second", "Millisecond",
	    "Timezone" };
    /**
     * Second index for MIN_ and MAX_FIELD_VALUES.
     */
    private static final int SECOND = 5;
    private static final BigInteger FOUR_HUNDRED = BigInteger.valueOf(400);
    private static final BigInteger HUNDRED = BigInteger.valueOf(100);
    private static final BigInteger FOUR = BigInteger.valueOf(4);
    /**
     * Hour index for MIN_ and MAX_FIELD_VALUES.
     */
    private static final int HOUR = 3;
    /**
     * Minute index for MIN_ and MAX_FIELD_VALUES.
     */
    private static final int MINUTE = 4;
    private static final BigInteger TWELVE = BigInteger.valueOf(12);
    private static final BigDecimal DECIMAL_TWELVE = BigDecimal.valueOf(12);
    /**
     * Year index for MIN_ and MAX_FIELD_VALUES.
     */
    private static final int YEAR = 0;
    private static final BigDecimal DECIMAL_SIXTY = BigDecimal.valueOf(60);
    private static final BigInteger SIXTY = BigInteger.valueOf(60);
    private static final BigInteger TWENTY_FOUR = BigInteger.valueOf(24);
    private static final BigDecimal DECIMAL_TWENTY_FOUR = BigDecimal.valueOf(24);

    /**
     * &lt;p&gt;Private constructor of value spaces that a
     * &lt;code&gt;java.util.GregorianCalendar&lt;/code&gt; instance would need to convert to an
     * &lt;code&gt;XMLGregorianCalendar&lt;/code&gt; instance.&lt;/p&gt;
     *
     * &lt;p&gt;&lt;code&gt;XMLGregorianCalendar eon&lt;/code&gt; and
     * &lt;code&gt;fractionalSecond&lt;/code&gt; are set to &lt;code&gt;null&lt;/code&gt;&lt;/p&gt;
     *
     * @param year of &lt;code&gt;XMLGregorianCalendar&lt;/code&gt; to be created.
     * @param month of &lt;code&gt;XMLGregorianCalendar&lt;/code&gt; to be created.
     * @param day of &lt;code&gt;XMLGregorianCalendar&lt;/code&gt; to be created.
     * @param hour of &lt;code&gt;XMLGregorianCalendar&lt;/code&gt; to be created.
     * @param minute of &lt;code&gt;XMLGregorianCalendar&lt;/code&gt; to be created.
     * @param second of &lt;code&gt;XMLGregorianCalendar&lt;/code&gt; to be created.
     * @param millisecond of &lt;code&gt;XMLGregorianCalendar&lt;/code&gt; to be created.
     * @param timezone of &lt;code&gt;XMLGregorianCalendar&lt;/code&gt; to be created.
     */
    private XMLGregorianCalendarImpl(int year, int month, int day, int hour, int minute, int second, int millisecond,
	    int timezone) {

	setYear(year);
	setMonth(month);
	setDay(day);
	setTime(hour, minute, second);
	setTimezone(timezone);
	BigDecimal realMilliseconds = null;
	if (millisecond != DatatypeConstants.FIELD_UNDEFINED) {
	    realMilliseconds = BigDecimal.valueOf(millisecond, 3);
	}
	setFractionalSecond(realMilliseconds);

	if (!isValid()) {

	    throw new IllegalArgumentException(DatatypeMessageFormatter.formatMessage(null, "InvalidXGCValue-milli",
		    new Object[] { year, month, day, hour, minute, second, millisecond, timezone }));
	}

	save();
    }

    /**
     * &lt;p&gt;Set year of XSD &lt;code&gt;dateTime&lt;/code&gt; year field.&lt;/p&gt;
     *
     * &lt;p&gt;Unset this field by invoking the setter with a parameter value of
     * {@link DatatypeConstants#FIELD_UNDEFINED}.&lt;/p&gt;
     *
     * &lt;p&gt;Note: if the absolute value of the &lt;code&gt;year&lt;/code&gt; parameter
     * is less than 10^9, the eon component of the XSD year field is set to
     * &lt;code&gt;null&lt;/code&gt; by this method.&lt;/p&gt;
     *
     * @param year value constraints are summarized in &lt;a href="#datetimefield-year"&gt;year field of date/time field mapping table&lt;/a&gt;.
     *   If year is {@link DatatypeConstants#FIELD_UNDEFINED}, then eon is set to &lt;code&gt;null&lt;/code&gt;.
     */
    public final void setYear(int year) {
	if (year == DatatypeConstants.FIELD_UNDEFINED) {
	    this.year = DatatypeConstants.FIELD_UNDEFINED;
	    this.eon = null;
	} else if (Math.abs(year) &lt; BILLION_I) {
	    this.year = year;
	    this.eon = null;
	} else {
	    BigInteger theYear = BigInteger.valueOf((long) year);
	    BigInteger remainder = theYear.remainder(BILLION_B);
	    this.year = remainder.intValue();
	    setEon(theYear.subtract(remainder));
	}
    }

    /**
     * &lt;p&gt;Set month.&lt;/p&gt;
     *
     * &lt;p&gt;Unset this field by invoking the setter with a parameter value of {@link DatatypeConstants#FIELD_UNDEFINED}.&lt;/p&gt;
     *
     * @param month value constraints summarized in &lt;a href="#datetimefield-month"&gt;month field of date/time field mapping table&lt;/a&gt;.
     *
     * @throws IllegalArgumentException if &lt;code&gt;month&lt;/code&gt; parameter is
     * outside value constraints for the field as specified in
     * &lt;a href="#datetimefieldmapping"&gt;date/time field mapping table&lt;/a&gt;.
     */
    public final void setMonth(int month) {
	if (month &lt; DatatypeConstants.JANUARY || DatatypeConstants.DECEMBER &lt; month)
	    if (month != DatatypeConstants.FIELD_UNDEFINED)
		invalidFieldValue(MONTH, month);
	this.month = month;
    }

    /**
     * &lt;p&gt;Set days in month.&lt;/p&gt;
     *
     * &lt;p&gt;Unset this field by invoking the setter with a parameter value of {@link DatatypeConstants#FIELD_UNDEFINED}.&lt;/p&gt;
     *
     * @param day value constraints summarized in &lt;a href="#datetimefield-day"&gt;day field of date/time field mapping table&lt;/a&gt;.
     *
     * @throws IllegalArgumentException if &lt;code&gt;day&lt;/code&gt; parameter is
     * outside value constraints for the field as specified in
     * &lt;a href="#datetimefieldmapping"&gt;date/time field mapping table&lt;/a&gt;.
     */
    public final void setDay(int day) {
	if (day &lt; 1 || 31 &lt; day)
	    if (day != DatatypeConstants.FIELD_UNDEFINED)
		invalidFieldValue(DAY, day);
	this.day = day;
    }

    /**
     * &lt;p&gt;Set time as one unit.&lt;/p&gt;
     *
     * @param hour value constraints are summarized in
     * &lt;a href="#datetimefield-hour"&gt;hour field of date/time field mapping table&lt;/a&gt;.
     * @param minute value constraints are summarized in
     * &lt;a href="#datetimefield-minute"&gt;minute field of date/time field mapping table&lt;/a&gt;.
     * @param second value constraints are summarized in
     * &lt;a href="#datetimefield-second"&gt;second field of date/time field mapping table&lt;/a&gt;.
     *
     * @see #setTime(int, int, int, BigDecimal)
     *
     * @throws IllegalArgumentException if any parameter is
     * outside value constraints for the field as specified in
     * &lt;a href="#datetimefieldmapping"&gt;date/time field mapping table&lt;/a&gt;.
     */
    public final void setTime(int hour, int minute, int second) {
	setTime(hour, minute, second, null);
    }

    /**
     * &lt;p&gt;Set the number of minutes in the timezone offset.&lt;/p&gt;
     *
     * &lt;p&gt;Unset this field by invoking the setter with a parameter value of {@link DatatypeConstants#FIELD_UNDEFINED}.&lt;/p&gt;
     *
     * @param offset value constraints summarized in &lt;a href="#datetimefield-timezone"&gt;
     *   timezone field of date/time field mapping table&lt;/a&gt;.
     *
     * @throws IllegalArgumentException if &lt;code&gt;offset&lt;/code&gt; parameter is
     * outside value constraints for the field as specified in
     * &lt;a href="#datetimefieldmapping"&gt;date/time field mapping table&lt;/a&gt;.
     */
    public final void setTimezone(int offset) {
	if (offset &lt; -14 * 60 || 14 * 60 &lt; offset)
	    if (offset != DatatypeConstants.FIELD_UNDEFINED)
		invalidFieldValue(TIMEZONE, offset);
	this.timezone = offset;
    }

    public final void setFractionalSecond(BigDecimal fractional) {
	if (fractional != null) {
	    if ((fractional.compareTo(DECIMAL_ZERO) &lt; 0) || (fractional.compareTo(DECIMAL_ONE) &gt; 0)) {
		throw new IllegalArgumentException(
			DatatypeMessageFormatter.formatMessage(null, "InvalidFractional", new Object[] { fractional }));
	    }
	}
	this.fractionalSecond = fractional;
    }

    /**
     * Validate instance by &lt;code&gt;getXMLSchemaType()&lt;/code&gt; constraints.
     * @return true if data values are valid.
     */
    public final boolean isValid() {
	// since setters do not allow for invalid values,
	// (except for exceptional case of year field of zero),
	// no need to check for anything except for constraints
	// between fields.

	// check if days in month is valid. Can be dependent on leap year.
	if (month != DatatypeConstants.FIELD_UNDEFINED && day != DatatypeConstants.FIELD_UNDEFINED) {
	    if (year != DatatypeConstants.FIELD_UNDEFINED) {
		if (eon == null) {
		    if (day &gt; maximumDayInMonthFor(year, month)) {
			return false;
		    }
		} else if (day &gt; maximumDayInMonthFor(getEonAndYear(), month)) {
		    return false;
		}
	    }
	    // Use 2000 as a default since it's a leap year.
	    else if (day &gt; maximumDayInMonthFor(2000, month)) {
		return false;
	    }
	}

	// http://www.w3.org/2001/05/xmlschema-errata#e2-45
	if (hour == 24 && (minute != 0 || second != 0
		|| (fractionalSecond != null && fractionalSecond.compareTo(DECIMAL_ZERO) != 0))) {
	    return false;
	}

	// XML Schema 1.0 specification defines year value of zero as
	// invalid. Allow this class to set year field to zero
	// since XML Schema 1.0 errata states that lexical zero will
	// be allowed in next version and treated as 1 B.C.E.
	if (eon == null && year == 0) {
	    return false;
	}
	return true;
    }

    /**
     * save original values
     */
    private void save() {
	orig_eon = eon;
	orig_year = year;
	orig_month = month;
	orig_day = day;
	orig_hour = hour;
	orig_minute = minute;
	orig_second = second;
	orig_fracSeconds = fractionalSecond;
	orig_timezone = timezone;
    }

    /**
     * &lt;p&gt;Set high order part of XSD &lt;code&gt;dateTime&lt;/code&gt; year field.&lt;/p&gt;
     *
     * &lt;p&gt;Unset this field by invoking the setter with a parameter value of
     * &lt;code&gt;null&lt;/code&gt;.&lt;/p&gt;
     *
     * @param eon value constraints summarized in &lt;a href="#datetimefield-year"&gt;year field of date/time field mapping table&lt;/a&gt;.
     */
    private void setEon(BigInteger eon) {
	if (eon != null && eon.compareTo(BigInteger.ZERO) == 0) {
	    // Treat ZERO as field being undefined.
	    this.eon = null;
	} else {
	    this.eon = eon;
	}
    }

    private void invalidFieldValue(int field, int value) {
	throw new IllegalArgumentException(DatatypeMessageFormatter.formatMessage(null, "InvalidFieldValue",
		new Object[] { value, FIELD_NAME[field] }));
    }

    /**
     * &lt;p&gt;Set time as one unit, including the optional infinite precison
     * fractional seconds.&lt;/p&gt;
     *
     * @param hour value constraints are summarized in
     * &lt;a href="#datetimefield-hour"&gt;hour field of date/time field mapping table&lt;/a&gt;.
     * @param minute value constraints are summarized in
     * &lt;a href="#datetimefield-minute"&gt;minute field of date/time field mapping table&lt;/a&gt;.
     * @param second value constraints are summarized in
     * &lt;a href="#datetimefield-second"&gt;second field of date/time field mapping table&lt;/a&gt;.
     * @param fractional value of &lt;code&gt;null&lt;/code&gt; indicates this optional
     *                   field is not set.
     *
     * @throws IllegalArgumentException if any parameter is
     * outside value constraints for the field as specified in
     * &lt;a href="#datetimefieldmapping"&gt;date/time field mapping table&lt;/a&gt;.
     */
    public final void setTime(int hour, int minute, int second, BigDecimal fractional) {

	setHour(hour, false);

	setMinute(minute);
	if (second != 60) {
	    setSecond(second);
	} else if ((hour == 23 && minute == 59) || (hour == 0 && minute == 0)) {
	    setSecond(second);
	} else {
	    invalidFieldValue(SECOND, second);
	}

	setFractionalSecond(fractional);

	// must test hour after setting seconds
	testHour();
    }

    private static int maximumDayInMonthFor(int year, int month) {
	if (month != DatatypeConstants.FEBRUARY) {
	    return DaysInMonth.table[month];
	} else {
	    if (((year % 400) == 0) || (((year % 100) != 0) && ((year % 4) == 0))) {
		// is a leap year.
		return 29;
	    } else {
		return DaysInMonth.table[DatatypeConstants.FEBRUARY];
	    }
	}
    }

    /**
     * &lt;p&gt;Return XML Schema 1.0 dateTime datatype field for
     * &lt;code&gt;year&lt;/code&gt;.&lt;/p&gt;
     *
     * &lt;p&gt;Value constraints for this value are summarized in
     * &lt;a href="#datetimefield-year"&gt;year field of date/time field mapping table&lt;/a&gt;.&lt;/p&gt;
     *
     * @return sum of &lt;code&gt;eon&lt;/code&gt; and &lt;code&gt;BigInteger.valueOf(year)&lt;/code&gt;
     * when both fields are defined. When only &lt;code&gt;year&lt;/code&gt; is defined,
     * return it. When both &lt;code&gt;eon&lt;/code&gt; and &lt;code&gt;year&lt;/code&gt; are not
     * defined, return &lt;code&gt;null&lt;/code&gt;.
     *
     * @see #getEon()
     * @see #getYear()
     */
    public BigInteger getEonAndYear() {

	// both are defined
	if (year != DatatypeConstants.FIELD_UNDEFINED && eon != null) {

	    return eon.add(BigInteger.valueOf((long) year));
	}

	// only year is defined
	if (year != DatatypeConstants.FIELD_UNDEFINED && eon == null) {

	    return BigInteger.valueOf((long) year);
	}

	// neither are defined
	// or only eon is defined which is not valid without a year
	return null;
    }

    private static int maximumDayInMonthFor(BigInteger year, int month) {
	if (month != DatatypeConstants.FEBRUARY) {
	    return DaysInMonth.table[month];
	} else {
	    if (year.mod(FOUR_HUNDRED).equals(BigInteger.ZERO)
		    || (!year.mod(HUNDRED).equals(BigInteger.ZERO) && year.mod(FOUR).equals(BigInteger.ZERO))) {
		// is a leap year.
		return 29;
	    } else {
		return DaysInMonth.table[month];
	    }
	}
    }

    private void setHour(int hour, boolean validate) {

	if (hour &lt; 0 || hour &gt; 24) {
	    if (hour != DatatypeConstants.FIELD_UNDEFINED) {
		invalidFieldValue(HOUR, hour);
	    }
	}

	this.hour = hour;

	if (validate) {
	    testHour();
	}
    }

    public void setMinute(int minute) {
	if (minute &lt; 0 || 59 &lt; minute)
	    if (minute != DatatypeConstants.FIELD_UNDEFINED)
		invalidFieldValue(MINUTE, minute);
	this.minute = minute;
    }

    public void setSecond(int second) {
	if (second &lt; 0 || 60 &lt; second) // leap second allows for 60
	    if (second != DatatypeConstants.FIELD_UNDEFINED)
		invalidFieldValue(SECOND, second);
	this.second = second;
    }

    private void testHour() {

	// http://www.w3.org/2001/05/xmlschema-errata#e2-45
	if (getHour() == 24) {
	    if (getMinute() != 0 || getSecond() != 0) {
		invalidFieldValue(HOUR, getHour());
	    }
	    // while 0-24 is acceptable in the lexical space, 24 is not valid in value space
	    // W3C XML Schema Part 2, Section 3.2.7.1
	    setHour(0, false);
	    add(new DurationImpl(true, 0, 0, 1, 0, 0, 0));
	}
    }

    /**
     * Return hours or {@link DatatypeConstants#FIELD_UNDEFINED}.
     * Returns {@link DatatypeConstants#FIELD_UNDEFINED} if this field is not defined.
     *
     * &lt;p&gt;Value constraints for this value are summarized in
     * &lt;a href="#datetimefield-hour"&gt;hour field of date/time field mapping table&lt;/a&gt;.&lt;/p&gt;
     * @see #setTime(int, int, int)
     */
    public int getHour() {
	return hour;
    }

    /**
     * Return minutes or {@link DatatypeConstants#FIELD_UNDEFINED}.&lt;\p&gt;
     * Returns {@link DatatypeConstants#FIELD_UNDEFINED} if this field is not defined.
     *
     * &lt;p&gt;Value constraints for this value are summarized in
     * &lt;a href="#datetimefield-minute"&gt;minute field of date/time field mapping table&lt;/a&gt;.&lt;/p&gt;
     * @see #setTime(int, int, int)
     */
    public int getMinute() {
	return minute;
    }

    /**
     * &lt;p&gt;Return seconds or {@link DatatypeConstants#FIELD_UNDEFINED}.&lt;\p&gt;
     *
     * &lt;p&gt;Returns {@link DatatypeConstants#FIELD_UNDEFINED} if this field is not defined.
     * When this field is not defined, the optional xs:dateTime
     * fractional seconds field, represented by
     * {@link #getFractionalSecond()} and {@link #getMillisecond()},
     * must not be defined.&lt;/p&gt;
     *
     * &lt;p&gt;Value constraints for this value are summarized in
     * &lt;a href="#datetimefield-second"&gt;second field of date/time field mapping table&lt;/a&gt;.&lt;/p&gt;
     *
     * @return Second  of this &lt;code&gt;XMLGregorianCalendar&lt;/code&gt;.
     *
     * @see #getFractionalSecond()
     * @see #getMillisecond()
     * @see #setTime(int, int, int)
     */
    public int getSecond() {
	return second;
    }

    /**
     * &lt;p&gt;Add &lt;code&gt;duration&lt;/code&gt; to this instance.&lt;\p&gt;
     *
     * &lt;p&gt;The computation is specified in
     * &lt;a href="http://www.w3.org/TR/xmlschema-2/#adding-durations-to-dateTimes"&gt;XML Schema 1.0 Part 2, Appendix E,
     * &lt;i&gt;Adding durations to dateTimes&lt;/i&gt;&gt;&lt;/a&gt;.
     * &lt;a href="#datetimefieldsmapping"&gt;date/time field mapping table&lt;/a&gt;
     * defines the mapping from XML Schema 1.0 &lt;code&gt;dateTime&lt;/code&gt; fields
     * to this class' representation of those fields.&lt;/p&gt;
     *
     * @param duration Duration to add to this &lt;code&gt;XMLGregorianCalendar&lt;/code&gt;.
     *
     * @throws NullPointerException  when &lt;code&gt;duration&lt;/code&gt; parameter is &lt;code&gt;null&lt;/code&gt;.
     */
    public void add(Duration duration) {

	/*
	   * Extracted from
	   * http://www.w3.org/TR/xmlschema-2/#adding-durations-to-dateTimes
	   * to ensure implemented properly. See spec for definitions of methods
	   * used in algorithm.
	   *
	   * Given a dateTime S and a duration D, specifies how to compute a
	   * dateTime E where E is the end of the time period with start S and
	   * duration D i.e. E = S + D.
	   *
	   * The following is the precise specification.
	   * These steps must be followed in the same order.
	   * If a field in D is not specified, it is treated as if it were zero.
	   * If a field in S is not specified, it is treated in the calculation
	   * as if it were the minimum allowed value in that field, however,
	   * after the calculation is concluded, the corresponding field in
	   * E is removed (set to unspecified).
	   *
	   * Months (may be modified additionally below)
	       *  temp := S[month] + D[month]
	       *  E[month] := modulo(temp, 1, 13)
	       *  carry := fQuotient(temp, 1, 13)
	   */

	boolean fieldUndefined[] = { false, false, false, false, false, false };

	int signum = duration.getSign();

	int startMonth = getMonth();
	if (startMonth == DatatypeConstants.FIELD_UNDEFINED) {
	    startMonth = DatatypeConstants.JANUARY;
	    fieldUndefined[MONTH] = true;
	}

	BigInteger dMonths = sanitize(duration.getField(DatatypeConstants.MONTHS), signum);
	BigInteger temp = BigInteger.valueOf((long) startMonth).add(dMonths);
	setMonth(temp.subtract(BigInteger.ONE).mod(TWELVE).intValue() + 1);
	BigInteger carry = new BigDecimal(temp.subtract(BigInteger.ONE)).divide(DECIMAL_TWELVE, RoundingMode.FLOOR)
		.toBigInteger();

	/* Years (may be modified additionally below)
	    *  E[year] := S[year] + D[year] + carry
	    */
	BigInteger startYear = getEonAndYear();
	if (startYear == null) {
	    fieldUndefined[YEAR] = true;
	    startYear = BigInteger.ZERO;
	}
	BigInteger dYears = sanitize(duration.getField(DatatypeConstants.YEARS), signum);
	BigInteger endYear = startYear.add(dYears).add(carry);
	setYear(endYear);

	/* Zone
	       *  E[zone] := S[zone]
	   *
	   * no-op since adding to this, not to a new end point.
	   */

	/* Seconds
	    *  temp := S[second] + D[second]
	    *  E[second] := modulo(temp, 60)
	    *  carry := fQuotient(temp, 60)
	    */
	BigDecimal startSeconds;
	if (getSecond() == DatatypeConstants.FIELD_UNDEFINED) {
	    fieldUndefined[SECOND] = true;
	    startSeconds = DECIMAL_ZERO;
	} else {
	    // seconds + fractionalSeconds
	    startSeconds = getSeconds();
	}

	// Duration seconds is SECONDS + FRACTIONALSECONDS.
	BigDecimal dSeconds = DurationImpl.sanitize((BigDecimal) duration.getField(DatatypeConstants.SECONDS), signum);
	BigDecimal tempBD = startSeconds.add(dSeconds);
	BigDecimal fQuotient = new BigDecimal(
		new BigDecimal(tempBD.toBigInteger()).divide(DECIMAL_SIXTY, RoundingMode.FLOOR).toBigInteger());
	BigDecimal endSeconds = tempBD.subtract(fQuotient.multiply(DECIMAL_SIXTY));

	carry = fQuotient.toBigInteger();
	setSecond(endSeconds.intValue());
	BigDecimal tempFracSeconds = endSeconds.subtract(new BigDecimal(BigInteger.valueOf((long) getSecond())));
	if (tempFracSeconds.compareTo(DECIMAL_ZERO) &lt; 0) {
	    setFractionalSecond(DECIMAL_ONE.add(tempFracSeconds));
	    if (getSecond() == 0) {
		setSecond(59);
		carry = carry.subtract(BigInteger.ONE);
	    } else {
		setSecond(getSecond() - 1);
	    }
	} else {
	    setFractionalSecond(tempFracSeconds);
	}

	/* Minutes
	       *  temp := S[minute] + D[minute] + carry
	       *  E[minute] := modulo(temp, 60)
	       *  carry := fQuotient(temp, 60)
	   */
	int startMinutes = getMinute();
	if (startMinutes == DatatypeConstants.FIELD_UNDEFINED) {
	    fieldUndefined[MINUTE] = true;
	    startMinutes = 0;
	}
	BigInteger dMinutes = sanitize(duration.getField(DatatypeConstants.MINUTES), signum);

	temp = BigInteger.valueOf(startMinutes).add(dMinutes).add(carry);
	setMinute(temp.mod(SIXTY).intValue());
	carry = new BigDecimal(temp).divide(DECIMAL_SIXTY, RoundingMode.FLOOR).toBigInteger();

	/* Hours
	       *  temp := S[hour] + D[hour] + carry
	       *  E[hour] := modulo(temp, 24)
	       *  carry := fQuotient(temp, 24)
	   */
	int startHours = getHour();
	if (startHours == DatatypeConstants.FIELD_UNDEFINED) {
	    fieldUndefined[HOUR] = true;
	    startHours = 0;
	}
	BigInteger dHours = sanitize(duration.getField(DatatypeConstants.HOURS), signum);

	temp = BigInteger.valueOf(startHours).add(dHours).add(carry);
	setHour(temp.mod(TWENTY_FOUR).intValue(), false);
	carry = new BigDecimal(temp).divide(DECIMAL_TWENTY_FOUR, RoundingMode.FLOOR).toBigInteger();

	/* Days
	   *  if S[day] &gt; maximumDayInMonthFor(E[year], E[month])
	   *       + tempDays := maximumDayInMonthFor(E[year], E[month])
	   *  else if S[day] &lt; 1
	   *       + tempDays := 1
	   *  else
	   *       + tempDays := S[day]
	   *  E[day] := tempDays + D[day] + carry
	   *  START LOOP
	   *       + IF E[day] &lt; 1
	   *             # E[day] := E[day] +
	    *                 maximumDayInMonthFor(E[year], E[month] - 1)
	   *             # carry := -1
	   *       + ELSE IF E[day] &gt; maximumDayInMonthFor(E[year], E[month])
	   *             # E[day] :=
	    *                    E[day] - maximumDayInMonthFor(E[year], E[month])
	   *             # carry := 1
	   *       + ELSE EXIT LOOP
	   *       + temp := E[month] + carry
	   *       + E[month] := modulo(temp, 1, 13)
	   *       + E[year] := E[year] + fQuotient(temp, 1, 13)
	   *       + GOTO START LOOP
	   */
	BigInteger tempDays;
	int startDay = getDay();
	if (startDay == DatatypeConstants.FIELD_UNDEFINED) {
	    fieldUndefined[DAY] = true;
	    startDay = 1;
	}
	BigInteger dDays = sanitize(duration.getField(DatatypeConstants.DAYS), signum);
	int maxDayInMonth = maximumDayInMonthFor(getEonAndYear(), getMonth());
	if (startDay &gt; maxDayInMonth) {
	    tempDays = BigInteger.valueOf(maxDayInMonth);
	} else if (startDay &lt; 1) {
	    tempDays = BigInteger.ONE;
	} else {
	    tempDays = BigInteger.valueOf(startDay);
	}
	BigInteger endDays = tempDays.add(dDays).add(carry);
	int monthCarry;
	int intTemp;
	while (true) {
	    if (endDays.compareTo(BigInteger.ONE) &lt; 0) {
		// calculate days in previous month, watch for month roll over
		BigInteger mdimf = null;
		if (month &gt;= 2) {
		    mdimf = BigInteger.valueOf(maximumDayInMonthFor(getEonAndYear(), getMonth() - 1));
		} else {
		    // roll over to December of previous year
		    mdimf = BigInteger.valueOf(maximumDayInMonthFor(getEonAndYear().subtract(BigInteger.ONE), 12));
		}
		endDays = endDays.add(mdimf);
		monthCarry = -1;
	    } else if (endDays.compareTo(BigInteger.valueOf(maximumDayInMonthFor(getEonAndYear(), getMonth()))) &gt; 0) {
		endDays = endDays.add(BigInteger.valueOf(-maximumDayInMonthFor(getEonAndYear(), getMonth())));
		monthCarry = 1;
	    } else {
		break;
	    }

	    intTemp = getMonth() + monthCarry;
	    int endMonth = (intTemp - 1) % (13 - 1);
	    int quotient;
	    if (endMonth &lt; 0) {
		endMonth = (13 - 1) + endMonth + 1;
		quotient = BigDecimal.valueOf(intTemp - 1).divide(DECIMAL_TWELVE, RoundingMode.UP).intValue();
	    } else {
		quotient = (intTemp - 1) / (13 - 1);
		endMonth += 1;
	    }
	    setMonth(endMonth);
	    if (quotient != 0) {
		setYear(getEonAndYear().add(BigInteger.valueOf(quotient)));
	    }
	}
	setDay(endDays.intValue());

	// set fields that where undefined before this addition, back to undefined.
	for (int i = YEAR; i &lt;= SECOND; i++) {
	    if (fieldUndefined[i]) {
		switch (i) {
		case YEAR:
		    setYear(DatatypeConstants.FIELD_UNDEFINED);
		    break;
		case MONTH:
		    setMonth(DatatypeConstants.FIELD_UNDEFINED);
		    break;
		case DAY:
		    setDay(DatatypeConstants.FIELD_UNDEFINED);
		    break;
		case HOUR:
		    setHour(DatatypeConstants.FIELD_UNDEFINED, false);
		    break;
		case MINUTE:
		    setMinute(DatatypeConstants.FIELD_UNDEFINED);
		    break;
		case SECOND:
		    setSecond(DatatypeConstants.FIELD_UNDEFINED);
		    setFractionalSecond(null);
		    break;
		}
	    }
	}
    }

    /**
     * &lt;p&gt;Return number of month or {@link DatatypeConstants#FIELD_UNDEFINED}.&lt;/p&gt;
     *
     * &lt;p&gt;Value constraints for this value are summarized in
     * &lt;a href="#datetimefield-month"&gt;month field of date/time field mapping table&lt;/a&gt;.&lt;/p&gt;
     *
     * @return year  of this &lt;code&gt;XMLGregorianCalendar&lt;/code&gt;.
     *
     */
    public int getMonth() {
	return month;
    }

    /**
     * Compute &lt;code&gt;value*signum&lt;/code&gt; where value==null is treated as
     * value==0.
     * @return non-null {@link BigInteger}.
     */
    static BigInteger sanitize(Number value, int signum) {
	if (signum == 0 || value == null) {
	    return BigInteger.ZERO;
	}
	return (signum &lt; 0) ? ((BigInteger) value).negate() : (BigInteger) value;
    }

    /**
     * &lt;p&gt;Set low and high order component of XSD &lt;code&gt;dateTime&lt;/code&gt; year field.&lt;/p&gt;
     *
     * &lt;p&gt;Unset this field by invoking the setter with a parameter value of &lt;code&gt;null&lt;/code&gt;.&lt;/p&gt;
     *
     * @param year value constraints summarized in &lt;a href="#datetimefield-year"&gt;year field of date/time field mapping table&lt;/a&gt;.
     *
     * @throws IllegalArgumentException if &lt;code&gt;year&lt;/code&gt; parameter is
     * outside value constraints for the field as specified in
     * &lt;a href="#datetimefieldmapping"&gt;date/time field mapping table&lt;/a&gt;.
     */
    public final void setYear(BigInteger year) {
	if (year == null) {
	    this.eon = null;
	    this.year = DatatypeConstants.FIELD_UNDEFINED;
	} else {
	    BigInteger temp = year.remainder(BILLION_B);
	    this.year = temp.intValue();
	    setEon(year.subtract(temp));
	}
    }

    /**
     * @return result of adding second and fractional second field
     */
    private BigDecimal getSeconds() {
	if (second == DatatypeConstants.FIELD_UNDEFINED) {
	    return DECIMAL_ZERO;
	}
	BigDecimal result = BigDecimal.valueOf((long) second);
	if (fractionalSecond != null) {
	    return result.add(fractionalSecond);
	} else {
	    return result;
	}
    }

    /**
     * Return day in month or {@link DatatypeConstants#FIELD_UNDEFINED}.&lt;/p&gt;
     *
     * &lt;p&gt;Value constraints for this value are summarized in
     * &lt;a href="#datetimefield-day"&gt;day field of date/time field mapping table&lt;/a&gt;.&lt;/p&gt;
     *
     * @see #setDay(int)
     */
    public int getDay() {
	return day;
    }

}

