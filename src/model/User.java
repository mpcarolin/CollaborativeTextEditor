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

	
	public User (String newUser, String pass, String newID, Object docPerm) {
		this.username = newUser;
		this.password = pass;
		this.id = newID;
		documentsOwned.add(docPerm);
	}
	
	public String getName() {
		return username;
	}
	
	public String getIDNum() {
		return id;
	}
	
	public boolean hasPermission(Object document) {
		for(Object o: documentsOwned) {
			if(o.equals(document))
				return true;
		}
		for(Object o: editableDocuments) {
			if(o.equals(document))
				return true;
		}
		return false;
	}
	
	public void givePermission(Object document) {
		editableDocuments.add(document);
	}
	
	public void resetPassword(String newPass) {
		password = newPass;
	}
	
	public void changePassword() {
		
	}
}

