import java.security.*;
import java.security.spec.*;
import javax.crypto.spec.DHParameterSpec;

class DHParameterGenerator extends AlgorithmParameterGeneratorSpi {
    /**
     * Generates the parameters.
     *
     * @return the new AlgorithmParameters object
     */
    @Override
    protected AlgorithmParameters engineGenerateParameters() {

	if (random == null) {
	    random = SunJCE.getRandom();
	}

	BigInteger paramP = null;
	BigInteger paramG = null;
	try {
	    AlgorithmParameterGenerator dsaParamGen = AlgorithmParameterGenerator.getInstance("DSA");
	    dsaParamGen.init(primeSize, random);
	    AlgorithmParameters dsaParams = dsaParamGen.generateParameters();
	    DSAParameterSpec dsaParamSpec = dsaParams.getParameterSpec(DSAParameterSpec.class);

	    DHParameterSpec dhParamSpec;
	    if (this.exponentSize &gt; 0) {
		dhParamSpec = new DHParameterSpec(dsaParamSpec.getP(), dsaParamSpec.getG(), this.exponentSize);
	    } else {
		dhParamSpec = new DHParameterSpec(dsaParamSpec.getP(), dsaParamSpec.getG());
	    }
	    AlgorithmParameters algParams = AlgorithmParameters.getInstance("DH", SunJCE.getInstance());
	    algParams.init(dhParamSpec);

	    return algParams;
	} catch (Exception ex) {
	    throw new ProviderException("Unexpected exception", ex);
	}
    }

    private SecureRandom random = null;
    private int primeSize = DEF_DH_KEY_SIZE;
    private int exponentSize = 0;

}

