package jstudio.db;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		db.clear();
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
	
	private String getSetTable(Field f){
		String t = f.getName().toLowerCase();
		if(t.endsWith("s")){
			return t.substring(0,t.length()-1);
		}else{
			return t;
		}
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
		throw new RuntimeException("NOT IMPLEMENTED!");
	}

	@Override
	public void restore(File src) throws Exception {
		// TODO Auto-generated method stub
		throw new RuntimeException("NOT IMPLEMENTED!");
	}

	@Override
	public void clear() {
		try {
			Statement s = connection.createStatement();
			String sql;
			for(String t: getTables()){
				sql = "DROP TABLE "+t+";";
				//TODO!
				s.execute(sql);
				logger.debug("clear: "+sql);
			}
		}catch(Exception e){
			logger.error("on clear",e);
		}
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
				try{
					switch(EntryType.valueOf(ftype)){
						case Integer:
						case Float:
						case String:
						case Long:
							Object _o = f.get(o);
							if(_o==null) fdef += "''";
							else fdef += "'"+_o.toString()+"' ";
							break;
						case Date:
							fdef+= "'"+SQLDateFormat.format(f.get(o))+"'";
							break;
						case Set:
							//the set requires another table referring to this entities
							Set<DatabaseObject> _s = (Set<DatabaseObject>)f.get(o);
							String _t = getSetTable(f);
							for(DatabaseObject dob: _s){
								store(_t, dob);
							}
							continue;
						default:
							logger.error("EntryType "+ftype+" not implemented");
							continue;
					}			
				}catch(IllegalArgumentException e){
					//custom external class
					Object _o = f.get(o);
					if(_o==null || !(_o instanceof DatabaseObject)) fdef += "''";
					else fdef += ((DatabaseObject)_o).getId();
				}
				++factual;
				sql+=fdef;
				logger.debug("sql: "+sql);
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
		table = table.toLowerCase();
		String sql = new String("DELETE FROM "+table+" WHERE id="+o.getId()+";");
		try {
			Statement s = connection.createStatement();
			s.execute(sql);
		} catch (Exception e) {
			logger.debug("executing "+sql,e);
		} 
	}
	
	private List<DatabaseObject> execute(String table, String sql, DatabaseObject parent) throws Exception{
		Class<?> c = this.initCache.get(table);
		if(c==null){
			throw new RuntimeException("Class "+table+" not initialized properly");
		}
		List<DatabaseObject> l = new ArrayList<DatabaseObject>();
		try {
			Statement s = connection.createStatement();
			ResultSet rs = s.executeQuery(sql);
			l = getMapped(rs, c, parent);
		} catch (Exception e) {
			logger.debug("executing "+sql,e);
		} 
		logger.debug("execute >"+sql+"< returns "+l.size()+" elements");
		return l;
	}
	
	private List<DatabaseObject> getMapped(ResultSet rs, Class<?> c, DatabaseObject parent) throws Exception{
		Field[] fs = this.getFields(c);
		List<DatabaseObject> l = new ArrayList<DatabaseObject>();
		while(rs.next()){
			DatabaseObject o = (DatabaseObject)c.newInstance();
			String ftype;
			for(Field f: fs){
				ftype = f.getType().getSimpleName();
				try{
					switch(EntryType.valueOf(ftype)){
						case Float:
							f.set(o, ((Double)rs.getObject(f.getName())).floatValue());
							break;
						case Integer:
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
							String table = getSetTable(f);
							List<DatabaseObject> _li = execute(table,"SELECT * FROM "+table+" WHERE "+c.getSimpleName().toLowerCase()+"="+o.getId(), o);
							Set<DatabaseObject> s = new HashSet<DatabaseObject>(_li);
							f.set(o, s);
							logger.debug("Set list of "+f.getName()+" "+ftype+" for "+o.getId()+" where parent is "+parent);
							break;
						default:
							logger.error("EntryType "+ftype+" not implemented");
							continue;
					}
				}catch(IllegalArgumentException e){
					//custom datatype
					Long id = 0l;
					try{
						id = ((Integer)rs.getObject(f.getName())).longValue();
					}catch(ClassCastException cce){
						logger.error("Unable to retrieve id for "+f.getName()+" object is "+rs.getObject(f.getName()));
					}
					String tab = ftype.toLowerCase();
					if(parent!=null && parent.getId()==id &&
							parent.getClass().getSimpleName().toLowerCase().equals(tab)){
						f.set(o, parent);
					}else{
						//loading the connected entity
						List<DatabaseObject> res = execute(tab, "SELECT * FROM "+tab+" WHERE id="+id+" LIMIT 1;", o);
						if(res.size()>0){
							f.set(o, res.get(0));
						}
					}
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
			return execute(table, sql, null);
		} catch (Exception e) {
			logger.error(sql,e);
		}
		return null;
	}

	@Override
	public List<? extends DatabaseObject> getAll(String table, String column) {
		table = table.toLowerCase();
		String sql = new String("SELECT * FROM "+table+" GROUP BY "+column+";");
		try {
			return execute(table, sql, null);
		} catch (Exception e) {
			logger.error(sql,e);
		}
		return null;
	}

	@Override
	public DatabaseObject get(String table, int id) {
		table = table.toLowerCase();
		String sql = new String("SELECT * FROM "+table+" WHERE id="+id+";");
		try {
			List<DatabaseObject> l = execute(table, sql, null);
			if(l.size()>0) return l.get(0);
		} catch (Exception e) {
			logger.error(sql,e);
		}
		return null;
	}

	@Override
	public List<? extends DatabaseObject> getBetween(String table, String field, String from, String to) {
		table = table.toLowerCase();
		String sql = new String("SELECT * FROM "+table+" WHERE "+field+" BETWEEN '"+from+"' AND '"+to+"';");
		try {
			return execute(table, sql, null);
		} catch (Exception e) {
			logger.error(sql,e);
		}
		return null;
	}

	@Override
	public List<? extends DatabaseObject> getAll(String table,
			Map<String, String> values) {
		// TODO Auto-generated method stub
		throw new RuntimeException("NOT IMPLEMENTED!");
	}

	@Override
	public List<? extends DatabaseObject> findAll(String table,
			String[] values, String[] columns) {
		return findAll(table, values, columns, new HashMap<String,String>());
	}

	@Override
	public List<? extends DatabaseObject> findAll(String source,
			String[] values, String[] columns, Map<String, String> constraints) {
		
		//building query
		StringBuffer sb = new StringBuffer();
    	sb.append("SELECT * FROM ");
    	sb.append(source.toLowerCase());
    	sb.append(" WHERE ");
    	int vsiz = values.length;
    	for(String v: values){
	    	int csiz = columns.length;
    		sb.append("(");
	    	for(String k: columns){
	    		sb.append(k);
	    		sb.append(" LIKE '%");
	    		sb.append(v);
	    		sb.append("%'");
	    		csiz--;
	    		if(csiz>0){
	    			sb.append(" OR ");
	    		}
	    	}
	    	sb.append(")");
	    	vsiz--;
	    	if(vsiz>0){
	    		sb.append(" AND ");
	    	}
    	}
    	int ksiz = constraints.keySet().size();
    	for(String k: constraints.keySet()){
        	if(ksiz>0) sb.append(" AND ");
    		sb.append(k);
    		sb.append(" LIKE '%");
    		sb.append(constraints.get(k));
    		sb.append("%'");
    		ksiz--;
    	}
    	
    	try {
			return execute(source.toLowerCase(), sb.toString(), null);
		} catch (Exception e) {
			logger.error(sb.toString(),e);
		}
		return null;
	}

}
