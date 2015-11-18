package model;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class DocumentSelectGUI extends JFrame {

	private JPanel thePanel;
	private JPanel optionPanel, docPanel;
	private ObjectInputStream fromServer;
	// private static DefaultListModel<String> ownedList, editList;
	// private static TableModel ownedTable, editTable;

//	 public static void main(String[] args) {
//	 new DocumentSelectGUI(null, null);
//	 }

	public DocumentSelectGUI(ObjectInputStream fromServer) {
		this.fromServer = fromServer;
		layoutGUI();
		// registerListeners();
	}

	private void layoutGUI() {
		// Create the document GUI
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Documents");
		setSize(900, 500);
		setLocation(300, 80);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and add a panel to the document GUI
		thePanel = new JPanel();
		thePanel.setLayout(null);
		thePanel.setBackground(Color.RED);
		this.add(thePanel);

		optionPanel = new JPanel();
		optionPanel.setLayout(new BorderLayout());
		optionPanel.setSize(300, 500);
		optionPanel.setLocation(0, 0);
		optionPanel.setBackground(Color.BLUE);
		thePanel.add(optionPanel);

		docPanel = new JPanel();
		docPanel.setLayout(new GridLayout(1, 1, 2, 2));
		docPanel.setSize(600, 480);
		docPanel.setLocation(300, 20);
		docPanel.setBackground(Color.GREEN);
		thePanel.add(docPanel);

		JLabel optionLabel = new JLabel("Options", SwingConstants.CENTER);
		optionLabel.setFont(new Font("default", Font.BOLD, 13));
		optionPanel.add(optionLabel, BorderLayout.NORTH);

		JLabel documentLabel = new JLabel("Documents", SwingConstants.CENTER);
		documentLabel.setFont(new Font("default", Font.BOLD, 13));
		documentLabel.setSize(600, 20);
		documentLabel.setLocation(300, 0);
		thePanel.add(documentLabel);

		TableModel ownedTable = null;
		try {
			ownedTable = (TableModel) fromServer.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		JTable owned = new JTable(ownedTable);
		owned.setModel(ownedTable);
		JScrollPane scrollPane = new JScrollPane(owned);
		TableRowSorter<TableModel> tableSort = new TableRowSorter<TableModel>();
		tableSort.setModel(ownedTable);
		// RowSorter<TableModel> rowSort = tableSort;
		owned.setAutoCreateRowSorter(true);
		owned.getSelectionModel().setSelectionInterval(0, 0);

		TableModel editTable = null;
		try {
			editTable = (TableModel) fromServer.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		JTable edit = new JTable(editTable);
		edit.setModel(editTable);
		JScrollPane scrollPaneEdit = new JScrollPane(edit);
		TableRowSorter<TableModel> tableSortEdit = new TableRowSorter<TableModel>();
		tableSortEdit.setModel(editTable);
		// RowSorter<TableModel> rowSortEdit = tableSortEdit;
		edit.setAutoCreateRowSorter(true);
		edit.getSelectionModel().setSelectionInterval(0, 0);

		JPanel holder = new JPanel();
		holder.setLayout(new BorderLayout());
		docPanel.add(holder);
		JTabbedPane tabbedDocs = new JTabbedPane();
		tabbedDocs.add("Owned Documents", scrollPane);
		tabbedDocs.add("Editable Documents", scrollPaneEdit);
		tabbedDocs.setBackground(Color.GREEN);
		holder.add(tabbedDocs);

	}
}
