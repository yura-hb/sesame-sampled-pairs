import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class RemovedImportCommentReassigner {
    /**
     * Assigns comments of removed import entries (those in {@code originalImports} but not in
     * {@code resultantImports}) to resultant import entries.
     * &lt;p&gt;
     * Returns a map containing the resulting assignments, where each key is an element of
     * {@code resultantImports} and each value is a collection of comments reassigned to that
     * resultant import.
     */
    Map&lt;ImportEntry, Collection&lt;ImportComment&gt;&gt; reassignComments(Collection&lt;ImportEntry&gt; resultantImports) {
	Map&lt;ImportEntry, Collection&lt;OriginalImportEntry&gt;&gt; importAssignments = assignRemovedImports(resultantImports);

	Map&lt;ImportEntry, Collection&lt;ImportComment&gt;&gt; commentAssignments = new HashMap&lt;ImportEntry, Collection&lt;ImportComment&gt;&gt;();

	for (Map.Entry&lt;ImportEntry, Collection&lt;OriginalImportEntry&gt;&gt; importAssignment : importAssignments.entrySet()) {
	    ImportEntry targetImport = importAssignment.getKey();
	    if (targetImport != null) {
		Deque&lt;ImportComment&gt; assignedComments = new ArrayDeque&lt;ImportComment&gt;();

		Collection&lt;OriginalImportEntry&gt; assignedImports = importAssignment.getValue();

		Iterator&lt;OriginalImportEntry&gt; nextAssignedImportIterator = assignedImports.iterator();
		if (nextAssignedImportIterator.hasNext()) {
		    nextAssignedImportIterator.next();
		}

		Iterator&lt;OriginalImportEntry&gt; assignedImportIterator = assignedImports.iterator();
		while (assignedImportIterator.hasNext()) {
		    OriginalImportEntry currentAssignedImport = assignedImportIterator.next();
		    OriginalImportEntry nextAssignedImport = nextAssignedImportIterator.hasNext()
			    ? nextAssignedImportIterator.next()
			    : null;

		    assignedComments.addAll(currentAssignedImport.comments);

		    if (nextAssignedImport != null && hasFloatingComment(nextAssignedImport)) {
			// Ensure that a blank line separates this removed import's comments
			// from the next removed import's floating comments.
			ImportComment lastComment = assignedComments.removeLast();
			ImportComment lastCommentWithTrailingBlankLine = new ImportComment(lastComment.region, 2);
			assignedComments.add(lastCommentWithTrailingBlankLine);
		    }
		}

		commentAssignments.put(targetImport, assignedComments);
	    }
	}

	return commentAssignments;
    }

    private final Collection&lt;OriginalImportEntry&gt; originalImportsWithComments;

    private Map&lt;ImportEntry, Collection&lt;OriginalImportEntry&gt;&gt; assignRemovedImports(Collection&lt;ImportEntry&gt; imports) {
	Collection&lt;OriginalImportEntry&gt; removedImportsWithComments = identifyRemovedImportsWithComments(imports);
	if (removedImportsWithComments.isEmpty()) {
	    return Collections.emptyMap();
	}

	Map&lt;ImportName, ImportEntry&gt; firstSingleForOnDemand = identifyFirstSingleForEachOnDemand(imports);
	Map&lt;ImportName, ImportEntry&gt; firstOccurrences = identifyFirstOccurrenceOfEachImportName(imports);

	Map&lt;ImportEntry, Collection&lt;OriginalImportEntry&gt;&gt; removedImportsForRetainedImport = new HashMap&lt;ImportEntry, Collection&lt;OriginalImportEntry&gt;&gt;();
	for (ImportEntry retainedImport : imports) {
	    removedImportsForRetainedImport.put(retainedImport, new ArrayList&lt;OriginalImportEntry&gt;());
	}
	// The null key will map to the removed imports not assigned to any import.
	removedImportsForRetainedImport.put(null, new ArrayList&lt;OriginalImportEntry&gt;());

	for (OriginalImportEntry removedImport : removedImportsWithComments) {
	    ImportName removedImportName = removedImport.importName;

	    final ImportEntry retainedImport;
	    if (removedImportName.isOnDemand()) {
		retainedImport = firstSingleForOnDemand.get(removedImportName);
	    } else {
		retainedImport = firstOccurrences.get(removedImportName.getContainerOnDemand());
	    }

	    // retainedImport will be null if there's no corresponding import to which to assign the removed import.
	    removedImportsForRetainedImport.get(retainedImport).add(removedImport);
	}

	return removedImportsForRetainedImport;
    }

    private static boolean hasFloatingComment(OriginalImportEntry nextAssignedImport) {
	for (ImportComment importComment : nextAssignedImport.comments) {
	    if (importComment.succeedingLineDelimiters &gt; 1) {
		return true;
	    }
	}

	return false;
    }

    private Collection&lt;OriginalImportEntry&gt; identifyRemovedImportsWithComments(Collection&lt;ImportEntry&gt; imports) {
	Collection&lt;OriginalImportEntry&gt; removedImports = new ArrayList&lt;OriginalImportEntry&gt;(
		this.originalImportsWithComments);
	removedImports.removeAll(imports);
	return removedImports;
    }

    /**
     * Assigns each removed on-demand import to the first single import in {@code imports} having
     * the same container name.
     * &lt;p&gt;
     * Returns a map where each key is a single import and each value is the corresponding
     * removed on-demand import.
     * &lt;p&gt;
     * The returned map only contains mappings to removed on-demand imports for which there are
     * corresponding single imports in {@code imports}.
     */
    private Map&lt;ImportName, ImportEntry&gt; identifyFirstSingleForEachOnDemand(Iterable&lt;ImportEntry&gt; imports) {
	Map&lt;ImportName, ImportEntry&gt; firstSingleImportForContainer = new HashMap&lt;ImportName, ImportEntry&gt;();
	for (ImportEntry currentImport : imports) {
	    if (!currentImport.importName.isOnDemand()) {
		ImportName containerOnDemand = currentImport.importName.getContainerOnDemand();
		if (!firstSingleImportForContainer.containsKey(containerOnDemand)) {
		    firstSingleImportForContainer.put(containerOnDemand, currentImport);
		}
	    }
	}
	return firstSingleImportForContainer;
    }

    private Map&lt;ImportName, ImportEntry&gt; identifyFirstOccurrenceOfEachImportName(Iterable&lt;ImportEntry&gt; imports) {
	Map&lt;ImportName, ImportEntry&gt; firstOccurrenceOfImport = new HashMap&lt;ImportName, ImportEntry&gt;();
	for (ImportEntry resultantImport : imports) {
	    if (!firstOccurrenceOfImport.containsKey(resultantImport.importName)) {
		firstOccurrenceOfImport.put(resultantImport.importName, resultantImport);
	    }
	}
	return firstOccurrenceOfImport;
    }

}

