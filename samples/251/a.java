class Block extends Statement {
    /**
    * Dispatch the call on its last statement.
    */
    @Override
    public void branchChainTo(BranchLabel label) {
	if (this.statements != null) {
	    this.statements[this.statements.length - 1].branchChainTo(label);
	}
    }

    public Statement[] statements;

}

