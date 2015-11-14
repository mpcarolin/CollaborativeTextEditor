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
   private static ServerSocket sSocket;

   private static Map<String, User> allUsers = Collections.synchronizedMap(new HashMap<>());
   private static List<ObjectOutputStream> clientOutStreams = Collections.synchronizedList(new ArrayList<>());
   // private static List<Object> documents = Collections.synchronizedList(new
   // ArrayList<>());
   private static Document currentDoc = new Document();

   public static void main(String[] args) throws IOException {

      hardCodeUsers();
      sSocket = new ServerSocket(SERVER_PORT);
      System.out.println("Server started on port " + SERVER_PORT);

      while (true) {
         Socket newClientSocket = sSocket.accept();
         ObjectInputStream inStream = new ObjectInputStream(newClientSocket.getInputStream());
         ObjectOutputStream newClientOutStream = new ObjectOutputStream(newClientSocket.getOutputStream());
         new ClientHandler(allUsers, inStream, clientOutStreams, newClientOutStream, currentDoc).start();
         System.out.println("Accepted a new connection from " + newClientSocket.getInetAddress());
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
 * Client handler that is created and started each time a new connection is created
 * Handles everything for one client (for now)
 */
class ClientHandler extends Thread {

   private Map<String, User> allUsers;          // list of all user accounts
   private ObjectInputStream input;             // input stream of the new client
   private List<ObjectOutputStream> clients;    // list of all of the client output streams
   private ObjectOutputStream newClient;        // output stream of the new client (not in list yet)
   private Document currentDoc;                 // current document being edited (temporary)
   private boolean running, remove;             // booleans...

   public ClientHandler(Map<String, User> allUsers, ObjectInputStream input, List<ObjectOutputStream> clients,
         ObjectOutputStream newClientOutStream, Document currentDoc) {
      this.allUsers = allUsers;
      this.input = input;
      this.clients = clients;
      this.newClient = newClientOutStream;
      this.currentDoc = currentDoc;
   }
   
   
   @Override
   public void run() {  
      // While the thread is still running, get the next ServerCommand from the client,
      // and call the respective method
      ServerCommand command;
      while (true && running) {
         try {
            command = (ServerCommand) input.readObject();
            switch (command) {
            case LOGIN:
               authenticateUser();
               setDoc();
               break;
            case CREATE_ACCOUNT:
               createAccount();
               setDoc();
               break;
            case CHAT_MSG:
               updateChat();
               break;
            case DOC_TEXT:
               readDoc();
               break;
            case LOGOUT:
               clients.remove(newClient);
               running = false;
               break;
            default:
               break;
            }
         } catch (ClassNotFoundException e) {
            e.printStackTrace();
         } catch (IOException e) {
            clients.remove(newClient);
            running = false;
            e.printStackTrace();
         }
         if (!running) {
            return;
         }
      }
   }

   
   /*
    * Checks if the user already has an account or not
    * Calls create account if the user does not already have an account
    * If the do have one, gets their credentials and checks if they match an existing account
    * Allows 3 tries before the connection is closed
    * Returns a boolean indicating whether the user successfully logged in (or created a new account) 
    */
   private boolean authenticateUser() throws ClassNotFoundException, IOException {
      // Read the ServerCommand to see if the user needs to create a new account
      // If they do, call createAccount() and return its return value
      User user = null;
      String username = null;
      String password = null;
      int tries = 0;
      do {
         username = (String) input.readObject();
         password = (String) input.readObject();

         if ((user = allUsers.get(username)) == null) {
            tries++;
            newClient.writeObject(false);
         } else if ((user.getID() + password).hashCode() != user.getHashPass()) {
            tries++;
            newClient.writeObject(false);
         } else {
            newClient.writeObject(true);
            return true;
         }
      } while (tries < 3);

      return false;
   }

   
   /*
    * Gets the username and password from the client, and creates a new userAccount
    * Returns a boolean indicating whether the account was successfully created or not
    */
   private boolean createAccount() {
      try {
         String username = (String) input.readObject();
         String password = (String) input.readObject();
         allUsers.put(username, new User(username, password));
         newClient.writeObject(true);
      } catch (ClassNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
         return false;
      }

      return true;
   }

   
   /*
    * Sends the current document to the new client
    */
   private void setDoc() {
      try {
         newClient.writeObject(currentDoc.getText());
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
   
   
   /*
    * Read the updated document from the client
    * Calls updateDoc() if the document is successfully read
    */
   private void readDoc() {
      try {
         currentDoc.replaceText((String) input.readObject());
      } catch (ClassNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
         clients.remove(newClient);
         running = false;
         return;
      }
      updateDoc();
   }
   
   
   /*
    * Sends the current document to all of the clients in the clients list
    * Keeps track of the clients that have disconnected and removes them
    */
   private void updateDoc() {
      remove = false;
      Set<ObjectOutputStream> closed = new HashSet<>();
      for (ObjectOutputStream client : clients) {
         if (client == newClient) {
            continue;
            // maybe this can be removed now?
         }
         try {
            client.reset();
            client.writeObject(currentDoc.getText());
         } catch (IOException e) {
            remove = true;
            closed.add(client);
         }
         if (remove) {
            clients.removeAll(closed);
            remove = false;
            closed = new HashSet<ObjectOutputStream>();
         }
      }
   }
   
   
   /*
    * Reads in the new chat message from the client, and sends it to all of the clients in the client list
    * Keeps track of the clients that have disconnected and removes them
    */
   private void updateChat() {
      String chatMessage = null;
      try {
         chatMessage = (String) input.readObject();
      } catch (ClassNotFoundException e1) {
         e1.printStackTrace();
      } catch (IOException e1) {
         e1.printStackTrace();
         clients.remove(newClient);
         running = false;
         return;
      }

      remove = false;
      Set<ObjectOutputStream> closed = new HashSet<>();
      for (ObjectOutputStream client : clients) {
         try {
            client.reset();
            client.writeObject(chatMessage);
         } catch (IOException e) {
            remove = true;
            closed.add(client);
         }
         if (remove) {
            clients.removeAll(closed);
            remove = false;
            closed = new HashSet<ObjectOutputStream>();
         }
      }
   }
   
   private void closeConnection() {
      // going to need to close things properly eventually
   }
}
