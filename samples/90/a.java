import java.lang.reflect.*;

abstract class TestCase extends Assert implements Test {
    /**
     * Override to run the test and assert its state.
     * @exception Throwable if any exception is thrown
     */
    protected void runTest() throws Throwable {
	assertNotNull(fName);
	Method runMethod = null;
	try {
	    // use getMethod to get all public inherited
	    // methods. getDeclaredMethods returns all
	    // methods of this class but excludes the
	    // inherited ones.
	    runMethod = getClass().getMethod(fName, null);
	} catch (NoSuchMethodException e) {
	    fail("Method \"" + fName + "\" not found");
	}
	if (!Modifier.isPublic(runMethod.getModifiers())) {
	    fail("Method \"" + fName + "\" should be public");
	}

	try {
	    runMethod.invoke(this, new Class[0]);
	} catch (InvocationTargetException e) {
	    e.fillInStackTrace();
	    throw e.getTargetException();
	} catch (IllegalAccessException e) {
	    e.fillInStackTrace();
	    throw e;
	}
    }

    /**
     * the name of the test case
     */
    private String fName;

}

