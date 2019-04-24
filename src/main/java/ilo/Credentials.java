package ilo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Credentials {
	private static ObjectMapper MAPPER = new ObjectMapper();
	
	public static Credentials fromEnvironment() {
		String username = System.getenv(Environment.USERNAME);
		String password = System.getenv(Environment.PASSWORD);
		return new Credentials(username, password);
	}

	private String username;
	private String password;

	public Credentials(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordMask() {
		if (password != null) {
			return password.replaceAll(".{1}", "*");
		}
		return password;
	}

	@Override
	public String toString() {
		return "Credentials [username=" + username + ", password=" + getPasswordMask() + "]";
	}
	
	public String toJson() {
		ObjectNode node = MAPPER.createObjectNode();
		node.put("UserName", username);
		node.put("Password", password);
		return node.toString();		
	}
}
