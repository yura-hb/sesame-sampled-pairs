class Random {
    /**
     * for testing purposes
     * @param args
     */
    public static void main(String[] args) {
	Random r = new Random(11);
	int[] a = new int[100];

	for (int i = 0; i &lt; 1000; i++) {
	    a[r.nextInt(100)]++;
	}

	for (int i = 0; i &lt; 100; i++) {
	    System.out.println(times(a[i]));
	}

    }

    private int current;
    private static long BASE = 1003001;

    public Random(int init) {
	this.current = init;
    }

    public int nextInt(int n) {
	return nextInt() % n;
    }

    public static String times(int n) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i &lt; n; i++) {
	    sb.append("*");
	}
	return sb.toString();
    }

    public int nextInt() {
	current = (int) ((long) current * current % BASE - 1);
	return current;
    }

}

