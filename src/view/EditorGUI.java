
package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.Timer;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.StyledEditorKit.FontFamilyAction;
import javax.swing.text.StyledEditorKit.ForegroundAction;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import model.ClientRequest;
import model.ServerResponse;
import net.atlanticbb.tantlinger.shef.HTMLEditorPane;
import net.atlanticbb.tantlinger.ui.text.WysiwygHTMLEditorKit;

@SuppressWarnings("serial")
public class EditorGUI extends JFrame {
	private double screenWidth;
	private double screenHeight;
	private ObjectOutputStream toServer;
	private ObjectInputStream fromServer;
	private JEditorPane textArea;
	private HTMLEditorKit editor;
	private int size = 12;
	private JTextArea chatTextArea;
	private JScrollPane scroll, chatScroll;
	private JPanel screenPanel, rightPanel;
	private JButton openChatButton;
	private JTextField chatText;
	private JComboBox<Integer> font;
	private JComboBox<String> fontStyle;
	private HTMLEditorPane secondEditor;
	private JMenuBar toolBar;
	private JToggleButton boldButton, italicsButton, underlineButton, colorFont;
	private boolean bold, underline, italic;
	private HTMLDocument doc;
	private Color color = Color.WHITE;
	private String style = "";
	private Action boldAction = new HTMLEditorKit.BoldAction();
	private Action italicsAction = new HTMLEditorKit.ItalicAction();
	private Action underlineAction = new HTMLEditorKit.UnderlineAction();
	private Action ColorAction = new StyledEditorKit.ForegroundAction("colorButtonListener", color);
	private Action fontSizeAction = new StyledEditorKit.FontSizeAction("fontSizeAction", size);
	private Action fontStyleAction = new FontFamilyAction("fontStyleAction", style);
	private Timer timer = new Timer(2000, new TimerListener());
	private StringBuilder chatString = new StringBuilder();

	// private Boolean logInSuccess = false;
	public EditorGUI() {
		this.fromServer = fromServer;
		this.toServer = toServer;
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
		
		// instantiate timer with 2000 ms == 2 seconds. 
		timer = new Timer(2000, new TimerListener());

		// ServerListener serverListener = new ServerListener();
		// serverListener.start();

	}

	public EditorGUI(ObjectInputStream fromServer, ObjectOutputStream toServer) {
		this.fromServer = fromServer;
		this.toServer = toServer;
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
		ServerListener serverListener = new ServerListener();
		serverListener.start();
	}

	public void layoutGUI() {
		int windowWidth = (int) (screenWidth * 0.75);

		// Chat Bar
		rightPanel = new JPanel(new BorderLayout());
		rightPanel.setPreferredSize(new Dimension(400, 300));
		rightPanel.setMinimumSize(new Dimension(400, 300));
		GridBagConstraints chatConstraints = new GridBagConstraints();
		chatConstraints.anchor = GridBagConstraints.SOUTHEAST;
		chatConstraints.gridx = 2;
		chatConstraints.gridy = 3;
		chatConstraints.gridheight = 1;
		chatConstraints.weightx = 1;

		// Button to begin chat
		openChatButton = new JButton("Open Chat!");
		openChatButton.addActionListener(new chatButtonListener());
		// openChatButton.addActionListener(new chatButtonListener());
		// //uncommit with rest of code
		rightPanel.add(openChatButton, BorderLayout.SOUTH);
		chatText = new JTextField();
		chatText.addActionListener(new newTextListener()); // uncommint with
		// rest of code
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
		secondEditor = new HTMLEditorPane();
		screenPanel.add(secondEditor);

		// Size of the textArea
		int textWidth = (int) (screenWidth * .5);
		secondEditor.setPreferredSize(new Dimension(textWidth + 500, 2000));
		secondEditor.addKeyListener(new characterListener());

		editor = new HTMLEditorKit();
		// doc= new HTMLDocument();
		// Create textArea To write on
		textArea = new JEditorPane();
		textArea.addKeyListener(new characterListener());
		// editor.install(textArea);
		//
		// textArea.setEditorKitForContentType("text/html", new
		// WysiwygHTMLEditorKit());
		// textArea.setContentType("text/html");
		// textArea.setEditorKit(editor);

		// textArea.setDocument(doc);
		textArea.setPreferredSize(new Dimension(textWidth + 500, 2000));
		// textArea.setLineWrap(true);

		// code
		// Create ScrollPane to put textAreaon
		scroll = new JScrollPane(textArea);
		scroll.setPreferredSize(new Dimension(textWidth - 30, (int) (screenHeight * 0.9)));

		/*
		 * SHef inserts
		 */
		// button group toolbar
		toolBar = new JMenuBar();
		toolBar.setPreferredSize(new Dimension(windowWidth, 20));

		toolBar.add(secondEditor.getEditMenu());
		toolBar.add(secondEditor.getFormatMenu());
		toolBar.add(secondEditor.getInsertMenu());

		// boldButton = new JToggleButton("Bold");
		// italicsButton = new JToggleButton("Italics");
		// underlineButton = new JToggleButton("Underline");
		// colorFont = new JToggleButton("Change Colors");
		// // highlight.addActionListener(new colorButtonListener());
		// fontStyle = new JComboBox<String>();
		// fontStyle.addItem(Font.SERIF);
		// fontStyle.addItem(Font.SANS_SERIF);
		// fontStyle.addItem(Font.MONOSPACED);
		// fontStyle.addItem(Font.DIALOG);
		//
		font = new JComboBox<Integer>();
		font.addItem(8);
		font.addItem(9);
		font.addItem(10);
		font.addItem(11);
		font.addItem(12);
		font.addItem(14);
		font.addItem(16);
		font.addItem(18);
		font.addItem(20);
		font.addItem(22);
		font.addItem(24);
		font.addItem(28);
		font.addItem(36);
		font.addItem(48);
		font.addItem(72);
		// font.setSelectedIndex(5);
		// font.setSize(80, 10);
		// toolBar.add(boldButton);
		// toolBar.add(italicsButton);
		// toolBar.add(underlineButton);
		// toolBar.add(new JLabel("Font Size:"));
		// toolBar.add(font);
		// toolBar.add(new JLabel("Font"));
		// toolBar.add(fontStyle);
		// toolBar.add(colorFont);
		// // Set listener
		// boldButton.addActionListener(new boldButtonListener());
		// underlineButton.addActionListener(new underLineButtonListener());
		// italicsButton.addActionListener(new italicsButtonListener());
		font.addItemListener(new selectSizeListener());
		// fontStyle.addItemListener(new selectStyleListener());
		//
		// // set tool bar layout and location
		// GridBagConstraints toolbarConstraint = new GridBagConstraints();
		// toolbarConstraint.anchor = GridBagConstraints.NORTHWEST;
		// toolbarConstraint.gridx = 0;
		// toolbarConstraint.gridy = 0;
		// toolbarConstraint.fill = GridBagConstraints.EAST;
		// toolbarConstraint.gridheight = 1;
		// toolbarConstraint.gridwidth = 3;
		// toolbarConstraint.weighty = 0;
		// toolbarConstraint.weightx = 0;
		// this.add(toolBar, toolbarConstraint);
		this.setJMenuBar(toolBar);
		textArea.addKeyListener(new characterListener()); 

		fontSizeAction.setEnabled(true);
		// Adds center Panel with text to Jframe
		screenPanel.setVisible(true);
		screenPanel.add(scroll);
		// this.add(screenPanel, c);
		// textArea.add(secondEditor);
		this.add(secondEditor, c);
		this.setVisible(true);
		//
		// // Starts buttons
		// colorFont.addActionListener(new colorButtonListener());
		// // textArea.addMouseMotionListener(new mousemotionListener());
		// this.addMouseListener(new clickListener());
		// boldAction.setEnabled(false);
		// italicsAction.setEnabled(false);
		// underlineAction.setEnabled(false);
	}


	private class selectSizeListener implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent e) {
			size = (int) e.getItem();
			font.addActionListener(new StyledEditorKit.FontSizeAction("action", size) {
				public void actionPerformed(ActionEvent e) {
					new StyledEditorKit.FontSizeAction("Action", size).actionPerformed(e);
				}
			});
			fontSizeAction.setEnabled(true);
		}

	}

	private class selectStyleListener implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent e) {
			style = (String) e.getItem();
			fontStyle.addActionListener(new FontFamilyAction("action", style) {
				public void actionPerformed(ActionEvent e) {
					new StyledEditorKit.FontFamilyAction("Action", style).actionPerformed(e);
				}
			});
			fontStyleAction.setEnabled(true);
		}

	}

	private class colorButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			color = JColorChooser.showDialog(null, "Choose a Color", color);

			colorFont.addActionListener(new ForegroundAction("action", color) {
				public void actionPerformed(ActionEvent e) {
					new StyledEditorKit.ForegroundAction("Action", color).actionPerformed(e);
				}
			});
			ColorAction.setEnabled(true);
		}
	}

	// private class mousemotionListener implements MouseMotionListener{
	//
	// @Override
	// public void mouseDragged(MouseEvent e) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void mouseMoved(MouseEvent e) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// }
	// private class clickListener implements MouseListener {
	//
	// @Override
	// public void mouseClicked(MouseEvent e) {
	//
	// }
	//
	// @Override
	// public void mousePressed(MouseEvent e) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void mouseReleased(MouseEvent e) {
	// // TODO Auto-generated method stub

	// }
	//
	// @Override
	// public void mouseEntered(MouseEvent e) {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void mouseExited(MouseEvent e) {
	// // TODO Auto-generated method stub
	// // System.out.print(boldAction.isEnabled());
	// if (boldAction.isEnabled()) {
	// boldButton.setSelected(true);
	// } else {
	// boldButton.setSelected(false);
	// }
	// if (italicsAction.isEnabled()) {
	// italicsButton.setSelected(true);
	// } else {
	// italicsButton.setSelected(false);
	// }
	// if (underlineAction.isEnabled()) {
	// underlineButton.setSelected(true);
	// } else {
	// underlineButton.setSelected(false);
	//
	// }
	// }
	//
	// }
	//
	// private class boldButtonListener implements ActionListener {
	//
	// @Override
	// public void actionPerformed(ActionEvent e) {
	// // TODO Auto-generated method stub
	// boldAction.actionPerformed(e);
	// bold = !bold;
	// System.out.print(bold);
	//
	// if (bold) {
	// boldAction.setEnabled(true);
	// } else {
	// boldAction.setEnabled(false);
	// }
	//
	// }
	//
	// }
	//
	// private class underLineButtonListener implements ActionListener {
	//
	// @Override
	// public void actionPerformed(ActionEvent e) {
	// // TODO Auto-generated method stub
	// underlineAction.actionPerformed(e);
	// underline = !underline;
	// if (underline) {
	// underlineAction.setEnabled(true);
	// } else {
	// underlineAction.setEnabled(false);
	// }
	//
	// }
	//
	// }
	//
	// private class italicsButtonListener implements ActionListener {
	//
	// @Override
	// public void actionPerformed(ActionEvent e) {
	// // TODO Auto-generated method stub
	// italic = !italic;
	// italicsAction.actionPerformed(e);
	// if (italic) {
	// italicsAction.setEnabled(true);
	// } else {
	// italicsAction.setEnabled(false);
	// }
	// }
	//
	// }

	// uncommit for server
	
	private class textUpdateListener implements DocumentListener {

		@Override
		public void insertUpdate(DocumentEvent e) {
			startTimer();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			startTimer();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			startTimer();
		}

		private void startTimer() {
			// initiate a new timer for indicating revisions to be saved in the server
			if (timer.isRunning()) {
				timer.restart();
				System.out.println("timer restarted");
			} else {
				timer = new Timer(2000, new TimerListener());
				timer.start();
				System.out.println("timer started");
			}
		}
		
	}

	private class characterListener implements KeyListener {
		// ascii-48-126
		@Override
		public void keyTyped(KeyEvent e) {

		}

		@Override
		public void keyPressed(KeyEvent e) {
			System.out.println("helelkej");
			try {
				toServer.writeObject(ClientRequest.DOC_TEXT);
				toServer.writeObject(textArea.getText());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {

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
					ServerResponse whatToUpdate = (ServerResponse) fromServer.readObject();
					String updatedText = (String) fromServer.readObject();
					if (whatToUpdate == ServerResponse.DOCUMENT_UPDATE) {
						updatedoc(updatedText);
					} else {
						updatechat(updatedText);
					}
					textArea.setText(updatedText);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void updatedoc(String text) {
		textArea.setText(text);
	}

	public void updatechat(String text) {
		chatString.append("/n" + text);
		chatTextArea.setText(chatString.toString());
	}

	// // chatbox listener to send text to collaborators
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

			try {
				toServer.writeObject(ClientRequest.CHAT_MSG);
				toServer.writeObject(text);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	// 
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
			EditorGUI.this.setVisible(true);
		}
	}
	
	private class TimerListener implements ActionListener {

		@Override
		// whenever the user has paused for two seconds, save a revision
		// then stop the timer so it doesn't repeat revision requests
		public void actionPerformed(ActionEvent e) {
			try {
				toServer.writeObject(ClientRequest.SAVE_REVISION);
				System.out.println("success");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			// stop timer regardless of server communication success
			} finally {
				timer.stop();
			}
		}
		
	}

	// testing
	public static void main(String[] args) {
		EditorGUI jake = new EditorGUI();
	}
}
