import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import javax.swing.*;

class bug6823603 {
    /**
     * @param args the command line arguments
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
	testRawSignatures();
	testGenericSignatures();

	testGetSelectedValuesList(); // new method
    }

    private static final String TEST_ELEMENT = "Test1";

    @SuppressWarnings("unchecked")
    private static void testRawSignatures() {
	// Test JList
	ListModel rawTestModel = new DefaultListModel();

	new JList();
	new JList(rawTestModel);
	new JList(new Object[] { TEST_ELEMENT });
	JList rawTestList = new JList(new Vector());
	rawTestList.getPrototypeCellValue();
	rawTestList.setPrototypeCellValue(TEST_ELEMENT);
	rawTestList.getCellRenderer();
	rawTestList.setCellRenderer(new DefaultListCellRenderer());
	rawTestList.getModel();
	rawTestList.setModel(rawTestModel);
	rawTestList.setListData(new Object[] { TEST_ELEMENT });
	rawTestList.setListData(new Vector());

	@SuppressWarnings("deprecation")
	Object[] selectedValues = rawTestList.getSelectedValues();
	rawTestList.getSelectedValue();

	// Test ListCellRenderer
	ListCellRenderer rawTestCellRenderer = new DefaultListCellRenderer();
	String testEntry = "Test";
	@SuppressWarnings("unchecked")
	JList rawJList = new JList(new Object[] { testEntry });

	rawTestCellRenderer.getListCellRendererComponent(rawJList, testEntry, 0, true, true);

	// Test ListModel
	DefaultListModel testModel = new DefaultListModel();
	testModel.addElement(TEST_ELEMENT);
	rawTestModel = testModel;
	rawTestModel.getElementAt(0);

	// Test DefaultListModel
	DefaultListModel defaultListModel = new DefaultListModel();

	defaultListModel.addElement(TEST_ELEMENT);
	defaultListModel.getElementAt(0);
	defaultListModel.elements();
	defaultListModel.elementAt(0);
	defaultListModel.firstElement();
	defaultListModel.lastElement();

	String testElement2 = "Test2";

	defaultListModel.setElementAt(testElement2, 0);
	defaultListModel.insertElementAt(TEST_ELEMENT, 0);
	defaultListModel.get(0);
	defaultListModel.set(0, testElement2);
	defaultListModel.add(0, TEST_ELEMENT);
	defaultListModel.remove(0);

	// Test AbstractListModel
	@SuppressWarnings("serial")
	ListModel abstractListModel = new AbstractListModel() {
	    public int getSize() {
		throw new UnsupportedOperationException("Not supported yet.");
	    }

	    public Object getElementAt(int index) {
		throw new UnsupportedOperationException("Not supported yet.");
	    }
	};

	// Test DefaultListCellRenderer
	DefaultListCellRenderer cellRenderer = new DefaultListCellRenderer();

	@SuppressWarnings("unchecked")
	JList list = new JList(new Object[] { testEntry });

	cellRenderer.getListCellRendererComponent(rawJList, testEntry, 0, true, true);
    }

    private static &lt;E&gt; void testGenericSignatures() {
	// Test JList
	ListModel&lt;String&gt; stringListModel = new DefaultListModel&lt;String&gt;();

	new JList&lt;String&gt;();
	new JList&lt;String&gt;(stringListModel);
	new JList&lt;String&gt;(new String[] { TEST_ELEMENT });

	JList&lt;String&gt; stringTestList = new JList&lt;String&gt;(new Vector&lt;String&gt;());

	stringTestList.getPrototypeCellValue();
	stringTestList.setPrototypeCellValue(TEST_ELEMENT);

	ListCellRenderer&lt;? super String&gt; cellRenderer = stringTestList.getCellRenderer();

	stringTestList.setCellRenderer(new DefaultListCellRenderer());

	ListModel&lt;String&gt; model = stringTestList.getModel();

	stringTestList.setModel(stringListModel);
	stringTestList.setListData(new String[] { TEST_ELEMENT });
	stringTestList.setListData(new Vector&lt;String&gt;());

	@SuppressWarnings("deprecation")
	Object[] selectedValues = stringTestList.getSelectedValues();

	stringTestList.getSelectedValue();

	// Test ListCellRenderer
	ListCellRenderer&lt;Object&gt; stringTestCellRenderer = new DefaultListCellRenderer();
	String testEntry = "Test";
	JList&lt;String&gt; stringJList = new JList&lt;String&gt;(new String[] { testEntry });

	Component listCellRendererComponent2 = stringTestCellRenderer.getListCellRendererComponent(stringJList,
		testEntry, 0, true, true);

	// Test ListModel
	DefaultListModel&lt;String&gt; testModel = new DefaultListModel&lt;String&gt;();
	testModel.addElement(TEST_ELEMENT);
	stringListModel = testModel;

	String element1 = stringListModel.getElementAt(0);

	// Test DefaultListModel
	DefaultListModel&lt;String&gt; stringTestModel = new DefaultListModel&lt;String&gt;();

	stringTestModel.addElement(TEST_ELEMENT);
	element1 = stringTestModel.getElementAt(0);
	Enumeration&lt;String&gt; elements = stringTestModel.elements();
	String element2 = stringTestModel.elementAt(0);
	String firstElement = stringTestModel.firstElement();
	String lastElement = stringTestModel.lastElement();

	String testElement2 = "Test2";
	stringTestModel.setElementAt(testElement2, 0);
	stringTestModel.insertElementAt(TEST_ELEMENT, 0);
	String element3 = stringTestModel.get(0);
	String element4 = stringTestModel.set(0, testElement2);
	stringTestModel.add(0, TEST_ELEMENT);
	String removedElement = stringTestModel.remove(0);

	// Test AbstractListModel
	stringListModel = new AbstractListModel&lt;String&gt;() {

	    public int getSize() {
		throw new UnsupportedOperationException("Not supported yet.");
	    }

	    public String getElementAt(int index) {
		throw new UnsupportedOperationException("Not supported yet.");
	    }
	};

	@SuppressWarnings("serial")
	ListModel&lt;E&gt; genericTestModel = new AbstractListModel&lt;E&gt;() {

	    public int getSize() {
		throw new UnsupportedOperationException("Not supported yet.");
	    }

	    public E getElementAt(int index) {
		throw new UnsupportedOperationException("Not supported yet.");
	    }
	};

	// Test DefaultListCellRenderer
	cellRenderer = new DefaultListCellRenderer();

	stringJList = new JList&lt;String&gt;(new String[] { testEntry });

	listCellRendererComponent2 = cellRenderer.getListCellRendererComponent(stringJList, testEntry, 0, true, true);
    }

    private static void testGetSelectedValuesList() {
	Vector&lt;Integer&gt; data = new Vector&lt;Integer&gt;();
	for (int i = 0; i &lt; 10; i++) {
	    data.add(i);
	}
	JList&lt;Integer&gt; list = new JList&lt;Integer&gt;(data);
	list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	list.setSelectedIndices(new int[] { 1, 2, 3, 5, 6, 8 });

	@SuppressWarnings("deprecation")
	Object[] expectedSelectedValues = list.getSelectedValues();
	List&lt;Integer&gt; selectedValuesList = list.getSelectedValuesList();
	assertEquals(expectedSelectedValues, selectedValuesList.toArray());
    }

    private static void assertEquals(Object[] expectedArray, Object[] actualArray) {
	if (!Arrays.equals(expectedArray, actualArray)) {
	    throw new RuntimeException(
		    "Expected: " + Arrays.toString(expectedArray) + " but was: " + Arrays.toString(actualArray));
	}
    }

}

