import java.util.Vector;

class LWList extends LWComponent implements ItemSelectable {
    /**
    * Add the specified item.
    *
    * @param listItem  the item
    */
    public void add(String listItem) {
	stringList.addElement(listItem);
	invalidate();
	repaint();
    }

    private Vector stringList;

}

