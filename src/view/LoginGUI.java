package view;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.SwingConstants;

import model.ClientRequest;
import model.DocumentSelectGUI;
import model.Server;
import model.ServerResponse;

public class LoginGUI extends JFrame {

		// screen size constant
		private final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		// JFrame components
		private JButton loginButton, createAccountButton, resetPassButton; 
		private JTextField usernameField;
		private JPasswordField passwordField;
		private JLabel usernameLabel, passwordLabel, instructionLabel;
		private JPanel usernamePanel, passwordPanel, bottomPanel, buttonPanel ;
		
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
			
			// message label indicating what the user should do
			instructionLabel = new JLabel("Enter login credentials to login or create a new account", SwingConstants.CENTER);

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
			
			// button listeners
			loginButton.addActionListener(new loginButtonListener());
			createAccountButton.addActionListener(new createAccountButtonListener());
			resetPassButton.addActionListener(new ResetAccountButtonListener());
			
			// button panel
			buttonPanel = new JPanel(new FlowLayout());
			buttonPanel.add(loginButton);
			buttonPanel.add(createAccountButton);
			buttonPanel.add(resetPassButton);
			
			// bottom panel
			bottomPanel = new JPanel(new BorderLayout());
			bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
			bottomPanel.add(instructionLabel, BorderLayout.CENTER);

			// add everything to the parent JFrame
			this.add(usernamePanel, BorderLayout.NORTH);
			this.add(passwordPanel, BorderLayout.CENTER);
			this.add(bottomPanel, BorderLayout.SOUTH);
			
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
				String password = passwordField.getText();
				
				try {
					// send to server and wait response
					toServer.writeObject(ClientRequest.LOGIN);
					toServer.writeObject(username);
					toServer.writeObject(password);
				
					// get response and process it
					ServerResponse response = (ServerResponse)fromServer.readObject();
	
					switch(response) {

					case LOGIN_SUCCESS:
						instructionLabel.setText("Login Successful");
						// open the Document Selector GUI
						//new DocumentSelectGUI(fromServer);
						LoginGUI.this.setVisible(false);
						LoginGUI.this.dispose();
						break;

					case LOGGED_IN:
						instructionLabel.setText("User " + username + " is already logged in.");
						instructionLabel.setForeground(Color.RED);
						break;

					case INCORRECT_USERNAME:
						usernameField.setText("");
						passwordField.setText("");
						instructionLabel.setText("You entered an invalid username.");
						instructionLabel.setForeground(Color.RED);
						break;

					case INCORRECT_PASSWORD:
						passwordField.setText("");
						instructionLabel.setText("You entered an invalid Password.");
						instructionLabel.setForeground(Color.RED);
						break;

					default:
						break;
					}
				} catch (IOException exception) {
					exception.printStackTrace();
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}
			}
		}
		
		private class createAccountButtonListener implements ActionListener {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					String username = usernameField.getText();
					String password = passwordField.getText();
					
					// send server username and password
					toServer.writeObject(ClientRequest.CREATE_ACCOUNT);
					toServer.writeObject(username);
					toServer.writeObject(password);
					
					// read server response and process
					ServerResponse response = (ServerResponse)fromServer.readObject();
					System.out.println(response);

					switch(response) {

					case ACCOUNT_CREATED:
						instructionLabel.setText("Account successfully created.");
						instructionLabel.setForeground(Color.GREEN);
						
						// server auto-logs user in after
						LoginGUI.this.setVisible(false);
						LoginGUI.this.dispose();
						break;

					case ACCOUNT_EXISTS:
						instructionLabel.setText("The username you entered already exists. Try again.");
						instructionLabel.setForeground(Color.RED);
						break;

					default:
						break;
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}
			}
		}
		
		private class ResetAccountButtonListener implements ActionListener {

			@Override
			public void actionPerformed(ActionEvent e) {
					String username = usernameField.getText();
					String newPassword = passwordField.getText();
				
					toServer.writeObject(ClientRequest.CHANGE_PASSWORD);
					toServer.writeObject(username);
					toServer.writeObject(newPassword);
					
					ServerResponse response = (ServerResponse)fromServer.readObject();
					switch (response) {
						case PASSWORD_CHANGED:
							instructionLabel.setText("Password reset for user " + username + " was successful.");
							instructionLabel.setForeground(Color.GREEN);
						case INCORRECT_USERNAME:
							usernameField.setText("");
							instructionLabel.setText("No account found for username " + username);
							instructionLabel.setForeground(Color.RED);
							break;
						default:
							break;
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
