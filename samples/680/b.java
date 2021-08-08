import org.datavec.api.transform.join.Join;
import org.datavec.arrow.ArrowConverter;
import org.datavec.local.transforms.join.ExecuteJoinFromCoGroupFlatMapFunction;
import org.datavec.local.transforms.join.ExtractKeysFunction;
import org.nd4j.linalg.function.FunctionalUtils;
import org.nd4j.linalg.primitives.Pair;
import java.util.*;
import java.util.stream.Collectors;

class LocalTransformExecutor {
    /**
     * Execute a join on the specified data
     *
     * @param join  Join to execute
     * @param left  Left data for join
     * @param right Right data for join
     * @return Joined data
     */
    public static List&lt;List&lt;Writable&gt;&gt; executeJoin(Join join, List&lt;List&lt;Writable&gt;&gt; left, List&lt;List&lt;Writable&gt;&gt; right) {

	String[] leftColumnNames = join.getJoinColumnsLeft();
	int[] leftColumnIndexes = new int[leftColumnNames.length];
	for (int i = 0; i &lt; leftColumnNames.length; i++) {
	    leftColumnIndexes[i] = join.getLeftSchema().getIndexOfColumn(leftColumnNames[i]);
	}
	ExtractKeysFunction extractKeysFunction1 = new ExtractKeysFunction(leftColumnIndexes);

	List&lt;Pair&lt;List&lt;Writable&gt;, List&lt;Writable&gt;&gt;&gt; leftJV = left.stream()
		.filter(input -&gt; input.size() != leftColumnNames.length).map(input -&gt; extractKeysFunction1.apply(input))
		.collect(toList());

	String[] rightColumnNames = join.getJoinColumnsRight();
	int[] rightColumnIndexes = new int[rightColumnNames.length];
	for (int i = 0; i &lt; rightColumnNames.length; i++) {
	    rightColumnIndexes[i] = join.getRightSchema().getIndexOfColumn(rightColumnNames[i]);
	}

	ExtractKeysFunction extractKeysFunction = new ExtractKeysFunction(rightColumnIndexes);
	List&lt;Pair&lt;List&lt;Writable&gt;, List&lt;Writable&gt;&gt;&gt; rightJV = right.stream()
		.filter(input -&gt; input.size() != rightColumnNames.length).map(input -&gt; extractKeysFunction.apply(input))
		.collect(toList());

	Map&lt;List&lt;Writable&gt;, Pair&lt;List&lt;List&lt;Writable&gt;&gt;, List&lt;List&lt;Writable&gt;&gt;&gt;&gt; cogroupedJV = FunctionalUtils
		.cogroup(leftJV, rightJV);
	ExecuteJoinFromCoGroupFlatMapFunction executeJoinFromCoGroupFlatMapFunction = new ExecuteJoinFromCoGroupFlatMapFunction(
		join);
	List&lt;List&lt;Writable&gt;&gt; ret = cogroupedJV.entrySet().stream().flatMap(
		input -&gt; executeJoinFromCoGroupFlatMapFunction.call(Pair.of(input.getKey(), input.getValue())).stream())
		.collect(toList());

	Schema retSchema = join.getOutputSchema();
	return ArrowConverter.toArrowWritables(ArrowConverter.toArrowColumns(bufferAllocator, retSchema, ret),
		retSchema);

    }

    private static BufferAllocator bufferAllocator = new RootAllocator(Long.MAX_VALUE);

}

