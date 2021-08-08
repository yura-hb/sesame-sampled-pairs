class SequenceDifferenceTransform implements Transform {
    /**
     * The output column names
     * This will often be the same as the input
     *
     * @return the output column names
     */
    @Override
    public String[] outputColumnNames() {
	return new String[] { columnName() };
    }

    private final String columnName;

    /**
     * Returns a singular column name
     * this op is meant to run on
     *
     * @return
     */
    @Override
    public String columnName() {
	return columnName;
    }

}

