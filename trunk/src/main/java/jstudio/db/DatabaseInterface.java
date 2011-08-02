package jstudio.db;

import java.util.HashMap;
import java.util.List;

public interface DatabaseInterface {
	
	public static final String
		KEY_DRIVER = "db.driver",
		KEY_PROTOCOL = "db.protocol",
		KEY_HOST = "db.host",
		KEY_NAME = "db.name",
		KEY_USER = "db.user",
		KEY_PASS = "db.password";
	
	public static final String
		DEF_DRIVER = "com.mysql.jdbc.Driver",
		DEF_PROTOCOL = "jdbc:mysql",
		DEF_HOST = "localhost",
		DEF_NAME = "none",
		DEF_USER = "",
		DEF_PASS = "";

	public void connect(String host, String table, String user, String pass);
	public boolean isConnected();
	public DatabaseObject store(String table, DatabaseObject o);
	public void delete(String table, DatabaseObject o);
	public List<? extends DatabaseObject> getAll(String table);
	public List<? extends DatabaseObject> getAll(String table, String column);
	public DatabaseObject get(String table, int id);
	public List<? extends DatabaseObject> getBetween(String table, String field, String from, String to);
	public List<? extends DatabaseObject> getAll(String table, HashMap<String, String> values);
}
