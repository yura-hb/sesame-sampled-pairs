import org.apache.commons.math4.util.FastMath;

class BlockRealMatrix extends AbstractRealMatrix implements Serializable {
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

    /** Block size. */
    public static final int BLOCK_SIZE = 52;

}

