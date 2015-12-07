package model;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RevisionHistory implements Serializable {

    private static final long serialVersionUID = 151000323660152929L;
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
	// replace 3 with 100 after testing
	if (revisionDeque.size() % 3 == 0) {
	    if (tenRevisions.size() == 10) {
		tenRevisions.remove(9);
	    }
	    tenRevisions.add(0, revision.toString());
	}
	if (revisionDeque.size() >= NUM_REVISIONS_STORED) {
	    revisionMap.remove(revisionDeque.removeLast().toString());
	}
	revisionDeque.addFirst(revision);
	revisionMap.put(revision.toString(), revision.getFullText());
    }

    String peekLastRevisionText() {
	if (revisionDeque.size() != 0) {
	    return revisionDeque.peekFirst().getFullText();
	} else {
	    return "";
	}
    }
    
    String peekLastRevisionKey() {
	if (revisionDeque.size() != 0) {
	    return revisionDeque.peekFirst().toString();
	} else {
	    return "";
	}
    }

    String getLastRevisionText() {
	if (revisionDeque.size() != 0) {
	    revisionMap.remove(revisionDeque.peekFirst().toString());
	    return revisionDeque.removeFirst().getFullText();
	} else {
	    return "";
	}
    }

    String getRevisionText(String revisionKey) {
	String documentText = revisionMap.get(revisionKey);
	return documentText;
    }

    List<String> getTenRevisionKeys() {
	return tenRevisions;
    }
}
