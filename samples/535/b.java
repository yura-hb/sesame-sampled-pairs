class LambdaTestMode extends Enum&lt;LambdaTestMode&gt; {
    /**
     *
     * @return the mode of test execution.
     */
    public static LambdaTestMode getMode() {
	return IS_LAMBDA_SERIALIZATION_MODE ? SERIALIZATION : NORMAL;
    }

    /**
     * {@code true} if tests are executed in the mode for testing lambda
     * Serialization ANd Deserialization (SAND).
     */
    private static final boolean IS_LAMBDA_SERIALIZATION_MODE = Boolean
	    .getBoolean("org.openjdk.java.util.stream.sand.mode");

}

