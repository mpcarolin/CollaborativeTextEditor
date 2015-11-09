import java.io.IOException;
import java.io.ObjectInput;
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
   
   
   private static List<Object> documents = Collections.synchronizedList(new ArrayList<>());
   
   private Object curDoc;

   public static void main(String[] args) throws IOException {
      sSocket = new ServerSocket(SERVER_PORT);
      System.out.println("Server started on port " + SERVER_PORT);

      while (true) {
         Socket newClientSocket = sSocket.accept();
         ObjectInputStream inStream = new ObjectInputStream(newClientSocket.getInputStream());
         ObjectOutputStream newClientOutStream = new ObjectOutputStream(newClientSocket.getOutputStream());
         new ClientHandler(allUsers, inStream, clientOutStreams, newClientOutStream).start();
         System.out.println("Accepted a new connection from " +
         newClientSocket.getInetAddress());
      }
   }
}

class ClientHandler extends Thread {

   private Map<String, User> allUsers;
   private ObjectInputStream input;
   private List<ObjectOutputStream> clients;
   private ObjectOutputStream newClient;
   private boolean running, remove;

   public ClientHandler(Map<String, User> allUsers, ObjectInputStream input, List<ObjectOutputStream> clients, ObjectOutputStream newClientOutStream) {
      this.allUsers = allUsers;
      this.input = input;
      this.clients = clients;
      this.newClient = newClientOutStream;
      if (!authenticateUser()) {
         return;
      } else {
         clients.add(newClient);
         running = true;
         editDoc();
      }

   }
   
   private boolean authenticateUser() {
      String userName = (String) input.readObject();
      User user = null;
      
      if ((user = allUsers.get(userName)) == null) {
         newClient.writeObject("No user with that name found");
      }
      
      String password = null;
      int trys = 0;
      do {
         if (trys == 3) {
            newClient.writeObject("Incorrect password, please try again later.");
            return false;
         }
         password = (String) input.readObject();
         if (password != user.getPassword()) {
            newClient.writeObject("Incorrect password, please try again.");
            trys++;
         } else {
            newClient.writeObject("Log in successfull.");
            return true;
         }
      } while (password != user.getPassword());
         
   }

   @Override
   public void run() {
      while (true && running) {
         try {
            curDocument = input.readObject();
         } catch (IOException e) {
            running = false;
         } catch (ClassNotFoundException e) {
            e.printStackTrace();
         }
         if (running) {
            updateDoc();
         }
      }
   }

   private void updateDoc() {
      remove = false;
      Set<ObjectOutputStream> closed = new HashSet<>();
      for (ObjectOutputStream client : clients) {
         try {
            client.reset();
            client.writeObject("");
         } catch (IOException e) {
            e.printStackTrace();
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