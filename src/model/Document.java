package model;

import java.util.LinkedList;
import java.util.Stack;

public class Document {
	
	private static final int NUM_REVISIONS_STORED = 20;
	private Stack<Revision> history; 
	private String currentText;
	private String priorText;
	private String documentName;
	private String ownerName;
	private LinkedList<String> editorNames;

	
	public Document(String documentName, String ownerName) {
		this.documentName = documentName;
		this.ownerName = ownerName;
		editorNames.add(ownerName);
		history = new Stack<Revision>();
		currentText = "";
	}
	
	public void replaceText(String newText, String revisingUser) {
		currentText = newText;
	}
	
	// appends a string to the end of the doc; no spaces are inserted
	public void append(String textToAppend, String revisingUser) {
		currentText = currentText + textToAppend; 
	}
	
	// creates a new revision object with revising user and the current saved text 
	public void saveRevision(String revisingUser) {
		// resize history if it exceeds constant 
		// TODO: probably ditch this b/c we need to construct full text using revisions
		if (history.size() > NUM_REVISIONS_STORED) {
			history.remove(0);
		}
		Revision revision = new Revision(currentText, peekLastRevision().getFullText(), revisingUser);
		history.push(revision);
	}
	
	private Revision peekLastRevision() {
		return history.peek();
	}

	public void addEditor(String editorUsername) {
		editorNames.add(editorUsername);
	}
	
	public String getText() {
		return currentText;
	}
	
	public Revision getLastRevision() {
		return history.pop();
	}
	
	public String getDocumentName() {
		return documentName;
	}
	
	@Deprecated
	// pushes to history, but resizes the stack if its length surpasses the limit
	private void addToHistory(String newText, String revisingUser, int caretLocation) {

		if (history.size() >= NUM_REVISIONS_STORED) {
			history.remove(0);
		}

		Revision revision = new Revision(newText, currentText, revisingUser); 
		history.push(revision);
	}
	
	public String getOwner() {
		return ownerName;
	}
	
	public LinkedList<String> getEditors() {
		return editorNames;
	}
}
