package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class Revision implements Serializable {

    private static final long serialVersionUID = 1296452894632152274L;
    private String newText;
    private String revisingUser;
    private LocalTime revisingTime;
    private LocalDate revisingDate;

    public Revision(String newText, String oldText, String revisingUser) {
	this.newText = newText;
	this.revisingUser = revisingUser;
	revisingTime = LocalTime.now();
	revisingDate = LocalDate.now();
    }

    /*
     * Getter methods
     */
    public String getFullText() {
	return newText;
    }

    public LocalTime getEditTime() {
	return revisingTime;
    }

    public LocalDate getEditDate() {
	return revisingDate;
    }

    public String toString() {
	return getEditTime().getHour() + ":" + getEditTime().getMinute() + ":" + getEditTime().getSecond() + " on "
		+ getEditDate() + " by " + revisingUser;
    }
}
