import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

class DiffBuilder implements Builder&lt;DiffResult&gt; {
    /**
     * &lt;p&gt;
     * Test if two {@code Objects}s are equal.
     * &lt;/p&gt;
     *
     * @param fieldName
     *            the field name
     * @param lhs
     *            the left hand {@code Object}
     * @param rhs
     *            the right hand {@code Object}
     * @return this
     * @throws IllegalArgumentException
     *             if field name is {@code null}
     */
    public DiffBuilder append(final String fieldName, final Object lhs, final Object rhs) {
	validateFieldNameNotNull(fieldName);
	if (objectsTriviallyEqual) {
	    return this;
	}
	if (lhs == rhs) {
	    return this;
	}

	Object objectToTest;
	if (lhs != null) {
	    objectToTest = lhs;
	} else {
	    // rhs cannot be null, as lhs != rhs
	    objectToTest = rhs;
	}

	if (objectToTest.getClass().isArray()) {
	    if (objectToTest instanceof boolean[]) {
		return append(fieldName, (boolean[]) lhs, (boolean[]) rhs);
	    }
	    if (objectToTest instanceof byte[]) {
		return append(fieldName, (byte[]) lhs, (byte[]) rhs);
	    }
	    if (objectToTest instanceof char[]) {
		return append(fieldName, (char[]) lhs, (char[]) rhs);
	    }
	    if (objectToTest instanceof double[]) {
		return append(fieldName, (double[]) lhs, (double[]) rhs);
	    }
	    if (objectToTest instanceof float[]) {
		return append(fieldName, (float[]) lhs, (float[]) rhs);
	    }
	    if (objectToTest instanceof int[]) {
		return append(fieldName, (int[]) lhs, (int[]) rhs);
	    }
	    if (objectToTest instanceof long[]) {
		return append(fieldName, (long[]) lhs, (long[]) rhs);
	    }
	    if (objectToTest instanceof short[]) {
		return append(fieldName, (short[]) lhs, (short[]) rhs);
	    }

	    return append(fieldName, (Object[]) lhs, (Object[]) rhs);
	}

	// Not array type
	if (lhs != null && lhs.equals(rhs)) {
	    return this;
	}

	diffs.add(new Diff&lt;Object&gt;(fieldName) {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public Object getLeft() {
		return lhs;
	    }

	    @Override
	    public Object getRight() {
		return rhs;
	    }
	});

	return this;
    }

    private final boolean objectsTriviallyEqual;
    private final List&lt;Diff&lt;?&gt;&gt; diffs;

    private void validateFieldNameNotNull(final String fieldName) {
	Validate.isTrue(fieldName != null, "Field name cannot be null");
    }

    /**
     * &lt;p&gt;
     * Test if two {@code boolean[]}s are equal.
     * &lt;/p&gt;
     *
     * @param fieldName
     *            the field name
     * @param lhs
     *            the left hand {@code boolean[]}
     * @param rhs
     *            the right hand {@code boolean[]}
     * @return this
     * @throws IllegalArgumentException
     *             if field name is {@code null}
     */
    public DiffBuilder append(final String fieldName, final boolean[] lhs, final boolean[] rhs) {
	validateFieldNameNotNull(fieldName);
	if (objectsTriviallyEqual) {
	    return this;
	}
	if (!Arrays.equals(lhs, rhs)) {
	    diffs.add(new Diff&lt;Boolean[]&gt;(fieldName) {
		private static final long serialVersionUID = 1L;

		@Override
		public Boolean[] getLeft() {
		    return ArrayUtils.toObject(lhs);
		}

		@Override
		public Boolean[] getRight() {
		    return ArrayUtils.toObject(rhs);
		}
	    });
	}
	return this;
    }

    /**
     * &lt;p&gt;
     * Test if two {@code byte[]}s are equal.
     * &lt;/p&gt;
     *
     * @param fieldName
     *            the field name
     * @param lhs
     *            the left hand {@code byte[]}
     * @param rhs
     *            the right hand {@code byte[]}
     * @return this
     * @throws IllegalArgumentException
     *             if field name is {@code null}
     */
    public DiffBuilder append(final String fieldName, final byte[] lhs, final byte[] rhs) {
	validateFieldNameNotNull(fieldName);

	if (objectsTriviallyEqual) {
	    return this;
	}
	if (!Arrays.equals(lhs, rhs)) {
	    diffs.add(new Diff&lt;Byte[]&gt;(fieldName) {
		private static final long serialVersionUID = 1L;

		@Override
		public Byte[] getLeft() {
		    return ArrayUtils.toObject(lhs);
		}

		@Override
		public Byte[] getRight() {
		    return ArrayUtils.toObject(rhs);
		}
	    });
	}
	return this;
    }

    /**
     * &lt;p&gt;
     * Test if two {@code char[]}s are equal.
     * &lt;/p&gt;
     *
     * @param fieldName
     *            the field name
     * @param lhs
     *            the left hand {@code char[]}
     * @param rhs
     *            the right hand {@code char[]}
     * @return this
     * @throws IllegalArgumentException
     *             if field name is {@code null}
     */
    public DiffBuilder append(final String fieldName, final char[] lhs, final char[] rhs) {
	validateFieldNameNotNull(fieldName);

	if (objectsTriviallyEqual) {
	    return this;
	}
	if (!Arrays.equals(lhs, rhs)) {
	    diffs.add(new Diff&lt;Character[]&gt;(fieldName) {
		private static final long serialVersionUID = 1L;

		@Override
		public Character[] getLeft() {
		    return ArrayUtils.toObject(lhs);
		}

		@Override
		public Character[] getRight() {
		    return ArrayUtils.toObject(rhs);
		}
	    });
	}
	return this;
    }

    /**
     * &lt;p&gt;
     * Test if two {@code double[]}s are equal.
     * &lt;/p&gt;
     *
     * @param fieldName
     *            the field name
     * @param lhs
     *            the left hand {@code double[]}
     * @param rhs
     *            the right hand {@code double[]}
     * @return this
     * @throws IllegalArgumentException
     *             if field name is {@code null}
     */
    public DiffBuilder append(final String fieldName, final double[] lhs, final double[] rhs) {
	validateFieldNameNotNull(fieldName);

	if (objectsTriviallyEqual) {
	    return this;
	}
	if (!Arrays.equals(lhs, rhs)) {
	    diffs.add(new Diff&lt;Double[]&gt;(fieldName) {
		private static final long serialVersionUID = 1L;

		@Override
		public Double[] getLeft() {
		    return ArrayUtils.toObject(lhs);
		}

		@Override
		public Double[] getRight() {
		    return ArrayUtils.toObject(rhs);
		}
	    });
	}
	return this;
    }

    /**
     * &lt;p&gt;
     * Test if two {@code float[]}s are equal.
     * &lt;/p&gt;
     *
     * @param fieldName
     *            the field name
     * @param lhs
     *            the left hand {@code float[]}
     * @param rhs
     *            the right hand {@code float[]}
     * @return this
     * @throws IllegalArgumentException
     *             if field name is {@code null}
     */
    public DiffBuilder append(final String fieldName, final float[] lhs, final float[] rhs) {
	validateFieldNameNotNull(fieldName);

	if (objectsTriviallyEqual) {
	    return this;
	}
	if (!Arrays.equals(lhs, rhs)) {
	    diffs.add(new Diff&lt;Float[]&gt;(fieldName) {
		private static final long serialVersionUID = 1L;

		@Override
		public Float[] getLeft() {
		    return ArrayUtils.toObject(lhs);
		}

		@Override
		public Float[] getRight() {
		    return ArrayUtils.toObject(rhs);
		}
	    });
	}
	return this;
    }

    /**
     * &lt;p&gt;
     * Test if two {@code int[]}s are equal.
     * &lt;/p&gt;
     *
     * @param fieldName
     *            the field name
     * @param lhs
     *            the left hand {@code int[]}
     * @param rhs
     *            the right hand {@code int[]}
     * @return this
     * @throws IllegalArgumentException
     *             if field name is {@code null}
     */
    public DiffBuilder append(final String fieldName, final int[] lhs, final int[] rhs) {
	validateFieldNameNotNull(fieldName);

	if (objectsTriviallyEqual) {
	    return this;
	}
	if (!Arrays.equals(lhs, rhs)) {
	    diffs.add(new Diff&lt;Integer[]&gt;(fieldName) {
		private static final long serialVersionUID = 1L;

		@Override
		public Integer[] getLeft() {
		    return ArrayUtils.toObject(lhs);
		}

		@Override
		public Integer[] getRight() {
		    return ArrayUtils.toObject(rhs);
		}
	    });
	}
	return this;
    }

    /**
     * &lt;p&gt;
     * Test if two {@code long[]}s are equal.
     * &lt;/p&gt;
     *
     * @param fieldName
     *            the field name
     * @param lhs
     *            the left hand {@code long[]}
     * @param rhs
     *            the right hand {@code long[]}
     * @return this
     * @throws IllegalArgumentException
     *             if field name is {@code null}
     */
    public DiffBuilder append(final String fieldName, final long[] lhs, final long[] rhs) {
	validateFieldNameNotNull(fieldName);

	if (objectsTriviallyEqual) {
	    return this;
	}
	if (!Arrays.equals(lhs, rhs)) {
	    diffs.add(new Diff&lt;Long[]&gt;(fieldName) {
		private static final long serialVersionUID = 1L;

		@Override
		public Long[] getLeft() {
		    return ArrayUtils.toObject(lhs);
		}

		@Override
		public Long[] getRight() {
		    return ArrayUtils.toObject(rhs);
		}
	    });
	}
	return this;
    }

    /**
     * &lt;p&gt;
     * Test if two {@code short[]}s are equal.
     * &lt;/p&gt;
     *
     * @param fieldName
     *            the field name
     * @param lhs
     *            the left hand {@code short[]}
     * @param rhs
     *            the right hand {@code short[]}
     * @return this
     * @throws IllegalArgumentException
     *             if field name is {@code null}
     */
    public DiffBuilder append(final String fieldName, final short[] lhs, final short[] rhs) {
	validateFieldNameNotNull(fieldName);

	if (objectsTriviallyEqual) {
	    return this;
	}
	if (!Arrays.equals(lhs, rhs)) {
	    diffs.add(new Diff&lt;Short[]&gt;(fieldName) {
		private static final long serialVersionUID = 1L;

		@Override
		public Short[] getLeft() {
		    return ArrayUtils.toObject(lhs);
		}

		@Override
		public Short[] getRight() {
		    return ArrayUtils.toObject(rhs);
		}
	    });
	}
	return this;
    }

    /**
     * &lt;p&gt;
     * Test if two {@code Object[]}s are equal.
     * &lt;/p&gt;
     *
     * @param fieldName
     *            the field name
     * @param lhs
     *            the left hand {@code Object[]}
     * @param rhs
     *            the right hand {@code Object[]}
     * @return this
     * @throws IllegalArgumentException
     *             if field name is {@code null}
     */
    public DiffBuilder append(final String fieldName, final Object[] lhs, final Object[] rhs) {
	validateFieldNameNotNull(fieldName);
	if (objectsTriviallyEqual) {
	    return this;
	}

	if (!Arrays.equals(lhs, rhs)) {
	    diffs.add(new Diff&lt;Object[]&gt;(fieldName) {
		private static final long serialVersionUID = 1L;

		@Override
		public Object[] getLeft() {
		    return lhs;
		}

		@Override
		public Object[] getRight() {
		    return rhs;
		}
	    });
	}

	return this;
    }

}

