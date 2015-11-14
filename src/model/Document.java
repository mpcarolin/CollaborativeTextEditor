package model;

import java.util.Stack;

public class Document {
	
	private static final int NUM_REVISIONS_STORED = 20;
	private Stack<Revision> history; 
	private String currentText;
	
	public Document() {
		history = new Stack<Revision>();
		currentText = "";
	}
	
	@Deprecated	// Dan: use the new method that specifies the revising user as an arg
	public void replaceText(String newText) {
		currentText = newText;
	}
	
	public void replaceText(String newText, String revisingUser) {
		addToHistory(newText, revisingUser);
		currentText = newText;
	}
	
	// appends a string to the end of the doc; no spaces are inserted
	public void append(String textToAppend, String revisingUser) {
		addToHistory(currentText + textToAppend, revisingUser);
		currentText = currentText + textToAppend; 
	}
	
	public String getText() {
		return currentText;
	}
	
	public Revision getLastRevision() {
		return history.pop();
	}
	
	// pushes to history, but resizes the stack if its length surpasses the limit
	private void addToHistory(String newText, String revisingUser) {

		if (history.size() >= NUM_REVISIONS_STORED) {
			history.remove(0);
		}

		Revision revision = new Revision(newText, currentText, revisingUser); 
		history.push(revision);
	}
}
