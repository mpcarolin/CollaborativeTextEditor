package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class ClientGUI extends JFrame  {
	
	// Java Swing Components
	JTextArea textArea;
	
	
	public ClientGUI() {

		// get screen size for proportional gui elements
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		double screenWidth = screensize.getWidth() * (3/4);
		double screenHeight = screensize.getHeight() * (3/4);
		Dimension windowSize = new Dimension((int)screenWidth, (int)screenHeight);
		this.setSize(windowSize);

		this.setTitle("Collaborative Text Editor");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLayout(new BorderLayout());

		layoutGUI();
		this.setVisible(true);
	}
	
	public void layoutGUI() {

		// document viewer
		textArea = new JTextArea();
		this.add(textArea, BorderLayout.CENTER);
		
	}
	
	// testing
	public static void main(String[] args) {
		new ClientGUI();
	}

}
