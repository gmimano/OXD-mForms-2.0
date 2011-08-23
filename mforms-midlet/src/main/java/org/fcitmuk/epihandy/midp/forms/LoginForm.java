package org.fcitmuk.epihandy.midp.forms;

import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

import org.fcitmuk.util.MenuText;


/**
 * 
 * @author Daniel
 *
 */
public class LoginForm extends Form{
	private TextField txtUserName;
	private TextField txtPassword;
	
	public LoginForm(String title , String userName) {
		super(title);
			
		txtUserName = new TextField(MenuText.USER_NAME(), userName,50,TextField.ANY);
		txtPassword = new TextField(MenuText.PASSWORD(), "",50,TextField.PASSWORD);

		this.append(txtUserName);
		this.append(txtPassword);
	}

	public String getPassword() {
		return this.txtPassword.getString();
	}

	public void setPassword(String password) {
		this.txtPassword.setString(password);
	}

	public String getUserName() {
		return this.txtUserName.getString();
	}

	public void setUserName(String userName) {
		this.txtUserName.setString(userName);
	}

}
