import java.util.*;
import nsk.share.gc.gp.array.*;
import nsk.share.gc.gp.string.*;
import nsk.share.gc.gp.list.*;
import nsk.share.gc.gp.tree.*;
import nsk.share.gc.gp.misc.*;
import nsk.share.gc.gp.classload.*;
import nsk.share.TestBug;

class GarbageUtils {
    /**
         * Get garbage producer by identifier.
         *
         * @param id identifier
         * @return garbage producer for this identifier
         */
    public static GarbageProducer getGarbageProducer(String id) {
	if (id == null || id.equals("byteArr"))
	    return new ByteArrayProducer();
	else if (id.equals("booleanArr"))
	    return new BooleanArrayProducer();
	else if (id.equals("shortArr"))
	    return new ShortArrayProducer();
	else if (id.equals("charArr"))
	    return new CharArrayProducer();
	else if (id.equals("intArr"))
	    return new IntArrayProducer();
	else if (id.equals("longArr"))
	    return new LongArrayProducer();
	else if (id.equals("floatArr"))
	    return new FloatArrayProducer();
	else if (id.equals("doubleArr"))
	    return new DoubleArrayProducer();
	else if (id.equals("objectArr"))
	    return new ObjectArrayProducer();
	else if (id.equals("randomString"))
	    return new RandomStringProducer();
	else if (id.equals("simpleString"))
	    return new SimpleStringProducer();
	else if (id.startsWith("interned("))
	    return new InternedStringProducer(getGarbageProducer(getInBrackets(id)));
	else if (id.startsWith("linearList("))
	    return new LinearListProducer(MemoryStrategy.fromString(getInBrackets(id)));
	else if (id.startsWith("circularList("))
	    return new CircularListProducer(MemoryStrategy.fromString(getInBrackets(id)));
	else if (id.startsWith("nonbranchyTree("))
	    return new NonbranchyTreeProducer(MemoryStrategy.fromString(getInBrackets(id)));
	else if (id.equals("class"))
	    return new GeneratedClassProducer();
	else if (id.startsWith("hashed("))
	    return new HashedGarbageProducer(getGarbageProducer(getInBrackets(id)));
	else if (id.startsWith("random("))
	    return new RandomProducer(getGarbageProducerList(getInBrackets(id)));
	else if (id.startsWith("twofields("))
	    return new TwoFieldsObjectProducer(getGarbageProducer(getInBrackets(id)));
	else if (id.startsWith("arrayof("))
	    return new ArrayOfProducer(getGarbageProducer(getInBrackets(id)));
	else if (id.startsWith("trace("))
	    return new TraceProducer(getGarbageProducer(getInBrackets(id)));
	else
	    throw new TestBug("Invalid garbage producer identifier: " + id);
    }

    private static GarbageProducers garbageProducers;

    private static String getInBrackets(String s) {
	int n1 = s.indexOf('(');
	if (n1 == -1)
	    throw new TestBug("Opening bracket not found: " + s);
	int n2 = s.lastIndexOf(')');
	if (n2 == -1)
	    throw new TestBug("Closing bracket not found: " + s);
	return s.substring(n1 + 1, n2);
    }

    private static List&lt;GarbageProducer&gt; getGarbageProducerList(String s) {
	if (s.equals("primitiveArrays"))
	    return getPrimitiveArrayProducers();
	else if (s.equals("arrays"))
	    return getArrayProducers();
	else {
	    String[] ids = s.split(",");
	    List&lt;GarbageProducer&gt; garbageProducers = new ArrayList&lt;GarbageProducer&gt;(ids.length);
	    for (int i = 0; i &lt; ids.length; ++i)
		garbageProducers.add(getGarbageProducer(ids[i]));
	    return garbageProducers;
	    //throw new TestBug("Invalid id for list of garbage producers: " + id);
	}
    }

    /**
         * Get all primitive array producers.
         */
    public static List&lt;GarbageProducer&gt; getPrimitiveArrayProducers() {
	return getGarbageProducers().getPrimitiveArrayProducers();
    }

    /**
         * Get all array producers.
         */
    public static List&lt;GarbageProducer&gt; getArrayProducers() {
	return getGarbageProducers().getArrayProducers();
    }

    public static GarbageProducers getGarbageProducers() {
	if (garbageProducers == null)
	    garbageProducers = new GarbageProducers();
	return garbageProducers;
    }

}

