package model;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

public class User implements Serializable {

	/**
	 * store passwords as a hash create user, generate unique/random id cast
	 * integer to string, concat to password at beginning hash it, store it, log
	 * into server to server -> get id -> get account -> get password check to
	 * make sure these are the same id+password hash stored 8 digits
	 */
	private static final long serialVersionUID = 4807570774011333657L;
	private String username;
	private int id;
	private int hashPass;
	private List<Object> documentsOwned;
	private List<Object> editableDocuments;

	public User(String newUser, String pass) {
		this.username = newUser;
		setPassword(pass);
	}

	public void setPassword(String pass) {
		Random rng = new Random();
		this.id = rng.nextInt(90000000);
		this.hashPass = (id + pass).hashCode();
	}

	public String getName() {
		return username;
	}

	public int getIDNum() {
		return id;
	}

	public int getHashPass() {
		return hashPass;
	}

	public boolean hasPermission(Object document) {
		return documentsOwned.contains(document) || editableDocuments.contains(document);
	}

	public void givePermission(Object document) {
		editableDocuments.add(document);
	}

}
