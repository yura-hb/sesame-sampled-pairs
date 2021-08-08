class DefaultCommentMapper {
    /**
     * Get comment of the list which includes a given position
     *
     * @param position The position belonging to the looked up comment
     * @return comment which includes the given position or null if none was found
     */
    Comment getComment(int position) {

	if (this.comments == null) {
	    return null;
	}
	int size = this.comments.length;
	if (size == 0) {
	    return null;
	}
	int index = getCommentIndex(0, position, 0);
	if (index &lt; 0) {
	    return null;
	}
	return this.comments[index];
    }

    Comment[] comments;

    private int getCommentIndex(int start, int position, int exact) {
	if (position == 0) {
	    if (this.comments.length &gt; 0 && this.comments[0].getStartPosition() == 0) {
		return 0;
	    }
	    return -1;
	}
	int bottom = start, top = this.comments.length - 1;
	int i = 0, index = -1;
	Comment comment = null;
	while (bottom &lt;= top) {
	    i = bottom + (top - bottom) / 2;
	    comment = this.comments[i];
	    int commentStart = comment.getStartPosition();
	    if (position &lt; commentStart) {
		top = i - 1;
	    } else if (position &gt;= (commentStart + comment.getLength())) {
		bottom = i + 1;
	    } else {
		index = i;
		break;
	    }
	}
	if (index &lt; 0 && exact != 0) {
	    comment = this.comments[i];
	    if (position &lt; comment.getStartPosition()) {
		return exact &lt; 0 ? i - 1 : i;
	    } else {
		return exact &lt; 0 ? i : i + 1;
	    }
	}
	return index;
    }

}

