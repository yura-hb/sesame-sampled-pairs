import java.lang.invoke.VarHandle;
import java.util.concurrent.ConcurrentHashMap;

class WhiteBox {
    /** Checks conditions which should always be true. */
    void assertInvariants(ConcurrentHashMap m) {
	if (!m.isEmpty())
	    assertNotNull(table(m));
    }

    final VarHandle TABLE, NEXTTABLE, SIZECTL;

    Object[] table(ConcurrentHashMap m) {
	return (Object[]) TABLE.getVolatile(m);
    }

}

