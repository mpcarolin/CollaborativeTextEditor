package view;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class CollaborativeTextEditor {

   private static final String ADDRESS = "localhost";
   //private static final String ADDRESS = "helen.twilightparadox.com";
   private static final int SERVER_PORT = 9001;


   public static void main(String[] args) {

      ObjectInputStream fromServer = null;
      ObjectOutputStream toServer = null;
      // open connection
      Socket server = null;

      try {
         server = new Socket(ADDRESS, SERVER_PORT);
         toServer = new ObjectOutputStream(server.getOutputStream());
         fromServer = new ObjectInputStream(server.getInputStream());
      } catch (IOException e) {
         e.printStackTrace();
      }

      LoginGUI login = new LoginGUI(fromServer, toServer);

      while (login.isVisible()) {
         try {
           Thread.sleep(3);
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
      }

     new DocumentSelectGUI(login.getUsername(), fromServer, toServer);

   }

}
