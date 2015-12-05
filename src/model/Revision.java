package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;

import model.diff_match_patch.Diff;

public class Revision implements Serializable {

    private static final long serialVersionUID = 1389885153513784014L;
    private String newText;
    private String revisingUser;
    private LocalTime revisingTime;
    private LocalDate revisingDate;
    private List<String> revisingActions;

    public Revision(String newText, String oldText, String revisingUser) {
	this.newText = newText;
	this.revisingUser = revisingUser;
	revisingTime = LocalTime.now();
	revisingDate = LocalDate.now();
	revisingActions = new LinkedList<String>();

	// gather the differences between newText and the oldText
	diff_match_patch differences = new diff_match_patch();
	LinkedList<Diff> modifications = differences.diff_main(oldText, newText);
	differences.diff_cleanupSemantic(modifications); // ignore trivial
							 // differences

	// add each modification to the list of revising actions
	for (Diff diff : modifications) {
	    switch (diff.operation) {
	    case EQUAL:
		break; // ignore equalities
	    case INSERT:
		// TODO: need to parse the insert to see if it inserts a html
		// formatting tag, then categorize it as a formatting
		// modification
		String insert = "inserted the text: " + "\"" + diff.text + "\"";
		revisingActions.add(insert);
		break;
	    case DELETE:
		// TODO: need to parse the deletion to see if it deletes a html
		// formatting tag, then categorize it as a formatting
		// modification
		String deletion = "deleted the text: " + "\"" + diff.text + "\"";
		revisingActions.add(deletion);
		break;
	    }
	}
    }

    /*
     * Getter methods
     */
    public List<String> getRevisingActions() {
	return revisingActions;
    }

    public String getFullText() {
	return newText;
    }

    public LocalTime getEditTime() {
	return revisingTime;
    }

    public LocalDate getEditDate() {
	return revisingDate;
    }

    public String getRevisingUsername() {
	return revisingUser;
    }

    public String toString() {
	return getEditTime().getHour() % 12 + ":" + getEditTime().getMinute() + " on " + getEditDate() + "by "
		+ revisingUser;
    }

    public String toStringChanges() {
	String toPrint = "At " + getEditTime().getHour() % 12 + ":" + getEditTime().getMinute() + " on " + getEditDate()
		+ ", " + revisingUser + " made the following edits:\n";

	for (String action : this.getRevisingActions()) {
	    toPrint += action + "\n";
	}

	return toPrint;
    }

    // testing
    public static void main(String[] args) {
	Revision rev = new Revision("Hello world, planet earth", "ello wurld original whoooo", "James");
	System.out.println(rev);
    }

}
