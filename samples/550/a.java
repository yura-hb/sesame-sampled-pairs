import org.nd4j.linalg.activations.impl.*;

class Activation extends Enum&lt;Activation&gt; {
    /**
     * Creates an instance of the activation function
     *
     * @return an instance of the activation function
     */
    public IActivation getActivationFunction() {
	switch (this) {
	case CUBE:
	    return new ActivationCube();
	case ELU:
	    return new ActivationELU();
	case HARDSIGMOID:
	    return new ActivationHardSigmoid();
	case HARDTANH:
	    return new ActivationHardTanH();
	case IDENTITY:
	    return new ActivationIdentity();
	case LEAKYRELU:
	    return new ActivationLReLU();
	case RATIONALTANH:
	    return new ActivationRationalTanh();
	case RECTIFIEDTANH:
	    return new ActivationRectifiedTanh();
	case RELU:
	    return new ActivationReLU();
	case RELU6:
	    return new ActivationReLU6();
	case SELU:
	    return new ActivationSELU();
	case SWISH:
	    return new ActivationSwish();
	case RRELU:
	    return new ActivationRReLU();
	case SIGMOID:
	    return new ActivationSigmoid();
	case SOFTMAX:
	    return new ActivationSoftmax();
	case SOFTPLUS:
	    return new ActivationSoftPlus();
	case SOFTSIGN:
	    return new ActivationSoftSign();
	case TANH:
	    return new ActivationTanH();
	case THRESHOLDEDRELU:
	    return new ActivationThresholdedReLU();
	default:
	    throw new UnsupportedOperationException("Unknown or not supported activation function: " + this);
	}
    }

}

