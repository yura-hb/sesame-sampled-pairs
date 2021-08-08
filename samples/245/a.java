import org.nd4j.imports.converters.DifferentialFunctionClassHolder;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.*;
import java.lang.reflect.Constructor;

class DefaultOpFactory implements OpFactory {
    /**
     * @param name
     * @param x
     * @param scalar
     * @return
     */
    @Override
    public ScalarOp createScalarTransform(String name, INDArray x, double scalar) {
	return createScalarTransform(name, x, null, x, null, scalar);
    }

    /**
     * @param name
     * @param x
     * @param y
     * @param z
     * @param extraArgs
     * @param scalar
     * @return
     */
    @Override
    public ScalarOp createScalarTransform(String name, INDArray x, INDArray y, INDArray z, Object[] extraArgs,
	    double scalar) {
	ScalarOp ret = null;

	try {
	    ret = (ScalarOp) DifferentialFunctionClassHolder.getInstance().getInstance(name).getClass()
		    .getConstructor(INDArray.class, INDArray.class, INDArray.class, long.class, Number.class)
		    .newInstance(x, y, z, x.length(), scalar);
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}

	/*
	switch(opName) {
	    case "add_scalar":
	        ret = new ScalarAdd(x,y,z,x.length(),scalar);
	        break;
	    case "sub_scalar":
	        ret = new ScalarSubtraction(x,y,z,x.length(),scalar);
	        break;
	    case "mul_scalar":
	        ret = new ScalarMultiplication(x,y,z,x.length(),scalar);
	        break;
	    case "div_scalar":
	        ret = new ScalarDivision(x,y,z,x.length(),scalar);
	        break;
	    case "equals_scalar":
	        ret = new ScalarEquals(x,y,z,x.length(),scalar);
	        break;
	    case "notequals_scalar":
	        ret = new ScalarNotEquals(x,y,z,x.length(),scalar);
	        break;
	    case "fmod_scalar":
	        ret = new ScalarFMod(x,y,z,x.length(),scalar);
	        break;
	    case "max_scalar":
	        ret = new ScalarMax(x,y,z,x.length(),scalar);
	        break;
	    case "min_scalar":
	        ret = new ScalarMin(x,y,z,x.length(),scalar);
	        break;
	    case "greaterthan_scalar":
	        ret = new ScalarGreaterThan(x,y,z,x.length(),scalar);
	        break;
	    case "greaterthanorequal_scalar":
	        ret = new ScalarGreaterThanOrEqual(x,y,z,x.length(),scalar);
	        break;
	    case "lessthan_scalar":
	        ret = new ScalarLessThan(x,y,z,x.length(),scalar);
	        break;
	    case "lessthanorequal_scalar":
	        ret = new ScalarLessThanOrEqual(x,y,z,x.length(),scalar);
	        break;
	    case "remainder_scalar":
	        ret = new ScalarRemainder(x,y,z,x.length(),scalar);
	        break;
	    case   "rdiv_scalar":
	        ret = new ScalarReverseDivision(x,y,z,x.length(),scalar);
	        break;
	    case   "rsub_scalar":
	        ret = new ScalarReverseSubtraction(x,y,z,x.length(),scalar);
	        break;
	}
	
	*/

	ret.setExtraArgs(extraArgs);
	return ret;
    }

}

