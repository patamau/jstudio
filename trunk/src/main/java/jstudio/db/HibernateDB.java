package jstudio.db;

import java.util.HashMap;
import java.util.List;

import jstudio.db.DatabaseInterface;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.cfg.Mappings;
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
    	// load the standard configuration
       	Configuration c = new Configuration().configure();
       	//override default configuration property
       	String url = protocol+"://"+host+"/"+dbname;
       	
       	c.setProperty(Environment.DRIVER, driver);
       	//XXX: forcing mysql dialect
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
    
    public DatabaseObject store(String table, DatabaseObject o){
    	// table is ignored because hibernate mapping already takes care of this
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
	public List<DatabaseObject> getAll(String table){
    	Session session = sessionFactory.getCurrentSession();
    	Transaction t = session.beginTransaction();
    	List<DatabaseObject> l = (List<DatabaseObject>)session.createQuery("from "+table).list();
    	commit(t);
    	return l;
    }

	public DatabaseObject get(String table, int id) {
		Session session = sessionFactory.getCurrentSession();
    	Transaction t = session.beginTransaction();
    	DatabaseObject o = (DatabaseObject)session.createQuery("from "+table+" where id="+id).uniqueResult();
    	commit(t);
    	return o;
	}

	@SuppressWarnings("unchecked")
	public List<DatabaseObject> getAll(String table,
			HashMap<String, String> values) {
		//if no values specified, bounce to default getAll
		if(null==values||0==values.keySet().size()) return getAll(table);
    	Session session = sessionFactory.getCurrentSession();
    	Transaction t = session.beginTransaction();
    	StringBuffer sb = new StringBuffer();
    	sb.append("from ");
    	sb.append(table);
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
    	List<DatabaseObject> l = (List<DatabaseObject>)session.createQuery("from "+table).list();
    	commit(t);
    	return l;
	}

}
