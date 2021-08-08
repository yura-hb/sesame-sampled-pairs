import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;

class CalendarMarkingEvaluator implements ICalendarMarkingEvaluator {
    /**
     * Don't use for endless repetitions!
     */
    public void print() {
	for (int i = 0; i &lt; mCalendarMarkings.sizeCalendarMarkingList(); i++) {
	    CalendarMarking marking = mCalendarMarkings.getCalendarMarking(i);
	    // get first occurrence:
	    Calendar firstDay = Calendar.getInstance();
	    firstDay.setTimeInMillis(marking.getStartDate());
	    RepetitionHandler handler = sHandlerMap.get(marking.getRepeatType());
	    firstDay = handler.getFirst(firstDay, marking);
	    printDate(firstDay);
	    while (firstDay != null) {
		firstDay = handler.getNext(firstDay, marking);
		printDate(firstDay);
	    }
	}
    }

    private CalendarMarkings mCalendarMarkings;
    private static HashMap&lt;String, RepetitionHandler&gt; sHandlerMap;

    public void printDate(Calendar firstDay) {
	if (firstDay != null) {
	    System.out.println(DateFormat.getDateInstance().format(firstDay.getTime()));
	}
    }

    interface RepetitionHandler {
	private CalendarMarkings mCalendarMarkings;
	private static HashMap&lt;String, RepetitionHandler&gt; sHandlerMap;

	Calendar getFirst(Calendar pStartDate, CalendarMarking pMarking);

	Calendar getNext(Calendar pDay, CalendarMarking pMarking);

    }

}

