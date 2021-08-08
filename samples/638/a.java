import org.apache.commons.math4.util.FastMath;

class BlockRealMatrix extends AbstractRealMatrix implements Serializable {
    /**
     * Compute the sum of this matrix and {@code m}.
     *
     * @param m Matrix to be added.
     * @return {@code this} + m.
     * @throws MatrixDimensionMismatchException if {@code m} is not the same
     * size as this matrix.
     */
    public BlockRealMatrix add(final BlockRealMatrix m) throws MatrixDimensionMismatchException {
	// safety check
	MatrixUtils.checkAdditionCompatible(this, m);

	final BlockRealMatrix out = new BlockRealMatrix(rows, columns);

	// perform addition block-wise, to ensure good cache behavior
	for (int blockIndex = 0; blockIndex &lt; out.blocks.length; ++blockIndex) {
	    final double[] outBlock = out.blocks[blockIndex];
	    final double[] tBlock = blocks[blockIndex];
	    final double[] mBlock = m.blocks[blockIndex];
	    for (int k = 0; k &lt; outBlock.length; ++k) {
		outBlock[k] = tBlock[k] + mBlock[k];
	    }
	}

	return out;
    }

    /** Number of rows of the matrix. */
    private final int rows;
    /** Number of columns of the matrix. */
    private final int columns;
    /** Blocks of matrix entries. */
    private final double blocks[][];
    /** Number of block rows of the matrix. */
    private final int blockRows;
    /** Block size. */
    public static final int BLOCK_SIZE = 52;
    /** Number of block columns of the matrix. */
    private final int blockColumns;

    /**
     * Create a new matrix with the supplied row and column dimensions.
     *
     * @param rows  the number of rows in the new matrix
     * @param columns  the number of columns in the new matrix
     * @throws NotStrictlyPositiveException if row or column dimension is not
     * positive.
     */
    public BlockRealMatrix(final int rows, final int columns) throws NotStrictlyPositiveException {
	super(rows, columns);
	this.rows = rows;
	this.columns = columns;

	// number of blocks
	blockRows = (rows + BLOCK_SIZE - 1) / BLOCK_SIZE;
	blockColumns = (columns + BLOCK_SIZE - 1) / BLOCK_SIZE;

	// allocate storage blocks, taking care of smaller ones at right and bottom
	blocks = createBlocksLayout(rows, columns);
    }

    /**
     * Create a data array in blocks layout.
     * &lt;p&gt;
     * This method can be used to create the array argument of the {@link
     * #BlockRealMatrix(int, int, double[][], boolean)} constructor.
     * &lt;/p&gt;
     * @param rows Number of rows in the new matrix.
     * @param columns Number of columns in the new matrix.
     * @return a new data array in blocks layout.
     * @see #toBlocksLayout(double[][])
     * @see #BlockRealMatrix(int, int, double[][], boolean)
     */
    public static double[][] createBlocksLayout(final int rows, final int columns) {
	final int blockRows = (rows + BLOCK_SIZE - 1) / BLOCK_SIZE;
	final int blockColumns = (columns + BLOCK_SIZE - 1) / BLOCK_SIZE;

	final double[][] blocks = new double[blockRows * blockColumns][];
	int blockIndex = 0;
	for (int iBlock = 0; iBlock &lt; blockRows; ++iBlock) {
	    final int pStart = iBlock * BLOCK_SIZE;
	    final int pEnd = FastMath.min(pStart + BLOCK_SIZE, rows);
	    final int iHeight = pEnd - pStart;
	    for (int jBlock = 0; jBlock &lt; blockColumns; ++jBlock) {
		final int qStart = jBlock * BLOCK_SIZE;
		final int qEnd = FastMath.min(qStart + BLOCK_SIZE, columns);
		final int jWidth = qEnd - qStart;
		blocks[blockIndex] = new double[iHeight * jWidth];
		++blockIndex;
	    }
	}

	return blocks;
    }

}

