package view;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import model.DocumentSelectGUI;

public class Main {
	
	
	public static void main(String[] args) {
		
		// open connection
		ObjectInputStream fromServer = null;
		ObjectOutputStream toServer = null;
	
		LoginGUI login = new LoginGUI(fromServer, toServer);
		boolean loginComplete = login.open();

		if (loginComplete) {
			DocumentSelectGUI selector = new DocumentSelectGUI(); 
			boolean selectionComplete = selector.open();
		}
		
	}

}
