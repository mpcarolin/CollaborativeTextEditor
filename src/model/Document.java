package model;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class Document {
   
   private static final int NUM_REVISIONS_STORED = 20;
   private Stack<Revision> history; 
   private String currentText;
   private String documentName;
   private String ownerName;
   private List<String> editorNames;

   
   public Document(String documentName, String ownerName) {
      this.documentName = documentName;
      this.ownerName = ownerName;
      editorNames = new LinkedList<String>();
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
   
   public Revision peekLastRevision() { 
	   if (history.size() > 0) {
		  return history.peek(); 
	   } else {
		  return new Revision("", "", null);
	   }
   }

   public void addEditor(String username) {
      editorNames.add(username);
   }
   
   public String getText() {
      return currentText;
   }
   
   public Revision getLastRevision() {

	  if (history.size() > 0) { 
		  Revision lastRevision = history.pop();
		  currentText = lastRevision.getFullText();
		  return lastRevision;
	  }

	  return null;
   }
   
   public String getDocumentName() {
      return documentName;
   }
      
   public String getOwner() {
      return ownerName;
   }
   
   public List<String> getEditors() {
      return editorNames;
   }
   
   public void removeEditor(String username) {
      editorNames.remove(username);
   }
}
