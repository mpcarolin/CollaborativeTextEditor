package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.JToolBar;

@SuppressWarnings("serial")
public class ClientGUIBorder extends JFrame  {
	
	private double screenWidth;
	private double screenHeight;

	
	// Java Swing Components
	private JEditorPane textPane;
	private JToolBar toolbar;
	private JCheckBoxMenuItem boldButton;
	private JCheckBoxMenuItem italicsButton;
	
	// images
	
	
	
	
	public ClientGUIBorder() {

		// get screen size for proportional gui elements
		Dimension screensize 		= Toolkit.getDefaultToolkit().getScreenSize();
		screenWidth 						= screensize.getWidth(); 
		screenHeight 						= screensize.getHeight();
		int windowWidth 				= (int)(screenWidth*0.8);
		int windowHeight 				= (int)(screenHeight*0.85);
		this.setSize(windowWidth, windowHeight);

		// set defaults and layout GUI
		this.setTitle("Collaborative Text Editor");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		BorderLayout layout = new BorderLayout();
		layout.setHgap(50);
		this.setLayout(layout);
		layoutGUI();

		this.setVisible(true);
	}
	
	public void layoutGUI() {
		
		
		// tool bar and associated buttons
		toolbar 						= new JToolBar();
		boldButton 					= new JCheckBoxMenuItem("Bold");
		italicsButton 				= new JCheckBoxMenuItem("Italicize");
		toolbar.add(boldButton);
		toolbar.add(italicsButton);
		this.add(toolbar, BorderLayout.NORTH);
		

		// document viewer
		textPane 						= new JEditorPane();
		int textWidth 			= (int)(screenWidth*0.5);
		int textHeight			= (int)(screenHeight*0.5);
		Dimension docSize 	= new Dimension(textWidth, textHeight);
		textPane.setPreferredSize(docSize);
		this.add(textPane, BorderLayout.CENTER);
		
		// margin elements
		Dimension margins = new Dimension((int)(screenWidth*0.1), (int)(screenHeight*(0.1)));
		JTextArea chatBox 	= new JTextArea("Chat here");
		JPanel chatPanel 		= new JPanel();
		JLabel collaborators	= new JLabel("chat history here");
		JLabel docOutline		= new JLabel("Show editable docs here");

		chatBox.setPreferredSize(margins);
		chatPanel.setLayout(new BorderLayout());
		chatPanel.setSize((new Dimension((int)(screenWidth*0.10), (int)screenHeight)));
		chatPanel.add(chatBox, BorderLayout.SOUTH);
		chatPanel.add(collaborators, BorderLayout.CENTER);
		this.add(chatPanel, BorderLayout.EAST);
		
		docOutline.setPreferredSize(margins);
		this.add(docOutline, BorderLayout.WEST);

	}
	
	// testing
	public static void main(String[] args) {
		new ClientGUIBorder();
	}

}
