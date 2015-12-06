package model;

public class Edit {

    private Operation operation;
    private String insertedText;
    private int startIndex;
    private int endIndex;

    /*
     * Constructors: one for each type of operation
     */

    // constructor for insert operations
    public Edit(int startIndex, String insertedText) {
	operation = Operation.INSERT;
	this.startIndex = startIndex;
	this.insertedText = insertedText;
    }

    // constructor for delete operations
    public Edit(int startIndex, int endIndex) {
	operation = Operation.DELETE;
	this.startIndex = startIndex;
	this.endIndex = startIndex;
    }

    // constructor for update operations
    public Edit(int startIndex, int endIndex, String newText) {
	operation = Operation.UPDATE;
	this.startIndex = startIndex;
	this.endIndex = startIndex;
	insertedText = newText;
    }

    /*
     * Operational Transformations -- maintains consistency if an edit index
     * occurs earlier in text
     */

    // shifts indices to the left due to an earlier delete or update
    public void shiftLeft(int amount) {
	startIndex -= amount;
	if (operation != Operation.INSERT) {
	    endIndex -= amount;
	}
    }

    // shifts indices to the right due to an earlier insert or update
    public void shiftRight(int amount) {
	startIndex += amount;
	if (operation != Operation.INSERT) {
	    endIndex += amount;
	}
    }

    /*
     * Getters
     */

    // returns one of three enums: INSERT, DELETE, or UPDATE
    public Operation getOperation() {
	return operation;
    }

    public int getStartIndex() {
	return startIndex;
    }

    // returns -1 if no end index exists (true only for insert operations)
    public int getEndIndex() {
	return operation == Operation.INSERT ? -1 : endIndex;
    }

    // returns null if no text exists (only occurs with deletions)
    public String getText() {
	return operation == Operation.DELETE ? null : insertedText;
    }

}
