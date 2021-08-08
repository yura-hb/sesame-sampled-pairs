import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import jdk.javadoc.internal.doclets.toolkit.SerializedFormWriter;
import jdk.javadoc.internal.doclets.toolkit.util.Utils;

class SerializedFormBuilder extends AbstractBuilder {
    /**
     * Build the information for the method.
     *
     * @param methodsContentTree content tree to which the documentation will be added
     * @throws DocletException if there is a problem while building the documentation
     */
    protected void buildMethodInfo(Content methodsContentTree) throws DocletException {
	if (configuration.nocomment) {
	    return;
	}

	buildMethodDescription(methodsContentTree);
	buildMethodTags(methodsContentTree);
    }

    /**
     * The writer for serializable method documentation.
     */
    private SerializedFormWriter.SerialMethodWriter methodWriter;
    /**
     * The current member being documented.
     */
    protected Element currentMember;

    /**
     * Build method description.
     *
     * @param methodsContentTree content tree to which the documentation will be added
     */
    protected void buildMethodDescription(Content methodsContentTree) {
	methodWriter.addMemberDescription((ExecutableElement) currentMember, methodsContentTree);
    }

    /**
     * Build the method tags.
     *
     * @param methodsContentTree content tree to which the documentation will be added
     */
    protected void buildMethodTags(Content methodsContentTree) {
	methodWriter.addMemberTags((ExecutableElement) currentMember, methodsContentTree);
	ExecutableElement method = (ExecutableElement) currentMember;
	if (method.getSimpleName().toString().compareTo("writeExternal") == 0
		&& utils.getSerialDataTrees(method).isEmpty()) {
	    if (configuration.serialwarn) {
		TypeElement encl = (TypeElement) method.getEnclosingElement();
		messages.warning(currentMember, "doclet.MissingSerialDataTag", encl.getQualifiedName().toString(),
			method.getSimpleName().toString());
	    }
	}
    }

}

