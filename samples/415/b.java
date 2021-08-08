import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongPredicate;

class WhileOps {
    /**
     * Appends a "dropWhile" operation to the provided LongStream.
     *
     * @param upstream a reference stream with element type T
     * @param predicate the predicate that returns false to halt dropping.
     */
    static LongStream makeDropWhileLong(AbstractPipeline&lt;?, Long, ?&gt; upstream, LongPredicate predicate) {
	Objects.requireNonNull(predicate);
	class Op extends LongPipeline.StatefulOp&lt;Long&gt; implements DropWhileOp&lt;Long&gt; {
	    public Op(AbstractPipeline&lt;?, Long, ?&gt; upstream, StreamShape inputShape, int opFlags) {
		super(upstream, inputShape, opFlags);
	    }

	    @Override
	    &lt;P_IN&gt; Spliterator&lt;Long&gt; opEvaluateParallelLazy(PipelineHelper&lt;Long&gt; helper,
		    Spliterator&lt;P_IN&gt; spliterator) {
		if (StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
		    return opEvaluateParallel(helper, spliterator, Long[]::new).spliterator();
		} else {
		    return new UnorderedWhileSpliterator.OfLong.Dropping(
			    (Spliterator.OfLong) helper.wrapSpliterator(spliterator), false, predicate);
		}
	    }

	    @Override
	    &lt;P_IN&gt; Node&lt;Long&gt; opEvaluateParallel(PipelineHelper&lt;Long&gt; helper, Spliterator&lt;P_IN&gt; spliterator,
		    IntFunction&lt;Long[]&gt; generator) {
		return new DropWhileTask&lt;&gt;(this, helper, spliterator, generator).invoke();
	    }

	    @Override
	    Sink&lt;Long&gt; opWrapSink(int flags, Sink&lt;Long&gt; sink) {
		return opWrapSink(sink, false);
	    }

	    public DropWhileSink&lt;Long&gt; opWrapSink(Sink&lt;Long&gt; sink, boolean retainAndCountDroppedElements) {
		class OpSink extends Sink.ChainedLong&lt;Long&gt; implements DropWhileSink&lt;Long&gt; {
		    long dropCount;
		    boolean take;

		    OpSink() {
			super(sink);
		    }

		    @Override
		    public void accept(long t) {
			boolean takeElement = take || (take = !predicate.test(t));

			// If ordered and element is dropped increment index
			// for possible future truncation
			if (retainAndCountDroppedElements && !takeElement)
			    dropCount++;

			// If ordered need to process element, otherwise
			// skip if element is dropped
			if (retainAndCountDroppedElements || takeElement)
			    downstream.accept(t);
		    }

		    @Override
		    public long getDropCount() {
			return dropCount;
		    }
		}
		return new OpSink();
	    }
	}
	return new Op(upstream, StreamShape.LONG_VALUE, DROP_FLAGS);
    }

    static final int DROP_FLAGS = StreamOpFlag.NOT_SIZED;

    class Op extends StatefulOp&lt;Long&gt; implements DropWhileOp&lt;Long&gt; {
	static final int DROP_FLAGS = StreamOpFlag.NOT_SIZED;

	@Override
	&lt;P_IN&gt; Node&lt;Long&gt; opEvaluateParallel(PipelineHelper&lt;Long&gt; helper, Spliterator&lt;P_IN&gt; spliterator,
		IntFunction&lt;Long[]&gt; generator) {
	    return new DropWhileTask&lt;&gt;(this, helper, spliterator, generator).invoke();
	}

	public DropWhileSink&lt;Long&gt; opWrapSink(Sink&lt;Long&gt; sink, boolean retainAndCountDroppedElements) {
	    class OpSink extends Sink.ChainedLong&lt;Long&gt; implements DropWhileSink&lt;Long&gt; {
		long dropCount;
		boolean take;

		OpSink() {
		    super(sink);
		}

		@Override
		public void accept(long t) {
		    boolean takeElement = take || (take = !predicate.test(t));

		    // If ordered and element is dropped increment index
		    // for possible future truncation
		    if (retainAndCountDroppedElements && !takeElement)
			dropCount++;

		    // If ordered need to process element, otherwise
		    // skip if element is dropped
		    if (retainAndCountDroppedElements || takeElement)
			downstream.accept(t);
		}

		@Override
		public long getDropCount() {
		    return dropCount;
		}
	    }
	    return new OpSink();
	}

	public Op(AbstractPipeline&lt;?, Long, ?&gt; upstream, StreamShape inputShape, int opFlags) {
	    super(upstream, inputShape, opFlags);
	}

	class OpSink extends ChainedLong&lt;Long&gt; implements DropWhileSink&lt;Long&gt; {
	    OpSink() {
		super(sink);
	    }

	}

    }

    class DropWhileTask&lt;P_IN, P_OUT&gt; extends AbstractTask&lt;P_IN, P_OUT, Node&lt;P_OUT&gt;, DropWhileTask&lt;P_IN, P_OUT&gt;&gt; {
	static final int DROP_FLAGS = StreamOpFlag.NOT_SIZED;

	DropWhileTask(AbstractPipeline&lt;P_OUT, P_OUT, ?&gt; op, PipelineHelper&lt;P_OUT&gt; helper, Spliterator&lt;P_IN&gt; spliterator,
		IntFunction&lt;P_OUT[]&gt; generator) {
	    super(helper, spliterator);
	    assert op instanceof DropWhileOp;
	    this.op = op;
	    this.generator = generator;
	    this.isOrdered = StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags());
	}

    }

    abstract class UnorderedWhileSpliterator&lt;T, T_SPLITR&gt; implements Spliterator&lt;T&gt; {
	static final int DROP_FLAGS = StreamOpFlag.NOT_SIZED;

	UnorderedWhileSpliterator(T_SPLITR s, boolean noSplitting) {
	    this.s = s;
	    this.noSplitting = noSplitting;
	    this.cancel = new AtomicBoolean();
	}

	abstract class OfLong extends UnorderedWhileSpliterator&lt;Long, OfLong&gt; implements LongConsumer, OfLong {
	    final T_SPLITR s;
	    final boolean noSplitting;
	    final AtomicBoolean cancel;

	    OfLong(Spliterator.OfLong s, boolean noSplitting, LongPredicate p) {
		super(s, noSplitting);
		this.p = p;
	    }

	    class Dropping extends OfLong {
		final LongPredicate p;

		Dropping(Spliterator.OfLong s, boolean noSplitting, LongPredicate p) {
		    super(s, noSplitting, p);
		}

	    }

	}

    }

}

