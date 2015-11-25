/*
 * Code that handles everything for the server
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Server {

   public static final int SERVER_PORT = 9002;

   static Map<String, User> allUsers = Collections.synchronizedMap(new HashMap<>());
   static Map<String, Document> allDocuments = Collections.synchronizedMap(new HashMap<>());
   static Map<String, OpenDocument> openDocuments = Collections.synchronizedMap(new HashMap<>());
   static List<ObjectOutputStream> clientOutStreams = Collections.synchronizedList(new ArrayList<>());

  
   /*
    * Listens for new incoming client connections, and creates a ClientHandler to deal with them. 
    */
   public static void main(String[] args) throws IOException {
      hardCodeUsers();
      hardCodeDocs();
      try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
         System.out.println("Server started on port " + SERVER_PORT);
         while (true) {
            new ClientHandler(serverSocket.accept()).start();
         }
      }
   }

   /*
    * Hard code some Users for testing purposes
    */
   private static void hardCodeUsers() {
      allUsers.put("Daniel", new User("Daniel", "Avetian"));
      allUsers.put("Michael", new User("Michael", "Carolin"));
      allUsers.put("Jacob", new User("Jacob", "Groh"));
      allUsers.put("Filbert", new User("Filbert", "Johnson"));
      allUsers.put("Orzy", new User("Orzy", "Hazan"));
   }
   
   /*
    * Hard code some Documents for testing purposes
    */
   private static void hardCodeDocs() {
      allDocuments.put("DanielsDoc", new Document("DanielsDoc", "Daniel"));
      allUsers.get("Daniel").addOwnedDocument("DanielsDoc");
      
      allDocuments.put("MichaelsDoc", new Document("MichaelsDoc", "Michael"));
      allUsers.get("Michael").addOwnedDocument("MichaelsDoc");
      
      allDocuments.put("JacobsDoc", new Document("JacobsDoc", "Jacob"));
      allUsers.get("Jacob").addOwnedDocument("JacobsDoc");
      
      allDocuments.put("FilbertsDoc", new Document("FilbertsDoc", "Filbert"));
      allUsers.get("Filbert").addOwnedDocument("FilbertsDoc");
      
      allDocuments.put("OrzysDoc", new Document("OrzysDoc", "Orzy"));
      allUsers.get("Orzy").addOwnedDocument("OrzysDoc");
   }
}

/*
 * Client handler that is created and started each time a new connection is
 * created. Handles everything for one client
 */
class ClientHandler extends Thread {

   private Socket clientSocket;
   private ObjectInputStream clientIn; // clientIn stream of the new client
   private ObjectOutputStream clientOut; // output stream of the new client
   private User currentUser; // current user logged in
   private OpenDocument currentOpenDoc; // current document being edited by the
   private boolean isRunning, removeStreams; // booleans...

   
   /*
    * Opens the Input and Output Streams. 
    */
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

   /*
    * While the thread is still running, get the next ClientRequest from the
    * client, and call the respective method.
    */
   @Override
   public void run() {
      ClientRequest command;
      while (isRunning) {
         try {
            command = (ClientRequest) clientIn.readObject();
            switch (command) {
            case LOGIN:
               authenticateUser();
               break;
            case CREATE_ACCOUNT:
               createAccount();
               break;
            case CHANGE_PASSWORD:
               changePassword();
               break;
            case GET_DOCS:
               sendDocumentList();
               break;
            case GET_USERS:
               searchUsers();
               break;
            case CHAT_MSG:
               updateChat();
               break;
            case CREATE_DOC:
               createDocument();
               break;
            case ADD_PERMISSION:
               addPermission();
               break;
            case OPEN_DOC:
               openDocument();
               break;
            case DOC_TEXT:
               updateDocument();
               break;
            case SAVE_REVISION:
               saveRevision();
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
    * Gets the username and password from the client, and creates a new User
    * account. Verifies that the username is unique.
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
    * Gets the credentials from the client and checks if the match a User
    * account. Verifies that the User is not already logged in
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

   /*
    * Changes the password associated with the User account.
    */
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

   /*
    * Sends a list of Documents owned by the User, and a list of Documents that
    * the User has permission to edit.
    */
   private void sendDocumentList() throws IOException {
      clientOut.writeObject(currentUser.getOwnedDocuments());
      clientOut.writeObject(currentUser.getEditableDocuments());
   }

   /*
    * Gets a String from the client, and creates a list of all User names that
    * contain the specified text. Sends the list to the client.
    */
   private void searchUsers() throws IOException, ClassNotFoundException {
      String searchFor = (String) clientIn.readObject();
      Set<String> userNames = Server.allUsers.keySet();
      Iterator<String> itr = userNames.iterator();
      List<String> foundUsers = new ArrayList<String>();
      String temp;
      while (itr.hasNext()) {
         if ((temp = itr.next()).contains(searchFor)) {
            foundUsers.add(temp);
         }
      }
      clientOut.writeObject(foundUsers);
   }

   /*
    * Creates a new Document, with the current User as the owner. Verifies that
    * the Document name is unique.
    */
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
    * Adds permission for a User to edit a Document.
    */
   private void addPermission() throws ClassNotFoundException, IOException {
      String username = (String) clientIn.readObject();
      String document = (String) clientIn.readObject();
   }

   /*
    * Removes permission for a User to edit a Document.
    */
   private void removePermission() throws ClassNotFoundException, IOException {
      String username = (String) clientIn.readObject();
      String document = (String) clientIn.readObject();
      
   }

   /*
    * Opens a Document for editing. Checks to see whether the Document exists,
    * if the User has permission to edit it. ALso check if there is already an
    * OpenDocument for the Document to open, which means the document is
    * currently being edited by another User(s). If not it creates an
    * OpenDocument for the Document to be opened.
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
         clientOut.writeObject(ServerResponse.DOCUMENT_OPENED);
         clientOut.writeObject(currentOpenDoc.getText());
      }
   }

   /*
    * Reads in the new chat message from the client.
    */
   private void updateChat() throws ClassNotFoundException, IOException {
      String chatMessage = (String) clientIn.readObject();
      sendUpdateToClients(ServerResponse.CHAT_UPDATE, chatMessage);
   }

   /*
    * Reads in the new Document update from the client.
    */
   private void updateDocument() throws ClassNotFoundException, IOException {
      currentOpenDoc.updateText((String) clientIn.readObject(), currentUser.getName());
      sendUpdateToClients(ServerResponse.DOCUMENT_UPDATE, currentOpenDoc.getText());
   }

   /*
    * Takes in a ServerResponse, that indicates what type of update is being
    * sent, (Document or chat), and a String containing the text of the update.
    * Sends the ServerResponse and the text to all of the clients currently in
    * the list of output streams in the OpenDocument. Keeps track of the clients
    * that have disconnected and removes them from the list of OuputStreams in
    * the current OpenDocument.
    */
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

   /*
    * Saves a revision.
    */
   private void saveRevision() {
      currentOpenDoc.saveRevision();
   }

   /*
    * Closes the current OpenDocument.
    */
   private void closeDocument() {
      Server.openDocuments.remove(currentOpenDoc);
   }

   /*
    * Logs out the current user and closes the connection.
    */
   private void logout() {
      Server.clientOutStreams.remove(clientOut);
      currentUser.setLogin(false);
      isRunning = false;
      closeConnection();
   }

   /*
    * Closes the socket.
    */
   private void closeConnection() {
      try {
         clientSocket.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}
