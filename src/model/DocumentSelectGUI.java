package model;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class DocumentSelectGUI extends JFrame {

	private JPanel thePanel;
	private JPanel optionPanel, docPanel;
	private ObjectInputStream fromServer;
	protected ObjectOutputStream toServer;
	private JTextField searchBar;
	private List<String> ownedModel, ownedEditable, userList;
	private DefaultListModel<String> userListDLM, ownedDocList, editDocList;
	private JList<String> ownDisplayList, editDisplayList, userListJL;
	private JTabbedPane tabbedDocs;
	private JButton createDoc, deleteDoc, openDoc, refreshList, removeUser, addUser;

	public DocumentSelectGUI(ObjectInputStream fromServer, ObjectOutputStream toServer) {
		this.fromServer = fromServer;
		this.toServer = toServer;
		layoutGUI();
		getDisplayList();
		registerListeners();
	}

	private void getDisplayList() {
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

		optionPanel = new JPanel();
		optionPanel.setLayout(new BorderLayout());
		optionPanel.setSize(300, 500);
		optionPanel.setLocation(600, 0);
		thePanel.add(optionPanel);

		docPanel = new JPanel();
		docPanel.setLayout(new GridLayout(1, 1, 2, 2));
		docPanel.setSize(600, 480);
		docPanel.setLocation(0, 20);
		thePanel.add(docPanel);

		JLabel documentLabel = new JLabel("Documents", SwingConstants.CENTER);
		JLabel optionLabel = new JLabel("Document Sharing", SwingConstants.CENTER);
		createDoc = new JButton("Create Document");
		deleteDoc = new JButton("Delete Document");
		openDoc = new JButton("Open Document");
		refreshList = new JButton("Refresh List");
		JPanel optionPanelInner = new JPanel();
		JPanel topInnerOption = new JPanel();
		JPanel bottomInnerOption = new JPanel();
		removeUser = new JButton("Remove User");
		addUser = new JButton("Add User");
		JPanel topHolder = new JPanel();
		JPanel bottomHolder = new JPanel();
		searchBar = new JTextField();
		JPanel holder = new JPanel();
		JPanel docButtons = new JPanel();
		tabbedDocs = new JTabbedPane();
		ownDisplayList = new JList<String>();
		editDisplayList = new JList<String>();

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
		tabbedDocs.add("Owned Documents", null);// scrollPane
		tabbedDocs.add("Editable Documents", null);// scrollPaneEdit
		tabbedDocs.setBackground(Color.DARK_GRAY);
		holder.add(tabbedDocs, BorderLayout.CENTER);
		docPanel.add(holder);
		this.setVisible(true);
	}

	private void registerListeners() {
		this.searchBar.getDocument().addDocumentListener(new SearchBarListener());
		this.createDoc.addActionListener(new CreateDocumentListener());
		this.refreshList.addActionListener(new RefreshListListener());

	}

	public ObjectOutputStream sendToServer() {
		return toServer;
	}

	private class SearchBarListener implements DocumentListener {

		@Override
		public void insertUpdate(DocumentEvent e) {
			updateUsers(searchBar.getText());
			// send client request
			// send text
			// get list
			// display list
			// repeat
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			updateUsers(searchBar.getText());
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
		}

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
		}

	}

	private class CreateDocumentListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Create new document functionality here
			// Open Pane to get the new document's name
			try {
				toServer.writeObject(ClientRequest.CREATE_DOC);

			} catch (IOException e1) {
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
}
