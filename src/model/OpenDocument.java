package model;

import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OpenDocument {
   
   private Document document;
   private Set<ObjectOutputStream> editingUsers;
   private List<String> editingUserNames;
   private String currentEditorName;
   
   public OpenDocument(Document document, ObjectOutputStream openingUser) {
      this.document = document;
      editingUsers = new HashSet<ObjectOutputStream>();
      addEditor(openingUser);
   }
   
   public void setCurrentEditor(String editorName) {
       currentEditorName = editorName;
   }
   
   public String getCurrentEditor() {
       return currentEditorName;
   }
   
   public void addEditor(ObjectOutputStream newEditor) {
      editingUsers.add(newEditor);
   }
   
   public void saveRevision(String username) {
      document.saveRevision(username);
   }
   
   public String undo() {
      return document.getLastRevisionText();
   }
   
   public void revert(String documentKey) {
       document.setRevisionText(documentKey);
   }
   
   public void removeEditor(ObjectOutputStream oldEditor) {
      editingUsers.remove(oldEditor);
   }
   
   public void removeClosedEditorStreams(Set<ObjectOutputStream> oldEditors) {
      editingUsers.removeAll(oldEditors);
   }
   
   public boolean hasNoEditors() {
      return editingUsers.isEmpty();
   }
   
   public String getText() {
      return document.getText();
   }
   
   public void updateText(String text) {
      document.replaceText(text);
   }
   
   public Set<ObjectOutputStream> getOutStreams() { 
      return editingUsers;
   }
   
   public Document getDocument() {
       return document;
   }
   
   public String getDocumentName() {
       return document.getDocumentName();
   }
   
   public List<String> getRevisionList() {
       return document.getTenRevisionKeys();
   }
}
