package model;

import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
   
   public void removeEditor(ObjectOutputStream oldEditor) {
      editingUsers.remove(oldEditor);
   }
   
   public String getText() {
      return document.getText();
   }
   
   public void updateText(String text, String editor) {
      document.replaceText(text, editor);
   }
}
