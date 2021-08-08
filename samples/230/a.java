class ArrayBinding extends TypeBinding {
    /**
    * Collect the substitutes into a map for certain type variables inside the receiver type
    * e.g.   Collection&lt;T&gt;.findSubstitute(T, Collection&lt;List&lt;X&gt;&gt;):   T --&gt; List&lt;X&gt;
    */
    public void collectSubstitutes(TypeBinding otherType, Map substitutes) {
	if (otherType.isArrayType()) {
	    int otherDim = otherType.dimensions();
	    if (otherDim == this.dimensions) {
		this.leafComponentType.collectSubstitutes(otherType.leafComponentType(), substitutes);
	    } else if (otherDim &gt; this.dimensions) {
		ArrayBinding otherReducedType = this.environment.createArrayType(otherType.leafComponentType(),
			otherDim - this.dimensions);
		this.leafComponentType.collectSubstitutes(otherReducedType, substitutes);
	    }
	}
    }

    public int dimensions;
    public TypeBinding leafComponentType;
    LookupEnvironment environment;

}

