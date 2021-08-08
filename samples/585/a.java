class CodeSnippetToCuMapper implements EvaluationConstants {
    /**
    * Returns the import defined at the given line number.
    */
    public char[] getImport(int lineNumber) {
	int importStartLine = this.lineNumberOffset - 1 - this.snippetImports.length;
	return this.snippetImports[lineNumber - importStartLine];
    }

    /**
     * Where the code snippet starts in the generated compilation unit.
     */
    public int lineNumberOffset = 0;
    char[][] snippetImports;

}

