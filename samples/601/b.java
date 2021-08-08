import com.sun.org.apache.bcel.internal.Const;

abstract class Attribute implements Cloneable, Node {
    /**
     * @return Name of attribute
     * @since 6.0
     */
    public String getName() {
	final ConstantUtf8 c = (ConstantUtf8) constant_pool.getConstant(name_index, Const.CONSTANT_Utf8);
	return c.getBytes();
    }

    private ConstantPool constant_pool;
    private int name_index;

}

