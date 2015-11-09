package model;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4807570774011333657L;
	private String username;
	private String password;
	private String id;
	private List<Object> documentsOwned;
	private List<Object> editableDocuments;

	public User(String newUser, String pass, String newID) {
		this.username = newUser;
		this.password = pass;
		this.id = newID;
	}

	// public User (String newUser, String pass, String newID, Object docPerm) {
	// this.username = newUser;
	// this.password = pass;
	// this.id = newID;
	// documentsOwned.add(docPerm);
	// editableDocuments.add(docPerm);
	// }

	public String getName() {
		return username;
	}

	public String getIDNum() {
		return id;
	}
	
	public String getPassword() {
	   return password;
	}

	public boolean hasPermission(Object document) {
		return documentsOwned.contains(document) || editableDocuments.contains(document);
	}

	public void givePermission(Object document) {
		editableDocuments.add(document);
	}

	public void setPassword(String newPass) {
		password = newPass;
	}

}
