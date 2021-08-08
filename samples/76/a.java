class LocalCache&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements ConcurrentMap&lt;K, V&gt; {
    /**
    * Applies a supplemental hash function to a given hash code, which defends against poor quality
    * hash functions. This is critical when the concurrent hash map uses power-of-two length hash
    * tables, that otherwise encounter collisions for hash codes that do not differ in lower or upper
    * bits.
    *
    * @param h hash code
    */
    static int rehash(int h) {
	// Spread bits to regularize both segment and index locations,
	// using variant of single-word Wang/Jenkins hash.
	// TODO(kevinb): use Hashing/move this to Hashing?
	h += (h &lt;&lt; 15) ^ 0xffffcd7d;
	h ^= (h &gt;&gt;&gt; 10);
	h += (h &lt;&lt; 3);
	h ^= (h &gt;&gt;&gt; 6);
	h += (h &lt;&lt; 2) + (h &lt;&lt; 14);
	return h ^ (h &gt;&gt;&gt; 16);
    }

}

