package ilo;

public class Credentials {
	public static Credentials fromEnvironment() {
		String username = System.getenv("ilo.username");
		String password = System.getenv("ilo.password");
		return new Credentials(username, password);
	}

	private String username;
	private String password;

	public Credentials(String username, String password) {
		super();
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
}
