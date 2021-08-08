class DoubleExpression extends ConstantExpression {
    /**
     * Check if the expression is equal to a value
     */
    public boolean equals(int i) {
	return value == i;
    }

    double value;

}

