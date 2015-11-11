package model;

import java.util.Stack;

public class Document {
	
	private static final int NUM_REVISIONS_STORED = 20;
	private Stack<Document> history = new Stack<Document>();
	
	private String currentText;
	
	public Document() {
		currentText = "";
	}
	
	public void replaceText(String newText) {
		currentText = newText;
	}
	
	public void append(String textToAppend) {
		currentText = currentText + textToAppend; 
	}
	
	public String getText() {
		return currentText;
	}
	
	// pushes to history, but manages the stack if length is beyond the 
	// num_revisions_stored limit
	private void addToHistory(Document lastRevision) {
		if (history.size() >= NUM_REVISIONS_STORED) {
			history.
		}
	}
	
	
	
	

}
