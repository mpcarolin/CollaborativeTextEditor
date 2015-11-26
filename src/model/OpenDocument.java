package model;

import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class OpenDocument {
   
   private Document document;
   private List<ObjectOutputStream> editingUsers = Collections.synchronizedList(new ArrayList<ObjectOutputStream>());
   
   public OpenDocument(Document document, ObjectOutputStream openingUser) {
      this.document = document;
      addEditor(openingUser);
   }
   
   public void addEditor(ObjectOutputStream newEditor) {
      editingUsers.add(newEditor);
   }
   
   public void saveRevision() {
      // 
   }
   
   public void removeClosedEditorStreams(Set<ObjectOutputStream> oldEditors) {
      editingUsers.removeAll(oldEditors);
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
