package model;

public class Edit {

	private Operation operation;
	private String insertedText;
	private int startIndex;
	private int endIndex;
	
	// constructor for insert op
	public Edit(int startIndex, String insertedText) {
		operation = Operation.INSERT;
		this.startIndex = startIndex;
		this.insertedText = insertedText;
	}

	// constructor for delete op
	public Edit(int startIndex, int endIndex) {
		operation = Operation.DELETE;
		this.startIndex = startIndex;
		this.endIndex = startIndex;
	}
	
	// constructor for update op
	public Edit(int startIndex, int endIndex, String newText) {
		operation = Operation.UPDATE;
		this.startIndex = startIndex;
		this.endIndex = startIndex;
		insertedText = newText;
	}
	
	public Operation getOperation() {
		return operation;
	}
	
	public int getStartIndex() {
		return startIndex;
	}
	
	public int getEndIndex() {
		return endIndex;
	}
	
	public String getText() {
		return insertedText;
	}
	
}
