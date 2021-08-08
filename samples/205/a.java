import java.util.*;

class Normalization {
    /**
     * Normalize each column of a sequence, based on min/max
     *
     * @param schema         Schema of the data
     * @param data           Data to normalize
     * @param min            New minimum value
     * @param max            New maximum value
     * @param excludeColumns List of columns to exclude
     * @return Normalized data
     */
    public static JavaRDD&lt;List&lt;List&lt;Writable&gt;&gt;&gt; normalizeSequence(Schema schema, JavaRDD&lt;List&lt;List&lt;Writable&gt;&gt;&gt; data,
	    double min, double max, List&lt;String&gt; excludeColumns) {
	if (excludeColumns == null)
	    excludeColumns = Arrays.asList(DataFrames.SEQUENCE_UUID_COLUMN, DataFrames.SEQUENCE_INDEX_COLUMN);
	else {
	    excludeColumns = new ArrayList&lt;&gt;(excludeColumns);
	    excludeColumns.add(DataFrames.SEQUENCE_UUID_COLUMN);
	    excludeColumns.add(DataFrames.SEQUENCE_INDEX_COLUMN);
	}
	DataRowsFacade frame = DataFrames.toDataFrameSequence(schema, data);
	return DataFrames.toRecordsSequence(normalize(frame, min, max, excludeColumns)).getSecond();
    }

    /**
     * Scale based on min,max
     *
     * @param dataFrame the dataframe to scale
     * @param min       the minimum value
     * @param max       the maximum value
     * @return the normalized dataframe per column
     */
    public static DataRowsFacade normalize(DataRowsFacade dataFrame, double min, double max, List&lt;String&gt; skipColumns) {
	List&lt;String&gt; columnsList = DataFrames.toList(dataFrame.get().columns());
	columnsList.removeAll(skipColumns);
	String[] columnNames = DataFrames.toArray(columnsList);
	//first row is min second row is max, each column in a row is for a particular column
	List&lt;Row&gt; minMax = minMaxColumns(dataFrame, columnNames);
	for (int i = 0; i &lt; columnNames.length; i++) {
	    String columnName = columnNames[i];
	    double dMin = ((Number) minMax.get(0).get(i)).doubleValue();
	    double dMax = ((Number) minMax.get(1).get(i)).doubleValue();
	    double maxSubMin = (dMax - dMin);
	    if (maxSubMin == 0)
		maxSubMin = 1;

	    Column newCol = dataFrame.get().col(columnName).minus(dMin).divide(maxSubMin).multiply(max - min).plus(min);
	    dataFrame = dataRows(dataFrame.get().withColumn(columnName, newCol));
	}

	return dataFrame;
    }

    /**
     * Returns the min and max of the given columns.
     * The list returned is a list of size 2 where each row
     * @param data the data to get the max for
     * @param columns the columns to get the
     * @return
     */
    public static List&lt;Row&gt; minMaxColumns(DataRowsFacade data, String... columns) {
	return aggregate(data, columns, new String[] { "min", "max" });
    }

    /**
     * Aggregate based on an arbitrary list
     * of aggregation and grouping functions
     * @param data the dataframe to aggregate
     * @param columns the columns to aggregate
     * @param functions the functions to use
     * @return the list of rows with the aggregated statistics.
     * Each row will be a function with the desired columnar output
     * in the order in which the columns were specified.
     */
    public static List&lt;Row&gt; aggregate(DataRowsFacade data, String[] columns, String[] functions) {
	String[] rest = new String[columns.length - 1];
	for (int i = 0; i &lt; rest.length; i++)
	    rest[i] = columns[i + 1];
	List&lt;Row&gt; rows = new ArrayList&lt;&gt;();
	for (String op : functions) {
	    Map&lt;String, String&gt; expressions = new ListOrderedMap();
	    for (String s : columns) {
		expressions.put(s, op);
	    }

	    //compute the aggregation based on the operation
	    DataRowsFacade aggregated = dataRows(data.get().agg(expressions));
	    String[] columns2 = aggregated.get().columns();
	    //strip out the op name and parentheses from the columns
	    Map&lt;String, String&gt; opReplace = new TreeMap&lt;&gt;();
	    for (String s : columns2) {
		if (s.contains("min(") || s.contains("max("))
		    opReplace.put(s, s.replace(op, "").replaceAll("[()]", ""));
		else if (s.contains("avg")) {
		    opReplace.put(s, s.replace("avg", "").replaceAll("[()]", ""));
		} else {
		    opReplace.put(s, s.replace(op, "").replaceAll("[()]", ""));
		}
	    }

	    //get rid of the operation name in the column
	    DataRowsFacade rearranged = null;
	    for (Map.Entry&lt;String, String&gt; entries : opReplace.entrySet()) {
		//first column
		if (rearranged == null) {
		    rearranged = dataRows(aggregated.get().withColumnRenamed(entries.getKey(), entries.getValue()));
		}
		//rearranged is just a copy of aggregated at this point
		else
		    rearranged = dataRows(rearranged.get().withColumnRenamed(entries.getKey(), entries.getValue()));
	    }

	    rearranged = dataRows(rearranged.get().select(DataFrames.toColumns(columns)));
	    //op
	    rows.addAll(rearranged.get().collectAsList());
	}

	return rows;
    }

}

