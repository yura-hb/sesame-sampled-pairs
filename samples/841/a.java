import junit.extensions.TestDecorator;

class TestTreeModel implements TreeModel {
    /**
     * Tests if the test is a leaf.
     */
    public boolean isLeaf(Object node) {
	return isTestSuite(node) == null;
    }

    /**
     * Tests if the node is a TestSuite.
     */
    TestSuite isTestSuite(Object node) {
	if (node instanceof TestSuite)
	    return (TestSuite) node;
	if (node instanceof TestDecorator) {
	    Test baseTest = ((TestDecorator) node).getTest();
	    return isTestSuite(baseTest);
	}
	return null;
    }

}

