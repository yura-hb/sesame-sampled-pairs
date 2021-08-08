import java.io.*;
import java.util.*;

class CheckpointListener extends BaseTrainingListener implements Serializable {
    /**
     * Return the most recent checkpoint, if one exists - otherwise returns null
     * @return Checkpoint
     */
    public Checkpoint lastCheckpoint() {
	List&lt;Checkpoint&gt; all = availableCheckpoints();
	if (all.isEmpty()) {
	    return null;
	}
	return all.get(all.size() - 1);
    }

    private File checkpointRecordFile;
    private File rootDir;

    /**
     * List all available checkpoints. A checkpoint is 'available' if the file can be loaded. Any checkpoint files that
     * have been automatically deleted (given the configuration) will not be returned here.
     *
     * @return List of checkpoint files that can be loaded
     */
    public List&lt;Checkpoint&gt; availableCheckpoints() {
	if (!checkpointRecordFile.exists()) {
	    return Collections.emptyList();
	}
	List&lt;String&gt; lines;
	try (InputStream is = new BufferedInputStream(new FileInputStream(checkpointRecordFile))) {
	    lines = IOUtils.readLines(is);
	} catch (IOException e) {
	    throw new RuntimeException(
		    "Error loading checkpoint data from file: " + checkpointRecordFile.getAbsolutePath(), e);
	}

	List&lt;Checkpoint&gt; out = new ArrayList&lt;&gt;(lines.size() - 1); //Assume first line is header
	for (int i = 1; i &lt; lines.size(); i++) {
	    Checkpoint c = Checkpoint.fromFileString(lines.get(i));
	    if (new File(rootDir, c.getFilename()).exists()) {
		out.add(c);
	    }
	}
	return out;
    }

}

