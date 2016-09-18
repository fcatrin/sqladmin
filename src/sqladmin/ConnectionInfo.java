package sqladmin;

public class ConnectionInfo {
	public String Name;
	public String Driver;
	public String url;
	public String User;
	public String pwd;

	public ConnectionInfo(String Name, String Driver, String url, String User, String pwd) {
		this.Name = Name;
		this.Driver = Driver;
		this.url = url;
		this.User = User;
		this.pwd = pwd;
	}
}
