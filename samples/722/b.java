abstract class TableView extends BoxView {
    class TableCell extends BoxView implements GridCell {
	/**
	 * Sets the grid location.
	 *
	 * @param row the row &gt;= 0
	 * @param col the column &gt;= 0
	 */
	public void setGridLocation(int row, int col) {
	    this.row = row;
	    this.col = col;
	}

	int row;
	int col;

    }

}

