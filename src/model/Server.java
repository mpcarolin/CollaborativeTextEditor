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

public class Server {

   public static final int SERVER_PORT = 9001;

   static Map<String, User> allUsers = Collections.synchronizedMap(new HashMap<>());
   static Map<String, Document> allDocuments = Collections.synchronizedMap(new HashMap<>());
   static List<Document> openDocuments = Collections.synchronizedList(new ArrayList<>());

   static List<ObjectOutputStream> clientOutStreams = Collections.synchronizedList(new ArrayList<>());
   static Document currentDoc;

   public static void main(String[] args) throws IOException {

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
      allUsers.put("Micheal", new User("Michael", "Carolin"));
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
   private ObjectInputStream clientIn;        // clientIn stream of the new client
   private ObjectOutputStream clientOut;      // output stream of the new client (not in list yet)
   private User currentUser;                  // current user logged in
   private OpenDocument currentDocument;      // current document being edited by the user
   private boolean isRunning, removeStreams;  // booleans...

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
      // While the thread is still isRunning, get the next ClientRequest from the client, and call the respective method
      ClientRequest command;
      while (true && isRunning) {
         try {
            command = (ClientRequest) clientIn.readObject();
            switch (command) {
            case LOGIN:
               authenticateUser();
               sendDocumentList();
               break;
            case CREATE_ACCOUNT:
               createAccount();
               // not sure if this needs to be here, the user wont have any documents yet
               sendDocumentList();
               break;
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
               readDoc();
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
            Server.clientOutStreams.remove(clientOut);
            isRunning = false;
            e.printStackTrace();
         }
         if (!isRunning) {
            closeConnection();
            return;
         }
      }
   }

   /*
    * Gets the credentials from the client and checks if the match a user
    * account Verifies that the user is not already logged in
    */
   private void authenticateUser() throws ClassNotFoundException, IOException {
      User user = null;
      do {
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
            return;
         }
      } while (true);
   }

   public void sendDocumentList() throws IOException {
      clientOut.writeObject(currentUser.getOwnedDocuments());
      clientOut.writeObject(currentUser.getEditableDocuments());
   }

   /*
    * Gets the username and password from the client, and creates a new
    * userAccount Verifies that the username is unique Returns a boolean
    * indicating whether the account was successfully created or not
    */
   private void createAccount() throws ClassNotFoundException, IOException {
      do {
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
            return;
         }
      } while (true);
   }

   private void createDocument() throws ClassNotFoundException, IOException {
      String docName = (String) clientIn.readObject();
      Server.currentDoc = new Document(docName, currentUser.getName());
   }

   /*
    * These are the methods that will be used when documents are created,
    * opened, and closed, rather than the current "one" document being used for
    * testing.
    */

   private void openDocument() throws ClassNotFoundException, IOException {
      String docName = (String) clientIn.readObject();
      Document openingDoc = Server.allDocuments.get(docName);
      
   }

   private void updateDocument() {

   }

   private void closeDocument() {

   }

   /*
    * Read the updated document from the client Calls updateDoc() if the
    * document is successfully read
    */
   private void readDoc() throws ClassNotFoundException, IOException {
      Server.currentDoc.replaceText((String) clientIn.readObject(), currentUser.getName());
      updateDoc();
   }

   /*
    * Sends the current document to all of the clientOutStreams in the
    * clientOutStreams list Keeps track of the clientOutStreams that have
    * disconnected and removes them
    */
   private void updateDoc() {
      removeStreams = false;
      Set<ObjectOutputStream> closed = new HashSet<>();
      for (ObjectOutputStream client : Server.clientOutStreams) {
         if (client == clientOut) {
            continue;
            // maybe this can be removed now?
         }
         try {
            client.reset();
            client.writeObject(Server.currentDoc.getText());
         } catch (IOException e) {
            removeStreams = true;
            closed.add(client);
         }
         if (removeStreams) {
            Server.clientOutStreams.removeAll(closed);
            removeStreams = false;
            closed = new HashSet<ObjectOutputStream>();
         }
      }
   }
   
   private void readChat() {
      
   }
   
   private void writeChat() {
      
   }
   
   

   /*
    * Reads in the new chat message from the client, and sends it to all of the
    * clientOutStreams in the client list Keeps track of the clientOutStreams
    * that have disconnected and removes them
    */
   private void updateChat() {
      String chatMessage = null;
      try {
         chatMessage = (String) clientIn.readObject();
      } catch (ClassNotFoundException e1) {
         e1.printStackTrace();
      } catch (IOException e1) {
         e1.printStackTrace();
         Server.clientOutStreams.remove(clientOut);
         isRunning = false;
         return;
      }

      removeStreams = false;
      Set<ObjectOutputStream> closed = new HashSet<>();
      for (ObjectOutputStream client : Server.clientOutStreams) {
         try {
            client.reset();
            client.writeObject(chatMessage);
         } catch (IOException e) {
            removeStreams = true;
            closed.add(client);
         }
         if (removeStreams) {
            Server.clientOutStreams.removeAll(closed);
            removeStreams = false;
            closed = new HashSet<ObjectOutputStream>();
         }
      }
   }

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
