import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.util.Messages;

class JavaModelStatus extends Status implements IJavaModelStatus, IJavaModelStatusConstants {
    /**
     * Returns the message that is relevant to the code of this status.
     */
    @Override
    public String getMessage() {
	Throwable exception = getException();
	if (exception == null) {
	    switch (getCode()) {
	    case CORE_EXCEPTION:
		return Messages.status_coreException;

	    case BUILDER_INITIALIZATION_ERROR:
		return Messages.build_initializationError;

	    case BUILDER_SERIALIZATION_ERROR:
		return Messages.build_serializationError;

	    case DEVICE_PATH:
		return Messages.bind(Messages.status_cannotUseDeviceOnPath, getPath().toString());

	    case DOM_EXCEPTION:
		return Messages.status_JDOMError;

	    case ELEMENT_DOES_NOT_EXIST:
		return Messages.bind(Messages.element_doesNotExist,
			((JavaElement) this.elements[0]).toStringWithAncestors());

	    case ELEMENT_NOT_ON_CLASSPATH:
		return Messages.bind(Messages.element_notOnClasspath,
			((JavaElement) this.elements[0]).toStringWithAncestors());

	    case EVALUATION_ERROR:
		return Messages.bind(Messages.status_evaluationError, this.string);

	    case INDEX_OUT_OF_BOUNDS:
		return Messages.status_indexOutOfBounds;

	    case INVALID_CONTENTS:
		return Messages.status_invalidContents;

	    case INVALID_DESTINATION:
		return Messages.bind(Messages.status_invalidDestination,
			((JavaElement) this.elements[0]).toStringWithAncestors());

	    case INVALID_ELEMENT_TYPES:
		StringBuffer buff = new StringBuffer(Messages.operation_notSupported);
		for (int i = 0; i &lt; this.elements.length; i++) {
		    if (i &gt; 0) {
			buff.append(", "); //$NON-NLS-1$
		    }
		    buff.append(((JavaElement) this.elements[i]).toStringWithAncestors());
		}
		return buff.toString();

	    case INVALID_NAME:
		return Messages.bind(Messages.status_invalidName, this.string);

	    case INVALID_PACKAGE:
		return Messages.bind(Messages.status_invalidPackage, this.string);

	    case INVALID_PATH:
		if (this.string != null) {
		    return this.string;
		} else {
		    return Messages.bind(Messages.status_invalidPath,
			    new String[] { getPath() == null ? "null" : getPath().toString() } //$NON-NLS-1$
		    );
		}

	    case INVALID_PROJECT:
		return Messages.bind(Messages.status_invalidProject, this.string);

	    case INVALID_RESOURCE:
		return Messages.bind(Messages.status_invalidResource, this.string);

	    case INVALID_RESOURCE_TYPE:
		return Messages.bind(Messages.status_invalidResourceType, this.string);

	    case INVALID_SIBLING:
		if (this.string != null) {
		    return Messages.bind(Messages.status_invalidSibling, this.string);
		} else {
		    return Messages.bind(Messages.status_invalidSibling,
			    ((JavaElement) this.elements[0]).toStringWithAncestors());
		}

	    case IO_EXCEPTION:
		return Messages.status_IOException;

	    case NAME_COLLISION:
		if (this.elements != null && this.elements.length &gt; 0) {
		    IJavaElement element = this.elements[0];
		    if (element instanceof PackageFragment && ((PackageFragment) element).isDefaultPackage()) {
			return Messages.operation_cannotRenameDefaultPackage;
		    }
		}
		if (this.string != null) {
		    return this.string;
		} else {
		    return Messages.bind(Messages.status_nameCollision, ""); //$NON-NLS-1$
		}
	    case NO_ELEMENTS_TO_PROCESS:
		return Messages.operation_needElements;

	    case NULL_NAME:
		return Messages.operation_needName;

	    case NULL_PATH:
		return Messages.operation_needPath;

	    case NULL_STRING:
		return Messages.operation_needString;

	    case PATH_OUTSIDE_PROJECT:
		return Messages.bind(Messages.operation_pathOutsideProject,
			new String[] { this.string, ((JavaElement) this.elements[0]).toStringWithAncestors() });

	    case READ_ONLY:
		IJavaElement element = this.elements[0];
		String name = element.getElementName();
		if (element instanceof IPackageFragment && name.equals(IPackageFragment.DEFAULT_PACKAGE_NAME)) {
		    return Messages.status_defaultPackageReadOnly;
		}
		return Messages.bind(Messages.status_readOnly, name);

	    case RELATIVE_PATH:
		return Messages.bind(Messages.operation_needAbsolutePath, getPath().toString());

	    case TARGET_EXCEPTION:
		return Messages.status_targetException;

	    case UPDATE_CONFLICT:
		return Messages.status_updateConflict;

	    case NO_LOCAL_CONTENTS:
		return Messages.bind(Messages.status_noLocalContents, getPath().toString());

	    case CP_CONTAINER_PATH_UNBOUND:
		IJavaProject javaProject = (IJavaProject) this.elements[0];
		ClasspathContainerInitializer initializer = JavaCore
			.getClasspathContainerInitializer(this.path.segment(0));
		String description = null;
		if (initializer != null)
		    description = initializer.getDescription(this.path, javaProject);
		if (description == null)
		    description = this.path.makeRelative().toString();
		return Messages.bind(Messages.classpath_unboundContainerPath,
			new String[] { description, javaProject.getElementName() });

	    case INVALID_CP_CONTAINER_ENTRY:
		javaProject = (IJavaProject) this.elements[0];
		IClasspathContainer container = null;
		description = null;
		try {
		    container = JavaCore.getClasspathContainer(this.path, javaProject);
		} catch (JavaModelException e) {
		    // project doesn't exist: ignore
		}
		if (container == null) {
		    initializer = JavaCore.getClasspathContainerInitializer(this.path.segment(0));
		    if (initializer != null)
			description = initializer.getDescription(this.path, javaProject);
		} else {
		    description = container.getDescription();
		}
		if (description == null)
		    description = this.path.makeRelative().toString();
		return Messages.bind(Messages.classpath_invalidContainer,
			new String[] { description, javaProject.getElementName() });

	    case CP_VARIABLE_PATH_UNBOUND:
		javaProject = (IJavaProject) this.elements[0];
		return Messages.bind(Messages.classpath_unboundVariablePath,
			new String[] { this.path.makeRelative().toString(), javaProject.getElementName() });

	    case CLASSPATH_CYCLE:
		javaProject = (IJavaProject) this.elements[0];
		return Messages.bind(Messages.classpath_cycle,
			new String[] { javaProject.getElementName(), this.string });

	    case DISABLED_CP_EXCLUSION_PATTERNS:
		javaProject = (IJavaProject) this.elements[0];
		String projectName = javaProject.getElementName();
		IPath newPath = this.path;
		if (this.path.segment(0).toString().equals(projectName)) {
		    newPath = this.path.removeFirstSegments(1);
		}
		return Messages.bind(Messages.classpath_disabledInclusionExclusionPatterns,
			new String[] { newPath.makeRelative().toString(), projectName });

	    case DISABLED_CP_MULTIPLE_OUTPUT_LOCATIONS:
		javaProject = (IJavaProject) this.elements[0];
		projectName = javaProject.getElementName();
		newPath = this.path;
		if (this.path.segment(0).toString().equals(projectName)) {
		    newPath = this.path.removeFirstSegments(1);
		}
		return Messages.bind(Messages.classpath_disabledMultipleOutputLocations,
			new String[] { newPath.makeRelative().toString(), projectName });

	    case CANNOT_RETRIEVE_ATTACHED_JAVADOC:
		if (this.elements != null && this.elements.length == 1) {
		    if (this.string != null) {
			return Messages.bind(Messages.status_cannot_retrieve_attached_javadoc,
				((JavaElement) this.elements[0]).toStringWithAncestors(), this.string);
		    }
		    return Messages.bind(Messages.status_cannot_retrieve_attached_javadoc,
			    ((JavaElement) this.elements[0]).toStringWithAncestors(), ""); //$NON-NLS-1$
		}
		if (this.string != null) {
		    return Messages.bind(Messages.status_cannot_retrieve_attached_javadoc, this.string, "");//$NON-NLS-1$
		}
		break;

	    case CANNOT_RETRIEVE_ATTACHED_JAVADOC_TIMEOUT:
		if (this.elements != null && this.elements.length == 1) {
		    if (this.string != null) {
			return Messages.bind(Messages.status_timeout_javadoc,
				((JavaElement) this.elements[0]).toStringWithAncestors(), this.string);
		    }
		    return Messages.bind(Messages.status_timeout_javadoc,
			    ((JavaElement) this.elements[0]).toStringWithAncestors(), ""); //$NON-NLS-1$
		}
		if (this.string != null) {
		    return Messages.bind(Messages.status_timeout_javadoc, this.string, "");//$NON-NLS-1$
		}
		break;

	    case UNKNOWN_JAVADOC_FORMAT:
		return Messages.bind(Messages.status_unknown_javadoc_format,
			((JavaElement) this.elements[0]).toStringWithAncestors());

	    case DEPRECATED_VARIABLE:
		javaProject = (IJavaProject) this.elements[0];
		return Messages.bind(Messages.classpath_deprecated_variable,
			new String[] { this.path.segment(0).toString(), javaProject.getElementName(), this.string });
	    case TEST_SOURCE_REQUIRES_SEPARATE_OUTPUT_LOCATION:
		javaProject = (IJavaProject) this.elements[0];
		projectName = javaProject.getElementName();
		newPath = this.path;
		if (this.path.segment(0).toString().equals(projectName)) {
		    newPath = this.path.removeFirstSegments(1);
		}
		return Messages.bind(Messages.classpath_testSourceRequiresSeparateOutputFolder,
			new String[] { newPath.makeRelative().toString(), projectName });
	    case TEST_OUTPUT_FOLDER_MUST_BE_SEPARATE_FROM_MAIN_OUTPUT_FOLDERS:
		javaProject = (IJavaProject) this.elements[0];
		projectName = javaProject.getElementName();
		newPath = this.path;
		if (this.path.segment(0).toString().equals(projectName)) {
		    newPath = this.path.removeFirstSegments(1);
		}
		return Messages.bind(Messages.classpath_testOutputFolderMustBeSeparateFromMainOutputFolders,
			new String[] { newPath.makeRelative().toString(), projectName });
	    }
	    if (this.string != null) {
		return this.string;
	    } else {
		return ""; //$NON-NLS-1$
	    }
	} else {
	    String message = exception.getMessage();
	    if (message != null) {
		return message;
	    } else {
		return exception.toString();
	    }
	}
    }

    /**
     * The elements related to the failure, or &lt;code&gt;null&lt;/code&gt;
     * if no elements are involved.
     */
    protected IJavaElement[] elements = new IJavaElement[0];
    /**
     * The &lt;code&gt;String&lt;/code&gt; related to the failure, or &lt;code&gt;null&lt;/code&gt;
     * if no &lt;code&gt;String&lt;/code&gt; is involved.
     */
    protected String string;
    /**
     * The path related to the failure, or &lt;code&gt;null&lt;/code&gt;
     * if no path is involved.
     */
    protected IPath path;

    /**
     * @see IJavaModelStatus#getPath()
     */
    @Override
    public IPath getPath() {
	return this.path;
    }

}

