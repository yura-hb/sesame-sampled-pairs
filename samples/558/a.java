abstract class BoundedLocalCache&lt;K, V&gt; extends DrainStatusRef&lt;K, V&gt; implements LocalCache&lt;K, V&gt; {
    /** Returns if the node's value is currently being computed, asynchronously. */
    final boolean isComputingAsync(Node&lt;?, ?&gt; node) {
	return isAsync && !Async.isReady((CompletableFuture&lt;?&gt;) node.getValue());
    }

    final boolean isAsync;

}

