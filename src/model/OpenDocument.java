package model;

import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OpenDocument {
   
   private Document document;
   private Set<ObjectOutputStream> editingUsers;
   
   public OpenDocument(Document document, ObjectOutputStream openingUser) {
      this.document = document;
      editingUsers = new HashSet<ObjectOutputStream>();
      addEditor(openingUser);
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
   
   public String revert(String documentKey) {
       return document.getRevisionText(documentKey);
   }
   
   public void removeEditor(ObjectOutputStream oldEditor) {
      editingUsers.remove(oldEditor);
   }
   
   public void removeClosedEditorStreams(Set<ObjectOutputStream> oldEditors) {
      editingUsers.removeAll(oldEditors);
   }
   
   public boolean noEditors() {
      return editingUsers.size() == 0;
   }
   
   public String getText() {
      return document.getText();
   }
   
   public void updateText(String text, String editor) {
      document.replaceText(text, editor);
   }
   
   public Set<ObjectOutputStream> getOutStreams() { 
      return editingUsers;
   }
   
   public Document getDocument() {
       return document;
   }
   
   public List<String> getRevisionList() {
       return document.getTenRevisionKeys();
   }
}
