class thrcputime002Thread extends Thread {
    /** Run some code with given number of iterations. */
    void runIterations(int n) {
	for (int k = 0; k &lt; n; k++) {
	    int s = k;
	    for (int i = 0; i &lt; n; i++) {
		if (i % 2 == 0) {
		    s += i * 10;
		} else {
		    s -= i * 10;
		}
	    }
	}
    }

}

