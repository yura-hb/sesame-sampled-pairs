import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;

class NegativeModelProc extends AbstractProcessor {
    /**
     * Check the annotations in the model of resources/targets.negative.pa.Negative2
     * @return true if all tests passed
     */
    public boolean checkNegative2() {
	TypeElement elementN2 = _elementUtils.getTypeElement("targets.negative.pa.Negative2");
	if (null == elementN2 || elementN2.getKind() != ElementKind.CLASS) {
	    reportError("Element Negative2 was not found or was not a class");
	    return false;
	}
	List&lt;? extends Element&gt; enclosedElements = elementN2.getEnclosedElements();
	for (Element element : enclosedElements) {
	    String name = element.getSimpleName().toString();
	    if ("m1".equals(name)) {
		AnnotationMirror am2 = findAnnotation(element, "Anno2");
		if (_reportFailingCases && null == am2) {
		    reportError("Couldn't find annotation Anno2 on method Negative2.m1");
		    return false;
		}
	    } else if ("m2".equals(name)) {
		AnnotationMirror am1 = findAnnotation(element, "Anno1");
		if (_reportFailingCases && null == am1) {
		    reportError("Couldn't find annotation Anno1 on method Negative2.m2");
		    return false;
		}
		AnnotationMirror am3 = findAnnotation(element, "FakeAnno3");
		if (_reportFailingCases && null == am3) {
		    reportError("Couldn't find annotation FakeAnno3 on method Negative2.m2");
		    return false;
		}
	    } else if ("m3".equals(name)) {
		AnnotationMirror am2 = findAnnotation(element, "Anno2");
		if (_reportFailingCases && null == am2) {
		    reportError("Couldn't find annotation Anno2 on method Negative2.m3");
		    return false;
		}
		AnnotationMirror am3 = findAnnotation(element, "FakeAnno3");
		if (_reportFailingCases && null == am3) {
		    reportError("Couldn't find annotation FakeAnno3 on method Negative2.m3");
		    return false;
		}
	    } else if ("m4".equals(name)) {
		AnnotationMirror am4 = findAnnotation(element, "Anno4");
		if (_reportFailingCases && null == am4) {
		    reportError("Couldn't find annotation Anno4 on method Negative2.m4");
		    return false;
		}
		Map&lt;? extends ExecutableElement, ? extends AnnotationValue&gt; values = am4.getElementValues();
		for (Map.Entry&lt;? extends ExecutableElement, ? extends AnnotationValue&gt; entry : values.entrySet()) {
		    if ("value".equals(entry.getKey().getSimpleName().toString())) {
			String value = entry.getValue().getValue().toString();
			if (!"123".equals(value) && !"&lt;error&gt;".equals(value)) {
			    reportError("Unexpected value for Anno4 on Negative1.s1: " + value);
			    return false;
			}
		    }
		}
	    }
	}
	return true;
    }

    private Elements _elementUtils;
    private boolean _reportFailingCases = true;
    private static final String CLASSNAME = NegativeModelProc.class.getName();

    /**
     * Report an error to the test case code.  
     * This is not the same as reporting via Messager!  Use this if some API fails.
     * @param value will be displayed in the test output, in the event of failure.
     * Can be anything except "succeeded".
     */
    public static void reportError(String value) {
	// Uncomment for processor debugging - don't report error
	// value = "succeeded";
	System.setProperty(CLASSNAME, value);
    }

    /**
     * Find a particular annotation on a specified element.
     * @param el the annotated element
     * @param name the simple name of the annotation
     * @return a mirror for the annotation, or null if the annotation was not found.
     */
    private AnnotationMirror findAnnotation(Element el, String name) {
	for (AnnotationMirror am : el.getAnnotationMirrors()) {
	    DeclaredType annoType = am.getAnnotationType();
	    if (null != annoType) {
		Element annoTypeElement = annoType.asElement();
		if (null != annoTypeElement) {
		    if (name.equals(annoTypeElement.getSimpleName().toString())) {
			return am;
		    }
		}
	    }
	}
	return null;
    }

}

