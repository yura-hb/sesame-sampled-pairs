abstract class AbstractSortedSetDecorator&lt;E&gt; extends AbstractSetDecorator&lt;E&gt; implements SortedSet&lt;E&gt; {
    /**
     * Gets the set being decorated.
     *
     * @return the decorated set
     */
    @Override
    protected SortedSet&lt;E&gt; decorated() {
	return (SortedSet&lt;E&gt;) super.decorated();
    }

}

