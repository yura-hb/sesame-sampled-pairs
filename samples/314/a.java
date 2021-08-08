import com.google.common.collect.testing.Helpers;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

class FeatureUtil {
    /**
    * Construct the set of requirements specified by annotations directly on a tester class or
    * method.
    *
    * @param classOrMethod a tester class or a test method thereof
    * @return all the constraints implicitly or explicitly required by annotations on the class or
    *     method.
    * @throws ConflictingRequirementsException if the requirements are mutually inconsistent.
    */
    public static TesterRequirements buildDeclaredTesterRequirements(AnnotatedElement classOrMethod)
	    throws ConflictingRequirementsException {
	TesterRequirements requirements = new TesterRequirements();

	Iterable&lt;Annotation&gt; testerAnnotations = getTesterAnnotations(classOrMethod);
	for (Annotation testerAnnotation : testerAnnotations) {
	    TesterRequirements moreRequirements = buildTesterRequirements(testerAnnotation);
	    incorporateRequirements(requirements, moreRequirements, testerAnnotation);
	}

	return requirements;
    }

    /** A cache of annotated objects (typically a Class or Method) to its set of annotations. */
    private static Map&lt;AnnotatedElement, List&lt;Annotation&gt;&gt; annotationCache = new HashMap&lt;&gt;();

    /**
    * Find all the tester annotations declared on a tester class or method.
    *
    * @param classOrMethod a class or method whose tester annotations to find
    * @return an iterable sequence of tester annotations on the class
    */
    public static Iterable&lt;Annotation&gt; getTesterAnnotations(AnnotatedElement classOrMethod) {
	synchronized (annotationCache) {
	    List&lt;Annotation&gt; annotations = annotationCache.get(classOrMethod);
	    if (annotations == null) {
		annotations = new ArrayList&lt;&gt;();
		for (Annotation a : classOrMethod.getDeclaredAnnotations()) {
		    if (a.annotationType().isAnnotationPresent(TesterAnnotation.class)) {
			annotations.add(a);
		    }
		}
		annotations = Collections.unmodifiableList(annotations);
		annotationCache.put(classOrMethod, annotations);
	    }
	    return annotations;
	}
    }

    /**
    * Find all the constraints explicitly or implicitly specified by a single tester annotation.
    *
    * @param testerAnnotation a tester annotation
    * @return the requirements specified by the annotation
    * @throws ConflictingRequirementsException if the requirements are mutually inconsistent.
    */
    private static TesterRequirements buildTesterRequirements(Annotation testerAnnotation)
	    throws ConflictingRequirementsException {
	Class&lt;? extends Annotation&gt; annotationClass = testerAnnotation.annotationType();
	final Feature&lt;?&gt;[] presentFeatures;
	final Feature&lt;?&gt;[] absentFeatures;
	try {
	    presentFeatures = (Feature[]) annotationClass.getMethod("value").invoke(testerAnnotation);
	    absentFeatures = (Feature[]) annotationClass.getMethod("absent").invoke(testerAnnotation);
	} catch (Exception e) {
	    throw new IllegalArgumentException("Error extracting features from tester annotation.", e);
	}
	Set&lt;Feature&lt;?&gt;&gt; allPresentFeatures = addImpliedFeatures(Helpers.&lt;Feature&lt;?&gt;&gt;copyToSet(presentFeatures));
	Set&lt;Feature&lt;?&gt;&gt; allAbsentFeatures = addImpliedFeatures(Helpers.&lt;Feature&lt;?&gt;&gt;copyToSet(absentFeatures));
	if (!Collections.disjoint(allPresentFeatures, allAbsentFeatures)) {
	    throw new ConflictingRequirementsException("Annotation explicitly or "
		    + "implicitly requires one or more features to be both present " + "and absent.",
		    intersection(allPresentFeatures, allAbsentFeatures), testerAnnotation);
	}
	return new TesterRequirements(allPresentFeatures, allAbsentFeatures);
    }

    /**
    * Incorporate additional requirements into an existing requirements object.
    *
    * @param requirements the existing requirements object
    * @param moreRequirements more requirements to incorporate
    * @param source the source of the additional requirements (used only for error reporting)
    * @return the existing requirements object, modified to include the additional requirements
    * @throws ConflictingRequirementsException if the additional requirements are inconsistent with
    *     the existing requirements
    */
    private static TesterRequirements incorporateRequirements(TesterRequirements requirements,
	    TesterRequirements moreRequirements, Object source) throws ConflictingRequirementsException {
	Set&lt;Feature&lt;?&gt;&gt; presentFeatures = requirements.getPresentFeatures();
	Set&lt;Feature&lt;?&gt;&gt; absentFeatures = requirements.getAbsentFeatures();
	Set&lt;Feature&lt;?&gt;&gt; morePresentFeatures = moreRequirements.getPresentFeatures();
	Set&lt;Feature&lt;?&gt;&gt; moreAbsentFeatures = moreRequirements.getAbsentFeatures();
	checkConflict("absent", absentFeatures, "present", morePresentFeatures, source);
	checkConflict("present", presentFeatures, "absent", moreAbsentFeatures, source);
	presentFeatures.addAll(morePresentFeatures);
	absentFeatures.addAll(moreAbsentFeatures);
	return requirements;
    }

    /**
    * Given a set of features, add to it all the features directly or indirectly implied by any of
    * them, and return it.
    *
    * @param features the set of features to expand
    * @return the same set of features, expanded with all implied features
    */
    public static Set&lt;Feature&lt;?&gt;&gt; addImpliedFeatures(Set&lt;Feature&lt;?&gt;&gt; features) {
	Queue&lt;Feature&lt;?&gt;&gt; queue = new ArrayDeque&lt;&gt;(features);
	while (!queue.isEmpty()) {
	    Feature&lt;?&gt; feature = queue.remove();
	    for (Feature&lt;?&gt; implied : feature.getImpliedFeatures()) {
		if (features.add(implied)) {
		    queue.add(implied);
		}
	    }
	}
	return features;
    }

    /** Construct a new {@link java.util.Set} that is the intersection of the given sets. */
    public static &lt;T&gt; Set&lt;T&gt; intersection(Set&lt;? extends T&gt; set1, Set&lt;? extends T&gt; set2) {
	Set&lt;T&gt; result = Helpers.&lt;T&gt;copyToSet(set1);
	result.retainAll(set2);
	return result;
    }

    private static void checkConflict(String earlierRequirement, Set&lt;Feature&lt;?&gt;&gt; earlierFeatures, String newRequirement,
	    Set&lt;Feature&lt;?&gt;&gt; newFeatures, Object source) throws ConflictingRequirementsException {
	if (!Collections.disjoint(newFeatures, earlierFeatures)) {
	    throw new ConflictingRequirementsException(String.format(Locale.ROOT,
		    "Annotation requires to be %s features that earlier " + "annotations required to be %s.",
		    newRequirement, earlierRequirement), intersection(newFeatures, earlierFeatures), source);
	}
    }

}

