package jstudio.db;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.util.Date;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jstudio.model.Event;
import jstudio.model.Invoice;
import jstudio.model.Person;
import jstudio.model.Product;
import jstudio.util.Language;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.mapping.PersistentClass;
import org.sqlite.SQLite;

public class SqlDB implements DatabaseInterface {
		
	public static void main(String[] args){
		BasicConfigurator.configure();
		SqlDB db = new SqlDB("test.db");
		try {
			db.connect("data.db",null,null,null);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	public static final String 
		SQLITE_PROTOCOL_PREFIX = "jdbc:sqlite:",
		SQLITE_DRIVER = "org.sqlite.JDBC";
	
	private static final Logger logger = Logger.getLogger(SqlDB.class);
	public static final DateFormat SQLDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	private String protocol, driver;
	private File dbfile;
	private Connection connection;
	
	private Map<String, Class<?>> initCache;
	
	public SqlDB(final File dbfile){
		this(dbfile.getName());
		this.dbfile = dbfile;
	}
	
	public SqlDB(final String dbfilename){
		this("jdbc:sqlite:"+dbfilename, "org.sqlite.JDBC");
	}
	
	public SqlDB(final String protocol, final String driver){
		this.protocol = protocol;
		this.driver = driver;
		initCache = new HashMap<String,Class<?>>();
	}
	
	/**
	 * Arguments are unused now.
	 * Protocol and driver set at initialization will be used
	 * File is the file used as db sources.
	 * table, user and password won't be used.
	 */
	@Override
	public void connect(String file, String table, String user, String pass) throws Exception {
		String p = protocol+":"+file;
		try {
			Class.forName(driver);
			connection = DriverManager.getConnection(p);
			logger.debug("Connection established, initializing...");
		} catch (SQLException e) {
			logger.error("Protocol "+p+" error",e);
			throw e;
		} catch (ClassNotFoundException e) {
			logger.error("Driver "+driver+" error",e);
			throw e;
		}
	}
	
	private String[] getTables(){
		String sql = "SELECT name FROM sqlite_master WHERE type = \"table\";";
		ArrayList<String> tables = new ArrayList<String>();
		Statement s;
		ResultSet rs = null;
		try{
			s = connection.createStatement();
			rs = s.executeQuery(sql);
			while(rs.next()){
				tables.add(rs.getString("name"));
			}
		}catch(SQLException e){
			logger.error(sql,e);
		}finally{
			if(rs!=null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
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
			if(connection!=null){
				connection.close();
			}
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
		ResultSet rs = null;
		try {
			s = connection.createStatement();
			rs = s.executeQuery(query);
			if(rs.next()){
				o = rs.getObject(1);
			}
		} catch (SQLException e) {
			logger.debug("executing "+query, e);
		} finally {
			if(rs!=null){
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		logger.debug("execute >"+query+"< returns "+o);
		return o;
	}

	@Override
	public void dump(File dest) throws Exception {
    	if(!isConnected()) throw new RuntimeException(Language.string("Not connected"));
    	if(dest==null) throw new NullPointerException(Language.string("No destination selected"));

		FileOutputStream fos = new FileOutputStream(dest);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
    	for (String source: getTables()) {
    		logger.info("Starting to dump "+source);
    		List<DatabaseObject> data = getAll(source);
    		for(DatabaseObject d : data){
    			oos.writeObject(d);
    		}
    		logger.info(source+" dump finished ("+data.size()+" objects)");
    	}
		oos.close();
	}

	@Override
	public void restore(File src) throws Exception {
    	if(!isConnected()) throw new RuntimeException(Language.string("Not connected"));
    	if(src==null) throw new NullPointerException(Language.string("No source selected"));
    	FileInputStream fis = new FileInputStream(src);
    	ObjectInputStream ios = new ObjectInputStream(fis);
    	DatabaseObject o;
    	int nobjs = 0;
    	String t = null;
    	try{
	    	while((o = (DatabaseObject)ios.readObject())!=null){
	    		initialize(o.getClass());
	    		store(t , o);
	    		nobjs++;
	    		logger.debug("Loaded "+o.getClass().getName()+" "+o);
	    	}
    	}catch(EOFException eof){
    		//end of file reached!
    	}
    	logger.info("Loaded "+nobjs+" objects");
    	ios.close();
	}

	@Override
	public void clear() {
		try {
			Statement s = connection.createStatement();
			String sql;
			for(String t: getTables()){
				sql = "DELETE FROM "+t+";";	
				s.execute(sql);
				logger.debug("clear: "+sql);
				//initCache.remove(t);
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
		try {
			String args = new String();
			String ftype;
			int fsize = 0;
			for(Field f: getFields(o.getClass())){
				ftype = f.getType().getSimpleName();
				try{
					if(EntryType.valueOf(ftype)==EntryType.Set) continue;
				}catch(IllegalArgumentException ex){
					//when no object is mapped in the entrytype enum
				}
				if(args.length()>0){
					args+=", ?";
				}else{
					args+="?";
				}
				++fsize;
			}
			String sql = "REPLACE INTO "+getClassTable(o.getClass())+" VALUES ("+args+")";
			logger.debug("PreparedStatement "+sql);
			PreparedStatement ps = connection.prepareStatement(sql);	
			int factual = 0;
			for(Field f: getFields(o.getClass())){
				ftype = f.getType().getSimpleName();
				logger.debug("factual is "+factual+"/"+fsize);
				try{
					switch(EntryType.valueOf(ftype)){
						case Set:
							//the set requires another table referring to this entities
							Set<DatabaseObject> _s = (Set<DatabaseObject>)f.get(o);
							String _t = getSetTable(f);
							for(DatabaseObject dob: _s){
								store(_t, dob);
							}
							break;
						case Integer:
							ps.setInt(++factual, (Integer)f.get(o));
							break;
						case Float:
							ps.setFloat(++factual, (Float)f.get(o));
							break;
						case String:
							ps.setString(++factual, (String)f.get(o));
							break;
						case Long:
							ps.setLong(++factual, (Long)f.get(o));
							break;
						case Date:			
							java.util.Date d = (java.util.Date)f.get(o);
							ps.setString(++factual, SQLDateFormat.format(d));
							break;
						default:
							Object _o = f.get(o);
							if(_o==null){
								ps.setObject(++factual, "");
							}else{
								ps.setObject(++factual, _o);
							}
							break;
					}
				}catch(IllegalArgumentException e){
					//custom external class
					Object _o = f.get(o);
					if(_o==null || !(_o instanceof DatabaseObject)){
						ps.setObject(++factual, "");
					}else{
						ps.setObject(++factual,((DatabaseObject)_o).getId());
					}
				}catch(Exception e){
					logger.error("Generic exception on "+f.get(o)+" as "+f.get(o).getClass().getSimpleName(),e);
				}
			}
			logger.debug("Executing update on prepared statement");
			ps.executeUpdate();
			ps.close();
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
		ResultSet rs = null;
		try {
			Statement s = connection.createStatement();
			rs = s.executeQuery(sql);
			l = getMapped(rs, c, parent);
		} catch (Exception e) {
			logger.debug("executing "+sql,e);
		} finally {
			if(rs!=null) rs.close();
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
					logger.debug("Trying to get field "+f.getName()+" as "+ftype);
					switch(EntryType.valueOf(ftype)){
						case Float:
							f.set(o, ((Double)rs.getObject(f.getName())).floatValue());
							break;
						case Integer:
						case String:
							f.set(o, rs.getObject(f.getName()));
							break;
						case Long:
							Object rso = rs.getObject(f.getName());
							if(rso instanceof Integer){
								f.set(o, ((Integer)rso).longValue());
							}else{
								f.set(o, ((Long)rso).longValue());
							}
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
	public List<DatabaseObject> getAll(String table) {
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
		//if no values specified, bounce to default getAll
		if(null==values||0==values.keySet().size()) return getAll(table);
		if(!isConnected()) return null;
    	StringBuffer sb = new StringBuffer();
    	sb.append("SELECT * FROM ");
    	sb.append(table.toLowerCase());
    	sb.append(" WHERE ");
    	int siz = values.size();
    	for(String k: values.keySet()){
    		sb.append(k);
    		sb.append("='");
    		sb.append(values.get(k));
    		sb.append("'");
    		siz--;
    		if(siz>0){
    			sb.append(" AND ");
    		}
    	}
    	try {
			return execute(table.toLowerCase(), sb.toString(), null);
		} catch (Exception e) {
			logger.error(sb.toString(),e);
		}
		return null;
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
