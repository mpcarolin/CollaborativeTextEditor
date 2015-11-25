
package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
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
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.Timer;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.StyledEditorKit.BoldAction;
import javax.swing.text.StyledEditorKit.FontFamilyAction;
import javax.swing.text.StyledEditorKit.ForegroundAction;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.hexidec.ekit.Ekit;

import model.ClientRequest;
import model.ServerResponse;
//import net.atlanticbb.tantlinger.shef.HTMLEditorPane;
//import net.atlanticbb.tantlinger.ui.text.WysiwygHTMLEditorKit;

@SuppressWarnings("serial")
public class EditorGUI extends JFrame {
	private double screenWidth;
	private double screenHeight;
	private ObjectOutputStream toServer;
	private ObjectInputStream fromServer;
	private JTextPane textArea;
	private HTMLEditorKit editor;
	private int size = 12;
	private JTextArea chatTextArea;
	private JScrollPane scroll, chatScroll;
	private JPanel screenPanel, rightPanel;
	private JButton openChatButton;
	private JButton leftAlign, centerAlign, rightAlign;
	private JTextField chatText;
	private JComboBox<Integer> font;
	private JComboBox<String> fontStyle;
	private JMenu file;
	private JMenuBar toolBar;
	private JToggleButton boldButton, italicsButton, underlineButton, colorFont;
	private boolean bold, underline, italic;
	private Color color = Color.BLACK;
	private String style = "";
	private int align = 0;
	private Action boldAction = new HTMLEditorKit.BoldAction();
	private Action italicsAction = new HTMLEditorKit.ItalicAction();
	private Action underlineAction = new HTMLEditorKit.UnderlineAction();
	private Action ColorAction = new StyledEditorKit.ForegroundAction("colorButtonListener", color);
	private Action fontSizeAction = new StyledEditorKit.FontSizeAction("fontSizeAction", size);
	private Action bulletAction= new HTMLEditorKit.InsertHTMLTextAction("bullet", "<ul><li></li></ul>", HTML.Tag.BODY, HTML.Tag.UL);

	private Timer timer = new Timer(2000, new TimerListener());
	private Action fontStyleAction = new StyledEditorKit.FontFamilyAction("fontStyleAction", style);
	private Action alignmentAction = new StyledEditorKit.AlignmentAction("alignmentAction", align);
	private StringBuilder chatString = new StringBuilder();

	public EditorGUI() {
		Ekit gui= new Ekit();
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
		try {
			String document = (String) fromServer.readObject();
			textArea.setText(document);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ServerListener serverListener = new ServerListener();
		serverListener.start();

		// instantiate timer with 2000 ms == 2 seconds.
		timer = new Timer(2000, new TimerListener());

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
		screenPanel.setMinimumSize(new Dimension((int) (screenWidth * 0.5), 1));
		// Size of the textArea
		int textWidth = (int) (screenWidth * .5);
		editor = new HTMLEditorKit();

		// Create textArea To write on
		textArea = new JTextPane();
		textArea.setContentType("text/html");
		textArea.setEditorKit(editor);
		textArea.setPreferredSize(new Dimension(textWidth + 500, 2000));
		textArea.addKeyListener(new DocCharacterListener());

		// Create ScrollPane to put textAreaon
		scroll = new JScrollPane(textArea);
		scroll.setPreferredSize(new Dimension(textWidth - 30, (int) (screenHeight * 0.9)));

		
		// button group toolbar
		toolBar = new JMenuBar();
		toolBar.setPreferredSize(new Dimension(windowWidth - 300, 20));

		leftAlign = new JButton("left");
		rightAlign = new JButton("right");
		centerAlign = new JButton("center");
		leftAlign.addActionListener(new leftListener());
		rightAlign.addActionListener(new rightListener());
		centerAlign.addActionListener(new centerListener());

		boldButton = new JToggleButton("Bold");
		italicsButton = new JToggleButton("Italics");
		underlineButton = new JToggleButton("Underline");
		colorFont = new JToggleButton("Change Colors");
		fontStyle = new JComboBox<String>();
		fontStyle.addItem(Font.SERIF);
		fontStyle.addItem(Font.SANS_SERIF);
		fontStyle.addItem(Font.MONOSPACED);
		fontStyle.addItem(Font.DIALOG);
		// File Menu Bar
		file = new JMenu("File");
		JMenuItem fileButton = new JMenuItem("File");
		file.add(fileButton);
		JMenu edit= new JMenu("Edit");
		
		//this.add(file);
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
		font.setSelectedIndex(5);
		font.setSize(80, 10);
		toolBar.add(file);
		toolBar.add(edit);
		toolBar.add(boldButton);
		toolBar.add(italicsButton);
		toolBar.add(underlineButton);
		toolBar.add(new JLabel("Font Size:"));
		toolBar.add(font);
		toolBar.add(new JLabel("Font"));
		toolBar.add(fontStyle);
		toolBar.add(colorFont);
		toolBar.add(leftAlign);
		toolBar.add(centerAlign);
		toolBar.add(rightAlign);
		toolBar.add(new JMenuItem(bulletAction));
		// Set listener
		boldButton.addActionListener(new boldButtonListener());
		underlineButton.addActionListener(new underLineButtonListener());
		italicsButton.addActionListener(new italicsButtonListener());
		font.addItemListener(new selectSizeListener());
		fontStyle.addItemListener(new selectStyleListener());
		
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
		textArea.addKeyListener(new DocCharacterListener());

		fontSizeAction.setEnabled(true);
		// Adds center Panel with text to Jframe
		screenPanel.setVisible(true);
		screenPanel.add(scroll);
		this.add(screenPanel, c);
		this.setVisible(true);

		colorFont.addActionListener(new colorButtonListener());
		// textArea.addMouseMotionListener(new mousemotionListener());
		textArea.addMouseListener(new clickListener());
		boldAction.setEnabled(false);
		italicsAction.setEnabled(false);
		underlineAction.setEnabled(false);

		boldButton.setEnabled(false);
		italicsButton.setEnabled(false);
		underlineButton.setEnabled(false);
		font.setEditable(false);
		colorFont.setEnabled(false);
		fontStyle.setEditable(false);
		font.setEnabled(false);
		fontStyle.setEnabled(false);
	}

	private class rightListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			align = 2;
			alignmentAction.setEnabled(true);
			new StyledEditorKit.AlignmentAction("action", align).actionPerformed(e);
		}

	}
	private class leftListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			align = 0;
			alignmentAction.setEnabled(true);
			new StyledEditorKit.AlignmentAction("action", align).actionPerformed(e);
		}

	}

	private class centerListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			align = 1;
			alignmentAction.setEnabled(true);
			new StyledEditorKit.AlignmentAction("action", align).actionPerformed(e);
		}

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
			ColorAction.setEnabled(true);
			new StyledEditorKit.ForegroundAction("action", color).actionPerformed(e);
		}
	}

	private class DocCharacterListener implements KeyListener {
		// ascii-48-126
		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {
			// starts a timer waiting for a pause to send the revision command
			startTimer();
		}

		@Override
		public void keyReleased(KeyEvent e) {
			try {
				// System.out.print("I got to the KeyListener :" +
				// textArea.getText());
				toServer.writeObject(ClientRequest.DOC_TEXT);
				toServer.writeObject(textArea.getText());
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}
	}
	// private class docListener implements DocumentListener {
	//
	// @Override
	// public void insertUpdate(DocumentEvent e) {
	// // TODO Auto-generated method stub
	// startTimer();
	// //System.out.println("i got to my doc Listener:" + textArea.getText());
	// try {
	// toServer.writeObject(ClientRequest.DOC_TEXT);
	// toServer.writeObject(textArea.getText());
	// } catch (IOException e1) {
	// e1.printStackTrace();
	// }
	// }
	// @Override
	// public void removeUpdate(DocumentEvent e) {
	// // TODO Auto-generated method stub
	// startTimer();
	// }
	// @Override
	// public void changedUpdate(DocumentEvent e) {
	// // TODO Auto-generated method stub
	// startTimer();
	// }
	//
	// }

	private class clickListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			boldButton.setEnabled(true);
			italicsButton.setEnabled(true);
			underlineButton.setEnabled(true);
			font.setEditable(false);
			colorFont.setEnabled(true);
			fontStyle.setEditable(false);
			font.setEnabled(true);
			fontStyle.setEnabled(true);

		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			if (textArea.getCaretPosition() < Jsoup.clean(textArea.getText(), new Whitelist()).length()) {
				try {
					AttributeSet attributeSet = textArea.getCharacterAttributes();
					// Object bold = attributeSet == null ? null :
					// attributeSet.getAttribute(StyleConstants.Bold);
					Object bold;
					if (attributeSet == null) {
						bold = null;
					} else {
						bold = attributeSet.getAttribute(StyleConstants.Bold);
					}
					if (bold.equals(true)) {
						boldButton.setSelected(true);
					} else {
						boldButton.setSelected(false);
					}
				} catch (Exception e1) {

				}

				try {
					AttributeSet attributeSet = textArea.getCharacterAttributes();
					Object underlined;
					if (attributeSet == null) {
						underlined = null;
					} else {
						underlined = attributeSet.getAttribute(StyleConstants.Underline);
					}
					if (underlined.equals(true)) {
						underlineButton.setSelected(true);
					} else {
						underlineButton.setSelected(false);
					}
				} catch (Exception e1) {
				}
				try {
					AttributeSet attributeSet = textArea.getCharacterAttributes();
					Object italics;
					if (attributeSet == null) {
						italics = null;
					} else {
						italics = attributeSet.getAttribute(StyleConstants.Italic);
					}
					if (italics.equals(true)) {
						italicsButton.setSelected(true);
					} else {
						italicsButton.setSelected(false);
					}
				} catch (Exception e1) {
					System.out.print("hello");
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub

		}

	}

	private class boldButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			boldAction.actionPerformed(e);
			bold = !bold;
			if (textArea.getSelectedText() == null) {
				if (bold) {
					boldAction.setEnabled(true);
				} else {
					boldAction.setEnabled(false);
				}
			}
		}

	}

	private class underLineButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			underlineAction.actionPerformed(e);
			underline = !underline;
			if (textArea.getSelectedText() == null) {

				if (underline) {
					underlineAction.setEnabled(true);
				} else {
					underlineAction.setEnabled(false);
				}
			}

		}

	}

	private class italicsButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			italic = !italic;
			italicsAction.actionPerformed(e);
			if (textArea.getSelectedText() == null) {
				if (italic) {
					italicsAction.setEnabled(true);
				} else {
					italicsAction.setEnabled(false);
				}
			}
		}

	}

	private void startTimer() {
		// initiate a new timer for indicating revisions to be saved in the
		// server
		if (timer.isRunning()) {
			timer.restart();
			System.out.println("timer restarted");
		} else {
			timer = new Timer(2000, new TimerListener());
			timer.start();
			System.out.println("timer started");
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
					System.out.println("the Server Response:" + whatToUpdate);

					String updatedText = (String) fromServer.readObject();
					System.out.println("the Server Response text:" + updatedText);

					if (whatToUpdate == ServerResponse.DOCUMENT_UPDATE) {
						updatedoc(updatedText);
					} else {
						updatechat(updatedText);
					}
					System.out.println(updatedText);
					// textArea.setText(updatedText);
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

			// if (chatTextArea.getText().equals("")) {
			// chatTextArea.setText(text);
			// } else {
			// text = chatTextArea.getText() + "\n" + text;
			// chatTextArea.setText(text);
			// }

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
