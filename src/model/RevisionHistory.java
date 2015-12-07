/*
 * Revision history collection that stores revisions in a deque, so it can be
 * used as a queue and a stack. Also stores the revision text in a Map, so 
 * any requested revision can be accessed in constant time. 
 */
package model;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RevisionHistory implements Serializable {

    private static final long serialVersionUID = -8495186217239125636L;
    private static final int NUM_REVISIONS_STORED = 1000;
    private Map<String, String> revisionMap;
    private Deque<Revision> revisionDeque;
    private List<String> tenRevisions;

    RevisionHistory() {
	revisionMap = new HashMap<String, String>();
	revisionDeque = new ArrayDeque<Revision>();
	tenRevisions = new ArrayList<String>();
    }

    boolean isEmpty() {
	return revisionDeque.isEmpty();
    }

    void add(Revision revision) {
	// Every (3) revisions, add the newly added revision to the list of ten
	// revisions that the user will see. If there are already ten, remove
	// the oldest one
	// replace 3 with 100 after testing
	if (revisionDeque.size() % 3 == 0) {
	    if (tenRevisions.size() == 10) {
		tenRevisions.remove(9);
	    }
	    tenRevisions.add(0, revision.toString());
	}

	// If the revisionDeque is at maximum capacity, remove the oldest
	// revision
	if (revisionDeque.size() >= NUM_REVISIONS_STORED) {
	    revisionMap.remove(revisionDeque.removeLast().toString());
	}
	revisionDeque.addFirst(revision);
	revisionMap.put(revision.toString(), revision.getFullText());
    }

    String peekLastRevisionText() {
	if (revisionDeque.size() > 0) {
	    return revisionDeque.peekFirst().getFullText();
	} else {
	    return "";
	}
    }

    String peekLastRevisionKey() {
	if (revisionDeque.size() > 0) {
	    return revisionDeque.peekFirst().toString();
	} else {
	    return "";
	}
    }

    String getLastRevisionText() {
	if (revisionDeque.size() > 0) {
	    revisionMap.remove(revisionDeque.peekFirst().toString());
	    return revisionDeque.removeFirst().getFullText();
	} else {
	    return "";
	}
    }

    String getRevisionText(String revisionKey) {
	return revisionMap.get(revisionKey);
    }

    List<String> getTenRevisionKeys() {
	return tenRevisions;
    }
}
