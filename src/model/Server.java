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
   // private static List<Object> documents = Collections.synchronizedList(new ArrayList<>());
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

   private static void hardCodeUsers() {
      allUsers.put("Daniel", new User("Daniel", "Avetian"));
      allUsers.put("Micheal", new User("Michael", "Carolin"));
      allUsers.put("Jacob", new User("Jacob", "Groh"));
      allUsers.put("Filbert", new User("Filbert", "Johnson"));
      allUsers.put("Orzy", new User("Orzy", "Hazan"));
   }
}

class ClientHandler extends Thread {

   private Map<String, User> allUsers;
   private ObjectInputStream input;
   private List<ObjectOutputStream> clients;
   private ObjectOutputStream newClient;
   private Document currentDoc;
   private boolean running, remove;

   public ClientHandler(Map<String, User> allUsers, ObjectInputStream input, List<ObjectOutputStream> clients,
         ObjectOutputStream newClientOutStream, Document currentDoc) {
      this.allUsers = allUsers;
      this.input = input;
      this.clients = clients;
      this.newClient = newClientOutStream;
      this.currentDoc = currentDoc;
      
      
      try {
         if (!authenticateUser()) {
            return;
         } else {
            clients.add(newClient);
            running = true;
            updateDoc();
         }
      } catch (IOException e) {
         e.printStackTrace();
         return;
      } catch (ClassNotFoundException e) {
         e.printStackTrace();
         return;
      }
   }

   private boolean authenticateUser() throws ClassNotFoundException, IOException {
      ServerCommand command = (ServerCommand) input.readObject();
      if (command == ServerCommand.CREATE_ACCOUNT) {
         // createAccount();
      }
      
      User user = null;
      String userName = null;
      String password = null;
      int trys = 0;
      do {
         password = (String) input.readObject();
         userName = (String) input.readObject();
         if ((user = allUsers.get(userName)) == null) {
            trys++;
            newClient.writeObject(false);
         } else if ((user.getID() + password).hashCode() != user.getHashPass()) {
            trys++;
            newClient.writeObject(false);
         } else {
            newClient.writeObject(true);
            return true;
         }
      } while (trys < 3);
      return false;
   }
   
   private void createAccount() {
      // create a new user account here;
   }

   @Override
   public void run() {
      while (true && running) {
         try {
            ServerCommand command = (ServerCommand) input.readObject();
            switch (command) {
            case CHAT_MSG:
               readDoc();
               break;
            case DOC_TEXT:
               break;
            case LOGOUT:
               running = false;
               break;
            default:
               break;
            
            }         
         } catch (IOException e) {
            running = false;
         } catch (ClassNotFoundException e) {
            e.printStackTrace();
         }
         if (!running) {
            return;
         }
      }
   }
   
   private void readDoc() {
      try {
         currentDoc.replaceText((String) input.readObject());
      } catch (ClassNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
      updateDoc();
   }
   
   private void updateDoc() {
      remove = false;
      Set<ObjectOutputStream> closed = new HashSet<>();
      for (ObjectOutputStream client : clients) {
         try {
            client.reset();
            client.writeObject(currentDoc.getText());
         } catch (IOException e) {
            remove = true;
            closed.add(client);
         }
      }
      if (remove) {
         clients.removeAll(closed);
         remove = false;
         closed = new HashSet<ObjectOutputStream>();
      }
   }
}
