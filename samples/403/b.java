import org.nd4j.linalg.primitives.Atomic;
import org.nd4j.parameterserver.distributed.v2.messages.pairs.params.ModelParametersMessage;
import org.nd4j.parameterserver.distributed.v2.messages.pairs.params.ModelParametersRequest;
import org.nd4j.parameterserver.distributed.v2.messages.pairs.params.UpdaterParametersMessage;
import org.nd4j.parameterserver.distributed.v2.messages.pairs.params.UpdaterParametersRequest;
import org.nd4j.parameterserver.distributed.v2.transport.Transport;
import org.nd4j.parameterserver.distributed.v2.transport.UpdaterParametersProvider;
import org.nd4j.parameterserver.distributed.v2.transport.UpdatesHandler;
import org.nd4j.parameterserver.distributed.v2.util.UpdaterParametersHolder;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class ModelParameterServer {
    /**
     * This method starts parameter server
     */
    public synchronized void launch() {
	log.info("ModelParameterServer starts");
	if (launchLock.get())
	    return;

	transport.setRestartCallback(new RestartCallback() {
	    @Override
	    public void call(HandshakeResponse response) {
		// upon restart command we'll request current parameters from the current upstream (without any propagation
		try {
		    log.info("Restart callback started...");
		    val msg = new ModelParametersRequest();
		    val rootId = transport.getRootId();
		    ModelParametersMessage modelParams = transport.sendMessageBlocking(msg, rootId);
		    val mParams = modelParams.getPayload();
		    modelParamsSubsribers.forEach(s -&gt; s.onNext(mParams));

		    // updating starting points
		    iterationNumber.set(modelParams.getIterationNumber());
		    epochNumber.set(modelParams.getEpochNumber());

		    // updater parameters are optional, it's possible to have models without updater parameters (i.e. SGD)
		    UpdaterParametersMessage updaterParams = transport
			    .sendMessageBlocking(new UpdaterParametersRequest(), rootId);
		    val uParams = updaterParams.getPayload();
		    if (uParams != null) {
			updaterParamsSubscribers.forEach(s -&gt; s.onNext(uParams));
			log.info("Updater parameters propagated...");
		    }
		} catch (Exception e) {
		    log.error("RestartCallback processing exception: {}", e);
		    throw new RuntimeException(e);
		}
	    }
	});

	// listener for model params requests
	transport.addRequestConsumer(ModelParametersRequest.class, new Consumer&lt;ModelParametersRequest&gt;() {
	    @Override
	    public void accept(ModelParametersRequest modelParametersRequest) throws Exception {
		// send model parameters somewhere
		val msg = new ModelParametersMessage(java.util.UUID.randomUUID().toString(),
			updatesSubscribers.get(0).getParametersArray());
		msg.setRequestId(modelParametersRequest.getRequestId());
		msg.setIterationNumber(iterationNumber.get());
		msg.setEpochNumber(epochNumber.get());
		transport.sendMessage(msg, modelParametersRequest.getOriginatorId());
	    }
	});

	if (masterMode) {
	    // on master node when updater params come - we're just storing them
	    addUpdaterParamsSubscriber(new AbstractSubscriber&lt;INDArray&gt;() {
		@Override
		public void onNext(INDArray array) {
		    // we're keeping first final updater params state
		    if (gotFinalState.get())
			return;
		    try {
			updaterParamsLock.writeLock().lock();

			// just store new array
			updaterParameters.get().setParameters(array);
			updaterParameters.get().setTimeReceived(System.currentTimeMillis());
		    } finally {
			updaterParamsLock.writeLock().unlock();
		    }
		}
	    });

	    // listener for updater params requests
	    transport.addRequestConsumer(UpdaterParametersRequest.class, new Consumer&lt;UpdaterParametersRequest&gt;() {
		@Override
		public void accept(UpdaterParametersRequest updaterParametersRequest) throws Exception {
		    // master mode physically can't have own updater parameters, so we're acting as proxy here

		    // we're not requesting updater params if
		    if (!gotFinalState.get()) {
			val tId = transport.getRandomDownstreamFrom(transport.getRootId(),
				updaterParametersRequest.getOriginatorId());
			log.info("Sending UpdaterParameters request to [{}]", tId);

			// trying to get updaters from root downstreams, excluding original message sender
			UpdaterParametersMessage updaterParams = transport
				.sendMessageBlocking(new UpdaterParametersRequest(), tId);
			val uParams = updaterParams.getPayload();

			try {
			    updaterParamsLock.writeLock().lock();

			    if (updaterParameters.get() == null) {
				updaterParameters
					.set(new UpdaterParametersHolder(uParams, System.currentTimeMillis(), false));
			    } else
				updaterParameters.get().setParameters(uParams);

			} finally {
			    updaterParamsLock.writeLock().unlock();
			}
		    }

		    try {
			updaterParamsLock.readLock().lock();

			// send updater parameters somewhere
			log.info("Trying to send back Updater parameters...");
			val msg = new UpdaterParametersMessage(java.util.UUID.randomUUID().toString(),
				updaterParameters.get().getParameters());
			msg.setRequestId(updaterParametersRequest.getRequestId());
			transport.sendMessage(msg, updaterParametersRequest.getOriginatorId());
		    } finally {
			updaterParamsLock.readLock().unlock();
		    }
		}
	    });
	} else {
	    // in case of regular
	    transport.addRequestConsumer(UpdaterParametersRequest.class, new Consumer&lt;UpdaterParametersRequest&gt;() {
		@Override
		public void accept(UpdaterParametersRequest updaterParametersRequest) throws Exception {
		    // master mode physically can't have updater parameters
		    log.info("Trying to send back Updater parameters...");
		    if (updaterParametersProvider == null) {
			log.warn("UpdaterParametersProvider wasn't set!");
			val msg = new UpdaterParametersMessage(java.util.UUID.randomUUID().toString(), null);
			msg.setRequestId(updaterParametersRequest.getRequestId());
			transport.sendMessage(msg, updaterParametersRequest.getOriginatorId());
		    } else {
			// send updater parameters back
			val msg = new UpdaterParametersMessage(java.util.UUID.randomUUID().toString(),
				updaterParametersProvider.getUpdaterParameters());
			msg.setRequestId(updaterParametersRequest.getRequestId());
			transport.sendMessage(msg, updaterParametersRequest.getOriginatorId());
		    }
		}
	    });
	}

	// this flow will be providing INDArray messages
	disposable = Flowable.fromPublisher(transport.incomingPublisher()).subscribe(message -&gt; {
	    /**
	     * We process messages here. Messages are either contain INDArrays, say, as gradients update, or as  model parameters.
	     */
	    if (message instanceof GradientsUpdateMessage) {
		// we don't really care about synchronization here
		val gum = (GradientsUpdateMessage) message;
		if (iterationNumber.get() &lt; gum.getIteration())
		    iterationNumber.set(gum.getIteration());

		if (epochNumber.get() &lt; gum.getEpoch())
		    epochNumber.set(gum.getEpoch());

		// it's possible to get updates messages BEFORE model was properly initalized
		if (updatesSubscribers.isEmpty()) {
		    log.info("Storing GradientsUpdateMessage into backlog queue...");
		    updatesQueue.add(message.getPayload());
		} else {
		    log.info("Propagating GradientsUpdateMessage to subscribers: [{}]", updatesSubscribers.size());
		    updatesSubscribers.forEach(s -&gt; s.onNext(message.getPayload()));
		}
	    } else
		throw new UnsupportedOperationException(
			"Unknown message received: [" + message.getClass().getCanonicalName() + "]");
	});

	// we start transport only once we're ready
	if (this.masterMode)
	    transport.launchAsMaster();
	else {
	    transport.launch();
	}

	// instance can be stopped now
	stopLock.set(false);

	launchLock.set(true);
    }

    private final AtomicBoolean launchLock = new AtomicBoolean(false);
    @Getter
    private Transport transport;
    protected final List&lt;Subscriber&lt;INDArray&gt;&gt; modelParamsSubsribers = new CopyOnWriteArrayList&lt;&gt;();
    private AtomicInteger iterationNumber = new AtomicInteger(0);
    private AtomicInteger epochNumber = new AtomicInteger(0);
    protected final List&lt;Subscriber&lt;INDArray&gt;&gt; updaterParamsSubscribers = new CopyOnWriteArrayList&lt;&gt;();
    protected final List&lt;UpdatesHandler&gt; updatesSubscribers = new CopyOnWriteArrayList&lt;&gt;();
    private boolean masterMode;
    protected final AtomicBoolean gotFinalState = new AtomicBoolean(false);
    protected final ReentrantReadWriteLock updaterParamsLock = new ReentrantReadWriteLock();
    protected final Atomic&lt;UpdaterParametersHolder&gt; updaterParameters = new Atomic&lt;&gt;();
    private UpdaterParametersProvider updaterParametersProvider;
    private Disposable disposable;
    private final AtomicBoolean stopLock = new AtomicBoolean(false);

    /**
     * This method adds subcriber that will be called upon updater params receival
     * @param s
     */
    public void addUpdaterParamsSubscriber(@NonNull Subscriber&lt;INDArray&gt; s) {
	updaterParamsSubscribers.add(s);
    }

}

