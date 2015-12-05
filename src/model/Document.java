package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class Document implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5200300073687331464L;
    private static final int NUM_REVISIONS_STORED = 1000;
    // private Stack<Revision> history;
    private String currentText;
    private String documentName;
    private String ownerName;
    private List<String> editorNames;
    private ArrayList<Revision> history; // maintained like a stack

    public Document(String documentName, String ownerName) {
	this.documentName = documentName;
	this.ownerName = ownerName;
	editorNames = Collections.synchronizedList(new LinkedList<String>());
	editorNames.add(ownerName);
	// history = new Stack<Revision>();
	history = new ArrayList<Revision>();
	currentText = "";
    }

    public void replaceText(String newText, String revisingUser) {
	currentText = newText;
    }

    // appends a string to the end of the doc; no spaces are inserted
    public void append(String textToAppend, String revisingUser) {
	currentText = currentText + textToAppend;
    }

    // creates a new revision object with revising user and the current saved
    // text
    public void saveRevision(String revisingUser) {
	// resize history if it exceeds constant
	if (history.size() >= NUM_REVISIONS_STORED) {
	    history.remove(0);
	}
	Revision revision = new Revision(currentText, peekLastRevision().getFullText(), revisingUser);
	history.add(revision);
    }

    public Revision peekLastRevision() {
	if (history.size() > 0) {
	    return history.get(history.size());
	} else {
	    return new Revision("", "", null);
	}
    }

    public void addEditor(String username) {
	editorNames.add(username);
    }

    public String getText() {
	return currentText;
    }

    public Revision getLastRevision() {
	if (history.size() > 0) {
	    Revision lastRevision = history.remove(history.size());
	    currentText = lastRevision.getFullText();
	    return lastRevision;
	}
	return null;
    }

    public List<Revision> getRevisions() {
	return history.subList(0, history.size() + 1);
    }

    public String getDocumentName() {
	return documentName;
    }

    public String getOwner() {
	return ownerName;
    }

    public boolean isEditableBy(String username) {
	return editorNames.contains(username);
    }

    public List<String> getEditors() {
	return editorNames;
    }

    public void removeEditor(String username) {
	editorNames.remove(username);
    }

    /*
     * Get's the nth + 1 revision for the client editor gui. Argument should be
     * between 0-9, referring to increments of 100 revisions stored.
     * 
     * Returns null if out of bounds
     */

    private Revision getRevisionForUser(int nthRevision) {
	if (nthRevision >= 0 && nthRevision < 10) {
	    return history.get((nthRevision) * 100);
	}

	return null;
    }

    /*
     * Returns UP to 10 revisions, separated in increments of 100, as a linked
     * list. If less than 1000 revisions are stored, it will return fewer.
     * 
     * Designated for the client editor gui that needs 10 revisions to view.
     */
    public LinkedList<Revision> getTenRevisionsForUser() {
	LinkedList<Revision> revisions = new LinkedList<Revision>();

	// find up to 10 revisions (separated by 100 revisions)
	for (int i = 0; i < 10; i++) {
	    Revision rev = getRevisionForUser(i);
	    if (rev == null) {
		return revisions;
	    }
	    revisions.add(rev);
	}

	return revisions;
    }

}
