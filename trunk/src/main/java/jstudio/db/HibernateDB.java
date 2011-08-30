package jstudio.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jstudio.db.DatabaseInterface;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateDB implements DatabaseInterface{

	private static final Logger logger = LoggerFactory.getLogger(HibernateDB.class);
    private SessionFactory sessionFactory;
    private String protocol, driver;
    
    public HibernateDB(String protocol, String driver){
    	sessionFactory = null;
    	this.protocol = protocol;
    	this.driver = driver;
    }
    
    /**
     * Host also accepts port option as a suffix like localhost:3306
     */
    public void connect(String host, String dbname, String user, String password){
       	//override default configuration property
       	String url = protocol+"://"+host+"/"+dbname;
    	// load the standard configuration
       	Configuration c = new Configuration();
       	c.addResource("person.hbm.xml");
       	c.addResource("event.hbm.xml");
       	c.addResource("product.hbm.xml");
       	c.addResource("invoice.hbm.xml");
       	c.addResource("comune.hbm.xml");
       	
       	c.setProperty(Environment.DRIVER, driver);
       	c.setProperty(Environment.CACHE_PROVIDER, "org.hibernate.cache.NoCacheProvider");
       	c.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");
       	c.setProperty(Environment.POOL_SIZE, "1");
       	//XXX: I'm forcing mysql dialect here
       	c.setProperty(Environment.DIALECT, "org.hibernate.dialect.MySQLDialect");
       	c.setProperty(Environment.HBM2DDL_AUTO, "update");
       	c.setProperty(Environment.URL, url);
       	c.setProperty(Environment.USER, user);
       	c.setProperty(Environment.PASS, password);       	
       	if(logger.isDebugEnabled()){
       		//XXX: enhance debugging for hibernate queries
       		c.setProperty(Environment.SHOW_SQL, Boolean.toString(true));
       	}
        try {
        	sessionFactory = c.buildSessionFactory();
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
    
    public DatabaseObject store(String source, DatabaseObject o){
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
    	Session session = sessionFactory.getCurrentSession();
    	Transaction t = session.beginTransaction();
    	List<DatabaseObject> l = (List<DatabaseObject>)session.createQuery("from "+source).list();
    	commit(t);
    	return l;
    }

	public DatabaseObject get(String source, int id) {
		Session session = sessionFactory.getCurrentSession();
    	Transaction t = session.beginTransaction();
    	DatabaseObject o = (DatabaseObject)session.createQuery("from "+source+" where id="+id).uniqueResult();
    	commit(t);
    	return o;
	}
	
	@SuppressWarnings("unchecked")
	public List<DatabaseObject> getBetween(String source, String field, String from, String to){
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
