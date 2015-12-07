package model;

import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OpenDocument {
   
   private Document document;
   private Set<ObjectOutputStream> editorStreams;
   private Set<String> editorNames;
   private String currentEditorName;
   
   public OpenDocument(Document document, ObjectOutputStream editorStream, String editorName) {
      this.document = document;
      editorStreams = new HashSet<ObjectOutputStream>();
      editorNames = new HashSet<String>();
      addEditor(editorStream, editorName);
   }
   
   public void setCurrentEditor(String editorName) {
       currentEditorName = editorName;
   }
   
   public String getCurrentEditor() {
       return currentEditorName;
   }
   
   public void addEditor(ObjectOutputStream editorStream, String editorName) {
      editorStreams.add(editorStream);
      editorNames.add(editorName);
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
   
   public void removeEditor(ObjectOutputStream editorStream, String editorName) {
      editorStreams.remove(editorStream);
      editorNames.remove(editorName);
   }
   
   public void removeClosedEditorStreams(Set<ObjectOutputStream> droppedEditors) {
      editorStreams.removeAll(droppedEditors);
   }
   
   public boolean hasNoEditors() {
      return editorStreams.isEmpty();
   }
   
   public String getText() {
      return document.getText();
   }
   
   public void updateText(String text) {
      document.replaceText(text);
   }
   
   public Set<ObjectOutputStream> getEditorOutStreams() { 
      return editorStreams;
   }
   
   public Set<String> getEditorNames() {
       return editorNames;
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
