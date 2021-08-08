import java.util.Arrays;

class JavacTask extends AbstractTask&lt;JavacTask&gt; {
    /**
     * Sets the classes to be analyzed.
     * @param classes the classes
     * @return this task object
     */
    public JavacTask classes(String... classes) {
	this.classes = Arrays.asList(classes);
	return this;
    }

    private List&lt;String&gt; classes;

}

