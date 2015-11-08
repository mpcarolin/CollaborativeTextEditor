
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
import javax.swing.JToolBar;

@SuppressWarnings("serial")
public class ClientGUI extends JFrame  {
	
	// Java Swing Components
	private JTextArea textArea, chatTextArea;
	private JScrollPane scroll, chatScroll;
	private JPanel screenPanel,leftPanel, rightPanel;
	private JButton openChat;
	private JTextField chatText;
	private JToolBar   toolBar;
	
	public ClientGUI() {

		// get screen size for proportional gui elements
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		System.out.println(screensize);
		double screenWidth = screensize.getWidth() * (.8);
		double screenHeight = screensize.getHeight() * (.8);
		Dimension windowSize = new Dimension((int)screenWidth, (int)screenHeight);
		this.setSize(windowSize);

		this.setTitle("Collaborative Text Editor");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLayout(new GridBagLayout());
		layoutGUI();
		this.setVisible(true);
	}
	public void layoutGUI() {
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		double screenWidth = screensize.getWidth() * (.75);
		double screenHeight = screensize.getHeight() * (.75);
		Dimension windowSize = new Dimension((int)screenWidth, (int)screenHeight);
		Dimension sideBar= new Dimension((int)((screensize.getWidth()-screenWidth)*.5),(int)((screensize.getHeight()-screenHeight)*.5));
		System.out.print(sideBar+ " " +windowSize);
		

	
//ChatBar		
		rightPanel= new JPanel(new BorderLayout());
		rightPanel.setPreferredSize(new Dimension(400,300));
		//rightPanel.setPreferredSize(new Dimension(400,300));
		rightPanel.setMinimumSize(new Dimension(400,300));
		rightPanel.setBackground(Color.BLUE);
		GridBagConstraints c= new GridBagConstraints();
		c.gridx=2;
		c.gridy=2;
		c.anchor=GridBagConstraints.PAGE_END;
		c.gridheight=1;
		c.weightx=1;
		//Creates button to write on
		openChat= new JButton("Open Chat!");
		openChat.addActionListener(new chatButtonListener());
		rightPanel.add(openChat, BorderLayout.SOUTH);
		chatText= new JTextField();
		chatText.addActionListener(new newTextListener());
		chatText.setMaximumSize(new Dimension(100,10));
		chatText.setVisible(false);
		
		//Here I need to make the chatText a chat TextArea with wrap around...
		
		
		
		
		rightPanel.add(chatText, BorderLayout.CENTER);
		//Create textArea To write on
		chatTextArea= new JTextArea();
		chatTextArea.setPreferredSize(new Dimension(100,2000));
		chatTextArea.setLineWrap(true);
		chatTextArea.setEditable(false);
		//Create ScrollPane to put textAreaon
		chatScroll = new JScrollPane(chatTextArea);
		chatScroll.setPreferredSize(new Dimension(100, 250));
		chatScroll.setMinimumSize(new Dimension(100,175));
		rightPanel.add(chatScroll, BorderLayout.NORTH);
		chatScroll.setVisible(false);
		this.add(rightPanel,c);

		
		// in the Center set the Text Area
		GridBagConstraints center= new GridBagConstraints();
		c.gridx=1;
		c.gridy=0;
		c.gridheight=3;
		c.gridwidth=1;
		c.weightx=0;
		//center.fill=center.HORIZONTAL;
		//center.weightx=1;
		//center.weighty=1;
		// Center Panel to put Text Area and JScrollPane one
		screenPanel= new JPanel();
		screenPanel.setPreferredSize(windowSize);
		//screenPanel.setPreferredSize(new Dimension(540,620));
		screenPanel.setMinimumSize(new Dimension(540,620));
		screenPanel.setBackground(Color.GREEN);
		//Size of the textArea
		int textWidth= (int) (screenWidth*.5);
		windowSize= new Dimension((int)textWidth+30, (int)screenHeight);
		//Create textArea To write on
		textArea= new JTextArea();
		textArea.setPreferredSize(new Dimension(textWidth+500,2000));
		textArea.setLineWrap(true);
		//Create ScrollPane to put textAreaon
		scroll = new JScrollPane(textArea);
		scroll.setPreferredSize(new Dimension(textWidth-30, 600));
		
		//Adds center Panel with text to Jframe
		screenPanel.setVisible(true);
		screenPanel.add(scroll);
		this.add(screenPanel,c);
		
		
//		
//		leftPanel= new JPanel();
//		leftPanel.setPreferredSize(sideBar);
//		leftPanel.setMinimumSize(sideBar);
//
//		leftPanel.setBackground(Color.MAGENTA);
//		c.gridx=0;
//		c.gridy=0;
//		c.gridheight=3;
//		
//		this.add(leftPanel,c);
//		
//		
		
		
		
		//adds left Panel to JFrame
		//leftPanel.setVisible(true);
		//Adds right Panel to JFrame
		rightPanel.setVisible(true);
		//Sets frames min size and sets the GridBagConstraints
		//this.setMinimumSize(new Dimension(textWidth+30,0));
		//this.add(leftPanel,left);
		//this.add(rightPanel, right);
		//this.add(screenPanel,center);
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
				openChat.setName("Close Chat");
			}else{
				chatScroll.setVisible(true);
				chatText.setVisible(true);
				openChat.setName("Open Chat");
			}
			ClientGUI.this.setVisible(true);	
		}
		
	}
	// testing
	public static void main(String[] args) {
		new ClientGUI();
	}

}