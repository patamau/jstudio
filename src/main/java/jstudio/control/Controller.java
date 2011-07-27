package jstudio.control;

import java.util.Collection;

import jstudio.db.DatabaseInterface;
import jstudio.db.DatabaseObject;

/**
 * Agenda holds events on a daily basis.
 * @author Matteo
 *
 */
public abstract class Controller<E extends DatabaseObject> {
	
	protected DatabaseInterface database;
	private String source;
	
	public Controller(DatabaseInterface database){
		this.database = database;
	}
	
	public final void setSource(String source){
		this.source=source;
	}
	
	public final String getSource(){
		return this.source;
	}
	
	@SuppressWarnings("unchecked")
	public E get(int id){
		return (E)database.get(source, id);
	}
	
	@SuppressWarnings("unchecked")
	public E add(E e){
		return (E)database.store(source, e);
	}
	
	@SuppressWarnings("unchecked")
	public Collection<E> getAll(){
		return (Collection<E>)database.getAll(source);
	}
	
	public void store(E o){
		database.store(source, o);
	}
}
