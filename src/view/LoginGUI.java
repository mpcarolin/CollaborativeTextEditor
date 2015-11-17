package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import model.ClientRequest;
import model.Server;
import model.ServerResponse;

public class LoginGUI extends JFrame {

		// screen size constant
		private final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		// JFrame components
		public JButton loginButton, createAccountButton, resetPassButton; 
		public JTextField usernameField;
		public JPasswordField passwordField;
		private JLabel usernameLabel, passwordLabel;
		private JPanel usernamePanel, passwordPanel, buttonPanel;
		
		// Server connection variables
		private ObjectInputStream fromServer;
		private ObjectOutputStream toServer;
		
		public LoginGUI(ObjectInputStream fromServer, ObjectOutputStream toServer) {
			this.fromServer = fromServer;
			this.toServer = toServer;
			layoutGUI();
		}

		private void layoutGUI() {
			// set program's dimensions
			int windowWidth = (int) (screenSize.getWidth() * 0.35);
			int windowHeight = (int) (screenSize.getHeight() * 0.20);

			// set defaults
			this.setDefaultCloseOperation(EXIT_ON_CLOSE);
			this.setSize(windowWidth, windowHeight);
			this.setTitle("Login Menu");
			this.setLayout(new BorderLayout());

			// username and password fields
			usernameLabel = new JLabel("Username: ");
			passwordLabel = new JLabel("Password: ");
			usernameField = new JTextField(10);
			passwordField = new JPasswordField(10);
			
			// center panels for fields 
			usernamePanel = new JPanel(new FlowLayout());
			passwordPanel = new JPanel(new FlowLayout());
			
			// layout username and password fields
			usernamePanel.add(usernameLabel);
			usernamePanel.add(usernameField);
			passwordPanel.add(passwordLabel);
			passwordPanel.add(passwordField);

			// buttons
			loginButton = new JButton("Login");
			createAccountButton = new JButton("Create New Account");
			resetPassButton = new JButton("Reset Password");
			
			// button panel
			buttonPanel = new JPanel(new FlowLayout());
			buttonPanel.add(loginButton);
			buttonPanel.add(createAccountButton);
			buttonPanel.add(resetPassButton);
			
			// add everything to the parent JFrame
			this.add(usernamePanel, BorderLayout.NORTH);
			this.add(passwordPanel, BorderLayout.CENTER);
			this.add(buttonPanel, BorderLayout.SOUTH);
			
			// parent JFrame defaults
			this.setLocation((int)(screenSize.getWidth()/4), (int)(screenSize.getHeight()/4));
			this.setResizable(false);
			this.setVisible(true);
		}
		
		private class loginButtonListener implements ActionListener {

			@Override
			public void actionPerformed(ActionEvent e) {
				// get username and password
				String username = usernameField.getText();
				String password = passwordField.getPassword().toString();
				
				try {
					// send to server and wait response
					toServer.writeObject(ClientRequest.LOGIN);
					toServer.writeObject(username);
					toServer.writeObject(password);
					
					// get response and process it
					ServerResponse response = (ServerResponse)fromServer.readObject();
					switch(response) {
					case LOGIN_SUCCESS:
						// call document selector gui
						break;
					case INCORRECT_USERNAME:
						usernameField.setText("");
						passwordField.setText("");
						JOptionPane.showMessageDialog(LoginGUI.this, 
								"Incorrect Username. Please try again.");
						break;
					case INCORRECT_PASSWORD:
						passwordField.setText("");
						JOptionPane.showMessageDialog(LoginGUI.this, 
								"Incorrect Password. Please try again.");
						break;
					default:
						break;
					}
				} catch (IOException exception) {
					exception.printStackTrace();
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
			}
			
		}
		
		
	
		// testing
		public static void main(String[] args) {
			ObjectOutputStream out = null;
			ObjectInputStream in = null;
			new LoginGUI(in, out);
		}

	
}
