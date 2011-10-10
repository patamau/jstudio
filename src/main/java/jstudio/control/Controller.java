package jstudio.control;

import java.util.Collection;
import java.util.Map;

import jstudio.JStudio;
import jstudio.db.DatabaseObject;

/**
 * Agenda holds events on a daily basis.
 * @author Matteo
 *
 */
public class Controller<E extends DatabaseObject> {
	
	protected String source;
	protected JStudio app;
	
	public Controller(JStudio app, String source){
		this.app = app;
		this.source = source;
	}
	
	public final JStudio getApplication(){
		return app;
	}
	
	public final String getSource(){
		return this.source;
	}
	
	public Long getNextId(){
		String query = "SELECT MAX(id) FROM "+source;
		Object o = app.getDatabase().execute(query);
		if(o!=null){
			return ((Long)o)+1;
		}else{
			return 1l;
		}
	}
	
	@SuppressWarnings("unchecked")
	public E get(int id){
		return (E)app.getDatabase().get(source, id);
	}
	
	@SuppressWarnings("unchecked")
	public E add(E e){
		return (E)app.getDatabase().store(source, e);
	}
	
	@SuppressWarnings("unchecked")
	public Collection<E> getAll(){
		return (Collection<E>)app.getDatabase().getAll(source);
	}
	
	@SuppressWarnings("unchecked")
	public Collection<E> getAll(Map<String,String> map){
		return (Collection<E>)app.getDatabase().getAll(source, map);
	}
	
	@SuppressWarnings("unchecked")
	public Collection<E> findAll(String[] values, String[] columns){
		return (Collection<E>)app.getDatabase().findAll(source, values, columns);
	}
	
	@SuppressWarnings("unchecked")
	public Collection<E> findAll(String[] values, String[] columns, Map<String,String> constraints){
		return (Collection<E>)app.getDatabase().findAll(source, values, columns, constraints);
	}
	
	public void store(E o){
		app.getDatabase().store(source, o);
	}
	
	public void delete(E o){
		app.getDatabase().delete(source, o);
	}
}
