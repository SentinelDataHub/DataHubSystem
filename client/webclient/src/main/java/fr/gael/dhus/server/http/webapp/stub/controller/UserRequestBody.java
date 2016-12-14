package fr.gael.dhus.server.http.webapp.stub.controller;

import fr.gael.dhus.database.object.User;

public class UserRequestBody{
	private User user;
	 private PasswordModel pm;

	public void setUser(User u) {
		this.user = u;
	}

	public User getUser() {
			  return user;
		  }
	public void setPm(PasswordModel pm) {
			   this.pm = pm;
		   }
	public PasswordModel getPm() {
			   return this.pm;
		   }
	public PasswordModel getPasswordModel() {
			  return pm;
		  }
		  
 }