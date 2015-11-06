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
	private List<Object> permissions;
	
	public User (String newUser, String pass, String newID, Object docPerm) {
		this.username = newUser;
		this.password = pass;
		this.id = newID;
		permissions.add(docPerm);
	}
	
	public String getName() {
		return username;
	}
	
	public String getIDNum() {
		return id;
	}
	
	public boolean hasPermission(Object document) {
		for(Object o: permissions) {
			if(o.equals(document))
				return true;
		}
		return false;
	}
	
	public void givePermission(Object document) {
		permissions.add(document);
	}
	
	public void changePassword(String newPass) {
		password = newPass;
	}
	
}

