
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
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

@SuppressWarnings("serial")
public class ClientGUI extends JFrame  {
	
	private double screenWidth;
	private double screenHeight;

	// Java Swing Components
	private JTextArea textArea, chatTextArea;
	private JScrollPane scroll, chatScroll;
	private JPanel screenPanel,leftPanel, rightPanel;
	private JButton openChatButton;
	private JTextField chatText;
	private JToolBar  toolBar;
	private JToggleButton boldButton, italicsButton;
	
	public ClientGUI() {

		// get screen size for proportional gui elements
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		System.out.println(screensize);
		screenWidth = screensize.getWidth() * 0.8;
		screenHeight = screensize.getHeight() * 0.8;
		this.setSize((int)screenWidth, (int)screenHeight);

		// set defaults and layoutGUI
		this.setTitle("Collaborative Text Editor");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLayout(new GridBagLayout());
		layoutGUI();
		this.setVisible(true);
	}
	public void layoutGUI() {
		int windowWidth 						= (int)(screenWidth * 0.75);
		int windowHeight						= (int)(screenHeight * 0.75);
		Dimension windowSize 			= new Dimension(windowWidth, windowHeight);
		Dimension sideBar						= new Dimension((int)(screenWidth - windowWidth*0.5),
																						 (int)((screenHeight-windowHeight)*0.5));
		
		// button group toolbar
		toolBar				= new JToolBar();
		boldButton 		= new JToggleButton("Bold");
		italicsButton	= new JToggleButton("Italics");
		toolBar.add(boldButton);
		toolBar.add(italicsButton);
		
		// set tool bar layout and location
		GridBagConstraints toolbarConstraint = new GridBagConstraints();
		toolbarConstraint.anchor 		= GridBagConstraints.NORTHWEST;
		toolbarConstraint.gridx 			= 0;
		toolbarConstraint.gridy 			= 0;
		toolbarConstraint.fill 				= GridBagConstraints.NONE;
		toolbarConstraint.gridheight	= 1;
		toolbarConstraint.gridwidth	= 2;
		toolbarConstraint.weighty 		= 0;
		toolbarConstraint.weightx 		= 0;
		this.add(toolBar, toolbarConstraint);
	
		// Chat Bar		
		rightPanel = new JPanel(new BorderLayout());
		rightPanel.setPreferredSize(new Dimension(400,300));
		rightPanel.setMinimumSize(new Dimension(400,300));
		rightPanel.setBackground(Color.BLUE);
		GridBagConstraints chatConstraints = new GridBagConstraints();
		chatConstraints.anchor 			= GridBagConstraints.SOUTHEAST;
		chatConstraints.gridx 				= 2;
		chatConstraints.gridy 				= 3;
		chatConstraints.gridheight		= 1;
		chatConstraints.weightx			= 1;

		// Button to begin chat
		openChatButton = new JButton("Open Chat!");
		openChatButton.addActionListener(new chatButtonListener());
		rightPanel.add(openChatButton, BorderLayout.SOUTH);
		chatText = new JTextField();
		chatText.addActionListener(new newTextListener());
		chatText.setMaximumSize(new Dimension(100,10));
		chatText.setVisible(false);
		
		//Here I need to make the chatText a chat TextArea with wrap around...
		
		
		
		
		rightPanel.add(chatText, BorderLayout.CENTER);

		// Create textArea To write on
		chatTextArea = new JTextArea();
		chatTextArea.setPreferredSize(new Dimension(100,2000));
		chatTextArea.setLineWrap(true);
		chatTextArea.setEditable(false);

		// Create ScrollPane to put textAreaon
		chatScroll = new JScrollPane(chatTextArea);
		chatScroll.setPreferredSize(new Dimension(100, 250));
		chatScroll.setMinimumSize(new Dimension(100,175));
		rightPanel.add(chatScroll, BorderLayout.NORTH);
		chatScroll.setVisible(false);
		this.add(rightPanel, chatConstraints);
		GridBagConstraints c = new GridBagConstraints();

		
		// in the Center set the Text Area
		c.gridx			= 1;
		c.gridy			= 1;
		c.gridheight	= 3;
		c.gridwidth	= 1;
		c.weightx		= 0;
		c.weighty 	= 0.5;
		c.anchor		= GridBagConstraints.FIRST_LINE_END;
		c.fill				= GridBagConstraints.FIRST_LINE_END;

		// Center Panel to put Text Area and JScrollPane one
		screenPanel = new JPanel();
		screenPanel.setPreferredSize(windowSize);

		// screenPanel.setPreferredSize(new Dimension(540,620));
		screenPanel.setMinimumSize(new Dimension((int)(screenWidth*0.7),(int)(screenHeight*0.9)));
		screenPanel.setBackground(Color.GREEN);

		// Size of the textArea
		int textWidth 	= (int) (screenWidth*.5);
		windowSize		= new Dimension((int)textWidth+30, (int)screenHeight);
	
		// Create textArea To write on
		textArea = new JTextArea();
		textArea.setPreferredSize(new Dimension(textWidth+500,2000));
		textArea.setLineWrap(true);

		//Create ScrollPane to put textAreaon
		scroll = new JScrollPane(textArea);
		scroll.setPreferredSize(new Dimension(textWidth-30, (int)(screenHeight*0.9)));
		
		//Adds center Panel with text to Jframe
		screenPanel.setVisible(true);
		screenPanel.add(scroll);
		this.add(screenPanel,c);
		this.setVisible(true);

	}
	private class newTextListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO 8: When the enter button is pressed, send the contents of the
			// JTextField to the server (add the username for extra style!)
			String text;
			text=chatText.getText();
			chatText.setText("");
			if(chatTextArea.getText().equals("")){
			chatTextArea.setText(text);
			}else{
				text= chatTextArea.getText()+ "\n"+ text;
				chatTextArea.setText(text);
			}
//			try {
//				oos.writeObject(username+ ": "+ s);
//			} catch (IOException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
		}
	}
	private class chatButtonListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			if(chatScroll.isVisible()){
				chatScroll.setVisible(false);
				chatText.setVisible(false);
				openChatButton.setName("Close Chat");
			}else{
				chatScroll.setVisible(true);
				chatText.setVisible(true);
				openChatButton.setName("Open Chat");
			}
			ClientGUI.this.setVisible(true);	
		}
		
	}

	// testing
	public static void main(String[] args) {
		new ClientGUI();
	}

}