
package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import model.Server;
import model.ServerCommand;

@SuppressWarnings("serial")
public class ClientGUI extends JFrame {

	private double screenWidth;
	private double screenHeight;
	private static final String ADDRESS = "localhost";
	private Socket server;
	private ObjectOutputStream toServer;
	private ObjectInputStream fromServer;
	private JEditorPane textArea;
	// Java Swing Components
	private JTextArea chatTextArea;
	private JScrollPane scroll, chatScroll;
	private JPanel screenPanel, leftPanel, rightPanel;
	private JButton openChatButton;
	private JTextField chatText;
	private JComboBox<Integer> font;
	private JToolBar toolBar;
	private JToggleButton boldButton, italicsButton, underLine;
	private boolean bold,underline,italic;
	// private Boolean logInSuccess = false;

	public ClientGUI() {

		// begin server connection
		openConnection();

		int userResponse = JOptionPane.showConfirmDialog(null, "Do you have an Account?", null,
				JOptionPane.YES_NO_CANCEL_OPTION);
		boolean loginResult = false;
		if (userResponse == JOptionPane.YES_OPTION) {
			loginResult = logIntoServer(ServerCommand.LOGIN);
		} else if (userResponse == JOptionPane.NO_OPTION) {
			loginResult = logIntoServer(ServerCommand.CREATE_ACCOUNT);
		}

		System.out.println(loginResult);
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
				if (!logInSuccess) {
					JOptionPane.showMessageDialog(null,
							"Sorry, You Suck and have an unknown amount of tries...." + "Please Try Again!");
				}
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
		// Set listener
		boldButton.addActionListener(new boldButtonListener());
		underLine.addActionListener(new underLineButtonListener());
		italicsButton.addActionListener(new italicsButtonListener());

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
		textArea = new JEditorPane();
		textArea.setContentType("text/html");

		textArea.setPreferredSize(new Dimension(textWidth + 500, 2000));
		// textArea.setLineWrap(true);
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

	
	
	private class boldButtonListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			bold=!bold;
		}
		
	}
	private class underLineButtonListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			underline=!underline;
		}
		
	}	private class italicsButtonListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			italic=!italic;
		}
		
	}
	//private StringBuilder list= new StringBuilder();
	private ArrayList<String> list= new ArrayList<String>();
	private boolean isbold=false,isunderlined=false,isitalics=false;
	private StringBuilder addToList,noTagString,stringWithTags;
	

	private class characterListener implements KeyListener {
		private int carrotPosition;
		//ascii-48-126
		@Override
		public void keyTyped(KeyEvent e) {
			if(e.getKeyChar()>47&&e.getKeyChar()<125 && bold||underline||italic){
				e.consume();
				addToList.insert(addToList.length(),e.getKeyChar());
			}
		}
		@Override
		public void keyPressed(KeyEvent e) {
			if(e.getKeyChar()>47&&e.getKeyChar()<125 && bold||underline||italic){
				e.consume();
				carrotPosition= textArea.getCaretPosition();
				//used to create inserting key typed
				addToList= new StringBuilder();
				if(bold){
					addToList.insert(addToList.length(),"<b>");
				}
				if(italic){
					addToList.insert(addToList.length(),"<i>");
				}			
				if(underline){
					addToList.insert(addToList.length(),"<u>");
				}
			}

		}

		@Override
		public void keyReleased(KeyEvent e) {
			if(e.getKeyChar()>47&&e.getKeyChar()<125 && bold||underline||italic){
				e.consume();
//			System.out.println(textArea.getText());
///use mouse courser as the relative position so just add /b first than string than b than move curser
//according to counter. 


// take two strings, one with <b> tags and one without. Than add element to one without, go through loop to parse it with the tags, update cursor position, than set text

			if(underline){
				addToList.insert(addToList.length(),"</u>");
			}
			if(italic){
				addToList.insert(addToList.length(),"</i>");
			}	
			if(bold){
				addToList.insert(addToList.length(),"</b>");
			}

			// has string to be inserted in correct format!!!
			String text=addToList.toString();


System.out.println(text);
			// create two String builders
			noTagString=new StringBuilder();
			stringWithTags=new StringBuilder();

			//make noTagString hold no html tags
			Whitelist deleteAllHTML= new Whitelist();
			noTagString= noTagString.append(Jsoup.clean(textArea.getText(), deleteAllHTML));
			
			// corner case, if user clicks very end of document text, it will fix the alighning issues
			if(textArea.getCaretPosition()>noTagString.length()){
				textArea.setCaretPosition(noTagString.length()+1);	
			}
			
			//wierd corner case with inital caret positions. Done to print current key typed in the 
			//right format to be pasted into the right location of the noHTML string
			if(textArea.getCaretPosition()==0){
				carrotPosition= textArea.getCaretPosition();
			}else{
				carrotPosition= textArea.getCaretPosition()-1;
			}
			System.out.print(carrotPosition);
			noTagString.insert(carrotPosition, text);
//			System.out.println("This is noTagString: "+ noTagString.toString());
			
			
			// sets the String with tags to only consis of letters and b,u,i tags
			Whitelist allowable= new Whitelist();
			allowable.addTags("b");
			allowable.addTags("i");
			allowable.addTags("u");
			System.out.print("text: "+ textArea.getText());
			String withTags= Jsoup.clean(textArea.getText(),allowable);
			//withTags= stringWithTags.toString().replace("/n","");
			System.out.println("this is frusterating : "+ withTags);
			stringWithTags= stringWithTags.insert(0, withTags);

			System.out.print("fjaklfjlajdflkajfklkslfjklasjflkjfkj"+ stringWithTags.toString());
			// goal of next part is to 
			// parse the two strings together with carrot Position being taken into effect
			// should only increase when a non tag is inserted
				int increment=0;		// increment will only increase when a key(not a tag) is copied
				int stopper= stringWithTags.length();
				//while(i<stopper){
				for(int i=0;i<stringWithTags.length();i++){
//System.out.println( "increment" + increment + "carrot "+ carrotPosition+ "string length: " + stringWithTags.length());
System.out.println("stringWithTags " +stringWithTags.toString());
System.out.println("notagString    " +noTagString.toString());
					// suppose to insert new tag into right location
System.out.println(increment);
					// idea is to insert string into String with tags when increment=carrot 
					// and copy rest of carrot into 

					if(increment==carrotPosition){
						stringWithTags.insert(i, text);
						increment=increment+text.length();
						i=i+text.length();
						System.out.println("this value of i = "+ i);
						// Another idea is to Break and just copy the rest of the string since it's going to be the same...
					}
					// inserts tags into string with no tags
					// while < is read (this only does one character at a time... inefficient... 
					while((stringWithTags.charAt(i)+"").equals("<")){
						System.out.print("twotimes?");
						// make sure stringBuilder Array's have capacity
						if(noTagString.length()<=i+6){
							noTagString.setLength(i+15);
						}
						if(stringWithTags.length()<=i+6){
							stringWithTags.setLength(i+15);
						}
						// this if says that if the thing after < is not equal do the loop
						if(!(stringWithTags.charAt(i+1)+"").equals(noTagString.charAt(i+1)+"")){
							
							if(!(stringWithTags.charAt(i+1)+"").equals("/")){
								System.out.println("value of i = "+ i);

								noTagString.insert(i, "<");
								i++;
								noTagString.insert(i, stringWithTags.charAt(i));
								//set tag to true
								if((stringWithTags.charAt(i)+"").equals("b")){
									isbold=true;
								}
								if((stringWithTags.charAt(i)+"").equals("i")){
									isitalics=true;
								}
								if((stringWithTags.charAt(i)+"").equals("u")){
									isunderlined=true;
								}

								i++;

								noTagString.insert(i, ">");
								i++;
								// go through the characters until you find a <
								while(!(stringWithTags.charAt(i)+"").equals("<")){
									i++;
									// copy rest of array over
									if(i+1>stringWithTags.length()-1){
										int currentLocation=noTagString.length();
										while(stringWithTags.length()>noTagString.length()){
											noTagString.insert(currentLocation, stringWithTags.charAt(currentLocation));
											currentLocation++;
											//System.out.println("final noTagString: " + noTagString.toString());
										}
										System.out.println("final noTagString: " + noTagString.toString());

										break;
									}

									System.out.println((stringWithTags.charAt(i)+"").equals("<"));
									System.out.println((stringWithTags.charAt(i+1)+"").equals("/"));

									increment++;
									// if inserting in middle of bold or ita, or under, insert.
						//This loop fixes inserting in middle but causes it to never become unbold or ui or uu...  			
									if(increment==carrotPosition){
										// here search for end tags before inserting text than insert begining tags
										//use is bold, italics, underlined
										System.out.println("with no changes- stringWithTags:"+ stringWithTags.toString());
										System.out.println("with no changes- noTagString:"+ noTagString.toString());
										if(isbold){
											stringWithTags.insert(i, "</b>");
											noTagString.insert(i, "</b>");
											i=i+4;
										}
										if(isunderlined){
											stringWithTags.insert(i, "</u>");
											noTagString.insert(i, "</u>");
											i=i+4;
										}
										if(isitalics){
											stringWithTags.insert(i, "</i>");
											noTagString.insert(i, "</i>");
											i=i+4;
										}
										stringWithTags.insert(i, text);
										increment=increment+text.length();
										i=i+text.length();
										System.out.println("this value of i is from this loop = "+ i);
										System.out.println("afterinsert- stringWithTags:"+ stringWithTags.toString());
										System.out.println("afterinsert- noTagString:"+ noTagString.toString());
										
										//add the tags back to the strings
										if(isbold){
											stringWithTags.insert(i, "<b>");
											noTagString.insert(i, "<b>");
											i=i+4;
										}
										if(isunderlined){
											stringWithTags.insert(i, "<u>");
											noTagString.insert(i, "<u>");
											i=i+4;
										}
										if(isitalics){
											stringWithTags.insert(i, "<i>");
											noTagString.insert(i, "<i>");
											i=i+4;
										}
										System.out.println("with changes- stringWithTags:"+ stringWithTags.toString());
										System.out.println("with changes- noTagString:"+ noTagString.toString());

										// Another idea is to Break and just copy the rest of the string since it's going to be the same...
									}
								}
						//to here. However without it, it acts correctlyish with each button, but cant edit from 
						// middle.

							}else{
								System.out.println("Kjdflkjdf");
								noTagString.insert(i, "</");
								i++;
								i++;
								noTagString.insert(i, stringWithTags.charAt(i));
								if((stringWithTags.charAt(i)+"").equals("b")){
									isbold=false;
								}
								if((stringWithTags.charAt(i)+"").equals("i")){
									isitalics=false;
								}
								if((stringWithTags.charAt(i)+"").equals("u")){
									isunderlined=false;
								}
								i++;
								noTagString.insert(i, ">");	
								i++;
								System.out.println("i is equal to=" +i);
								System.out.println("just curious with changes- stringWithTags:"+ stringWithTags.toString());
								System.out.println("just curious with changes- noTagString:"+ noTagString.toString());

								System.out.println("this string is equal to: "+(stringWithTags.charAt(i)+""));

								System.out.println((stringWithTags.charAt(i)+"").equals("<"));
								
							}
						}else{
							i++;
						}						
					}
					increment++;
					i++;
				}
	//		}
//			System.out.println(noTagString.toString());
//			System.out.println(stringWithTags.toString());

			carrotPosition= textArea.getCaretPosition();
			textArea.setText(noTagString.toString());
			//textArea.setText(list.toString());
			//textArea.setText(wholetext);
			if(carrotPosition==0){
				textArea.setCaretPosition(carrotPosition+2);
			}else{
				textArea.setCaretPosition(carrotPosition+1);
			}

			
			
			
			
			
			
			
			
				System.out.println();
				System.out.println();
//				System.out.print("stringBuilder= " + string);
				System.out.println();
				System.out.println();

				
				
			//takes text from chat and past it into the other docs 
			//textArea.setText(text);
			//textArea.setText(text);
			//chatTextArea.setText(text);
			try {
				toServer.writeObject(ServerCommand.DOC_TEXT);
						toServer.writeObject(noTagString.toString());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
//			
//			
			
			
			
			}
			try {
				toServer.writeObject(ServerCommand.DOC_TEXT);
						toServer.writeObject(textArea.getText());
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
