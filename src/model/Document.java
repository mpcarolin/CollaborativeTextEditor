package model;

import java.util.Stack;

public class Document {
	
	private static final int NUM_REVISIONS_STORED = 20;
	private Stack<String> history = new Stack<String>();
	
	private String currentText;
	
	public Document() {
		currentText = "";
	}
	
	public void replaceText(String newText) {
		currentText = newText;
		addToHistory(currentText);
	}
	
	public void append(String textToAppend) {
		currentText = currentText + textToAppend; 
		addToHistory(currentText);
	}
	
	public String getText() {
		return currentText;
	}
	
	public void getLastRevision() {
		currentText = history.pop();
	}
	
	// pushes to history, but manages the stack if length is beyond the 
	// num_revisions_stored limit
	private void addToHistory(String lastRevision) {
		if (history.size() >= NUM_REVISIONS_STORED) {
			history.remove(0);
		}
		
		history.push(lastRevision);
	}
	

	
	
	
	

}
