package jstudio.db;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jstudio.model.Comune;
import jstudio.model.Event;
import jstudio.model.Invoice;
import jstudio.model.Person;
import jstudio.model.Product;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class SqlDB implements DatabaseInterface {
		
	public static void main(String[] args){
		BasicConfigurator.configure();
		SqlDB db = new SqlDB("test.db");
		db.connect(null,null,null,null);
		for(String t: db.getTables()){
			logger.debug(t);
		}
		db.initialize(new Person());
		db.initialize(new Event());
		db.initialize(new Product());
		db.initialize(new Invoice());
		db.store(null, new Invoice());
		db.execute("SELECT MAX(id) FROM invoice");
		db.close();
	}
	
	public enum EntryType{
		Integer,
		Long,
		Float,
		Date,
		String,
		Set,
	}
	
	private static final Logger logger = Logger.getLogger(SqlDB.class);
	private String protocol, driver;
	private Connection connection;
	
	private Map<String, Class> initCache;
	
	public SqlDB(String databasefile){
		this("jdbc:sqlite:"+databasefile, "org.sqlite.JDBC");
	}
	
	protected SqlDB(String protocol, String driver){
		this.protocol = protocol;
		this.driver = driver;
		initCache = new HashMap<String,Class>();
	}

	/**
	 * Arguments are unusued now.
	 * Protocol and driver will be used
	 */
	@Override
	public void connect(String host, String table, String user, String pass) {
		try {
			Class.forName(driver);
			connection = DriverManager.getConnection(protocol);
			logger.debug("Connection established, initializing...");
		} catch (SQLException e) {
			logger.error("Protocol "+protocol+" error",e);
		} catch (ClassNotFoundException e) {
			logger.error("Driver "+driver+" error",e);
		}
	}
	
	private String[] getTables(){
		String sql = "SELECT name FROM sqlite_master WHERE type = \"table\";";
		ArrayList<String> tables = new ArrayList<String>();
		Statement s;
		try{
			s = connection.createStatement();
			ResultSet rs = s.executeQuery(sql);
			while(rs.next()){
				tables.add(rs.getString("name"));
			}
		}catch(SQLException e){
			logger.error(sql,e);
		}
		return tables.toArray(new String[0]);
	}
	
	private Class getGenericType(Field f){
		String tname = f.getGenericType().getClass().getSimpleName();
		return f.getGenericType().getClass();
	}
	
	private void initialize(DatabaseObject o){
		initialize(o.getClass());
	}
	
	private String getClassTable(Class<?> c){
		return c.getSimpleName().toLowerCase();
	}
	
	private Field[] getFields(Class<?> c){
		ArrayList<Field> rf = new ArrayList<Field>();
		Field[] fields = c.getDeclaredFields();
		int fieldsn = fields.length;
		for(int i=0; i<fieldsn; ++i){
			Field f = fields[i];
			if(!Modifier.isStatic(f.getModifiers())&&
					!Modifier.isVolatile(f.getModifiers())){
				f.setAccessible(true);
				rf.add(f);
			}
		}
		return rf.toArray(new Field[rf.size()]);
	}
	
	/**
	 * This method will look for suitable tables in the db
	 * If they do not exist they will be created anew.
	 * If they exist but are different, errors will be eventually thrown later
	 */
	private void initialize(Class<? extends DatabaseObject> c){
		String classname;
		if(initCache.get(c)!=null){
			logger.warn("Class "+c+" already initialized");
			return;
		}else{
			classname = getClassTable(c);
			initCache.put(classname, c);
		}
		String sql = "CREATE TABLE IF NOT EXISTS "+classname+"(";
		int factual = 0;
		for(Field f: getFields(c)){
			Class<?> tc = f.getType();
			String fname = f.getName();
			logger.debug(fname+" "+f.getType().getName()+" "+getGenericType(f));
			String ftype = f.getType().getSimpleName();
			String fdef = (factual>0?", ":"")+fname;
			try{
				switch(EntryType.valueOf(ftype)){
					case Integer:
						fdef+=" INTEGER (5)";
						if(fname.equals("id")){
							fdef += " PRIMARY KEY"; 
						}
						break;
					case Long:
						fdef+=" INTEGER (11)";
						if(fname.equals("id")){
							fdef += " PRIMARY KEY"; 
						}
						break;
					case Float:
						fdef+= " FLOAT";
						break;
					case String:
						fdef += " VARCHAR (255)";		
						break;
					case Date:
						fdef += " DATETIME";
						break;
					case Set:
						//the set requires another table referring to this entities
						break;
					default:
						logger.error("EntryType "+ftype+" not implemented");
						continue;
				}
			}catch(IllegalArgumentException e){
				//custom object type
				String clazz = f.getType().getName();
				try {
					Class refc = Class.forName(clazz);
					initialize(refc);
				} catch (ClassNotFoundException e1) {
					logger.error("Custom class not found "+clazz);
				}
				
			}
			++factual;
			sql += fdef;
		}
		
		sql +=");";
		logger.debug(sql);
		Statement s;
		try {
			s = connection.createStatement();
			s.execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
/**
public void run() throws Exception {
 
//sq lite driver
Class.forName("org.sqlite.JDBC");
//database path, if it's new data base it will be created in project folder
con = DriverManager.getConnection("jdbc:sqlite:mydb.db");
Statement stat = con.createStatement();
 
stat.executeUpdate("drop table if exists weights");
 
//creating table
stat.executeUpdate("create table weights(id integer,"
+ "firstName varchar(30)," + "age INT," + "sex varchar(15),"
+ "weight INT," + "height INT,"
+ "idealweight INT, primary key (id));");
 
PreparedStatement prep = con
.prepareStatement("insert into weights values(?,?,?,?,?,?,?);");
prep.setString(2, "vasea");
prep.setString(3, "21");
prep.setString(4, "male");
prep.setString(5, "77");
prep.setString(6, "185");
prep.setString(7, "76");
prep.execute();
 
//getting data
ResultSet res = stat.executeQuery("select * from weights");
while (res.next()) {
System.out.println(res.getString("id") + " " + res.getString("age")
+ " " + res.getString("firstName") + " "
+ res.getString("sex") + " " + res.getString("weight")
+ " " + res.getString("height") + " "
+ res.getString("idealweight"));
}
 
}
 */

	@Override
	public void close() {
		try {
			if(connection!=null) connection.close();
		} catch (SQLException e) {
			logger.error("Closing connection",e);
		}
	}

	@Override
	public boolean isConnected() {
		try {
			return (connection!=null)&&!(connection.isClosed());
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	public Object execute(String query) {
		Statement s;
		Object o = null;
		try {
			s = connection.createStatement();
			ResultSet rs = s.executeQuery(query);
			rs.next();
			o = rs.getObject(1);
		} catch (SQLException e) {
			logger.debug(e);
		}
		logger.debug("execute >"+query+"< returns "+o);
		return o;
	}

	@Override
	public void dump(File dest) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restore(File src) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DatabaseObject store(String table, DatabaseObject o) {
		initialize(o);
		int factual = 0;
		try {
			Statement s = connection.createStatement();
			String sql = "INSERT INTO "+getClassTable(o.getClass())+" VALUES(";
			for(Field f: getFields(o.getClass())){
				if(factual>0){
					sql += ", ";
				}
				sql+= "'"+f.get(o).toString()+"'";
				++factual;
			}
			sql += ");";
			logger.debug(sql);
		} catch (Exception e) {
			logger.error("store("+o+")",e);
		} 
		return o;
	}

	@Override
	public void delete(String table, DatabaseObject o) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<? extends DatabaseObject> getAll(String table) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends DatabaseObject> getAll(String table, String column) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DatabaseObject get(String table, int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends DatabaseObject> getBetween(String table,
			String field, String from, String to) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends DatabaseObject> getAll(String table,
			Map<String, String> values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends DatabaseObject> findAll(String table,
			String[] values, String[] columns) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends DatabaseObject> findAll(String source,
			String[] values, String[] columns, Map<String, String> constraints) {
		// TODO Auto-generated method stub
		return null;
	}

}
