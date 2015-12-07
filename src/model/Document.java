package model;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Document implements Serializable {

    private static final long serialVersionUID = -8233181825529016137L;
    private String currentText;
    private String documentName;
    private String ownerName;
    private List<String> editorNames;
    private RevisionHistory history;

    public Document(String documentName, String ownerName) {
	this.documentName = documentName;
	this.ownerName = ownerName;
	editorNames = Collections.synchronizedList(new LinkedList<String>());
	editorNames.add(ownerName);
	history = new RevisionHistory();
	currentText = "";
    }

    public void replaceText(String newText) {
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
	history.add(new Revision(currentText, history.peekLastRevisionText(), revisingUser));
    }

    public void addEditor(String username) {
	editorNames.add(username);
    }

    public String getText() {
	return currentText;
    }
    
    public boolean hasNoRevisions() {
	return history.isEmpty();
    }
    
    public String getLastRevisionText() {
	return history.getLastRevisionText();
    }
    
    public String getLastRevisionKey() {
	return history.peekLastRevisionKey();
    }

    public List<String> getTenRevisionKeys() {
	return history.getTenRevisionKeys();
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
    
    public void setRevisionText(String revisionKey) {
	replaceText(history.getRevisionText(revisionKey));
    }
}
