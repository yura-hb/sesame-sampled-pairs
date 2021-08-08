import java.util.Arrays;

class JavacTask extends AbstractTask&lt;JavacTask&gt; {
    /**
     * Sets the options.
     * @param options the options
     * @return this task object
     */
    public JavacTask options(String... options) {
	this.options = Arrays.asList(options);
	return this;
    }

    private List&lt;String&gt; options;

}

