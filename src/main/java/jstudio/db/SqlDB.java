package jstudio.db;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
		db.initialize(null, Person.class);
		db.initialize(null, Event.class);
		db.initialize(null, Product.class);
		db.initialize(null, Invoice.class);
		db.store(null, new Invoice(1l));
		db.store(null, new Invoice(2l));
		db.store(null, new Invoice(3l));
		db.execute("SELECT MAX(id) FROM invoice");
		List<? extends DatabaseObject> list = db.getAll("invoice");
		for(DatabaseObject o: list){
			logger.debug(o.toString());
		}
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
	public static final DateFormat SQLDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	private String protocol, driver;
	private File dbfile;
	private Connection connection;
	
	private Map<String, Class> initCache;
	
	public SqlDB(final File dbfile){
		this(dbfile.getName());
		this.dbfile = dbfile;
	}
	
	private SqlDB(final String dbfilename){
		this("jdbc:sqlite:"+dbfilename, "org.sqlite.JDBC");
	}
	
	private SqlDB(String protocol, String driver){
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
	
	public void initialize(String table, Class c){
		initialize(c);
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
		String classname = getClassTable(c);
		if(initCache.get(classname)!=null){
			logger.warn("Table "+classname+" already initialized");
			return;
		}else{
			initCache.put(classname, c);
		}
		String sql = "CREATE TABLE IF NOT EXISTS "+classname+"(";
		int factual = 0;
		for(Field f: getFields(c)){
			Class<?> tc = f.getType();
			String fname = f.getName();
			//logger.debug(fname+" "+f.getType().getName()+" "+getGenericType(f));
			String ftype = f.getType().getSimpleName();
			String fdef = (factual>0?", ":"")+fname;
			try{
				switch(EntryType.valueOf(ftype)){
					case Integer:
						fdef+=" INTEGER (6)";
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
						continue;
						//break;
					default:
						logger.error("EntryType "+ftype+" not implemented");
						continue;
						//break;
				}
			}catch(IllegalArgumentException e){
				//custom object type
				String clazz = f.getType().getName();
				try {
					Class refc = Class.forName(clazz);
					initialize(refc);
					fdef+=" INTEGER (11)";
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
			if(rs.next()){
				o = rs.getObject(1);
			}
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
	/**
	 * Table is ignored here
	 */
	public DatabaseObject store(String table, DatabaseObject o) {
		initialize(table, o.getClass());
		int factual = 0;
		try {
			Statement s = connection.createStatement();
			String sql = "REPLACE INTO "+getClassTable(o.getClass())+" VALUES(";
			String ftype;
			String fdef = "";
			for(Field f: getFields(o.getClass())){
				if(factual>0){
					fdef = ", ";
				}
				ftype = f.getType().getSimpleName();
				switch(EntryType.valueOf(ftype)){
					case Integer:
					case Float:
					case String:
					case Long:
						fdef+= "'"+f.get(o).toString()+"'";
						break;
					case Date:
						fdef+= "'"+SQLDateFormat.format(f.get(o))+"'";
						break;
					case Set:
						//the set requires another table referring to this entities
						continue;
					default:
						logger.error("EntryType "+ftype+" not implemented");
						continue;
				}				
				++factual;
				sql+=fdef;
			}
			sql += ");";
			logger.debug(sql);
			s.executeUpdate(sql);
		} catch (Exception e) {
			logger.error("store("+o+")",e);
		} 
		return o;
	}

	@Override
	public void delete(String table, DatabaseObject o) {
		// TODO Auto-generated method stub
		throw new RuntimeException("NOT IMPLEMENTED");
	}
	
	private List<DatabaseObject> execute(String table, String sql) throws Exception{
		Class<?> c = this.initCache.get(table);
		if(c==null){
			throw new RuntimeException("Class "+table+" not initialized properly");
		}
		List<DatabaseObject> l = new ArrayList<DatabaseObject>();
		try {
			Statement s = connection.createStatement();
			ResultSet rs = s.executeQuery(sql);
			l = getMapped(rs, c);
		} catch (Exception e) {
			logger.debug("executing "+sql,e);
		} 
		logger.debug("execute >"+sql+"< returns "+l.size()+" elements");
		return l;
	}
	
	private List<DatabaseObject> getMapped(ResultSet rs, Class<?> c) throws Exception{
		Field[] fs = this.getFields(c);
		List<DatabaseObject> l = new ArrayList<DatabaseObject>();
		while(rs.next()){
			DatabaseObject o = (DatabaseObject)c.newInstance();
			String ftype;
			for(Field f: fs){
				ftype = f.getType().getSimpleName();
				switch(EntryType.valueOf(ftype)){
					case Integer:
					case Float:
					case String:
						f.set(o, rs.getObject(f.getName()));
						break;
					case Long:
						f.set(o, ((Integer)rs.getObject(f.getName())).longValue());
						break;
					case Date:
						String thedate = (String)rs.getObject(f.getName());
						f.set(o, SQLDateFormat.parse(thedate));
						break;
					case Set:
						//the set requires another table referring to this entities
						break;
					default:
						logger.error("EntryType "+ftype+" not implemented");
						continue;
				}
			}
			l.add(o);
		}
		return l;
	}

	@Override
	public List<? extends DatabaseObject> getAll(String table) {
		table = table.toLowerCase();
		String sql = new String("SELECT * FROM "+table+";");
		try {
			return execute(table, sql);
		} catch (Exception e) {
			logger.error(sql,e);
		}
		return null;
	}

	@Override
	public List<? extends DatabaseObject> getAll(String table, String column) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DatabaseObject get(String table, int id) {
		table = table.toLowerCase();
		String sql = new String("SELECT * FROM "+table+" WHERE id="+id+";");
		try {
			List<DatabaseObject> l = execute(table, sql);
			if(l.size()>0) return l.get(0);
		} catch (Exception e) {
			logger.error(sql,e);
		}
		return null;
	}

	@Override
	public List<? extends DatabaseObject> getBetween(String table,
			String field, String from, String to) {
		table = table.toLowerCase();
		String sql = new String("SELECT * FROM "+table+" WHERE "+field+" BETWEEN '"+from+"' AND '"+to+"';");
		try {
			return execute(table, sql);
		} catch (Exception e) {
			logger.error(sql,e);
		}
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
