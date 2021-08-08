import javax.lang.model.element.TypeElement;
import jdk.javadoc.internal.doclets.toolkit.util.Utils;
import jdk.javadoc.internal.doclets.toolkit.util.links.LinkInfo;

class LinkInfoImpl extends LinkInfo {
    /**
     * {@inheritDoc}
     *
     * This method sets the link attributes to the appropriate values
     * based on the context.
     *
     * @param c the context id to set.
     */
    public final void setContext(Kind c) {
	//NOTE:  Put context specific link code here.
	switch (c) {
	case ALL_CLASSES_FRAME:
	case PACKAGE_FRAME:
	case IMPLEMENTED_CLASSES:
	case SUBCLASSES:
	case EXECUTABLE_ELEMENT_COPY:
	case VARIABLE_ELEMENT_COPY:
	case PROPERTY_COPY:
	case CLASS_USE_HEADER:
	    includeTypeInClassLinkLabel = false;
	    break;

	case ANNOTATION:
	    excludeTypeParameterLinks = true;
	    excludeTypeBounds = true;
	    break;

	case IMPLEMENTED_INTERFACES:
	case SUPER_INTERFACES:
	case SUBINTERFACES:
	case CLASS_TREE_PARENT:
	case TREE:
	case CLASS_SIGNATURE_PARENT_NAME:
	    excludeTypeParameterLinks = true;
	    excludeTypeBounds = true;
	    includeTypeInClassLinkLabel = false;
	    includeTypeAsSepLink = true;
	    break;

	case PACKAGE:
	case CLASS_USE:
	case CLASS_HEADER:
	case CLASS_SIGNATURE:
	case RECEIVER_TYPE:
	    excludeTypeParameterLinks = true;
	    includeTypeAsSepLink = true;
	    includeTypeInClassLinkLabel = false;
	    break;

	case MEMBER_TYPE_PARAMS:
	    includeTypeAsSepLink = true;
	    includeTypeInClassLinkLabel = false;
	    break;

	case RETURN_TYPE:
	case SUMMARY_RETURN_TYPE:
	    excludeTypeBounds = true;
	    break;
	case EXECUTABLE_MEMBER_PARAM:
	    excludeTypeBounds = true;
	    break;
	}
	context = c;
	if (type != null && utils.isTypeVariable(type)
		&& utils.isExecutableElement(utils.asTypeElement(type).getEnclosingElement())) {
	    excludeTypeParameterLinks = true;
	}
    }

    /**
     * The location of the link.
     */
    public Kind context = Kind.DEFAULT;
    public final Utils utils;

}

