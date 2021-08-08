import org.eclipse.jdt.core.JavaCore;

class JavaProject extends Openable implements IJavaProject, IProjectNature, SuffixConstants {
    /**
     * Returns a default class path.
     * This is the root of the project
     */
    protected IClasspathEntry[] defaultClasspath() {

	return new IClasspathEntry[] { JavaCore.newSourceEntry(this.project.getFullPath()) };
    }

    /**
     * The platform project this &lt;code&gt;IJavaProject&lt;/code&gt; is based on
     */
    protected IProject project;

}

