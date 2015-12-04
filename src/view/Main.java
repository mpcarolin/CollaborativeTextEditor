package view;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import model.Server;

public class Main {

//   private static final String ADDRESS = "helen.twilightparadox.com";
   private static final String ADDRESS = "localhost";

   public static void main(String[] args) {

      // open connection
      ObjectInputStream fromServer = null;
      ObjectOutputStream toServer = null;
      Socket server = null;

      try {
         server = new Socket(ADDRESS, Server.SERVER_PORT);
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
     new DocumentSelectGUI(fromServer, toServer);
   }

}
