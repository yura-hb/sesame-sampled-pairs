import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

class Defaults {
    /**
     * Simulates races by modifying the map within the remapping function.
     */
    @Test
    public void testConcurrentMap_computeIfPresent_racy() {
	final AtomicBoolean b = new AtomicBoolean(true);
	final ConcurrentMap&lt;Long, Long&gt; map = new ImplementsConcurrentMap&lt;&gt;();
	final Long two = 2L;
	BiFunction&lt;Long, Long, Long&gt; f, g;

	for (Long val : new Long[] { null, 86L }) {
	    map.clear();

	    // Function not invoked if no mapping exists
	    f = (k, v) -&gt; {
		map.put(two, 42L);
		return val;
	    };
	    assertNull(map.computeIfPresent(two, f));
	    assertNull(map.get(two));

	    map.put(two, 42L);
	    f = (k, v) -&gt; {
		map.put(two, 86L);
		return val;
	    };
	    g = (k, v) -&gt; {
		assertSame(two, k);
		assertEquals(86L, (long) v);
		return null;
	    };
	    assertNull(map.computeIfPresent(two, twoStep(b, f, g)));
	    assertFalse(map.containsKey(two));
	    assertTrue(b.get());

	    map.put(two, 42L);
	    f = (k, v) -&gt; {
		map.put(two, 86L);
		return val;
	    };
	    g = (k, v) -&gt; {
		assertSame(two, k);
		assertEquals(86L, (long) v);
		return 99L;
	    };
	    assertEquals(99L, (long) map.computeIfPresent(two, twoStep(b, f, g)));
	    assertTrue(map.containsKey(two));
	    assertTrue(b.get());
	}
    }

    /** A function that flipflops between running two other functions. */
    static &lt;T, U, V&gt; BiFunction&lt;T, U, V&gt; twoStep(AtomicBoolean b, BiFunction&lt;T, U, V&gt; first,
	    BiFunction&lt;T, U, V&gt; second) {
	return (t, u) -&gt; {
	    boolean bb = b.get();
	    try {
		return (b.get() ? first : second).apply(t, u);
	    } finally {
		b.set(!bb);
	    }
	};
    }

    class ImplementsConcurrentMap&lt;K, V&gt; extends ExtendsAbstractMap&lt;ConcurrentMap&lt;K, V&gt;, K, V&gt;
	    implements ConcurrentMap&lt;K, V&gt; {
	public ImplementsConcurrentMap() {
	    super(new ConcurrentHashMap&lt;K, V&gt;());
	}

    }

    class ExtendsAbstractMap&lt;M, K, V&gt; extends AbstractMap&lt;K, V&gt; {
	protected ExtendsAbstractMap(M map) {
	    this.map = map;
	}

    }

}

