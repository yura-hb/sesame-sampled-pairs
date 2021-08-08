import java.util.List;

class NullPointerTester {
    /**
    * Ignore {@code constructor} in the tests that follow. Returns this object.
    *
    * @since 22.0
    */
    public NullPointerTester ignore(Constructor&lt;?&gt; constructor) {
	ignoredMembers.add(checkNotNull(constructor));
	return this;
    }

    private final List&lt;Member&gt; ignoredMembers = Lists.newArrayList();

}

