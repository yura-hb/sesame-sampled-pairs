import java.util.LinkedList;
import jdk.nashorn.internal.objects.annotations.Attribute;
import jdk.nashorn.internal.objects.annotations.Where;
import jdk.nashorn.internal.runtime.ScriptObject;
import jdk.nashorn.internal.runtime.linker.NashornCallSiteDescriptor;

class NativeDebug extends ScriptObject {
    /**
     * Return the last runtime event in the queue
     * @param self self reference
     * @return the freshest event, null if queue is empty
     */
    @Function(attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static Object getLastRuntimeEvent(final Object self) {
	final LinkedList&lt;RuntimeEvent&lt;?&gt;&gt; q = getEventQueue(self);
	return q.isEmpty() ? null : q.getLast();
    }

    private static final String EVENT_QUEUE = "__eventQueue__";

    @SuppressWarnings("unchecked")
    private static LinkedList&lt;RuntimeEvent&lt;?&gt;&gt; getEventQueue(final Object self) {
	final ScriptObject sobj = (ScriptObject) self;
	LinkedList&lt;RuntimeEvent&lt;?&gt;&gt; q;
	if (sobj.has(EVENT_QUEUE)) {
	    q = (LinkedList&lt;RuntimeEvent&lt;?&gt;&gt;) ((ScriptObject) self).get(EVENT_QUEUE);
	} else {
	    ((ScriptObject) self).set(EVENT_QUEUE, q = new LinkedList&lt;&gt;(), NashornCallSiteDescriptor.CALLSITE_STRICT);
	}
	return q;
    }

}

