package model;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RevisionHistory implements Serializable {

    private static final long serialVersionUID = -850895045810920159L;
    private static final int NUM_REVISIONS_STORED = 1000;
    private Map<String, String> revisionMap;
    private Deque<Revision> revisionDeque;
    private List<String> tenRevisions;

    public RevisionHistory() {
	revisionMap = new HashMap<String, String>();
	revisionDeque = new ArrayDeque<Revision>();
	tenRevisions = new ArrayList<String>();
    }

    public void add(Revision revision) {
	// replace 3 with 100 after testing
	if (revisionDeque.size() % 3 == 0) {
	    tenRevisions.add(0, revision.toString());
	    if (tenRevisions.size() == 11) {
		tenRevisions.remove(10);
	    }
	}
	if (revisionDeque.size() >= NUM_REVISIONS_STORED) {
	    revisionMap.remove(revisionDeque.removeLast().toString());
	}
	revisionDeque.addFirst(revision);
	revisionMap.put(revision.toString(), revision.getFullText());
    }

    public String peekLastRevisionText() {
	if (revisionDeque.size() != 0) {
	    return revisionDeque.peekFirst().getFullText();
	} else {
	    return "";
	}
    }

    public String getLastRevisionText() {
	if (revisionDeque.size() != 0) {
	    revisionMap.remove(revisionDeque.peekFirst().toString());
	    return revisionDeque.removeFirst().getFullText();
	} else {
	    return "";
	}
    }
    
    public String getRevisionText(String revisionKey) {
	String str = revisionMap.get(revisionKey);
	
	System.out.println("In revisionHistory: " + str); // debugging
	
	return str;
    }

    public List<String> getTenRevisionKeys() {
	return tenRevisions;
    }
}
