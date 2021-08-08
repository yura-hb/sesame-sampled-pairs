import java.util.NoSuchElementException;
import java.util.Objects;

class StackStreamFactory {
    abstract class FrameBuffer&lt;F&gt; {
	/**
	 * Gets the class at the current frame and move to the next frame.
	 */
	final Class&lt;?&gt; next() {
	    if (isEmpty()) {
		throw new NoSuchElementException("origin=" + origin + " fence=" + fence);
	    }
	    Class&lt;?&gt; c = at(origin);
	    origin++;
	    if (isDebug) {
		int index = origin - 1;
		System.out.format("  next frame at %d: %s (origin %d fence %d)%n", index, Objects.toString(c), index,
			fence);
	    }
	    return c;
	}

	int origin;
	int fence;
	static final int START_POS = 2;

	final boolean isEmpty() {
	    return origin &gt;= fence || (origin == START_POS && fence == 0);
	}

	/**
	 * Return the class at the given position in the current batch.
	 * @param index the position of the frame.
	 * @return the class at the given position in the current batch.
	 */
	abstract Class&lt;?&gt; at(int index);

    }

    final static boolean isDebug = "true".equals(GetPropertyAction.privilegedGetProperty("stackwalk.debug"));

}

