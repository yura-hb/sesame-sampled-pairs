import org.apache.commons.math4.RealFieldElement;
import org.apache.commons.math4.ode.AbstractFieldIntegrator;

abstract class AdaptiveStepsizeFieldIntegrator&lt;T&gt; extends AbstractFieldIntegrator&lt;T&gt; {
    /** Reset internal state to dummy values. */
    protected void resetInternalState() {
	setStepStart(null);
	setStepSize(minStep.multiply(maxStep).sqrt());
    }

    /** Minimal step. */
    private T minStep;
    /** Maximal step. */
    private T maxStep;

}

