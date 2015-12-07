package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Document implements Serializable {

    private static final long serialVersionUID = 8599807108117037091L;
    private String documentName;
    private String ownerName;
    private String currentText;
    private List<String> editorNames;
    private RevisionHistory history;

    Document(String documentName, String ownerName) {
	this.documentName = documentName;
	this.ownerName = ownerName;
	editorNames = new ArrayList<String>();
	editorNames.add(ownerName);
	history = new RevisionHistory();
	currentText = "";
    }

    void replaceText(String newText) {
	currentText = newText;
    }

    void append(String textToAppend, String revisingUser) {
	currentText = currentText + textToAppend;
    }

    void saveRevision(String revisingUser) {
	history.add(new Revision(currentText, revisingUser));
    }

    void addEditor(String username) {
	editorNames.add(username);
    }

    String getText() {
	return currentText;
    }
    
    boolean hasNoRevisions() {
	return history.isEmpty();
    }
    
    String getLastRevisionText() {
	return history.getLastRevisionText();
    }
    
    String getLastRevisionKey() {
	return history.peekLastRevisionKey();
    }

    List<String> getTenRevisionKeys() {
	return history.getTenRevisionKeys();
    }

    String getDocumentName() {
	return documentName;
    }

    String getOwner() {
	return ownerName;
    }

    boolean isEditableBy(String username) {
	return editorNames.contains(username);
    }

    List<String> getEditors() {
	return editorNames;
    }

    void removeEditor(String username) {
	editorNames.remove(username);
    }
    
    void setTextToRevision(String revisionKey) {
	replaceText(history.getRevisionText(revisionKey));
    }
}
