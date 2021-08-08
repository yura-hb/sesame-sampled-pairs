import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.builder.JavaBuilder;
import org.eclipse.jdt.internal.core.util.Messages;

class JavaModelManager implements ISaveParticipant, IContentTypeChangeListener {
    /**
     * Reads the build state for the relevant project.
     */
    protected Object readState(IProject project) throws CoreException {
	File file = getSerializationFile(project);
	if (file != null && file.exists()) {
	    try {
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
		try {
		    String pluginID = in.readUTF();
		    if (!pluginID.equals(JavaCore.PLUGIN_ID))
			throw new IOException(Messages.build_wrongFileFormat);
		    String kind = in.readUTF();
		    if (!kind.equals("STATE")) //$NON-NLS-1$
			throw new IOException(Messages.build_wrongFileFormat);
		    if (in.readBoolean())
			return JavaBuilder.readState(project, in);
		    if (JavaBuilder.DEBUG)
			System.out.println("Saved state thinks last build failed for " + project.getName()); //$NON-NLS-1$
		} finally {
		    in.close();
		}
	    } catch (Exception e) {
		e.printStackTrace();
		throw new CoreException(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, Platform.PLUGIN_ERROR,
			"Error reading last build state for project " + project.getName(), e)); //$NON-NLS-1$
	    }
	} else if (JavaBuilder.DEBUG) {
	    if (file == null)
		System.out.println("Project does not exist: " + project); //$NON-NLS-1$
	    else
		System.out.println("Build state file " + file.getPath() + " does not exist"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	return null;
    }

    /**
     * Returns the File to use for saving and restoring the last built state for the given project.
     */
    private File getSerializationFile(IProject project) {
	if (!project.exists())
	    return null;
	IPath workingLocation = project.getWorkingLocation(JavaCore.PLUGIN_ID);
	return workingLocation.append("state.dat").toFile(); //$NON-NLS-1$
    }

}

