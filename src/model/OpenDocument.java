package model;

import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OpenDocument {
   
   private Document document;
   private List<ObjectOutputStream> editingUsers;
   
   public OpenDocument(Document document, ObjectOutputStream openingUser) {
      this.document = document;
      editingUsers = new ArrayList<ObjectOutputStream>();
      addEditor(openingUser);
   }
   
   public void addEditor(ObjectOutputStream newEditor) {
      editingUsers.add(newEditor);
   }
   
   public void saveRevision(String username) {
      document.saveRevision(username);
   }
   
   public String revert() {
      return document.getLastRevision().getFullText();
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
   
   public List<ObjectOutputStream> getOutStreams() { 
      return editingUsers;
   }
}
