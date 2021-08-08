class ConstantValue extends Attribute {
    /**
     * @return deep copy of this attribute
     */
    @Override
    public Attribute copy(final ConstantPool _constant_pool) {
	final ConstantValue c = (ConstantValue) clone();
	c.setConstantPool(_constant_pool);
	return c;
    }

}

