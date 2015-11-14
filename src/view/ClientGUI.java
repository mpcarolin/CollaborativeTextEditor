
package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Element;

import model.Server;
import model.ServerCommand;

@SuppressWarnings("serial")
public class ClientGUI extends JFrame {

	private double screenWidth;
	private double screenHeight;
	// private String username;
	// private String password;
	// Client and server socket configuration
	private static final String ADDRESS = "localhost";

	private Socket server;
	private ObjectOutputStream toServer;
	private ObjectInputStream fromServer;

	// Java Swing Components
	private JTextArea textArea, chatTextArea;
	private JScrollPane scroll, chatScroll;
	private JPanel screenPanel, leftPanel, rightPanel;
	private JButton openChatButton;
	private JTextField chatText;
	private JComboBox<Integer> font;
	private JToolBar toolBar;
	private JToggleButton boldButton, italicsButton, underLine;
	// private Boolean logInSuccess = false;

	public ClientGUI() {

		// begin server connection
		openConnection();

		// create new user
		// if(userResponse==JOptionPane.NO_OPTION){
		// username = JOptionPane.showInputDialog("Create A Username?");
		// password= JOptionPane.showInputDialog("Create A Password?");
		// writeUserAndPassToServer();
		// }
		int userResponse = JOptionPane.showConfirmDialog(null, "Do you have an Account?", null,
				JOptionPane.YES_NO_CANCEL_OPTION);
		// try {
		// toServer.writeObject(ServerCommand.LOGIN);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		boolean loginResult = false;
		if (userResponse == JOptionPane.YES_OPTION) {
			loginResult = logIntoServer(ServerCommand.LOGIN);
		} else if (userResponse == JOptionPane.NO_OPTION) {
			loginResult = logIntoServer(ServerCommand.CREATE_ACCOUNT);
		}

		if (loginResult) {
			loggedIn();
			ServerListener serverListener = new ServerListener();
			serverListener.start();
		}
	}

	private boolean logIntoServer(ServerCommand command) {
		boolean logInSuccess = false;
		try {
			toServer.writeObject(command);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		while (!logInSuccess) {
			try {
				String username = JOptionPane.showInputDialog("Username:");
				String password = JOptionPane.showInputDialog("Password:");
				toServer.writeObject(username);
				toServer.writeObject(password);
				logInSuccess = (boolean) fromServer.readObject();
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Maximum login attempts reached: Please try again later.");
				System.exit(1);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return logInSuccess;
	}

	private void loggedIn() {
		// get screen size for proportional gui elements
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		screenWidth = screensize.getWidth() * 0.8;
		screenHeight = screensize.getHeight() * 0.8;
		this.setSize((int) screenWidth, (int) screenHeight);

		// set defaults and layoutGUI
		this.setTitle("Collaborative Text Editor");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLayout(new GridBagLayout());
		layoutGUI();
		this.setVisible(true);

	}

	// private int count = 1;
	// private int tries = 2;
	//
	// // need to log into server
	// private void logIntoServer() {
	// // TODO Auto-generated method stub
	// if (count != 1) {
	// if (count != 4) {
	// JOptionPane.showConfirmDialog(null,
	// "Incorrect Username or Password" + "\n" + "You have " + tries + " tries
	// remaining", null,
	// JOptionPane.OK_OPTION);
	// }
	// tries--;
	// }
	// if (count == 4) {
	// JOptionPane.showConfirmDialog(null, "Run out of attempts, Try Later",
	// null, JOptionPane.OK_OPTION);
	//
	// } else {
	// username = JOptionPane.showInputDialog("Username:");
	// password = JOptionPane.showInputDialog("Password:");
	// writeUserAndPassToServer();
	// count++;
	// }
	//
	// }
	//
	// private void writeUserAndPassToServer() {
	// try {
	// toServer.writeObject(username);
	// toServer.writeObject(password);
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// }

	public void layoutGUI() {
		int windowWidth = (int) (screenWidth * 0.75);
		int windowHeight = (int) (screenHeight * 0.75);
		Dimension windowSize = new Dimension(windowWidth, windowHeight);
		Dimension sideBar = new Dimension((int) (screenWidth - windowWidth * 0.5),
				(int) ((screenHeight - windowHeight) * 0.5));

		// button group toolbar
		toolBar = new JToolBar();
		toolBar.setPreferredSize(new Dimension(windowWidth, 20));
		boldButton = new JToggleButton("Bold");
		italicsButton = new JToggleButton("Italics");
		underLine = new JToggleButton("Underline");
		font = new JComboBox<Integer>();
		font.addItem(10);
		font.addItem(11);
		font.addItem(12);
		font.addItem(13);
		font.addItem(14);

		font.setSize(80, 10);
		toolBar.add(boldButton);
		toolBar.add(italicsButton);
		toolBar.add(underLine);
		toolBar.add(new JLabel("Font Size:"));
		toolBar.add(font);

		// set tool bar layout and location
		GridBagConstraints toolbarConstraint = new GridBagConstraints();
		toolbarConstraint.anchor = GridBagConstraints.NORTHWEST;
		toolbarConstraint.gridx = 0;
		toolbarConstraint.gridy = 0;
		toolbarConstraint.fill = GridBagConstraints.EAST;
		toolbarConstraint.gridheight = 1;
		toolbarConstraint.gridwidth = 3;
		toolbarConstraint.weighty = 0;
		toolbarConstraint.weightx = 0;
		this.add(toolBar, toolbarConstraint);

		// Chat Bar
		rightPanel = new JPanel(new BorderLayout());
		rightPanel.setPreferredSize(new Dimension(400, 300));
		rightPanel.setMinimumSize(new Dimension(400, 300));
		rightPanel.setBackground(Color.BLUE);
		GridBagConstraints chatConstraints = new GridBagConstraints();
		chatConstraints.anchor = GridBagConstraints.SOUTHEAST;
		chatConstraints.gridx = 2;
		chatConstraints.gridy = 3;
		chatConstraints.gridheight = 1;
		chatConstraints.weightx = 1;

		// Button to begin chat
		openChatButton = new JButton("Open Chat!");
		openChatButton.addActionListener(new chatButtonListener());
		rightPanel.add(openChatButton, BorderLayout.SOUTH);
		chatText = new JTextField();
		chatText.addActionListener(new newTextListener());
		chatText.setMaximumSize(new Dimension(100, 10));
		chatText.setVisible(false);

		rightPanel.add(chatText, BorderLayout.CENTER);

		// Create textArea To write on
		chatTextArea = new JTextArea();
		chatTextArea.setPreferredSize(new Dimension(100, 2000));
		chatTextArea.setLineWrap(true);
		chatTextArea.setEditable(false);

		// Create ScrollPane to put textAreaon
		chatScroll = new JScrollPane(chatTextArea);
		chatScroll.setPreferredSize(new Dimension(100, 250));
		chatScroll.setMinimumSize(new Dimension(100, 175));
		rightPanel.add(chatScroll, BorderLayout.NORTH);
		chatScroll.setVisible(false);
		this.add(rightPanel, chatConstraints);
		GridBagConstraints c = new GridBagConstraints();

		// in the Center set the Text Area
		c.gridx = 1;
		c.gridy = 1;
		c.gridheight = 3;
		c.gridwidth = 1;
		c.weightx = 0;
		c.weighty = 0.5;
		c.anchor = GridBagConstraints.FIRST_LINE_END;
		c.anchor = GridBagConstraints.CENTER;

		c.fill = GridBagConstraints.VERTICAL;
		// Center Panel to put Text Area and JScrollPane one
		screenPanel = new JPanel();
		screenPanel.setPreferredSize(new Dimension((int) (screenWidth * .5), 1500));

		// screenPanel.setPreferredSize(new Dimension(540,620));
		screenPanel.setMinimumSize(new Dimension((int) (screenWidth * 0.5), 1));
		// screenPanel.setBackground(Color.GREEN);

		// Size of the textArea
		int textWidth = (int) (screenWidth * .5);
		windowSize = new Dimension((int) textWidth + 30, (int) screenHeight);

		// Create textArea To write on
		textArea = new JTextArea();
		textArea.setPreferredSize(new Dimension(textWidth + 500, 2000));
		textArea.setLineWrap(true);
		// textArea.getDocument().addDocumentListener(new myDocumentListener());
		textArea.addKeyListener(new characterListener());
		// Create ScrollPane to put textAreaon
		scroll = new JScrollPane(textArea);
		scroll.setPreferredSize(new Dimension(textWidth - 30, (int) (screenHeight * 0.9)));

		// Adds center Panel with text to Jframe
		screenPanel.setVisible(true);
		screenPanel.add(scroll);
		this.add(screenPanel, c);
		this.setVisible(true);
	}

	private class characterListener implements KeyListener {

		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {

		}

		@Override
		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub
			String text = "";
			text = textArea.getText();
			// textArea.setText(text);
			chatTextArea.setText(text);
			try {
				toServer.writeObject(ServerCommand.DOC_TEXT);
				toServer.writeObject(text);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}

	/*
	 * Connects to a server
	 */
	private void openConnection() {
		try {
			server = new Socket(ADDRESS, Server.SERVER_PORT);
			toServer = new ObjectOutputStream(server.getOutputStream());
			fromServer = new ObjectInputStream(server.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* Listeners */
	// Server listener
	private class ServerListener extends Thread {

		@Override
		public void run() {
			while (true) {
				// obtain updated doc text from server in a try-catch
				try {
					String updatedText = (String) fromServer.readObject();
					textArea.setText(updatedText);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	// chatbox listener to send text to collaborators
	private class newTextListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO 8: When the enter button is pressed, send the contents of
			// the
			// JTextField to the server (add the username for extra style!)

			String text;
			text = chatText.getText();
			chatText.setText("");

			if (chatTextArea.getText().equals("")) {
				chatTextArea.setText(text);
			} else {
				text = chatTextArea.getText() + "\n" + text;
				chatTextArea.setText(text);
			}

			// try {
			// toServer.writeObject(userName+ ": "+ text);
			// } catch (IOException e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// }
		}
	}

	private class chatButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			if (chatScroll.isVisible()) {
				chatScroll.setVisible(false);
				chatText.setVisible(false);
				openChatButton.setName("Close Chat");
			} else {
				chatScroll.setVisible(true);
				chatText.setVisible(true);
				openChatButton.setName("Open Chat");
			}
			ClientGUI.this.setVisible(true);
		}

	}

	// testing
	public static void main(String[] args) {
		ClientGUI jake = new ClientGUI();
	}

}
