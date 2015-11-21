/*
 * Code that handles everything for the server (for now)
 */

package model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

public class Server {

   public static final int SERVER_PORT = 9001;

   static Map<String, User> allUsers = Collections.synchronizedMap(new HashMap<>());
   static Map<String, Document> allDocuments = Collections.synchronizedMap(new HashMap<>());
   static Map<String, OpenDocument> openDocuments = Collections.synchronizedMap(new HashMap<>());
   static List<ObjectOutputStream> clientOutStreams = Collections.synchronizedList(new ArrayList<>());

   public static void main(String[] args) throws IOException {
      
      JOptionPane.showMessageDialog(null, "Complete team breakdown.", "ERROR",
            JOptionPane.ERROR_MESSAGE);
      
      hardCodeUsers();
      try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
         System.out.println("Server started on port " + SERVER_PORT);
         while (true) {
            new ClientHandler(serverSocket.accept()).start();
         }
      }
   }

   /*
    * Hard code some users for testing purposes
    */
   private static void hardCodeUsers() {
      allUsers.put("Daniel", new User("Daniel", "Avetian"));
      allUsers.put("Michael", new User("Michael", "Carolin"));
      allUsers.put("Jacob", new User("Jacob", "Groh"));
      allUsers.put("Filbert", new User("Filbert", "Johnson"));
      allUsers.put("Orzy", new User("Orzy", "Hazan"));
   }
}

/*
 * Client handler that is created and started each time a new connection is
 * created Handles everything for one client (for now)
 */
class ClientHandler extends Thread {

   private Socket clientSocket;
   private ObjectInputStream clientIn; // clientIn stream of the new client
   private ObjectOutputStream clientOut; // output stream of the new client 
   private User currentUser; // current user logged in
   private OpenDocument currentOpenDoc; // current document being edited by the
   private boolean isRunning, removeStreams; // booleans...

   public ClientHandler(Socket clientSocket) {
      this.clientSocket = clientSocket;
      try {
         clientIn = new ObjectInputStream(clientSocket.getInputStream());
         clientOut = new ObjectOutputStream(clientSocket.getOutputStream());
         isRunning = true;
         System.out.println("Accepted a new connection from " + clientSocket.getInetAddress());
      } catch (IOException e) {
         e.printStackTrace();
         closeConnection();
      }
   }

   @Override
   public void run() {
      // While the thread is still isRunning, get the next ClientRequest from
      // the client, and call the respective method
      ClientRequest command;
      while (isRunning) {
         try {
            command = (ClientRequest) clientIn.readObject();
            switch (command) {
            case LOGIN:
               if (authenticateUser()) {
                  JOptionPane.showMessageDialog(null, "User " + currentUser.getName() + " successfully logged in.");
               } else {
                  JOptionPane.showMessageDialog(null, "Log in failed.");
               }
               break;
            case CREATE_ACCOUNT:
               if (createAccount()) {
                  JOptionPane.showMessageDialog(null,
                        "User account " + currentUser.getName() + " created successfully.");
               } else {
                  JOptionPane.showMessageDialog(null, "Account created failed");
               }
               break;
            case CHANGE_PASSWORD:
               changePassword();
               break;
            case GET_DOCS:
               sendDocumentList();
            case CHAT_MSG:
               updateChat();
               break;
            case CREATE_DOC:
               createDocument();
               break;
            case OPEN_DOC:
               openDocument();
               break;
            case DOC_TEXT:
               updateDocument();
               break;
            case CLOSE_DOC:
               closeDocument();
               break;
            case LOGOUT:
               logout();
               break;
            default:
               System.out.println("Catastrophic Failure.");
               System.out.flush();
               System.exit(1);
               break;
            }
         } catch (ClassNotFoundException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
            Server.clientOutStreams.remove(clientOut);
            isRunning = false;
         }
      }
      logout();
   }
   
   /*
    * Gets the username and password from the client, and creates a new
    * userAccount Verifies that the username is unique Returns a boolean
    * indicating whether the account was successfully created or not
    */
   private boolean createAccount() throws ClassNotFoundException, IOException {
      String username = (String) clientIn.readObject();
      String password = (String) clientIn.readObject();
      if (Server.allUsers.get(username) != null) {
         clientOut.writeObject(ServerResponse.ACCOUNT_EXISTS);
      } else {
         User newUser = new User(username, password);
         Server.allUsers.put(username, newUser);
         newUser.setLogin(true);
         currentUser = newUser;
         Server.clientOutStreams.add(clientOut);
         clientOut.writeObject(ServerResponse.ACCOUNT_CREATED);
         return true;
      }
      return false;
   }

   
   /*
    * Gets the credentials from the client and checks if the match a user
    * account Verifies that the user is not already logged in
    */
   private boolean authenticateUser() throws ClassNotFoundException, IOException {
      User user = null;
      String username = (String) clientIn.readObject();
      String password = (String) clientIn.readObject();

      if ((user = Server.allUsers.get(username)) == null) {
         clientOut.writeObject(ServerResponse.INCORRECT_USERNAME);
      } else if (user.isLoggedIn()) {
         clientOut.writeObject(ServerResponse.LOGGED_IN);
      } else if ((user.getSalt() + password).hashCode() != user.getHashPass()) {
         clientOut.writeObject(ServerResponse.INCORRECT_PASSWORD);
      } else {
         user.setLogin(true);
         currentUser = user;
         Server.clientOutStreams.add(clientOut);
         clientOut.writeObject(ServerResponse.LOGIN_SUCCESS);
         return true;
      }
      return false;
   }
   
   private void changePassword() throws ClassNotFoundException, IOException {
      String username = (String) clientIn.readObject();
      String newPassword = (String) clientIn.readObject();
      User user = Server.allUsers.get(username);
      if (user == null) {
         clientOut.writeObject(ServerResponse.INCORRECT_USERNAME);
      } else {
         user.setPassword(newPassword);
         clientOut.writeObject(ServerResponse.PASSWORD_CHANGED); 
      }
   }

   private void sendDocumentList() throws IOException {
      clientOut.writeObject(currentUser.getOwnedDocuments());
      clientOut.writeObject(currentUser.getEditableDocuments());
   }

   private boolean createDocument() throws ClassNotFoundException, IOException {
      String docName = (String) clientIn.readObject();
      if (Server.allDocuments.get(docName) != null) {
         clientOut.writeObject(ServerResponse.DOCUMENT_EXISTS);
         return false;
      }
      Document newDocument = new Document(docName, currentUser.getName());
      Server.allDocuments.put(docName, newDocument);
      currentOpenDoc = new OpenDocument(newDocument, clientOut);
      Server.openDocuments.put(docName, currentOpenDoc);
      clientOut.writeObject(ServerResponse.DOCUMENT_CREATED);
      return true;
   }

   /*
    * These are the methods that will be used when documents are created,
    * opened, and closed, rather than the current "one" document being used for
    * testing.
    */

   private void openDocument() throws ClassNotFoundException, IOException {
      String docName = (String) clientIn.readObject();
      Document openingDoc = Server.allDocuments.get(docName);
      if (openingDoc == null) {
         clientOut.writeObject(ServerResponse.NO_DOCUMENT);
      } else if (!currentUser.hasPermission(docName)) {
         clientOut.writeObject(ServerResponse.PERMISSION_DENIED);
      } else {
         currentOpenDoc = Server.openDocuments.get(docName);
         currentOpenDoc = (currentOpenDoc == null) ? new OpenDocument(openingDoc, clientOut) : currentOpenDoc;
      }
   }
   
   /*
    * Reads in the new chat message from the client, and sends it to all of the
    * clientOutStreams in the client list Keeps track of the clientOutStreams
    * that have disconnected and removes them
    */
   private void updateChat() throws ClassNotFoundException, IOException {
      String chatMessage = (String) clientIn.readObject();
      sendUpdateToClients(ServerResponse.CHAT_UPDATE, chatMessage);
   }

   private void updateDocument() throws ClassNotFoundException, IOException {
      currentOpenDoc.updateText((String) clientIn.readObject(), currentUser.getName());
      sendUpdateToClients(ServerResponse.DOCUMENT_UPDATE, currentOpenDoc.getText());
   }

   public void sendUpdateToClients(ServerResponse response, String text) {
      removeStreams = false;
      Set<ObjectOutputStream> closedEditors = new HashSet<ObjectOutputStream>();
      for (ObjectOutputStream editorOutStream : currentOpenDoc.getOutStreams()) {
         if (response == ServerResponse.DOCUMENT_UPDATE && editorOutStream == clientOut) {
            continue;
            // maybe this can be removed now?
         }
         try {
            editorOutStream.reset();
            editorOutStream.writeObject(response);
            editorOutStream.writeObject(text);
         } catch (IOException e) {
            removeStreams = true;
            closedEditors.add(editorOutStream);
         }
      }
      if (removeStreams) {
         currentOpenDoc.removeClosedEditorStreams(closedEditors);
         removeStreams = false;
      }
   }

   private void closeDocument() {

   }

   /*
    * Sends the current document to all of the clientOutStreams in the
    * clientOutStreams list Keeps track of the clientOutStreams that have
    * disconnected and removes them
    */

   private void logout() {
      Server.clientOutStreams.remove(clientOut);
      currentUser.setLogin(false);
      isRunning = false;
      closeConnection();
   }

   private void closeConnection() {
      try {
         clientSocket.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}
