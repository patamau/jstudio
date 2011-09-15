package jstudio.db;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jstudio.util.Language;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.mapping.PersistentClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateDB implements DatabaseInterface{

	private static final Logger logger = LoggerFactory.getLogger(HibernateDB.class);
    private SessionFactory sessionFactory;
    private String protocol, driver;
    private Configuration configuration;
    
    public HibernateDB(String protocol, String driver){
    	sessionFactory = null;
    	this.protocol = protocol;
    	this.driver = driver;
    }
    
    public void dump(File dest) throws IOException{
    	if(!isConnected()) throw new RuntimeException(Language.string("Not connected"));
    	if(dest==null) throw new NullPointerException(Language.string("No destination selected"));

		FileOutputStream fos = new FileOutputStream(dest);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
    	for (@SuppressWarnings("unchecked")
		Iterator<PersistentClass> iter=configuration.getClassMappings(); iter.hasNext();) {
    		PersistentClass persistentClass = iter.next();
    		String source = persistentClass.getClassName();
    		logger.info("Starting to dump "+source);
    		List<DatabaseObject> data = getAll(source);
    		for(DatabaseObject d : data){
    			oos.writeObject(d);
    		}
    		logger.info(source+" dump finished ("+data.size()+" objects)");
    	}
		oos.close();
    }
    
    public void clear(){
    	if(!isConnected()) return;
		for(@SuppressWarnings("unchecked")
		Iterator<PersistentClass> iter=configuration.getClassMappings(); iter.hasNext();) {
    		PersistentClass persistentClass = iter.next();
    		String source = persistentClass.getClassName();
    		logger.info("Clearing "+source);
    		List<DatabaseObject> data = getAll(source);
    		for(DatabaseObject d : data){
    			logger.debug("Removing "+d.getClass().getName()+" id:"+d.getId());
    			delete(null, d); //empty source thanks to mappings
    		}
    		logger.info(source+" clear finished (removed "+data.size()+" objects)");
    	}
    }
    
    public void restore(File src) throws Exception{
    	if(!isConnected()) throw new RuntimeException(Language.string("Not connected"));
    	if(src==null) throw new NullPointerException(Language.string("No source selected"));
    	FileInputStream fis = new FileInputStream(src);
    	ObjectInputStream ios = new ObjectInputStream(fis);
    	DatabaseObject o;
    	int nobjs = 0;
    	try{
	    	while((o = (DatabaseObject)ios.readObject())!=null){
	    		try{
	    			this.store(null, o); //empty source thanks to mappings
	    		}catch(Exception e){
	    			this.forceStore(null, o);
	    		}
	    		nobjs++;
	    	}
    	}catch(EOFException eof){
    		//end of file reached!
    	}
    	logger.info("Loaded "+nobjs+" objects");
    	ios.close();
    }
    
    /**
     * Host also accepts port option as a suffix like localhost:3306
     */
    public void connect(String host, String dbname, String user, String password){
       	//override default configuration property
       	String url = protocol+"://"+host+"/"+dbname;
    	// load the standard configuration
        configuration = new Configuration();
       	configuration.addResource("person.hbm.xml");
       	configuration.addResource("event.hbm.xml");
       	configuration.addResource("product.hbm.xml");
       	configuration.addResource("invoice.hbm.xml");
       	configuration.addResource("comune.hbm.xml");
       	
       	configuration.setProperty(Environment.DRIVER, driver);
       	configuration.setProperty(Environment.CACHE_PROVIDER, "org.hibernate.cache.NoCacheProvider");
       	configuration.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
       	configuration.setProperty(Environment.POOL_SIZE, "1");
       	//XXX: I'm forcing mysql dialect here
       	configuration.setProperty(Environment.DIALECT, "org.hibernate.dialect.MySQLDialect");
       	configuration.setProperty(Environment.HBM2DDL_AUTO, "update");
       	configuration.setProperty(Environment.URL, url);
       	configuration.setProperty(Environment.USER, user);
       	configuration.setProperty(Environment.PASS, password);       	
       	if(logger.isDebugEnabled()){
       		//XXX: enhance debugging for hibernate queries
       		configuration.setProperty(Environment.SHOW_SQL, Boolean.toString(true));
       	}
        try {
        	sessionFactory = configuration.buildSessionFactory();
        }catch (Throwable ex) {
            logger.error("Initial SessionFactory creation failed.", ex);
        }
    }
    
    public boolean isConnected(){
    	if(null!=sessionFactory){
    		try{
	    		Session s = sessionFactory.getCurrentSession();
	    		Transaction t = s.beginTransaction();
				if(!sessionFactory.getCurrentSession().isConnected()){
					return false;
				}
	    		try{
	    			t.commit();
	    		}catch(Exception ex){
	    			return false;
	    		}
	    		return true;
    		}catch(Throwable t){
    			logger.error("Cannot verify connection",t);
    			return false;
    		}
    	}
    	return false;
    }
    
    public void forceStore(String source, DatabaseObject o){
    	if(!isConnected()) return;
    	// source is ignored because hibernate mapping already takes care of this
    	Session session = sessionFactory.getCurrentSession();
    	Transaction t = session.beginTransaction();
    	session.save(o);
    	commit(t);
    }
    
    public DatabaseObject store(String source, DatabaseObject o){
    	if(!isConnected()) return null;
    	// source is ignored because hibernate mapping already takes care of this
    	Session session = sessionFactory.getCurrentSession();
    	Transaction t = session.beginTransaction();
    	session.saveOrUpdate(o);
    	commit(t);
    	return o;
    }
    
    private void commit(Transaction t){
    	try{
    		t.commit();
    	}catch(HibernateException e){
    		logger.warn("unable to commit the transaction",e);
    		if (t != null && t.isActive()) {
    			try {
    				t.rollback();
    			} catch (HibernateException e1) {
    				logger.warn("unable to rollback",e1);
    			}
    		}
    	}
    }
    
    @SuppressWarnings("unchecked")
	public List<DatabaseObject> getAll(String source){
    	if(!isConnected()) return null;
    	Session session = sessionFactory.getCurrentSession();
    	Transaction t = session.beginTransaction();
    	List<DatabaseObject> l = (List<DatabaseObject>)session.createQuery("from "+source).list();
    	commit(t);
    	return l;
    }

	public DatabaseObject get(String source, int id) {
		if(!isConnected()) return null;
		Session session = sessionFactory.getCurrentSession();
    	Transaction t = session.beginTransaction();
    	DatabaseObject o = (DatabaseObject)session.createQuery("from "+source+" where id="+id).uniqueResult();
    	commit(t);
    	return o;
	}
	
	@SuppressWarnings("unchecked")
	public List<DatabaseObject> getBetween(String source, String field, String from, String to){
		if(!isConnected()) return null;
		Session session = sessionFactory.getCurrentSession();
    	Transaction t = session.beginTransaction();
    	StringBuffer sb = new StringBuffer();
    	sb.append("from ");
    	sb.append(source);
    	sb.append(" where ");
    	sb.append(field);
    	sb.append(" between '");
    	sb.append(from);
    	sb.append("' and '");
    	sb.append(to);
    	sb.append("'");    	
    	List<DatabaseObject> l = (List<DatabaseObject>)session.createQuery(sb.toString()).list();
    	commit(t);
    	return l;
	}

	@SuppressWarnings("unchecked")
	public List<DatabaseObject> getAll(String source, Map<String, String> values) {
		//if no values specified, bounce to default getAll
		if(null==values||0==values.keySet().size()) return getAll(source);
		if(!isConnected()) return null;
    	Session session = sessionFactory.getCurrentSession();
    	Transaction t = session.beginTransaction();
    	StringBuffer sb = new StringBuffer();
    	sb.append("from ");
    	sb.append(source);
    	sb.append(" where ");
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
    	List<DatabaseObject> l = (List<DatabaseObject>)session.createQuery(sb.toString()).list();
    	commit(t);
    	return l;
	}

	@SuppressWarnings("unchecked")
	public List<DatabaseObject> getAll(String source, String column) {
		if(!isConnected()) return null;
		Session session = sessionFactory.getCurrentSession();
    	Transaction t = session.beginTransaction();
    	StringBuffer sb = new StringBuffer();
    	sb.append("from ");
    	sb.append(source);
    	sb.append(" group by ");
    	sb.append(column);
    	List<DatabaseObject> l = (List<DatabaseObject>)session.createQuery(sb.toString()).list();
    	commit(t);
    	return l;
	}

	public void delete(String table, DatabaseObject o) {
		if(!isConnected()) return;
    	// source is ignored because hibernate mapping already takes care of this
    	Session session = sessionFactory.getCurrentSession();
    	Transaction t = session.beginTransaction();
    	session.delete(o);
    	commit(t);
	}
	
	@Override
	public List<DatabaseObject> findAll(String source, String[] values, String[] columns){
		return findAll(source, values, columns, new HashMap<String,String>());
	}

	@Override
	public List<DatabaseObject> findAll(String source, String[] values, String[] columns, Map<String,String> constraints) {
		//if no values specified, bounce to default getAll
		if(null==values||0==values.length||
				null==columns||0==columns.length) return getAll(source);
    	Session session = sessionFactory.getCurrentSession();
    	Transaction t = session.beginTransaction();
    	if(!session.isConnected()){
    		return null;
    	}
    	StringBuffer sb = new StringBuffer();
    	sb.append("from ");
    	sb.append(source);
    	sb.append(" where ");
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
    	
    	@SuppressWarnings("unchecked")
		List<DatabaseObject> l = (List<DatabaseObject>)session.createQuery(sb.toString()).list();
    	commit(t);
    	return l;
	}

}
