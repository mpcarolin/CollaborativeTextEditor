package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@SuppressWarnings("serial")
public class User implements Serializable {

   /**
    * store passwords as a hash create user, generate unique/random id cast
    * integer to string, concat to password at beginning hash it, store it, log
    * into server to server -> get id -> get account -> get password check to
    * make sure these are the same id+password hash stored 8 digits
    */
   private String username;
   private int salt;
   private int hashPass;
   private boolean loggedIn;
   private List<String> ownedDocuments;
   private List<String> editableDocuments;

   public User(String newUser, String pass) {
      this.username = newUser;
      setPassword(pass);
   }

   public void setPassword(String pass) {
      Random rng = new Random();
      salt = rng.nextInt(90000000);
      hashPass = (salt + pass).hashCode();
      ownedDocuments = Collections.synchronizedList(new ArrayList<String>());
      editableDocuments = Collections.synchronizedList(new ArrayList<String>());
   }

   public String getName() {
      return username;
   }

   public int getSalt() {
      return salt;
   }

   public int getHashPass() {
      return hashPass;
   }

   public void setLogin(boolean status) {
      loggedIn = status;
   }

   public boolean isLoggedIn() {
      return loggedIn;
   }
   
   public boolean owns(String documentName) {
      return ownedDocuments.contains(documentName);
   }

   public boolean hasPermission(String documentName) {
      return ownedDocuments.contains(documentName) || editableDocuments.contains(documentName);
   }
   
   public void addOwnedDocument(String documentName) {
      ownedDocuments.add(documentName);
      editableDocuments.add(documentName);
   }
   
   public void addEditableDocument(String documentName) {
      editableDocuments.add(documentName);
   }
   
   public void removeDocument(String documentName) {
      ownedDocuments.remove(documentName);
      editableDocuments.remove(documentName);
   }
   
   public List<String> getOwnedDocuments() {
      List<String> copy = new ArrayList<String>();
      copy.addAll(ownedDocuments);
      return copy;
   }
   
   public List<String> getEditableDocuments() {
      List<String> copy = new ArrayList<String>();
      copy.addAll(editableDocuments);
      return copy;
   }
}
