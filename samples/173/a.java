import org.nd4j.autodiff.samediff.SameDiff;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.weightinit.impl.ZeroInitScheme;
import java.io.*;
import java.util.*;

abstract class BaseGraphMapper&lt;GRAPH_TYPE, NODE_TYPE, ATTR_TYPE, TENSOR_TYPE&gt;
	implements GraphMapper&lt;GRAPH_TYPE, NODE_TYPE, ATTR_TYPE, TENSOR_TYPE&gt; {
    /**
     *
     * @param inputStream
     * @return
     */
    @Override
    public SameDiff importGraph(InputStream inputStream) {
	GRAPH_TYPE def = readGraph(inputStream);
	return importGraph(def);
    }

    protected GRAPH_TYPE readGraph(InputStream inputStream) {
	byte[] bytes = null;
	GRAPH_TYPE def = null;
	try {
	    bytes = IOUtils.toByteArray(inputStream);
	    def = parseGraphFrom(bytes);
	} catch (IOException e) {
	    try (BufferedInputStream bis2 = new BufferedInputStream(new ByteArrayInputStream(bytes));
		    BufferedReader reader = new BufferedReader(new InputStreamReader(bis2))) {
		Message.Builder builder = getNewGraphBuilder();

		StringBuilder str = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
		    str.append(line);//.append("\n");
		}

		TextFormat.getParser().merge(str.toString(), builder);
		def = (GRAPH_TYPE) builder.build();
	    } catch (Exception e2) {
		e2.printStackTrace();
	    }
	}

	return def;
    }

    /**
     * This method converts given TF
     * @param tfGraph
     * @return
     */
    @Override
    public SameDiff importGraph(GRAPH_TYPE tfGraph) {
	SameDiff diff = SameDiff.create();
	ImportState&lt;GRAPH_TYPE, TENSOR_TYPE&gt; importState = new ImportState&lt;&gt;();
	importState.setSameDiff(diff);
	importState.setGraph(tfGraph);

	val variablesForGraph = variablesForGraph(tfGraph);
	importState.setVariables(variablesForGraph);

	//map the names of the nodes while accumulating the vertex ids
	//for each variable
	for (Map.Entry&lt;String, TENSOR_TYPE&gt; entry : variablesForGraph.entrySet()) {
	    DataBuffer.Type dt = dataTypeForTensor(entry.getValue());
	    if (dt == DataBuffer.Type.UNKNOWN && !unknownTypeNodeImportable(entry.getValue())) {
		val var = importState.getSameDiff().var(entry.getKey(), null, new ZeroInitScheme('c'));
		//mark as place holder for validating resolution later.
		if (isPlaceHolder(entry.getValue())) {
		    importState.getSameDiff().addAsPlaceHolder(var.getVarName());
		    if (var.getShape() != null)
			importState.getSameDiff().setOriginalPlaceHolderShape(var.getVarName(), var.getShape());
		} else {
		    //Not a placeholder, but SameDiff.var(String, shape=null, ZeroInitScheme()) above marked it as such due to null shape
		    importState.getSameDiff().removeAsPlaceholder(var.getVarName());
		}

		continue;
	    }

	    val arr = getNDArrayFromTensor(entry.getKey(), entry.getValue(), tfGraph);
	    if (arr != null) {
		val var = importState.getSameDiff().var(entry.getKey(), arr);
		//ensure the array is made available for later processing
		diff.associateArrayWithVariable(arr, var);

		if (isConstant(entry.getValue())) {
		    if (diff.getImportedConstants() == null) {
			diff.setImportedConstants(new LinkedHashSet&lt;String&gt;());
		    }
		    diff.getImportedConstants().add(entry.getKey());
		}
	    } else if (getShapeFromTensor(entry.getValue()) == null) {
		val var = importState.getSameDiff().var(entry.getKey(), null, new ZeroInitScheme('c'));
		//mark as place holder for validating resolution later.

		//note that this vertex id can still be a place holder
		//with a -1 shape. Just because a shape is "known" doesn't mean
		//that it isn't  a place holder.
		if (isPlaceHolder(entry.getValue())) {
		    val originalShape = getShapeFromTensor(entry.getValue());
		    importState.getSameDiff().addAsPlaceHolder(var.getVarName());
		    if (var.getShape() != null)
			importState.getSameDiff().setOriginalPlaceHolderShape(var.getVarName(), originalShape);

		} else {
		    //Not a placeholder, but SameDiff.var(String, shape=null, ZeroInitScheme()) above marked it as such due to null shape
		    importState.getSameDiff().removeAsPlaceholder(var.getVarName());
		}

	    } else {
		val originalShape = getShapeFromTensor(entry.getValue());
		val var = importState.getSameDiff().var(entry.getKey(), originalShape);
		//mark as place holder for validating resolution later.

		//note that this vertex id can still be a place holder
		//with a -1 shape. Just because a shape is "known" doesn't mean
		//that it isn't  a place holder.
		if (isPlaceHolder(entry.getValue())) {
		    importState.getSameDiff().addAsPlaceHolder(var.getVarName());
		    importState.getSameDiff().setOriginalPlaceHolderShape(var.getVarName(), originalShape);
		} else if (originalShape == null) {
		    //Not a placeholder, but SameDiff.var(String, shape=null, ZeroInitScheme()) above marked it as such due to null shape
		    importState.getSameDiff().removeAsPlaceholder(var.getVarName());
		}

	    }

	}

	//setup vertex ids for  names

	//handle mapping vertex ids properly

	val tfNodesList = getNodeList(tfGraph);
	for (NODE_TYPE tfNode : tfNodesList) {
	    if (!opsToIgnore().contains(getOpType(tfNode)) || isOpIgnoreException(tfNode))
		mapNodeType(tfNode, importState);
	}

	//We aren't guaranteed to have ops imported in the order that they can be executed, so check + fix that
	diff.validateExecutionOrder();

	return diff;
    }

}

