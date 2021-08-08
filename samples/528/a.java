class EvaluationContext implements EvaluationConstants, SuffixConstants {
    /**
    * Returns the source of the CodeSnippet class.
    * This is used to generate the binary of the CodeSnippetClass
    */
    public static String getCodeSnippetSource() {
	return "package org.eclipse.jdt.internal.eval.target;\n" + //$NON-NLS-1$
		"\n" + //$NON-NLS-1$
		"/*\n" + //$NON-NLS-1$
		" * (c) Copyright IBM Corp. 2000, 2001.\n" + //$NON-NLS-1$
		" * All Rights Reserved.\n" + //$NON-NLS-1$
		" */\n" + //$NON-NLS-1$
		"/**\n" + //$NON-NLS-1$
		" * The root of all code snippet classes. Code snippet classes\n" + //$NON-NLS-1$
		" * are supposed to overide the run() method.\n" + //$NON-NLS-1$
		" * &lt;p&gt;\n" + //$NON-NLS-1$
		" * IMPORTANT NOTE:\n" + //$NON-NLS-1$
		" * All methods in this class must be public since this class is going to be loaded by the\n" + //$NON-NLS-1$
		" * bootstrap class loader, and the other code snippet support classes might be loaded by \n" + //$NON-NLS-1$
		" * another class loader (so their runtime packages are going to be different).\n" + //$NON-NLS-1$
		" */\n" + //$NON-NLS-1$
		"public class CodeSnippet {\n" + //$NON-NLS-1$
		"	private Class resultType = void.class;\n" + //$NON-NLS-1$
		"	private Object resultValue = null;\n" + //$NON-NLS-1$
		"/**\n" + //$NON-NLS-1$
		" * Returns the result type of the code snippet evaluation.\n" + //$NON-NLS-1$
		" */\n" + //$NON-NLS-1$
		"public Class getResultType() {\n" + //$NON-NLS-1$
		"	return this.resultType;\n" + //$NON-NLS-1$
		"}\n" + //$NON-NLS-1$
		"/**\n" + //$NON-NLS-1$
		" * Returns the result value of the code snippet evaluation.\n" + //$NON-NLS-1$
		" */\n" + //$NON-NLS-1$
		"public Object getResultValue() {\n" + //$NON-NLS-1$
		"	return this.resultValue;\n" + //$NON-NLS-1$
		"}\n" + //$NON-NLS-1$
		"/**\n" + //$NON-NLS-1$
		" * The code snippet. Subclasses must override this method with a transformed code snippet\n" + //$NON-NLS-1$
		" * that stores the result using setResult(Class, Object).\n" + //$NON-NLS-1$
		" */\n" + //$NON-NLS-1$
		"public void run() {\n" + //$NON-NLS-1$
		"}\n" + //$NON-NLS-1$
		"/**\n" + //$NON-NLS-1$
		" * Stores the result type and value of the code snippet evaluation.\n" + //$NON-NLS-1$
		" */\n" + //$NON-NLS-1$
		"public void setResult(Object someResultValue, Class someResultType) {\n" + //$NON-NLS-1$
		"	this.resultValue = someResultValue;\n" + //$NON-NLS-1$
		"	this.resultType = someResultType;\n" + //$NON-NLS-1$
		"}\n" + //$NON-NLS-1$
		"}\n"; //$NON-NLS-1$
    }

}

