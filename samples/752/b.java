import java.math.BigDecimal;
import java.text.*;

class BigDecimalParse extends IntlTest {
    /**
     * Test for special numbers
     *    Double.NaN
     *    Double.POSITIVE_INFINITY
     *    Double.NEGATIVE_INFINITY
     */
    void test_Parse_in_DecimalFormat_SpecialNumber() {
	df = new DecimalFormat();
	df.setParseBigDecimal(true);

	String[] numbers = { "0", "0.0", "25", "25.0", "25.5", "\u221e", "\ufffd", "-0", "-0.0", "-25", "-25.0",
		"-25.5", "-\u221e", };
	int multipliers[] = { 5, -5 };
	Number[][] expected = {
		{ new BigDecimal("0"), new BigDecimal("0.0"), new BigDecimal("5"), new BigDecimal("5.0"),
			new BigDecimal("5.1"), Double.POSITIVE_INFINITY, Double.NaN, new BigDecimal("0"),
			new BigDecimal("0.0"), new BigDecimal("-5"), new BigDecimal("-5.0"), new BigDecimal("-5.1"),
			Double.NEGATIVE_INFINITY, Double.NaN, },
		{ new BigDecimal("0"), new BigDecimal("0.0"), new BigDecimal("-5"), new BigDecimal("-5.0"),
			new BigDecimal("-5.1"), Double.NEGATIVE_INFINITY, Double.NaN, new BigDecimal("0"),
			new BigDecimal("0.0"), new BigDecimal("5"), new BigDecimal("5.0"), new BigDecimal("5.1"),
			Double.POSITIVE_INFINITY, }, };

	for (int i = 0; i &lt; multipliers.length; i++) {
	    df.setMultiplier(multipliers[i]);
	    for (int j = 0; j &lt; numbers.length; j++) {
		check(String.valueOf(numbers[j]), expected[i][j]);
	    }
	}
    }

    DecimalFormat df;
    ParsePosition pp;
    Number parsed = null;
    boolean exceptionOccurred;

    protected void check(String from, Number to) {
	pp = new ParsePosition(0);
	try {
	    parsed = df.parse(from, pp);
	} catch (Exception e) {
	    exceptionOccurred = true;
	    errln(e.getMessage());
	}
	if (!exceptionOccurred) {
	    checkParse(from, to, parsed);
	    checkType(from, getType(to), getType(parsed));
	    checkParsePosition(from, from.length(), pp.getIndex());
	}
    }

    private void checkParse(String orig, Number expected, Number got) {
	if (!expected.equals(got)) {
	    errln("Parsing... failed." + "\n   original: " + orig + "\n   parsed:   " + got + "\n   expected: "
		    + expected + "\n");
	}
    }

    private String getType(Number number) {
	return number.getClass().getName();
    }

    private void checkType(String orig, String expected, String got) {
	if (!expected.equals(got)) {
	    errln("Parsing... unexpected Class returned." + "\n   original: " + orig + "\n   got:      " + got
		    + "\n   expected: " + expected + "\n");
	}
    }

    private void checkParsePosition(String orig, int expected, int got) {
	if (expected != got) {
	    errln("Parsing... wrong ParsePosition returned." + "\n   original: " + orig + "\n   got:      " + got
		    + "\n   expected: " + expected + "\n");
	}
    }

}

