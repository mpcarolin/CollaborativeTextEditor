package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import model.ClientRequest;
import model.ServerResponse;

@SuppressWarnings("serial")
public class DocumentSelectGUI extends JFrame {

    private JPanel optionPanel, docPanel, bottomHolder, thePanel, topHolder;
    private JScrollPane ownedDocPanel, editDocPanel;
    private ObjectInputStream fromServer;
    protected ObjectOutputStream toServer;
    private JTextField searchBar;
    private List<String> ownedModel, ownedEditable, userList, editingUsersList;
    private DefaultListModel<String> userListDLM, editingUserListModel, ownedDocList, editDocList;
    private JList<String> ownDisplayList, editDisplayList, userListJL, editingUsersJList;
    private JTabbedPane tabbedDocs;
    private JButton createDoc, deleteDoc, openDoc, refreshList, removeUser, addUser;
    private String userName;
    private Font font;
    private JLabel optionLabel;

    public DocumentSelectGUI(String username, ObjectInputStream fromServer, ObjectOutputStream toServer) {
	this.userName = username;
	this.fromServer = fromServer;
	this.toServer = toServer;
	instantiateLists();
	layoutGUI();
	getDisplayList();
	registerListeners();
    }

    private void instantiateLists() {
	font = new Font("default", Font.PLAIN, 13);
	editingUsersList = new LinkedList<String>();
	ownedDocList = new DefaultListModel<String>();
	editDocList = new DefaultListModel<String>();
	ownDisplayList = new JList<String>();
	ownDisplayList.setFont(font);
	editDisplayList = new JList<String>();
	editDisplayList.setFont(font);
	userListDLM = new DefaultListModel<String>();
	editingUserListModel = new DefaultListModel<String>();
	userListJL = new JList<String>();
	userListJL.setFont(font);
	editingUsersJList = new JList<String>(editingUserListModel);
	editingUsersJList.setFont(font);
    }

    private void getUserUpdates() {
	userListJL.setModel(userListDLM);
	bottomHolder.add(userListJL);
    }

    // updates the model and refreshes the editing user list panel
    @SuppressWarnings("unchecked")
    private void refreshEditingUserLists(String docName) {

	// get list from server
	try {
	    // clear model because will be completely updated
	    editingUserListModel.clear();
	    toServer.writeObject(ClientRequest.GET_EDITORS);
	    toServer.writeObject(docName);
	    ServerResponse response = (ServerResponse) fromServer.readObject();
	    switch (response) {
	    case NO_DOCUMENT:
		JOptionPane.showMessageDialog(null, "That document no longer exists.");
		break;
	    case DOCUMENT_EXISTS:
		// obtain editors from server
		editingUsersList = (List<String>) fromServer.readObject();

		for (String editor : editingUsersList) {
		    editingUserListModel.addElement(editor);
		}
		editingUsersJList.setModel(editingUserListModel);
		break;

	    default:
		break;
	    }
	} catch (IOException | ClassNotFoundException e) {
	    e.printStackTrace();
	}

    }

    @SuppressWarnings("unchecked")
    private void getDisplayList() {
	ownedDocList.clear();
	editDocList.clear();
	try {
	    toServer.writeObject(ClientRequest.GET_DOCS);
	    ownedModel = (List<String>) fromServer.readObject();
	    ownedEditable = (List<String>) fromServer.readObject();
	    for (String s : ownedModel) {
			ownedDocList.addElement(s);
	    }
	    for (String s : ownedEditable) {
			editDocList.addElement(s);
	    }
	    ownDisplayList.setModel(ownedDocList);
	    editDisplayList.setModel(editDocList);
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	}
    }

    private int getInt(double number) {
	return (int) (number / 8);
    }

    private void layoutGUI() {

	Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
	double screenWidth = screensize.getWidth() * 0.65;
	double screenHeight = screensize.getHeight() * 0.6;
	this.setSize((int) screenWidth, (int) screenHeight);
	System.out.println(screenWidth + " height: " + screenHeight);

	// Create the document GUI
	this.setTitle(userName + "'s Document Selector Hub");
	this.setLocation(getInt(screenWidth), getInt(screenHeight));
	this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	this.setResizable(false);

	// Create and add a panel to the document GUI
	thePanel = new JPanel();
	thePanel.setLayout(null);
	this.add(thePanel);

	// Create and add the Options panel to the right side of the view
	optionPanel = new JPanel();
	optionPanel.setLayout(new BorderLayout());
	optionPanel.setSize((int) (screenWidth * .3205), (int) (screenHeight * .926));
	optionPanel.setLocation((int) (screenWidth * .647), 0);

	thePanel.add(optionPanel);

	// Create and add the Documents panel to the left side of the view
	docPanel = new JPanel();
	docPanel.setLayout(new GridLayout(1, 1, 2, 2));
	docPanel.setSize((int) (screenWidth * .61), (int) (screenHeight * .889));

	docPanel.setLocation((int) (screenWidth * .01), (int) (screenWidth * .027));
	thePanel.add(docPanel);

	ownedDocPanel = new JScrollPane(ownDisplayList);
	ownedDocPanel.setBackground(Color.BLACK);
	editDocPanel = new JScrollPane(editDisplayList);
	editDocPanel.setBackground(Color.BLACK);

	// Create and/or instantiate all Labels, Panels, etc...
	JLabel documentLabel;
	if (userName.equals("Filbert")) {
	    documentLabel = new JLabel(userName + "'s Bundle of Sticks", SwingConstants.CENTER);
	} else {
	    documentLabel = new JLabel(userName + "'s Documents", SwingConstants.CENTER);
	}
	optionLabel = new JLabel(" Editors", SwingConstants.CENTER);
	JPanel optionPanelInner = new JPanel();
	JPanel topInnerOption = new JPanel();
	JPanel bottomInnerOption = new JPanel();
	topHolder = new JPanel();
	JPanel holder = new JPanel();
	JPanel docButtons = new JPanel();

	// Create the JButtons on the Document panel, for document functionality
	createDoc = new JButton("Create Document");
	deleteDoc = new JButton("Delete Document");
	openDoc = new JButton("Open Document");
	refreshList = new JButton("Refresh List");

	// set default button for pressing enter
	this.getRootPane().setDefaultButton(openDoc);

	// Create the JButtons and JTextField on the Options panel, placed in
	// top or bottom
	removeUser = new JButton("Remove User");
	addUser = new JButton("Add User");
	searchBar = new JTextField("Search for a name here");
	searchBar.setForeground(Color.GRAY);
	// button listeners

	bottomHolder = new JPanel();

	tabbedDocs = new JTabbedPane();

	documentLabel.setFont(new Font("Arial", Font.BOLD, 14));
	documentLabel.setSize((int) (screenWidth * .64), (int) (screenHeight * .037));

	documentLabel.setLocation(0, 0);
	thePanel.add(documentLabel);
	optionLabel.setFont(new Font("Arial", Font.BOLD, 14));
	optionPanel.add(optionLabel, BorderLayout.NORTH);
	optionPanelInner.setLayout(new GridLayout(2, 1, 2, 2));
	topInnerOption.setLayout(new BorderLayout());
	bottomInnerOption.setLayout(new BorderLayout());
	topHolder.setLayout(new BorderLayout());
	topHolder.setBackground(Color.WHITE);
	topHolder.add(editingUsersJList);
	topHolder.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	bottomHolder.setLayout(new BorderLayout());
	bottomHolder.setBackground(Color.WHITE);
	JLabel removeUsers = new JLabel("Select a user to add", SwingConstants.CENTER);
	removeUsers.setFont(new Font("Arial", Font.BOLD, 14));
	bottomInnerOption.add(removeUsers, BorderLayout.NORTH);
	topInnerOption.add(topHolder, BorderLayout.CENTER);
	topInnerOption.add(removeUser, BorderLayout.SOUTH);
	bottomInnerOption.add(bottomHolder, BorderLayout.CENTER);
	bottomInnerOption.add(addUser, BorderLayout.SOUTH);
	bottomHolder.add(searchBar, BorderLayout.SOUTH);
	bottomHolder.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	optionPanelInner.add(topInnerOption);
	optionPanelInner.add(bottomInnerOption);
	optionPanel.add(optionPanelInner, BorderLayout.CENTER);
	holder.setLayout(new BorderLayout());
	docButtons.setLayout(new FlowLayout());
	holder.add(docButtons, BorderLayout.SOUTH);
	docButtons.add(createDoc);
	docButtons.add(deleteDoc);
	docButtons.add(openDoc);
	docButtons.add(refreshList);
	tabbedDocs.add("Owned Documents", ownedDocPanel);// scrollPane
	tabbedDocs.add("Editable Documents", editDocPanel);// scrollPaneEdit
	tabbedDocs.setBackground(Color.LIGHT_GRAY);
	holder.add(tabbedDocs, BorderLayout.CENTER);
	docPanel.add(holder);

	this.setVisible(true);

    }

    private void registerListeners() {
	searchBar.addKeyListener(new searchBarListener());
	searchBar.addMouseListener(new SearchMouseListener());
	this.createDoc.addActionListener(new CreateDocumentListener());
	this.refreshList.addActionListener(new RefreshListListener());
	this.deleteDoc.addActionListener(new DeleteDocumentListener());
	this.addWindowListener(new MyWindowListener());
	ownDisplayList.addListSelectionListener(new ListSelectionHandler());
	editDisplayList.addListSelectionListener(new ListSelectionHandler());
	tabbedDocs.addChangeListener(new tabbedChangedListener());
	openDoc.addActionListener(new OpenDocumentListener());
	addUser.addActionListener(new AddUserButtonListener());
	removeUser.addActionListener(new RemoveButtonListener());
    }

    public String getUserName() {
	return userName;
    }

    public ObjectOutputStream sendToServer() {
	return toServer;
    }

    private class SearchMouseListener implements MouseListener {

	@Override
	public void mouseClicked(MouseEvent e) {
	    searchBar.setText("");
	    searchBar.setForeground(Color.BLACK);
	}

	@Override
	public void mousePressed(MouseEvent e) {
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

    private class searchBarListener implements KeyListener {

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	    if (!searchBar.getText().isEmpty()) {
		updateUsers(searchBar.getText());
	    } else {
		userListDLM.clear();
	    }
	}

	@SuppressWarnings("unchecked")
	private void updateUsers(String text) {
	    try {
		toServer.writeObject(ClientRequest.GET_USERS);
		toServer.writeObject(text);
		userList = (List<String>) fromServer.readObject();
	    } catch (IOException e) {
		e.printStackTrace();
	    } catch (ClassNotFoundException e) {
		e.printStackTrace();
	    }
	    userListDLM.clear();
	    for (String str : userList) {
		if (str.contains(text))
		    userListDLM.addElement(str);
	    }
	    getUserUpdates();
	}
    }

    private class CreateDocumentListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
	    // Open Pane to get the new document's name
	    String newDocName = JOptionPane.showInputDialog("Please enter the new document's name:");
	    if (newDocName == null || newDocName.equals("")) {
		JOptionPane.showMessageDialog(null, "Please enter a name for the document.");
		return;
	    }
	    try {
		toServer.writeObject(ClientRequest.CREATE_DOC);
		toServer.writeObject(newDocName);
		ServerResponse response = (ServerResponse) fromServer.readObject();

		switch (response) {
		case DOCUMENT_EXISTS:
		    JOptionPane.showMessageDialog(null, "Sorry, this document already exists.");
		    return;
		case DOCUMENT_CREATED:
		    // obtain text from server to send to editor gui first --
		    // enables us to the refresh
		    // the display list before launching the gui
		    String startingText = (String) fromServer.readObject();
		    getDisplayList();
		    new EditorGUI(fromServer, toServer, DocumentSelectGUI.this, startingText, newDocName);
		    return;
		default:
		    JOptionPane.showMessageDialog(null, "Incompatible server response.");
		    return;
		}

	    } catch (IOException e1) {
		e1.printStackTrace();
	    } catch (ClassNotFoundException e1) {
		e1.printStackTrace();
		// } catch (InterruptedException e1) {
		// this is for the selectGUI.wait() method call
		// e1.printStackTrace();
	    } catch (IllegalMonitorStateException imse) {
		imse.printStackTrace();
	    }
	}
    }

    private class RefreshListListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
	    getDisplayList();
	}
    }

    private class AddUserButtonListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {

	    // get selected user name
	    String username = userListJL.getSelectedValue();
	    System.out.println("");

	    // send client request to server to add user, then send username
	    if (username != null) {

		int index = ownDisplayList.getSelectedIndex();
		if (index < 0) {
		    JOptionPane.showMessageDialog(null, "Please select a document.");
		    return;
		}

		String docName = ownedDocList.getElementAt(index);

		System.out.println(docName);

		try {
		    toServer.writeObject(ClientRequest.ADD_PERMISSION);
		    toServer.writeObject(username);
		    toServer.writeObject(docName);

		    ServerResponse response = (ServerResponse) fromServer.readObject();

		    switch (response) {
		    case PERMISSION_ADDED:
			refreshEditingUserLists(docName);
			break;
		    case NO_DOCUMENT:
			JOptionPane.showMessageDialog(null, "Cannot add user to a document that does not exist.");
			break;
		    default:
			System.out.println("Incompatible server response");
		    }

		} catch (IOException e1) {
		    e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
		    e1.printStackTrace();
		}
	    }
	}
    }

    private class RemoveButtonListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent clicked) {
	    // get selected user name
	    String username = editingUsersJList.getSelectedValue();
	    // send client request to server to add user, then send username
	    if (username != null) {

		int index = ownDisplayList.getSelectedIndex();
		if (index < 0) {
		    JOptionPane.showMessageDialog(null, "Please select a document that you own.");
		    return;
		}

		String docName = ownedDocList.getElementAt(index);

		try {
		    toServer.writeObject(ClientRequest.REMOVE_PERMISSION);
		    toServer.writeObject(username);
		    toServer.writeObject(docName);

		    ServerResponse response = (ServerResponse) fromServer.readObject();
		    System.out.println(response);

		    switch (response) {
		    case PERMISSION_REMOVED:
			// user
			refreshEditingUserLists(docName);
			break;
		    case NO_DOCUMENT:
			JOptionPane.showMessageDialog(null, "Cannot remove user from a document that does not exist.");
			break;
		    case PERMISSION_DENIED:
			JOptionPane.showMessageDialog(null,
				"You cannot remove the owner from the document's editor list.");
			break;
		    default:
			System.out.println("Incompatible server response");
			break;
		    }

		} catch (IOException e1) {
		    e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
		    e1.printStackTrace();
		}
	    }
	}
    }

    private class DeleteDocumentListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
	    String docName = null;
	    if (ownDisplayList.isShowing()) {
		int index = ownDisplayList.getSelectedIndex();
		docName = ownedDocList.getElementAt(index);
		if (index < 0) {
		    JOptionPane.showMessageDialog(null, "Please select a document.");
		    return;
		}
	    } else {
		int index = editDisplayList.getSelectedIndex();
		if (index < 0) {
		    JOptionPane.showMessageDialog(null, "Please select a document.");
		    return;
		}
		docName = editDocList.getElementAt(index);
	    }
	    connectAndDelete(docName);
	}

	public void connectAndDelete(String document) {
	    try {
		toServer.writeObject(ClientRequest.DELETE_DOC);
		toServer.writeObject(document);
		ServerResponse response = (ServerResponse) fromServer.readObject();
		switch (response) {
		case NO_DOCUMENT:
		    JOptionPane.showMessageDialog(null, "That document does not exist.");
		    return;
		case PERMISSION_DENIED:
		    JOptionPane.showMessageDialog(null,
			    "You are not the owner of this document, and do not have permission to delete.");
		    return;
		case DOCUMENT_OPENED:
		    JOptionPane.showMessageDialog(null,
			    "The document is currently being edited. You cannot delete now, try again later.");
		    return;
		case DOCUMENT_DELETED:
		    JOptionPane.showMessageDialog(null, "Document was successfuly deleted.");
		    getDisplayList();
		    return;
		default:
		    JOptionPane.showMessageDialog(null, "Incompatible server response. " + response);
		    System.out.println("LOOK " + response + " HERE");
		    return;
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    } catch (ClassNotFoundException e) {
		e.printStackTrace();
	    }
	}
    }

    private class OpenDocumentListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {

	    // obtain the document name from the currently opened tab
	    String docName = null;
	    if (ownDisplayList.isShowing()) {
		int index = ownDisplayList.getSelectedIndex();
		if (index < 0) {
		    JOptionPane.showMessageDialog(null, "Please Select A Document.");
		    return;
		} else {
		    docName = ownedDocList.getElementAt(index);
		}
	    } else {
		int index = editDisplayList.getSelectedIndex();
		if (index < 0) {
		    JOptionPane.showMessageDialog(null, "Please Select A Document.");
		    return;
		} else {
		    docName = editDocList.getElementAt(index);
		}
	    }
	    connectAndOpen(docName);
	}

	private void connectAndOpen(String docName) {
	    try {

		// tell the server we want to open the docName document
		toServer.writeObject(ClientRequest.OPEN_DOC);
		toServer.writeObject(docName);

		// receive and process server's response
		ServerResponse response = (ServerResponse) fromServer.readObject();

		switch (response) {
		case PERMISSION_DENIED:
		    JOptionPane.showMessageDialog(null,
			    "Permission Denied: You are not a member of the Document's Editors.");
		    return;
		case NO_DOCUMENT:
		    JOptionPane.showMessageDialog(null, "Document does not exist.");
		    return;
		case DOCUMENT_OPENED:
		    toServer.flush();
		    String text = (String) fromServer.readObject();
		    // Set<String> = (String) fromServ
		    new EditorGUI(fromServer, toServer, DocumentSelectGUI.this, text, docName);
		    return;
		default:
		    JOptionPane.showMessageDialog(null, "Incompatible server response." + response);
		    System.out.println("@LOOK " + response + " HERE 2");
		    return;
		}
	    } catch (IOException e1) {
		e1.printStackTrace();
	    } catch (ClassNotFoundException e1) {
		e1.printStackTrace();
	    }
	}
    }

    private class MyWindowListener implements WindowListener {

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
	    int decision = JOptionPane.showConfirmDialog(null, "Are you sure you want to logout?", "Confirm Logout",
		    JOptionPane.YES_NO_OPTION);
	    if (decision == JOptionPane.YES_OPTION) {
		try {
		    toServer.writeObject(ClientRequest.OPEN_DOC);
		} catch (IOException e1) {
		    e1.printStackTrace();
		}
		System.exit(NORMAL);
	    } else if (decision == JOptionPane.CANCEL_OPTION || decision == JOptionPane.NO_OPTION) {
	    }
	}

	@Override
	public void windowClosed(WindowEvent e) {
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

    /*
     * Ensures invisible tabbed pane isn't selected to properly refresh
     */
    private class tabbedChangedListener implements ChangeListener {

	@Override
	public void stateChanged(ChangeEvent e) {
	    if (!ownedDocPanel.isShowing()) {
		ownDisplayList.clearSelection();
	    } else {
		editDisplayList.clearSelection();
	    }
	}
    }

    private class ListSelectionHandler implements ListSelectionListener {

	@SuppressWarnings("unchecked")
	@Override
	public void valueChanged(ListSelectionEvent e) {
	    JList<String> list = (JList<String>) e.getSource();
	    String docName = (String) list.getSelectedValue();
	    System.out.println(docName);

	    if (list.isSelectionEmpty()) {
	    } else {
		String displayName = docName.substring(0, docName.indexOf("  -  "));
		optionLabel.setText(displayName + "'s Editors");
		refreshEditingUserLists(docName);
	    }
	    list = null;
	}
    }
}
