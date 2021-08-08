import java.util.Arrays;

class JavacTask extends AbstractTask&lt;JavacTask&gt; {
    /**
     * Sets the classpath.
     * @param classpath the classpath
     * @return this task object
     */
    public JavacTask classpath(Path... classpath) {
	this.classpath = Arrays.asList(classpath);
	return this;
    }

    private List&lt;Path&gt; classpath;

}

