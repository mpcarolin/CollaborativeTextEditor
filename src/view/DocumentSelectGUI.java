package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;

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
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import model.ClientRequest;
import model.ServerResponse;

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
	private boolean firstTime;

	public DocumentSelectGUI(ObjectInputStream fromServer, ObjectOutputStream toServer) {
		this.fromServer = fromServer;
		this.toServer = toServer;
		this.firstTime = true;
		instantiateLists();
		layoutGUI();
		getDisplayList();
		registerListeners();
	}

	private void instantiateLists() {
		editingUsersList = new LinkedList<String>();
		ownedDocList = new DefaultListModel<String>();
		editDocList = new DefaultListModel<String>();
		ownDisplayList = new JList<String>();
		editDisplayList = new JList<String>();
		userListDLM = new DefaultListModel<String>();
		editingUserListModel = new DefaultListModel<String>();
		userListJL = new JList<String>();
		editingUsersJList = new JList<String>();
	}

	private void getUserUpdates() {
		userListJL.setModel(userListDLM);
		bottomHolder.add(userListJL);
	}
	
	// updates the model and refreshes the editing user list panel 
	private void refreshEditingUserLists() {
		// clear model because will be completely updated
		editingUserListModel.clear();
		for (String name : editingUsersList) {
			editingUserListModel.addElement(name);
		}
		editingUsersJList.setModel(editingUserListModel);
		topHolder.add(editingUsersJList);
	}
	

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
		// if (!firstTime) {
		// firstTime = false;
		// ownedDocPanel.add(ownDisplayList);
		// editDocPanel.add(editDisplayList);
		// }
	}

	private void layoutGUI() {
		// Create the document GUI
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Document Selector Hub");
		this.setSize(900, 520);
		this.setLocation(300, 80);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);

		// Create and add a panel to the document GUI
		thePanel = new JPanel();
		thePanel.setLayout(null);
		this.add(thePanel);

		// Create and add the Options panel to the right side of the view
		optionPanel = new JPanel();
		optionPanel.setLayout(new BorderLayout());
		optionPanel.setSize(300, 500);
		optionPanel.setLocation(600, 0);
		thePanel.add(optionPanel);

		// Create and add the Documents panel to the left side of the view
		docPanel = new JPanel();
		docPanel.setLayout(new GridLayout(1, 1, 2, 2));
		docPanel.setSize(600, 480);
		docPanel.setLocation(0, 20);
		thePanel.add(docPanel);

		// //TODO Attempts to hardcode placed in here
		ownedDocPanel = new JScrollPane(ownDisplayList);
		ownedDocPanel.setBackground(Color.YELLOW);
		editDocPanel = new JScrollPane(editDisplayList);
		editDocPanel.setBackground(Color.RED);
		// ownedModel = new LinkedList<String>();
		// ownedModel.add("HELLO.txt");
		// ownedDocList.addElement(ownedModel.get(0));
		// ownDisplayList.setModel(ownedDocList);
		// //TODO end of hardcode attempts.

		// Create and/or instantiate all Labels, Panels, etc...
		JLabel documentLabel = new JLabel("Documents", SwingConstants.CENTER);
		JLabel optionLabel = new JLabel("Document Sharing", SwingConstants.CENTER);
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

		// Create the JButtons and JTextField on the Options panel, placed in
		// top or bottom
		removeUser = new JButton("Remove User");
		addUser = new JButton("Add User");
		searchBar = new JTextField();

		// button listeners
		openDoc.addActionListener(new OpenDocumentListener());
		addUser.addActionListener(new AddUserButtonListener());

		bottomHolder = new JPanel();

		tabbedDocs = new JTabbedPane();

		documentLabel.setFont(new Font("default", Font.BOLD, 13));
		documentLabel.setSize(600, 20);
		documentLabel.setLocation(0, 0);
		thePanel.add(documentLabel);
		optionLabel.setFont(new Font("default", Font.BOLD, 13));
		optionPanel.add(optionLabel, BorderLayout.NORTH);
		optionPanelInner.setLayout(new GridLayout(2, 1, 2, 2));
		topInnerOption.setLayout(new BorderLayout());
		bottomInnerOption.setLayout(new BorderLayout());
		topHolder.setLayout(new BorderLayout());
		topHolder.setBackground(Color.WHITE);
		bottomHolder.setLayout(new BorderLayout());
		bottomHolder.setBackground(Color.WHITE);
		topInnerOption.add(topHolder, BorderLayout.CENTER);
		topInnerOption.add(removeUser, BorderLayout.SOUTH);
		bottomInnerOption.add(bottomHolder, BorderLayout.CENTER);
		bottomInnerOption.add(addUser, BorderLayout.SOUTH);
		// searchBar.setText("Search for username here.");
		bottomHolder.add(searchBar, BorderLayout.SOUTH);
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
		tabbedDocs.setBackground(Color.DARK_GRAY);
		holder.add(tabbedDocs, BorderLayout.CENTER);
		docPanel.add(holder);

		this.setVisible(true);

	}

	private void registerListeners() {
		searchBar.addKeyListener(new searchBarListener());
		// this.searchBar.getDocument().addDocumentListener(new
		// SearchBarListener());
		this.createDoc.addActionListener(new CreateDocumentListener());
		this.refreshList.addActionListener(new RefreshListListener());
		this.deleteDoc.addActionListener(new DeleteDocumentListener());

	}

	public ObjectOutputStream sendToServer() {
		return toServer;
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
			updateUsers(searchBar.getText());
		}

		private void updateUsers(String text) {
			try {
				toServer.writeObject(ClientRequest.GET_USERS);
				toServer.writeObject(text);
				userList = (List<String>) fromServer.readObject();
				// System.out.println(userList.get(0));
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

	// private class SearchBarListener implements DocumentListener {
	//
	// @Override
	// public void insertUpdate(DocumentEvent e) {
	// updateUsers(searchBar.getText());
	// // send client request
	// // send text
	// // get list
	// // display list
	// // repeat
	// }
	//
	// @Override
	// public void removeUpdate(DocumentEvent e) {
	// //updateUsers(searchBar.getText());
	// }
	//
	// @Override
	// public void changedUpdate(DocumentEvent e) {
	// }
	//
	// private void updateUsers(String text) {
	// try {
	// toServer.writeObject(ClientRequest.GET_USERS);
	// toServer.writeObject(text);
	// userList = (List<String>) fromServer.readObject();
	// System.out.println(userList.get(0));
	// } catch (IOException e) {
	// e.printStackTrace();
	// } catch (ClassNotFoundException e) {
	// e.printStackTrace();
	// }
	// userListDLM.clear();
	// for (String str : userList) {
	// if (str.contains(text))
	// userListDLM.addElement(str);
	// }
	// getUserUpdates();
	// }
	// }

	private class CreateDocumentListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// Open Pane to get the new document's name
			String newDocName = JOptionPane.showInputDialog("Please enter the new document's name:");
			try {
				toServer.writeObject(ClientRequest.CREATE_DOC);
				toServer.writeObject(newDocName);
				ServerResponse response = (ServerResponse) fromServer.readObject();
				switch (response) {
				case DOCUMENT_EXISTS:
					JOptionPane.showMessageDialog(null, "Sorry, this document already exists.");
					return;
				case DOCUMENT_CREATED:
					getDisplayList();
					new EditorGUI();
					return;
				default:
					JOptionPane.showMessageDialog(null, "Incompatible server response.");
					return;
				}

			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
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
			
			// send client request to server to add user, then send username
			if (username != null) {

				int index = ownDisplayList.getSelectedIndex();
				String docName = ownedDocList.getElementAt(index);

				System.out.println(docName);

				try {
					toServer.writeObject(ClientRequest.ADD_PERMISSION);
					toServer.writeObject(docName);
					toServer.writeObject(username);
					
					ServerResponse response = (ServerResponse) fromServer.readObject();
					System.out.println(response);
					
					switch (response) {
					case PERMISSION_ADDED:
						//user
						editingUsersList.add(username);
						refreshEditingUserLists();
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

	private class DeleteDocumentListener implements ActionListener {

		// if exists
		// if owner
		// if currently edited
		@Override
		public void actionPerformed(ActionEvent e) {
			String docName = null;
			if (ownDisplayList.isShowing()) {
				int index = ownDisplayList.getSelectedIndex();
				docName = ownedDocList.getElementAt(index);
			} else {
				int index = editDisplayList.getSelectedIndex();
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
					JOptionPane.showMessageDialog(null, "Incompatible server response.");
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
				docName = ownedDocList.getElementAt(index);
			} else {
				int index = editDisplayList.getSelectedIndex();
				docName = editDocList.getElementAt(index);
			}
			connectAndOpen(docName);
		}

		private void connectAndOpen(String docName) {
			try {

				// tell the server we want to open the docName document
				toServer.writeObject(ClientRequest.OPEN_DOC);
				toServer.writeObject(docName);
				System.out.println(docName);

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
					new EditorGUI(fromServer, toServer);
					return;
				default:
					JOptionPane.showMessageDialog(null, "Incompatible server response.");
					return;
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
		}
	}
}
