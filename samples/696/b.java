import com.sun.org.apache.xerces.internal.dom.events.EventImpl;
import com.sun.org.apache.xerces.internal.dom.events.MutationEventImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventException;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.MutationEvent;

class DocumentImpl extends CoreDocumentImpl implements DocumentTraversal, DocumentEvent, DocumentRange {
    /**
     * A method to be called when an attribute value has been modified
     */
    void modifiedAttrValue(AttrImpl attr, String oldvalue) {
	if (mutationEvents) {
	    // MUTATION POST-EVENTS:
	    dispatchAggregateEvents(attr, attr, oldvalue, MutationEvent.MODIFICATION);
	}
    }

    /** Bypass mutation events firing. */
    protected boolean mutationEvents = false;
    /** Table for event listeners registered to this document nodes. */
    protected Map&lt;NodeImpl, List&lt;LEntry&gt;&gt; eventListeners;

    /**
     * NON-DOM INTERNAL: Generate the "aggregated" post-mutation events
     * DOMAttrModified and DOMSubtreeModified.
     * Both of these should be issued only once for each user-requested
     * mutation operation, even if that involves multiple changes to
     * the DOM.
     * For example, if a DOM operation makes multiple changes to a single
     * Attr before returning, it would be nice to generate only one
     * DOMAttrModified, and multiple changes over larger scope but within
     * a recognizable single subtree might want to generate only one
     * DOMSubtreeModified, sent to their lowest common ancestor.
     * &lt;p&gt;
     * To manage this, use the "internal" versions of insert and remove
     * with MUTATION_LOCAL, then make an explicit call to this routine
     * at the higher level. Some examples now exist in our code.
     *
     * @param node The node to dispatch to
     * @param enclosingAttr The Attr node (if any) whose value has been changed
     * as a result of the DOM operation. Null if none such.
     * @param oldValue The String value previously held by the
     * enclosingAttr. Ignored if none such.
     * @param change Type of modification to the attr. See
     * MutationEvent.attrChange
     */
    protected void dispatchAggregateEvents(NodeImpl node, AttrImpl enclosingAttr, String oldvalue, short change) {
	// We have to send DOMAttrModified.
	NodeImpl owner = null;
	if (enclosingAttr != null) {
	    LCount lc = LCount.lookup(MutationEventImpl.DOM_ATTR_MODIFIED);
	    owner = (NodeImpl) enclosingAttr.getOwnerElement();
	    if (lc.total &gt; 0) {
		if (owner != null) {
		    MutationEventImpl me = new MutationEventImpl();
		    me.initMutationEvent(MutationEventImpl.DOM_ATTR_MODIFIED, true, false, enclosingAttr, oldvalue,
			    enclosingAttr.getNodeValue(), enclosingAttr.getNodeName(), change);
		    owner.dispatchEvent(me);
		}
	    }
	}
	// DOMSubtreeModified gets sent to the lowest common root of a
	// set of changes.
	// "This event is dispatched after all other events caused by the
	// mutation have been fired."
	LCount lc = LCount.lookup(MutationEventImpl.DOM_SUBTREE_MODIFIED);
	if (lc.total &gt; 0) {
	    MutationEvent me = new MutationEventImpl();
	    me.initMutationEvent(MutationEventImpl.DOM_SUBTREE_MODIFIED, true, false, null, null, null, null,
		    (short) 0);

	    // If we're within an Attr, DStM gets sent to the Attr
	    // and to its owningElement. Otherwise we dispatch it
	    // locally.
	    if (enclosingAttr != null) {
		dispatchEvent(enclosingAttr, me);
		if (owner != null)
		    dispatchEvent(owner, me);
	    } else
		dispatchEvent(node, me);
	}
    }

    /**
     * Introduced in DOM Level 2. &lt;p&gt;
     * Distribution engine for DOM Level 2 Events.
     * &lt;p&gt;
     * Event propagation runs as follows:
     * &lt;ol&gt;
     * &lt;li&gt;Event is dispatched to a particular target node, which invokes
     *   this code. Note that the event's stopPropagation flag is
     *   cleared when dispatch begins; thereafter, if it has
     *   been set before processing of a node commences, we instead
     *   immediately advance to the DEFAULT phase.
     * &lt;li&gt;The node's ancestors are established as destinations for events.
     *   For capture and bubble purposes, node ancestry is determined at
     *   the time dispatch starts. If an event handler alters the document
     *   tree, that does not change which nodes will be informed of the event.
     * &lt;li&gt;CAPTURING_PHASE: Ancestors are scanned, root to target, for
     *   Capturing listeners. If found, they are invoked (see below).
     * &lt;li&gt;AT_TARGET:
     *   Event is dispatched to NON-CAPTURING listeners on the
     *   target node. Note that capturing listeners on this node are _not_
     *   invoked.
     * &lt;li&gt;BUBBLING_PHASE: Ancestors are scanned, target to root, for
     *   non-capturing listeners.
     * &lt;li&gt;Default processing: Some DOMs have default behaviors bound to
     *   specific nodes. If this DOM does, and if the event's preventDefault
     *   flag has not been set, we now return to the target node and process
     *   its default handler for this event, if any.
     * &lt;/ol&gt;
     * &lt;p&gt;
     * Note that registration of handlers during processing of an event does
     * not take effect during this phase of this event; they will not be called
     * until the next time this node is visited by dispatchEvent. On the other
     * hand, removals take effect immediately.
     * &lt;p&gt;
     * If an event handler itself causes events to be dispatched, they are
     * processed synchronously, before processing resumes
     * on the event which triggered them. Please be aware that this may
     * result in events arriving at listeners "out of order" relative
     * to the actual sequence of requests.
     * &lt;p&gt;
     * Note that our implementation resets the event's stop/prevent flags
     * when dispatch begins.
     * I believe the DOM's intent is that event objects be redispatchable,
     * though it isn't stated in those terms.
     * @param node node to dispatch to
     * @param event the event object to be dispatched to
     *              registered EventListeners
     * @return true if the event's &lt;code&gt;preventDefault()&lt;/code&gt;
     *              method was invoked by an EventListener; otherwise false.
    */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected boolean dispatchEvent(NodeImpl node, Event event) {
	if (event == null)
	    return false;

	// Can't use anyone else's implementation, since there's no public
	// API for setting the event's processing-state fields.
	EventImpl evt = (EventImpl) event;

	// VALIDATE -- must have been initialized at least once, must have
	// a non-null non-blank name.
	if (!evt.initialized || evt.type == null || evt.type.equals("")) {
	    String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "UNSPECIFIED_EVENT_TYPE_ERR",
		    null);
	    throw new EventException(EventException.UNSPECIFIED_EVENT_TYPE_ERR, msg);
	}

	// If nobody is listening for this event, discard immediately
	LCount lc = LCount.lookup(evt.getType());
	if (lc.total == 0)
	    return evt.preventDefault;

	// INITIALIZE THE EVENT'S DISPATCH STATUS
	// (Note that Event objects are reusable in our implementation;
	// that doesn't seem to be explicitly guaranteed in the DOM, but
	// I believe it is the intent.)
	evt.target = node;
	evt.stopPropagation = false;
	evt.preventDefault = false;

	// Capture pre-event parentage chain, not including target;
	// use pre-event-dispatch ancestors even if event handlers mutate
	// document and change the target's context.
	// Note that this is parents ONLY; events do not
	// cross the Attr/Element "blood/brain barrier".
	// DOMAttrModified. which looks like an exception,
	// is issued to the Element rather than the Attr
	// and causes a _second_ DOMSubtreeModified in the Element's
	// tree.
	List&lt;Node&gt; pv = new ArrayList&lt;&gt;(10);
	Node p = node;
	Node n = p.getParentNode();
	while (n != null) {
	    pv.add(n);
	    p = n;
	    n = n.getParentNode();
	}

	// CAPTURING_PHASE:
	if (lc.captures &gt; 0) {
	    evt.eventPhase = Event.CAPTURING_PHASE;
	    // Ancestors are scanned, root to target, for
	    // Capturing listeners.
	    for (int j = pv.size() - 1; j &gt;= 0; --j) {
		if (evt.stopPropagation)
		    break; // Someone set the flag. Phase ends.

		// Handle all capturing listeners on this node
		NodeImpl nn = (NodeImpl) pv.get(j);
		evt.currentTarget = nn;
		ArrayList&lt;LEntry&gt; nodeListeners = (ArrayList&lt;LEntry&gt;) getEventListeners(nn);
		if (nodeListeners != null) {
		    List&lt;LEntry&gt; nl = (ArrayList&lt;LEntry&gt;) nodeListeners.clone();
		    // call listeners in the order in which they got registered
		    int nlsize = nl.size();
		    for (int i = 0; i &lt; nlsize; i++) {
			LEntry le = nl.get(i);
			if (le.useCapture && le.type.equals(evt.type) && nodeListeners.contains(le)) {
			    try {
				le.listener.handleEvent(evt);
			    } catch (Exception e) {
				// All exceptions are ignored.
			    }
			}
		    }
		}
	    }
	}

	// Both AT_TARGET and BUBBLE use non-capturing listeners.
	if (lc.bubbles &gt; 0) {
	    // AT_TARGET PHASE: Event is dispatched to NON-CAPTURING listeners
	    // on the target node. Note that capturing listeners on the target
	    // node are _not_ invoked, even during the capture phase.
	    evt.eventPhase = Event.AT_TARGET;
	    evt.currentTarget = node;
	    ArrayList&lt;LEntry&gt; nodeListeners = (ArrayList&lt;LEntry&gt;) getEventListeners(node);
	    if (!evt.stopPropagation && nodeListeners != null) {
		List&lt;LEntry&gt; nl = (ArrayList&lt;LEntry&gt;) nodeListeners.clone();
		// call listeners in the order in which they got registered
		int nlsize = nl.size();
		for (int i = 0; i &lt; nlsize; i++) {
		    LEntry le = nl.get(i);
		    if (!le.useCapture && le.type.equals(evt.type) && nodeListeners.contains(le)) {
			try {
			    le.listener.handleEvent(evt);
			} catch (Exception e) {
			    // All exceptions are ignored.
			}
		    }
		}
	    }
	    // BUBBLING_PHASE: Ancestors are scanned, target to root, for
	    // non-capturing listeners. If the event's preventBubbling flag
	    // has been set before processing of a node commences, we
	    // instead immediately advance to the default phase.
	    // Note that not all events bubble.
	    if (evt.bubbles) {
		evt.eventPhase = Event.BUBBLING_PHASE;
		int pvsize = pv.size();
		for (int j = 0; j &lt; pvsize; j++) {
		    if (evt.stopPropagation)
			break; // Someone set the flag. Phase ends.

		    // Handle all bubbling listeners on this node
		    NodeImpl nn = (NodeImpl) pv.get(j);
		    evt.currentTarget = nn;
		    nodeListeners = (ArrayList&lt;LEntry&gt;) getEventListeners(nn);
		    if (nodeListeners != null) {
			List&lt;LEntry&gt; nl = (ArrayList&lt;LEntry&gt;) nodeListeners.clone();
			// call listeners in the order in which they got
			// registered
			int nlsize = nl.size();
			for (int i = 0; i &lt; nlsize; i++) {
			    LEntry le = nl.get(i);
			    if (!le.useCapture && le.type.equals(evt.type) && nodeListeners.contains(le)) {
				try {
				    le.listener.handleEvent(evt);
				} catch (Exception e) {
				    // All exceptions are ignored.
				}
			    }
			}
		    }
		}
	    }
	}

	// DEFAULT PHASE: Some DOMs have default behaviors bound to specific
	// nodes. If this DOM does, and if the event's preventDefault flag has
	// not been set, we now return to the target node and process its
	// default handler for this event, if any.
	// No specific phase value defined, since this is DOM-internal
	if (lc.defaults &gt; 0 && (!evt.cancelable || !evt.preventDefault)) {
	    // evt.eventPhase = Event.DEFAULT_PHASE;
	    // evt.currentTarget = node;
	    // DO_DEFAULT_OPERATION
	}

	return evt.preventDefault;
    }

    /**
     * Retreive event listener registered on a given node
     */
    protected List&lt;LEntry&gt; getEventListeners(NodeImpl n) {
	if (eventListeners == null) {
	    return null;
	}
	return eventListeners.get(n);
    }

}

