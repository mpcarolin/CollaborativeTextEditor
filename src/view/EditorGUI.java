package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.StyledEditorKit.FontFamilyAction;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import model.ClientRequest;
import model.ServerResponse;

@SuppressWarnings("serial")
public class EditorGUI extends JFrame {

	private double screenWidth;
	private double screenHeight;
	private int carrotPosition;

	// server and client instances
	private ObjectOutputStream toServer;
	private ObjectInputStream fromServer;

	// Editor components
	private JTextPane textArea;
	private HTMLEditorKit editor;
	private int currentFontSize = 12;
	private JTextArea chatTextArea;
	private JScrollPane scroll, chatScroll;
	private JPanel screenPanel, rightPanel;
	private JButton openChatButton;
	private JToggleButton centerAlign, rightAlign, leftAlign;
	private JTextField chatText;
	private JComboBox<Integer> font;
	private JComboBox<String> fontStyle;

	// file dropdown menu items
	private JMenu revisionListMenu;
	private JPopupMenu revisionPop;

	
	// menu items
	private JMenuBar toolBar;
	private JMenu file;
	private JToggleButton boldButton, italicsButton, underlineButton;
	private JButton colorFont;
	private Color color = Color.BLACK;
	private DocumentListener doclistener;
	private JButton linkButton;
	private JToggleButton editButton;
	private String style = "";
	private int align = 0;

	// document gui that called this class
	private DocumentSelectGUI documentGUI;
	
	// Editor actions
	private boolean bold, underline, italic;
	private Action boldAction = new HTMLEditorKit.BoldAction();
	private Action italicsAction = new HTMLEditorKit.ItalicAction();
	private Action underlineAction = new HTMLEditorKit.UnderlineAction();
	private Action ColorAction = new StyledEditorKit.ForegroundAction("colorButtonListener", color);
	private Action fontSizeAction = new StyledEditorKit.FontSizeAction("fontSizeAction", currentFontSize);
	private Action bulletAction = new HTMLEditorKit.InsertHTMLTextAction("", "<ul><li></li></ul>", HTML.Tag.BODY,
			HTML.Tag.UL);
	private Action hyperLinkAction = new HTMLEditorKit.InsertHTMLTextAction("", "<a>link</a>", HTML.Tag.BODY,
			HTML.Tag.UL);

	private Timer timer = new Timer(2000, new TimerListener());
	private Action fontStyleAction = new StyledEditorKit.FontFamilyAction("fontStyleAction", style);
	private Action alignmentAction = new StyledEditorKit.AlignmentAction("alignmentAction", align);
	private StringBuilder chatString = new StringBuilder();

	public EditorGUI() {
		// get screen size for proportional gui elements
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		screenWidth = screensize.getWidth() * 0.8;
		screenHeight = screensize.getHeight() * 0.8;
		this.setSize((int) screenWidth, (int) screenHeight);

		// set defaults and layoutGUI
		this.setTitle("Collaborative Text Editor");
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		this.setLayout(new GridBagLayout());
		layoutGUI();
		this.setVisible(true);

		// instantiate timer with 2000 ms == 2 seconds.
		timer = new Timer(2000, new TimerListener());
	}

	public EditorGUI(ObjectInputStream fromServer, ObjectOutputStream toServer, DocumentSelectGUI documentgui, String startingText) {
		this.documentGUI = documentgui;
		documentGUI.setVisible(false);
		this.fromServer = fromServer;
		this.toServer = toServer;
		// get screen size for proportional gui elements
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		screenWidth = screensize.getWidth() * 0.75;
		screenHeight = screensize.getHeight() * 0.8;
		this.setSize((int) screenWidth - 100, (int) screenHeight);
		// set defaults and layoutGUI
		this.setTitle("Collaborative Text Editor");
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.addWindowListener(new windowListener());
		this.setLayout(new GridBagLayout());
		layoutGUI();
		this.setVisible(true);
		/*
		try {
			//String document = (String) fromServer.readObject();
			//textArea.setText(document);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
		textArea.setText(startingText);
		ServerListener serverListener = new ServerListener();
		serverListener.start();

		// instantiate timer with 2000 ms == 2 seconds.
		// timer = new Timer(2000, new TimerListener());

	}

	public void layoutGUI() {
		int windowWidth = (int) (screenWidth * 0.75);

		// Chat Bar
		rightPanel = new JPanel(new BorderLayout());
		rightPanel.setPreferredSize(new Dimension(400, 300));
		rightPanel.setMinimumSize(new Dimension(400, 300));
		GridBagConstraints chatConstraints = new GridBagConstraints();
		chatConstraints.anchor = GridBagConstraints.SOUTHEAST;
		chatConstraints.gridx = 1;
		chatConstraints.gridy = 3;
		chatConstraints.gridheight = 1;
		chatConstraints.weightx = 1;

		// Button to begin chat
		openChatButton = new JButton("Open Chat!");
		openChatButton.addActionListener(new chatButtonListener());
		// openChatButton.addActionListener(new chatButtonListener());
		// uncommit with rest of code
		rightPanel.add(openChatButton, BorderLayout.SOUTH);
		chatText = new JTextField();
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
		c.gridx = 0;
		c.gridy = 1;
		c.gridheight = 3;
		c.gridwidth = 1;
		c.weightx = 0;
		c.weighty = 0.5;
		// c.anchor = GridBagConstraints.CENTER;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.VERTICAL;

		// Center Panel to put Text Area and JScrollPane one
		screenPanel = new JPanel();
		screenPanel.setPreferredSize(new Dimension((int) (screenWidth * .6), 1500));
		screenPanel.setMinimumSize(new Dimension((int) (screenWidth * 0.6), 1));
		// Size of the textArea
		int textWidth = (int) (screenWidth * .5);
		editor = new HTMLEditorKit();

		// Create textArea To write on
		textArea = new JTextPane();
		textArea.setContentType("text/html");
		textArea.setEditorKit(editor);
		textArea.setPreferredSize(new Dimension(textWidth + 500, 2000));

		// Create ScrollPane to put textAreaon
		scroll = new JScrollPane(textArea);
		scroll.setPreferredSize(new Dimension(textWidth - 30, (int) (screenHeight * 0.9)));

		// button group toolbar
		toolBar = new JMenuBar();
		toolBar.setPreferredSize(new Dimension(windowWidth - 300, 20));

		Image boldImage;
		ImageIcon boldImageIcon = new ImageIcon();
		Image italicImage;
		ImageIcon italicImageIcon = new ImageIcon();
		Image underlineImage;
		ImageIcon underlineImageIcon = new ImageIcon();
		Image leftAlignImage;
		ImageIcon leftAlignIcon = new ImageIcon();
		Image centerAlignImage;
		ImageIcon centerAlignIcon = new ImageIcon();
		Image rightAlignImage;
		ImageIcon rightAlignIcon = new ImageIcon();
		Image colorImage;
		ImageIcon colorIcon = new ImageIcon();
		Image bulletImage;
		ImageIcon bulletIcon = new ImageIcon();
		try {
			boldImage = ImageIO.read(new File("./images/bold.png"));
			boldImageIcon = new ImageIcon(boldImage);
			italicImage = ImageIO.read(new File("./images/italic.png"));
			italicImageIcon = new ImageIcon(italicImage);
			underlineImage = ImageIO.read(new File("./images/underline.png"));
			underlineImageIcon = new ImageIcon(underlineImage);
			leftAlignImage = ImageIO.read(new File("./images/al_left.png"));
			leftAlignIcon = new ImageIcon(leftAlignImage);
			centerAlignImage = ImageIO.read(new File("./images/al_center.png"));
			centerAlignIcon = new ImageIcon(centerAlignImage);
			rightAlignImage = ImageIO.read(new File("./images/al_right.png"));
			rightAlignIcon = new ImageIcon(rightAlignImage);
			colorImage = ImageIO.read(new File("./images/color.png"));
			colorIcon = new ImageIcon(colorImage);
			bulletImage = ImageIO.read(new File("./images/UListHK.png"));
			bulletIcon = new ImageIcon(bulletImage);
		} catch (IOException e) {
			e.printStackTrace();
		}

		leftAlign = new JToggleButton();
		leftAlign.setIcon(leftAlignIcon);
		rightAlign = new JToggleButton();
		rightAlign.setIcon(rightAlignIcon);
		centerAlign = new JToggleButton();
		centerAlign.setIcon(centerAlignIcon);

		boldButton = new JToggleButton();
		boldButton.setIcon(boldImageIcon);

		italicsButton = new JToggleButton();
		italicsButton.setIcon(italicImageIcon);

		underlineButton = new JToggleButton();
		underlineButton.setIcon(underlineImageIcon);

		colorFont = new JButton();
		colorFont.setIcon(colorIcon);

		JButton bulletItem = new JButton(bulletAction);
		bulletItem.setIcon(bulletIcon);

		linkButton = new JButton();
		linkButton.setText("Link");

		editButton = new JToggleButton("Edit");
		// textArea.insertComponent(new JButton("hello"));

		fontStyle = new JComboBox<String>();
		fontStyle.addItem(Font.SERIF);
		fontStyle.addItem(Font.SANS_SERIF);
		fontStyle.addItem(Font.MONOSPACED);
		fontStyle.addItem(Font.DIALOG);
		// fontStyle.setMinimumSize(new Dimension(50,30));
		// fontStyle.setPreferredSize(new Dimension(50,30));

		// File Menu Bar elements 
		file = new JMenu("File");
		JMenuItem fileButton = new JMenuItem("File");
		revisionListMenu = new JMenu("Load Revision");
		String[] strings = {"a", "b", "c"};

	

		

		/*
		 * Mouse listener to obtain ten revisions from server
		 */
		revisionListMenu.addMouseListener(new LoadRevisionListener());
		
		
		file.add(fileButton);
		file.add(revisionListMenu);
		JMenu edit = new JMenu("Edit");
		JMenuItem undo = new JMenuItem("Undo");
		edit.add(undo);

		toolBar.add(file);
		toolBar.add(edit);

		// this.add(file);
		font = new JComboBox<Integer>();
		int[] fontSizes = {8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 28, 36, 48, 72};
		for (int fontSize : fontSizes) {
			font.addItem(fontSize);
		}
		font.setSelectedIndex(5);
		font.setMinimumSize(new Dimension(50, 30));

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
		// this.setJMenuBar(toolBar);

		// textArea.getDocument().addDocumentListener(new docListener());
		JToolBar toolBar2 = new JToolBar();
		toolBar2.setMinimumSize(new Dimension((int) screenWidth, 15));
		toolBar2.add(new JLabel("Size:"));
		toolBar2.add(font);
		toolBar2.add(new JLabel("Fonts:"));
		toolBar2.add(fontStyle);
		toolBar2.add(boldButton);
		toolBar2.add(italicsButton);
		toolBar2.add(underlineButton);
		toolBar2.add(bulletItem);
		toolBar2.add(leftAlign);
		toolBar2.add(centerAlign);
		toolBar2.add(rightAlign);
		toolBar2.add(colorFont);
		toolBar2.add(linkButton);
		toolBar2.add(editButton);

		screenPanel.add(toolBar2);
		this.setJMenuBar(toolBar);

		fontSizeAction.setEnabled(true);
		// Adds center Panel with text to Jframe
		screenPanel.setVisible(true);
		screenPanel.add(scroll);
		this.add(screenPanel, c);
		this.setVisible(true);

		// ActionListener
		chatText.addActionListener(new newTextListener());
		textArea.addHyperlinkListener(new hyperLinkListener());
		editButton.addActionListener(new editableListener());
		textArea.addMouseListener(new mouseListener());
		linkButton.addActionListener(new linkListener());
		undo.addActionListener(new undoListener());
		leftAlign.addActionListener(new leftListener());
		rightAlign.addActionListener(new rightListener());
		centerAlign.addActionListener(new centerListener());
		boldButton.addActionListener(new boldButtonListener());
		underlineButton.addActionListener(new underLineButtonListener());
		italicsButton.addActionListener(new italicsButtonListener());
		font.addItemListener(new selectSizeListener());
		fontStyle.addItemListener(new selectStyleListener());
		// textArea.addKeyListener(new DocCharacterListener());

		doclistener = new docListener();
		textArea.getDocument().addDocumentListener(doclistener);
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

	private class windowListener implements WindowListener {
		@Override
		public void windowOpened(WindowEvent e) {
		}

		@Override
		public void windowClosing(WindowEvent e) {
			//isclosed = true;
		}

		@Override
		public void windowClosed(WindowEvent e) {

			documentGUI.setVisible(true);
			try {
				EditorGUI.this.setVisible(false);
				EditorGUI.this.setEnabled(false);
				toServer.writeObject(ClientRequest.CLOSE_DOC);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}

		@Override
		public void windowIconified(WindowEvent e) {
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
		}

		@Override
		public void windowActivated(WindowEvent e) {
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
		}
	}

	// makes the hyperlinks clickable if edit is on.
	private class editableListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (editButton.isSelected()) {
				textArea.setEditable(false);
			} else {
				textArea.setEditable(true);
			}
		}
	}

	// adds ability to have Hyperlinks
	private class linkListener implements ActionListener {
		private String website;

		@Override
		public void actionPerformed(ActionEvent e) {
			String message = textArea.getSelectedText();
			website = JOptionPane.showInputDialog("Enter URL (ex: http://www.google.com)");
			String replacement = "<a href=\\" + "\"" + website + "\\" + "\"" + ">" + message + "</a>";
			try {
				editor.insertHTML((HTMLDocument) textArea.getDocument(), carrotPosition, replacement, 0, 0, HTML.Tag.A);
				textArea.replaceSelection("");
			} catch (BadLocationException | IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private class hyperLinkListener implements HyperlinkListener {
		@Override
		public void hyperlinkUpdate(HyperlinkEvent e) {
			if (HyperlinkEvent.EventType.ACTIVATED == e.getEventType()) {
				Desktop desktop = Desktop.getDesktop();
				if (Desktop.isDesktopSupported()) {
					try {
						StringBuilder newURIBuilder = new StringBuilder(e.getDescription());
						newURIBuilder.deleteCharAt(0);
						String uriString = "";
						if (newURIBuilder.charAt(newURIBuilder.length() - 1) == '\\') {
							uriString = newURIBuilder.substring(0, newURIBuilder.length() - 1);
						}
						URI uri = new URI(uriString);
						desktop.browse(uri);
					} catch (Exception ex) {
						System.out.println("error");
						ex.printStackTrace();
					}
				}
			}
		}
	}

	private class mouseListener implements MouseListener {
		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			carrotPosition = textArea.getCaretPosition();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}
	}

	private class undoListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				toServer.writeObject(ClientRequest.UNDO);
				System.out.println("Reverted to most recent revision");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	private class rightListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			align = 2;
			alignmentAction.setEnabled(true);
			new StyledEditorKit.AlignmentAction("action", align).actionPerformed(e);
			leftAlign.setSelected(false);
			centerAlign.setSelected(false);
		}
	}

	private class leftListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			align = 0;
			alignmentAction.setEnabled(true);
			new StyledEditorKit.AlignmentAction("action", align).actionPerformed(e);
			rightAlign.setSelected(false);
			centerAlign.setSelected(false);
		}
	}

	private class centerListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			align = 1;
			alignmentAction.setEnabled(true);
			new StyledEditorKit.AlignmentAction("action", align).actionPerformed(e);
			leftAlign.setSelected(false);
			rightAlign.setSelected(false);
		}
	}

	private class selectSizeListener implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			currentFontSize = (int) e.getItem();
			font.addActionListener(new StyledEditorKit.FontSizeAction("action", currentFontSize) {
				public void actionPerformed(ActionEvent e) {
					new StyledEditorKit.FontSizeAction("Action", currentFontSize).actionPerformed(e);
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

	//
	// private class DocCharacterListener implements KeyListener {
	// // ascii-48-126
	// @Override
	// public void keyTyped(KeyEvent e) {
	// }
	//
	// @Override
	// public void keyPressed(KeyEvent e) {
	// // starts a timer waiting for a pause to send the revision command
	// startTimer();
	// }
	//
	// @Override
	// public void keyReleased(KeyEvent e) {
	// try {
	// // System.out.print("I got to the KeyListener :" +
	// // textArea.getText());
	// toServer.writeObject(ClientRequest.DOC_TEXT);
	// toServer.writeObject(textArea.getText());
	// } catch (IOException e1) {
	// e1.printStackTrace();
	// }
	//
	// }
	// }

	private class docListener implements DocumentListener {
		@Override
		public void insertUpdate(DocumentEvent e) {
			try {
				toServer.writeObject(ClientRequest.DOC_TEXT);
				toServer.writeObject(textArea.getText());
				startTimer();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			try {
				toServer.writeObject(ClientRequest.DOC_TEXT);
				toServer.writeObject(textArea.getText());
				startTimer();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			try {
				toServer.writeObject(ClientRequest.DOC_TEXT);
				toServer.writeObject(textArea.getText());
				startTimer();
				// ifTrueDontUpdate=true;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

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
			if (textArea.getCaretPosition() < Jsoup.clean(textArea.getText(), new Whitelist()).length()) {
				try {
					AttributeSet attributeSet = textArea.getCharacterAttributes();
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
					e1.printStackTrace();
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
					e1.printStackTrace();
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
					underlineButton.setSelected(false);
				}

				// Change the font to the correct value
				AttributeSet attributeSet = textArea.getCharacterAttributes();
				Object fontSize;
				fontSize = attributeSet.getAttribute(StyleConstants.FontSize);

				if (fontSize == null) {
					font.setSelectedIndex(5);
				} else {
					switch ((int) fontSize) {
					case 8:
						font.setSelectedIndex(0);
						return;
					case 9:
						font.setSelectedIndex(1);
						return;
					case 10:
						font.setSelectedIndex(2);
						return;
					case 11:
						font.setSelectedIndex(3);
						return;
					case 12:
						font.setSelectedIndex(4);
						return;
					// case 14: font.setSelectedIndex(5);
					// return;
					case 16:
						font.setSelectedIndex(6);
						return;
					case 18:
						font.setSelectedIndex(7);
						return;
					case 20:
						font.setSelectedIndex(8);
						return;
					case 22:
						font.setSelectedIndex(9);
						return;
					case 24:
						font.setSelectedIndex(10);
						return;
					case 28:
						font.setSelectedIndex(11);
						return;
					case 36:
						font.setSelectedIndex(12);
						return;
					case 48:
						font.setSelectedIndex(13);
						return;
					case 72:
						font.setSelectedIndex(14);
						return;
					}
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}
	}

	private class boldButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
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
		// initiate timer for indicating revisions to be saved in the server
		if (timer.isRunning()) {
			timer.restart();
		} else {
			timer = new Timer(2000, new TimerListener());
			timer.start();
		}
	}

	/* Listeners */
	// Server listener
	private class ServerListener extends Thread {

		private volatile boolean isRunning = true;

		@Override
		public void run() {
			while (isRunning) {
				// obtain updated doc text from server in a try-catch
				try {
					ServerResponse response = (ServerResponse) fromServer.readObject();
					System.out.println(response);
					switch (response) {
					case NO_DOCUMENT:
						JOptionPane.showMessageDialog(null, "That revision is no longer stored.");
						break;
					case DOCUMENT_UPDATE:
						String updatedText = (String) fromServer.readObject();
						EditorGUI.this.updatedoc(updatedText);
						break;
					case CHAT_UPDATE:
						String updatedChatText = (String) fromServer.readObject();
						EditorGUI.this.updatechat(updatedChatText);
						break;
					case DOCUMENT_CLOSED:
						stopRunning();
						return;
					case REVISION_LIST:
						List<String> revisionKeys = (List<String>) fromServer.readObject();
						refreshRevisionPopUp(revisionKeys);
						return;
					case DOCUMENT_REVERTED:
						String revertedText = (String) fromServer.readObject();
						System.out.println(revertedText);
						EditorGUI.this.updatedoc(revertedText);
						return;
					default:
						stopRunning();
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
					isRunning = false;
				}
			}
		}

		/*
		 *  Fill the Revision Drop-down menu with revisions,
		 *  and assign each revision item to a new listener that,
		 *  upon being clicked, requests the the server to send
		 *  back a string that represents a earlier revision 
		 */
		private void refreshRevisionPopUp(List<String> revisionKeys) {
			revisionListMenu.removeAll();

			for (String key : revisionKeys) {
				JMenuItem newKey = new JMenuItem(key);

				newKey.addMouseListener(new MouseListener() {
					@Override
					public void mousePressed(MouseEvent clickedOnKey) {
						JMenuItem currentKey = (JMenuItem)clickedOnKey.getSource();
						String revisionKey = currentKey.getText();
						
						try {
							toServer.writeObject(ClientRequest.REVERT_DOC);
							toServer.writeObject(revisionKey);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}

					public void mouseReleased(MouseEvent e) {}
					public void mouseClicked(MouseEvent e) { }
					public void mouseEntered(MouseEvent e) {}
					public void mouseExited(MouseEvent e) {}
				});

				revisionListMenu.add(newKey);
			}	
		}

		private void stopRunning() throws IOException {
			isRunning = false;
			toServer.reset();
		}
	}

	// revision
	public void updateRevisionEditorGui() {

	}

	public void updatedoc(String text) {
		// iSentThis = true;
		textArea.getDocument().removeDocumentListener(doclistener);
		textArea.setText(text);
		textArea.getDocument().addDocumentListener(doclistener);
	}

	public void updatechat(String text) {
		chatString.append("\n" + text);
		chatTextArea.setText(chatString.toString());
	}

	// // chatbox listener to send text to collaborators
	private class newTextListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {

			String text;
			text = chatText.getText();
			chatText.setText("");

			try {
				toServer.writeObject(ClientRequest.CHAT_MSG);
				toServer.writeObject(text);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
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
			EditorGUI.this.setVisible(true);
		}
	}

	private class TimerListener implements ActionListener {
		@Override
		// whenever the user has paused for two seconds, save a revision
		// then stop the timer so it doesn't repeat revision requests
		public void actionPerformed(ActionEvent e) {
			try {
				System.out.println("saved revision");
				toServer.writeObject(ClientRequest.SAVE_REVISION);
			} catch (IOException e1) {
				e1.printStackTrace();
				// stop timer regardless of server communication success
			} finally {
				timer.stop();
			}
		}
	}
	
	private class LoadRevisionListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			System.out.println(e.getSource());
		}

		@Override
		public void mousePressed(MouseEvent e) {}

		@Override
		public void mouseReleased(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {
			try {
				toServer.writeObject(ClientRequest.GET_REVISIONS);
			} catch (IOException io) {
				io.printStackTrace();
			}
		}
		@Override
		public void mouseExited(MouseEvent e) {}
		
	}
	
	private class RevisionPopUpListener implements PopupMenuListener  {

		@Override
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			try {
				toServer.writeObject(ClientRequest.GET_REVISIONS);
			} catch (IOException io) {
				io.printStackTrace();
			}
		}

		@Override
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {		}

		@Override
		public void popupMenuCanceled(PopupMenuEvent e) {
			
		}
		
	}
	
	// testing
	public static void main(String[] args) {
		EditorGUI jake = new EditorGUI();
	}
}
