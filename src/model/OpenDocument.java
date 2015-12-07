package model;

import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OpenDocument {
   
   private Document document;
   private Set<ObjectOutputStream> editorStreams;
   private Set<String> editorNames;
   
   OpenDocument(Document document, ObjectOutputStream editorStream, String editorName) {
      this.document = document;
      editorStreams = new HashSet<ObjectOutputStream>();
      editorNames = new HashSet<String>();
      addEditor(editorStream, editorName);
   }
      
   void addEditor(ObjectOutputStream editorStream, String editorName) {
      editorStreams.add(editorStream);
      editorNames.add(editorName);
   }
   
   void saveRevision(String username) {
      document.saveRevision(username);
   }
   
   String undo() {
      return document.getLastRevisionText();
   }
   
   void revert(String documentKey) {
       document.setRevisionText(documentKey);
   }
   
   void removeEditor(ObjectOutputStream editorStream, String editorName) {
      editorStreams.remove(editorStream);
      editorNames.remove(editorName);
   }
   
   void removeClosedEditorStreams(Set<ObjectOutputStream> droppedEditors) {
      editorStreams.removeAll(droppedEditors);
   }
   
   boolean hasNoEditors() {
      return editorStreams.isEmpty();
   }
   
   String getText() {
      return document.getText();
   }
   
   void updateText(String text) {
      document.replaceText(text);
   }
   
   Set<ObjectOutputStream> getEditorOutStreams() { 
      return editorStreams;
   }
   
   Set<String> getEditorNames() {
       return editorNames;
   }
   
   Document getDocument() {
       return document;
   }
   
   String getDocumentName() {
       return document.getDocumentName();
   }
   
   List<String> getRevisionList() {
       return document.getTenRevisionKeys();
   }
}
